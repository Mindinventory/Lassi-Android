package com.lassi.presentation.camera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.lassi.R
import com.lassi.common.extenstions.hide
import com.lassi.common.extenstions.invisible
import com.lassi.common.extenstions.show
import com.lassi.common.utils.CropUtils
import com.lassi.common.utils.KeyUtils
import com.lassi.common.utils.ToastUtils
import com.lassi.data.common.VideoRecord
import com.lassi.data.media.MiMedia
import com.lassi.domain.common.SafeObserver
import com.lassi.domain.media.LassiConfig
import com.lassi.domain.media.MediaType
import com.lassi.presentation.cameraview.audio.Audio
import com.lassi.presentation.cameraview.audio.Flash
import com.lassi.presentation.cameraview.audio.Mode
import com.lassi.presentation.cameraview.controls.CameraListener
import com.lassi.presentation.cameraview.controls.CameraOptions
import com.lassi.presentation.cameraview.controls.PictureResult
import com.lassi.presentation.cameraview.controls.VideoResult
import com.lassi.presentation.common.LassiBaseViewModelFragment
import com.lassi.presentation.videopreview.VideoPreviewActivity
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.File

class CameraFragment : LassiBaseViewModelFragment<CameraViewModel>(), View.OnClickListener {

    private lateinit var cameraMode: Mode
    private val TAG = CameraFragment::class.java.simpleName
    val showRationale = shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            var valid = true
            for (grantResult in it.entries) {
                valid = valid && grantResult.value
            }
            if (valid && !cameraView.isOpened) {
                cameraView.open()
            } else {
                showPermissionDisableAlert()
            }
        }

    private val permissionSettingResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            initCamera()
        }

    override fun buildViewModel(): CameraViewModel {
        return ViewModelProvider(this)[CameraViewModel::class.java]
    }

    override fun getContentResource() = R.layout.activity_camera

    override fun getBundle() {
        super.getBundle()
        cameraMode = if (LassiConfig.getConfig().mediaType == MediaType.VIDEO) {
            Mode.VIDEO
        } else {
            Mode.PICTURE
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.hide()
        if (checkPermissions(cameraView.audio))
            cameraView.open()
    }

    override fun initViews() {
        super.initViews()
        ivCaptureImage.setOnClickListener(this)
        ivFlipCamera.setOnClickListener(this)
        ivFlash.setOnClickListener(this)
        cameraView.setLifecycleOwner(this)
        cameraView.addCameraListener(object : CameraListener() {
            override fun onCameraOpened(options: CameraOptions) {
                cameraView.mode = cameraMode
            }

            override fun onPictureTaken(result: PictureResult) {
                super.onPictureTaken(result)
                viewModel.onPictureTaken(result.data)
            }

            override fun onVideoTaken(video: VideoResult) {
                super.onVideoTaken(video)
                stopVideoRecording()
                VideoPreviewActivity.startVideoPreview(activity, video.file.absolutePath)
            }
        })
        initCamera()
    }

    override fun initLiveDataObservers() {
        super.initLiveDataObservers()
        viewModel.startVideoRecord.observe(this, SafeObserver(this::handleVideoRecord))
        viewModel.cropImageLiveData.observe(this, SafeObserver { uri ->
            if (LassiConfig.getConfig().isCrop && LassiConfig.getConfig().maxCount <= 1) {
                CropUtils.beginCrop(requireActivity(), uri)
            } else {
                ArrayList<MiMedia>().also {
                    MiMedia().apply {
                        this.path = uri.path
                        it.add(this)
                    }
                    setResultOk(it)
                }
            }
        })
    }

    private fun toggleCamera() {
        if (cameraView.isTakingPicture || cameraView.isTakingVideo) return
        cameraView.toggleFacing()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivCaptureImage -> {
                if (cameraMode == Mode.PICTURE) {
                    if (cameraView.isTakingPicture || cameraView.isTakingVideo) return
                    cameraView.takePicture()
                } else {
                    if (!cameraView.isTakingVideo) {
                        viewModel.startVideoRecording()
                    } else {
                        viewModel.stopVideoRecording()
                    }
                }
            }
            R.id.ivFlipCamera -> toggleCamera()
            R.id.ivFlash -> {
                //Check whether the flashlight is available or not?
                val isFlashAvailable =
                    requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
                if (isFlashAvailable) {
                    if (cameraMode == Mode.PICTURE) {
                        when (cameraView.flash) {
                            Flash.AUTO -> flashOn()
                            Flash.ON -> flashOff()
                            else -> flashAuto()
                        }
                    } else {
                        when (cameraView.flash) {
                            Flash.OFF -> flashTorch()
                            Flash.TORCH -> flashOff()
                            else -> flashAuto()
                        }
                    }
                }
            }
        }
    }

    private fun handleVideoRecord(videoRecord: VideoRecord<File>) {
        when (videoRecord) {
            is VideoRecord.Start -> startVideoRecording(videoRecord.item)
            is VideoRecord.Timer -> tvTimer.text = videoRecord.item
            is VideoRecord.End -> stopVideoRecording()
            is VideoRecord.Error -> showVideoError(videoRecord.minVideoTime)
        }
    }

    private fun startVideoRecording(videoFile: File) {
        cameraView.takeVideo(videoFile)
        ivFlipCamera.invisible()
        tvTimer.show()
    }

    private fun stopVideoRecording() {
        if (cameraView.isTakingVideo) {
            cameraView.stopVideo()
            ivFlipCamera.show()
            tvTimer.hide()
        }
    }

    private fun showVideoError(minVideoTime: String) {
        ToastUtils.showToast(
            requireContext(),
            String.format(getString(R.string.min_video_recording_time_error), minVideoTime)
        )
    }

    private fun flashOn() {
        cameraView.flash = Flash.ON
        ivFlash.setImageResource(R.drawable.ic_flash_on_white)
    }

    private fun flashTorch() {
        cameraView.flash = Flash.TORCH
        ivFlash.setImageResource(R.drawable.ic_flash_on_white)
    }

    private fun flashOff() {
        cameraView.flash = Flash.OFF
        ivFlash.setImageResource(R.drawable.ic_flash_off_white)
    }

    private fun flashAuto() {
        cameraView.flash = Flash.AUTO
        ivFlash.setImageResource(R.drawable.ic_flash_auto_white)
    }

    private fun showPermissionDisableAlert() {
        val alertMessage = if (LassiConfig.getConfig().mediaType == MediaType.VIDEO) {
            R.string.camera_audio_storage_permission_rational
        } else {
            R.string.camera_storage_permission_rational
        }
        val alertDialog = AlertDialog.Builder(requireContext(), R.style.dialogTheme)
            .setMessage(alertMessage)
            .setCancelable(false)
            .setPositiveButton(R.string.ok) { _, _ ->
                val intent = Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", activity?.packageName, null)
                }
                permissionSettingResult.launch(intent)
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                activity?.onBackPressed()
            }
        val permissionDialog = alertDialog.create()
        permissionDialog.setCancelable(false)
        permissionDialog.show()
    }

    private fun checkPermissions(audio: Audio): Boolean {
        cameraView.checkPermissionsManifestOrThrow(audio)
        // Manifest is OK at this point. Let's check runtime permissions.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true

        var needsCamera = true
        var needsStorage = true
        var needsAudio = LassiConfig.getConfig().mediaType == MediaType.VIDEO && audio == Audio.ON

        needsCamera =
            needsCamera && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Log.d(TAG, "!@# checkPermissions < TIRAMISU 263")
            println("!@# checkPermissions < TIRAMISU 263")

            needsStorage =
                needsStorage && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d(TAG, "!@# checkPermissions READ_MEDIA_IMAGES 273")
            println("!@# checkPermissions READ_MEDIA_IMAGES 273")
            /*needsStorage = needsStorage && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_IMAGES
            ) != PackageManager.PERMISSION_GRANTED

            Log.d(TAG, "!@# checkPermissions READ_MEDIA_AUDIO 279")
            println("!@# checkPermissions READ_MEDIA_AUDIO 279")
            needsStorage =
                needsStorage && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_AUDIO
                ) != PackageManager.PERMISSION_GRANTED

            Log.d(TAG, "!@# checkPermissions READ_MEDIA_VIDEO 286")
            needsStorage =
                needsStorage && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_VIDEO
                ) != PackageManager.PERMISSION_GRANTED*/
            if (LassiConfig.getConfig().mediaType == MediaType.IMAGE) {
                needsStorage = needsStorage && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            } else if (LassiConfig.getConfig().mediaType == MediaType.VIDEO) {
                needsStorage = needsStorage && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_VIDEO
                ) != PackageManager.PERMISSION_GRANTED
            } else {
                if (LassiConfig.getConfig().mediaType == MediaType.AUDIO) {
                    needsStorage = needsStorage && ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_MEDIA_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED
                }
            }
        }

        Log.d(TAG, "!@# checkPermissions RECORD_AUDIO 294")
        println("!@# checkPermissions RECORD_AUDIO 294")
        if (needsAudio)
            needsAudio =
                needsAudio && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED

        return !needsCamera && !needsAudio && !needsStorage
    }

    private fun setResultOk(selectedMedia: ArrayList<MiMedia>?) {
        val intent = Intent().apply {
            putExtra(KeyUtils.SELECTED_MEDIA, selectedMedia)
        }
        activity?.setResult(Activity.RESULT_OK, intent)
        activity?.finish()
    }

    private fun requestForPermissions() {
        Log.d(TAG, "!@# requestForPermissions 314")
        println("!@# requestForPermissions 314")
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_IMAGES
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_VIDEO
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val permissions = mutableListOf(
                Manifest.permission.CAMERA
            )

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                Log.d(TAG, "!@# requestForPermissions WRITE_EXTERNAL_STORAGE 331 IF")
                println("!@# requestForPermissions WRITE_EXTERNAL_STORAGE 331 IF")
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Log.d(TAG, "!@# requestForPermissions READ_MEDIA_IMAGES, VID, AUD 334 ELSE IF")
                println("!@# requestForPermissions READ_MEDIA_IMAGES, VID, AUD 334 ELSE IF")
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
                permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            }

            val needsAudio =
                LassiConfig.getConfig().mediaType == MediaType.VIDEO
                        && cameraView.audio == Audio.ON
                        && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED

            // Request audio permission only for video recording
            if (needsAudio) {
                Log.d(TAG, "!@# requestForPermissions READ_AUD 350 IF")
                println("!@# requestForPermissions READ_AUD 350 IF")
                permissions.add(Manifest.permission.RECORD_AUDIO)
            }
            Log.d(TAG, "!@# requestForPermissions permissions.toTypedArray() 362 => ${permissions.toTypedArray()}")
            println("!@# requestForPermissions permissions.toTypedArray() 362 => ${permissions.toTypedArray()}")

            requestPermission.launch(permissions.toTypedArray())
        }
    }

    private fun initCamera() {
        if (checkPermissions(cameraView.audio))
            cameraView.open()
        else {
            requestForPermissions()
        }
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity).supportActionBar?.show()
    }
}
