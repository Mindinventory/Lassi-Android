package com.lassi.presentation.camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.lassi.R
import com.lassi.common.extenstions.hide
import com.lassi.common.extenstions.invisible
import com.lassi.common.extenstions.show
import com.lassi.common.utils.CropUtils
import com.lassi.common.utils.KeyUtils.SETTINGS_REQUEST_CODE
import com.lassi.data.common.VideoRecord
import com.lassi.domain.common.SafeObserver
import com.lassi.domain.media.LassiConfig
import com.lassi.domain.media.MediaType
import com.lassi.presentation.cameraview.audio.Audio
import com.lassi.presentation.cameraview.audio.Flash
import com.lassi.presentation.cameraview.audio.Mode
import com.lassi.presentation.cameraview.controls.CameraListener
import com.lassi.presentation.cameraview.controls.CameraOptions
import com.lassi.presentation.cameraview.controls.CameraView.PERMISSION_REQUEST_CODE
import com.lassi.presentation.cameraview.controls.PictureResult
import com.lassi.presentation.cameraview.controls.VideoResult
import com.lassi.presentation.common.LassiBaseViewModelFragment
import com.lassi.presentation.videopreview.VideoPreviewActivity
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.File

class CameraFragment : LassiBaseViewModelFragment<CameraViewModel>(), View.OnClickListener {

    private lateinit var cameraMode: Mode
    private val lassiConfig = LassiConfig.getConfig()

    override fun buildViewModel(): CameraViewModel {
        return ViewModelProviders.of(this)[CameraViewModel::class.java]
    }

    override fun getContentResource() = R.layout.activity_camera

    override fun getBundle() {
        super.getBundle()
        cameraMode = if (lassiConfig.mediaType == MediaType.VIDEO) {
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
        viewModel.cropImageLiveData.observe(this, SafeObserver {
            CropUtils.beginCrop(requireActivity(), it)
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

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            var valid = true
            for (grantResult in grantResults) {
                valid = valid && grantResult == PackageManager.PERMISSION_GRANTED
            }
            if (valid && !cameraView.isOpened) {
                cameraView.open()
            } else {
                showPermissionDisableAlert()
            }
        }
    }

    private fun showPermissionDisableAlert() {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setMessage(R.string.camera_audio_permission_rational)
            .setCancelable(false)
            .setPositiveButton(R.string.ok) { _, _ ->
                val intent = Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", activity?.packageName, null)
                }
                startActivityForResult(intent, SETTINGS_REQUEST_CODE)
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
        var needsStoragePermission = true
        var needsAudio = audio == Audio.ON

        needsCamera =
            needsCamera && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        needsStoragePermission =
            needsStoragePermission && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        needsAudio =
            needsAudio && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED

        return !needsCamera && !needsAudio && !needsStoragePermission
    }

    private fun requestForPermissions() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SETTINGS_REQUEST_CODE) {
            initCamera()
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