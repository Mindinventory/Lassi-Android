package com.lassi.presentation.camera

import TimerUtils
import android.net.Uri
import android.os.CountDownTimer
import android.os.Environment
import com.lassi.common.utils.KeyUtils
import com.lassi.common.utils.Logger
import com.lassi.data.common.VideoRecord
import com.lassi.domain.common.SingleLiveEvent
import com.lassi.domain.media.LassiConfig
import com.lassi.presentation.common.LassiBaseViewModel
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class CameraViewModel : LassiBaseViewModel() {
    private val logTag = CameraViewModel::class.java.simpleName

    val cropImageLiveData = SingleLiveEvent<Uri>()
    val startVideoRecord = SingleLiveEvent<VideoRecord<File>>()

    private val maxVideoTime by lazy {
        if (LassiConfig.getConfig().maxTime
            == KeyUtils.DEFAULT_DURATION
        ) {
            KeyUtils.TEN_SECOND_INTERVAL // 10 sec
        } else {
            (LassiConfig.getConfig().maxTime * KeyUtils.ONE_SECOND_INTERVAL)
        }
    }
    private val minVideoTime by lazy {
        if (LassiConfig.getConfig().minTime
            == KeyUtils.DEFAULT_DURATION
        ) {
            KeyUtils.FIVE_SECOND_INTERVAL  // 5 sec
        } else {
            (LassiConfig.getConfig().minTime * KeyUtils.ONE_SECOND_INTERVAL)
        }
    }
    private var remainingVideoTime = maxVideoTime

    private val countDownTimer: CountDownTimer by lazy {
        object : CountDownTimer(maxVideoTime, KeyUtils.ONE_SECOND_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                remainingVideoTime = maxVideoTime - millisUntilFinished
                startVideoRecord.value =
                    VideoRecord.Timer(TimerUtils.formatTimeInMinuteSecond(millisUntilFinished))
            }

            override fun onFinish() {
                stopVideoRecording()
            }
        }
    }

    fun onPictureTaken(data: ByteArray?) {
        var image: File? = null
        Single.fromCallable {
            val storageDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                "Camera"
            )
            if (!storageDir.exists()) {
                val isDirectoryCreated = File(storageDir.path).mkdirs()
                Logger.d(logTag, "isDirectoryCreated >> $isDirectoryCreated")
            } else {
                Logger.d(logTag, "directory alredy exists")
            }
            image = File.createTempFile(
                "IMG-",  // prefix
                ".jpeg", // suffix
                storageDir // directory
            )
            var outputStream: OutputStream? = null
            try {
                outputStream = FileOutputStream(image)
                outputStream.write(data)
                outputStream.close()
            } catch (e: IOException) {
                Logger.e(logTag, "Cannot write to $image >> $e")
            } finally {
                outputStream?.close()
            }
        }.subscribeOn(Schedulers.newThread())
            .subscribe({
                image?.let {
                    val uri = Uri.fromFile(it)
                    if (uri != null) {
                        cropImageLiveData.postValue(uri)
                    }
                }
            }, {}).collect()
    }


    fun startVideoRecording() {
        Logger.d(logTag, "videoTime >> $maxVideoTime")

        val storageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            "Camera"
        )
        if (!storageDir.exists()) {
            val isDirectoryCreated = File(storageDir.path).mkdirs()
            Logger.d(logTag, "isDirectoryCreated >> $isDirectoryCreated")
        } else {
            Logger.d(logTag, "directory already exists")
        }
        val videoFile = File.createTempFile(
            "VID-",  // prefix
            ".mp4", // suffix
            storageDir // directory
        )
        startVideoRecord.value = VideoRecord.Start(videoFile)
        countDownTimer.start()
    }

    fun stopVideoRecording() {
        Logger.d(
            logTag,
            "stopVideoRecording : ${(minVideoTime)} , $remainingVideoTime"
        )
        if (minVideoTime >= remainingVideoTime) {
            startVideoRecord.value =
                VideoRecord.Error(TimerUtils.formatTimeInMinuteSecond(minVideoTime))
        } else {
            startVideoRecord.value = VideoRecord.End()
        }
    }
}