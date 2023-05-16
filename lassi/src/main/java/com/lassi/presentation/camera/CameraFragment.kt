package com.lassi.presentation.camera

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
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
import com.lassi.common.utils.KeyUtils
import com.lassi.common.utils.ToastUtils
import com.lassi.data.common.StartVideoContract
import com.lassi.data.common.VideoRecord
import com.lassi.data.media.MiMedia
import com.lassi.databinding.FragmentCameraBinding
import com.lassi.domain.common.SafeObserver
import com.lassi.domain.media.LassiConfig
import com.lassi.domain.media.LassiOption
import com.lassi.domain.media.MediaType
import com.lassi.presentation.cameraview.audio.Audio
import com.lassi.presentation.cameraview.audio.Flash
import com.lassi.presentation.cameraview.audio.Mode
import com.lassi.presentation.cameraview.controls.CameraListener
import com.lassi.presentation.cameraview.controls.CameraOptions
import com.lassi.presentation.cameraview.controls.PictureResult
import com.lassi.presentation.cameraview.controls.VideoResult
import com.lassi.presentation.common.LassiBaseViewModelFragment
import com.lassi.presentation.cropper.CropImageContract
import com.lassi.presentation.cropper.CropImageContractOptions
import com.lassi.presentation.cropper.CropImageOptions
import com.lassi.presentation.cropper.CropImageView
import com.lassi.presentation.media.SelectedMediaViewModel
import com.lassi.presentation.mediadirectory.FolderViewModel
import com.lassi.presentation.mediadirectory.FolderViewModelFactory
import com.lassi.presentation.mediadirectory.SelectedMediaViewModelFactory
import java.io.File

class CameraFragment : LassiBaseViewModelFragment<CameraViewModel, FragmentCameraBinding>(),
    View.OnClickListener {

    private lateinit var cameraMode: Mode

    private val cameraViewModel by lazy {
        ViewModelProvider(
            this, SelectedMediaViewModelFactory(requireContext())
        )[SelectedMediaViewModel::class.java]
    }

    private val folderViewModel by lazy {
        ViewModelProvider(
            this, FolderViewModelFactory(requireContext())
        )[FolderViewModel::class.java]
    }

    private val startVideoContract = registerForActivityResult(StartVideoContract()) { miMedia ->
        if (LassiConfig.isSingleMediaSelection()) {
            miMedia?.let { setResultOk(arrayListOf(it)) }
        } else {
            LassiConfig.getConfig().selectedMedias.add(miMedia!!)
            cameraViewModel.addSelectedMedia(miMedia)
            folderViewModel.checkInsert()
            if (LassiConfig.getConfig().lassiOption == LassiOption.CAMERA_AND_GALLERY || LassiConfig.getConfig().lassiOption == LassiOption.GALLERY) {
                miMedia?.let { setResultOk(arrayListOf(it)) }
            }
        }
    }

    private val cropImage = registerForActivityResult(CropImageContract()) { miMedia ->
        if (LassiConfig.isSingleMediaSelection()) {
            miMedia?.let { setResultOk(arrayListOf(it)) }
        } else {
            LassiConfig.getConfig().selectedMedias.add(miMedia!!)
            cameraViewModel.addSelectedMedia(miMedia)
            folderViewModel.checkInsert()
            if (LassiConfig.getConfig().lassiOption == LassiOption.CAMERA_AND_GALLERY || LassiConfig.getConfig().lassiOption == LassiOption.GALLERY) {
                setResultOk(arrayListOf(miMedia))
                parentFragmentManager.popBackStack()
            }
        }
    }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            var valid = true
            for (grantResult in it.entries) {
                valid = valid && grantResult.value
            }
            if (valid && !binding.cameraView.isOpened) {
                binding.cameraView.open()
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

    override fun inflateLayout(layoutInflater: LayoutInflater): FragmentCameraBinding {
        return FragmentCameraBinding.inflate(layoutInflater)
    }

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
        if (checkPermissions(binding.cameraView.audio))
            binding.cameraView.open()
    }

    override fun initViews() {
        super.initViews()
        binding.also {
            it.ivCaptureImage.setOnClickListener(this)
            it.ivFlipCamera.setOnClickListener(this)
            it.ivFlash.setOnClickListener(this)
            it.cameraView.setLifecycleOwner(this)
            it.cameraView.addCameraListener(object : CameraListener() {
                override fun onCameraOpened(options: CameraOptions) {
                    it.cameraView.mode = cameraMode
                }

                override fun onPictureTaken(result: PictureResult) {
                    super.onPictureTaken(result)
                    viewModel.onPictureTaken(result.data)
                }

                override fun onVideoTaken(video: VideoResult) {
                    super.onVideoTaken(video)
                    stopVideoRecording()
                    startVideoContract.launch(video.file.absolutePath)
                }
            })
        }
        initCamera()
    }

    override fun initLiveDataObservers() {
        super.initLiveDataObservers()
        viewModel.startVideoRecord.observe(this, SafeObserver(this::handleVideoRecord))
        viewModel.cropImageLiveData.observe(this, SafeObserver { uri ->
            if (LassiConfig.getConfig().isCrop && LassiConfig.getConfig().maxCount <= 1) {
                croppingOptions(uri = uri)
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

    private fun croppingOptions(
        uri: Uri? = null,
        includeCamera: Boolean? = false,
        includeGallery: Boolean? = false
    ) {
        /**
         * Start picker to get image for cropping and then use the image in cropping activity.
         */
        cropImage.launch(
            includeCamera?.let { includeCamera ->
                includeGallery?.let { includeGallery ->
                    CropImageOptions(
                        imageSourceIncludeCamera = includeCamera,
                        imageSourceIncludeGallery = includeGallery,
                        cropShape = CropImageView.CropShape.RECTANGLE,
                        showCropOverlay = true,
                        guidelines = CropImageView.Guidelines.ON,
                        multiTouchEnabled = false,
                    )
                }
            }?.let {
                CropImageContractOptions(
                    uri = uri,
                    cropImageOptions = it,
                )
            }
        )
    }


    private fun toggleCamera() {
        binding.apply {
            if (cameraView.isTakingPicture || cameraView.isTakingVideo) return
            cameraView.toggleFacing()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivCaptureImage -> {
                binding.apply {
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
            }
            R.id.ivFlipCamera -> toggleCamera()
            R.id.ivFlash -> {
                //Check whether the flashlight is available or not?
                val isFlashAvailable =
                    requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
                if (isFlashAvailable) {
                    binding.apply {
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
    }

    private fun handleVideoRecord(videoRecord: VideoRecord<File>) {
        when (videoRecord) {
            is VideoRecord.Start -> startVideoRecording(videoRecord.item)
            is VideoRecord.Timer -> binding.tvTimer.text = videoRecord.item
            is VideoRecord.End -> stopVideoRecording()
            is VideoRecord.Error -> showVideoError(videoRecord.minVideoTime)
        }
    }

    private fun startVideoRecording(videoFile: File) {
        binding.apply {
            cameraView.takeVideo(videoFile)
            ivFlipCamera.invisible()
            tvTimer.show()
        }
    }

    private fun stopVideoRecording() {
        binding.apply {
            if (cameraView.isTakingVideo) {
                cameraView.stopVideo()
                ivFlipCamera.show()
                tvTimer.hide()
            }
        }
    }

    private fun showVideoError(minVideoTime: String) {
        ToastUtils.showToast(
            requireContext(),
            String.format(getString(R.string.min_video_recording_time_error), minVideoTime)
        )
    }

    private fun flashOn() {
        binding.apply {
            cameraView.flash = Flash.ON
            ivFlash.setImageResource(R.drawable.ic_flash_on_white)
        }
    }

    private fun flashTorch() {
        binding.apply {
            cameraView.flash = Flash.TORCH
            ivFlash.setImageResource(R.drawable.ic_flash_on_white)
        }
    }

    private fun flashOff() {
        binding.apply {
            cameraView.flash = Flash.OFF
            ivFlash.setImageResource(R.drawable.ic_flash_off_white)
        }
    }

    private fun flashAuto() {
        binding.apply {
            cameraView.flash = Flash.AUTO
            ivFlash.setImageResource(R.drawable.ic_flash_auto_white)
        }
    }

    private fun showPermissionDisableAlert() {
        val alertMessage =
            if (LassiConfig.getConfig().mediaType == MediaType.VIDEO
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
            ) {
                R.string.camera_audio_storage_permission_rational
            } else if (LassiConfig.getConfig().mediaType == MediaType.VIDEO
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            ) {
                R.string.camera_audio_permission_rational
            } else if (LassiConfig.getConfig().mediaType == MediaType.IMAGE
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            ) {
                R.string.camera_permission_rational
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

        with(LassiConfig.getConfig()) {
            permissionDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                .setTextColor(alertDialogNegativeButtonColor)
            permissionDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setTextColor(alertDialogPositiveButtonColor)
        }
    }

    private fun checkPermissions(audio: Audio): Boolean {
        binding.cameraView.checkPermissionsManifestOrThrow(audio)
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

            needsStorage =
                needsStorage && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //Storage permission is not required for Tiramisu
        }

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
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }

            val needsAudio =
                LassiConfig.getConfig().mediaType == MediaType.VIDEO
                        && binding.cameraView.audio == Audio.ON
                        && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED

            // Request audio permission only for video recording
            if (needsAudio) {
                permissions.add(Manifest.permission.RECORD_AUDIO)
            }

            requestPermission.launch(permissions.toTypedArray())
        }
    }

    private fun initCamera() {
        if (checkPermissions(binding.cameraView.audio))
            binding.cameraView.open()
        else {
            requestForPermissions()
        }
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity).supportActionBar?.show()
    }
}
