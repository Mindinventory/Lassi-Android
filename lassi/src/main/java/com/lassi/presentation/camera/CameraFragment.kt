package com.lassi.presentation.camera

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
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
import com.lassi.common.utils.UriHelper.getCompressFormatForUri
import com.lassi.data.common.StartVideoContract
import com.lassi.data.common.VideoRecord
import com.lassi.data.media.MiMedia
import com.lassi.databinding.FragmentCameraBinding
import com.lassi.domain.common.SafeObserver
import com.lassi.domain.media.LassiConfig
import com.lassi.domain.media.LassiOption
import com.lassi.domain.media.MediaType
import com.lassi.domain.media.MultiLangConfig
import com.lassi.presentation.cameraview.audio.Audio
import com.lassi.presentation.cameraview.audio.Flash
import com.lassi.presentation.cameraview.audio.Mode
import com.lassi.presentation.cameraview.controls.CameraListener
import com.lassi.presentation.cameraview.controls.CameraOptions
import com.lassi.presentation.cameraview.controls.PictureResult
import com.lassi.presentation.cameraview.controls.VideoResult
import com.lassi.presentation.common.LassiBaseViewModelFragment
import com.lassi.presentation.cropper.BitmapUtils.decodeUriToBitmap
import com.lassi.presentation.cropper.BitmapUtils.writeBitmapToUri
import com.lassi.presentation.cropper.CropImageContract
import com.lassi.presentation.cropper.CropImageContractOptions
import com.lassi.presentation.cropper.CropImageOptions
import com.lassi.presentation.cropper.CropImageView
import com.lassi.presentation.media.SelectedMediaViewModel
import com.lassi.presentation.mediadirectory.FolderViewModel
import com.lassi.presentation.mediadirectory.FolderViewModelFactory
import com.lassi.presentation.mediadirectory.SelectedMediaViewModelFactory
import java.io.File
import kotlin.math.log

class CameraFragment : LassiBaseViewModelFragment<CameraViewModel, FragmentCameraBinding>(),
    View.OnClickListener {

    private val TAG: String = "CameraFragment"
    private lateinit var cameraMode: Mode
    private val config = LassiConfig.getConfig()
    private val cameraViewModel by lazy {
        ViewModelProvider(
            this, SelectedMediaViewModelFactory(requireContext())
        )[SelectedMediaViewModel::class.java]
    }

    private var currentCropIndex = 0
    private var croppedMediaList: ArrayList<MiMedia> = ArrayList()
    private var mediaList: ArrayList<MiMedia> = ArrayList()
    private var selected =
        LassiConfig.getConfig().selectedMedias // this gives the gallery selected images.
    private var isFromCropNext = false

    private val cropImage = registerForActivityResult(CropImageContract()) { miMedia ->
        if (LassiConfig.isSingleMediaSelection()) {
            isFromCropNext = true
            miMedia?.let { setResultOk(arrayListOf(it)) }
            return@registerForActivityResult
        }

        if (miMedia != null) {
            // Replace cropped into selected
            selected[currentCropIndex] = miMedia

            // Compress the cropped image
            val compressed = compressSingleMedia(miMedia)
            selected[currentCropIndex] = compressed

            croppedMediaList.add(compressed)
        }
        cropNext()
    }

    private fun getSelectedAndCapturedImages() {
        selected = config.selectedMedias
        selected.addAll(mediaList)
    }

    private fun startCroppingSequence() {
        if (selected.isNotEmpty()) {
            currentCropIndex = 0
            croppedMediaList.clear()
            val firstPath = selected[0].path
            if (!firstPath.isNullOrEmpty()) {
                val uri = Uri.fromFile(File(firstPath))
                croppingOptions(uri)
            } else {
                // skip invalid first path
                cropImage.launch(null)
            }
        } else {
            Log.d(TAG, "startCroppingSequence: selected is empty")
        }
    }

    private fun cropNext() {
        currentCropIndex++
        if (currentCropIndex < selected.size) {
            val path = selected[currentCropIndex].path
            if (!path.isNullOrEmpty()) {
                val uri = Uri.fromFile(File(path))
                croppingOptions(uri)
            } else {
                cropNext()
            }
        } else {
            isFromCropNext = true
            setResultOk(selected)
        }
    }

    private fun compressSingleMedia(miMedia: MiMedia): MiMedia {
        miMedia.path?.let { path ->
            val uri = Uri.fromFile(File(path))
            val compressFormat = getCompressFormatForUri(uri, requireContext())
            val newUri = writeBitmapToUri(
                requireContext(),
                decodeUriToBitmap(requireContext(), uri),
                compressQuality = LassiConfig.getConfig().compressionRatio,
                customOutputUri = null,
                compressFormat = compressFormat
            )
            return miMedia.copy(path = newUri.path)
        }
        return miMedia // return original if path is null
    }

    private val folderViewModel by lazy {
        ViewModelProvider(
            this, FolderViewModelFactory(requireContext())
        )[FolderViewModel::class.java]
    }

    private val startVideoContract = registerForActivityResult(StartVideoContract()) { miMedia ->
        if (LassiConfig.isSingleMediaSelection()) {
            isFromCropNext = true
            miMedia?.let { setResultOk(arrayListOf(it)) }
        } else {
            LassiConfig.getConfig().selectedMedias.add(miMedia!!)
            cameraViewModel.addSelectedMedia(miMedia)
            folderViewModel.checkInsert()
            if (LassiConfig.getConfig().lassiOption == LassiOption.CAMERA_AND_GALLERY || LassiConfig.getConfig().lassiOption == LassiOption.GALLERY) {
                setResultOk(arrayListOf(miMedia))
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
            val config = LassiConfig.getConfig()


            mediaList = arrayListOf(createMiMedia(uri.path))
            croppedMediaList.addAll(config.selectedMedias + mediaList)
            if (config.compressionRatio > 0 && !config.isCrop) {
                compressMedia(croppedMediaList)
            } else { // user has selected the crop option.
                setResultOk(croppedMediaList)
            }
        })
    }

    // Helper function to create MiMedia object
    private fun createMiMedia(path: String?): MiMedia {
        return MiMedia().apply { this.path = path }
    }

    private fun compressMedia(mediaPaths: ArrayList<MiMedia>) {
        mediaPaths.forEachIndexed { index, miMedia ->
            miMedia.path?.let { path ->
                val uri = Uri.fromFile(File(path))
                val compressFormat = getCompressFormatForUri(uri, requireContext())
                val newUri = writeBitmapToUri(
                    requireContext(),
                    decodeUriToBitmap(requireContext(), uri),
                    compressQuality = LassiConfig.getConfig().compressionRatio,
                    customOutputUri = null,
                    compressFormat = compressFormat
                )
                mediaPaths[index] = miMedia.copy(path = newUri.path)
            }
        }
        setResultOk(mediaPaths)
    }

    private fun croppingOptions(uri: Uri) {
        val config = LassiConfig.getConfig()
        val aspectX: Int = config.cropAspectRatio?.x ?: return
        val aspectY: Int = config.cropAspectRatio?.y ?: return

        val cropOptions = CropImageOptions(
            imageSourceIncludeCamera = false,
            imageSourceIncludeGallery = false,
            cropShape = config.cropType,
            showCropOverlay = true,
            guidelines = CropImageView.Guidelines.ON,
            multiTouchEnabled = false,
            aspectRatioX = aspectX,
            aspectRatioY = aspectY,
            fixAspectRatio = config.enableActualCircleCrop,
            outputCompressQuality = config.compressionRatio
        )

        val contractOptions = CropImageContractOptions(uri, cropOptions)
        cropImage.launch(contractOptions)
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
                MultiLangConfig.getConfig().cameraAudioStoragePermissionRational
            } else if (LassiConfig.getConfig().mediaType == MediaType.VIDEO
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            ) {
                MultiLangConfig.getConfig().cameraAudioPermissionRational
            } else if (LassiConfig.getConfig().mediaType == MediaType.IMAGE
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            ) {
                MultiLangConfig.getConfig().cameraPermissionRational
            } else {
                MultiLangConfig.getConfig().cameraStoragePermissionRational
            }

        val alertDialog = AlertDialog.Builder(requireContext(), R.style.dialogTheme)
            .setMessage(alertMessage)
            .setCancelable(false)
            .setPositiveButton(MultiLangConfig.getConfig().ok) { _, _ ->

                val intent = Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", activity?.packageName, null)
                }
                permissionSettingResult.launch(intent)
            }
            .setNegativeButton(MultiLangConfig.getConfig().cancel) { _, _ ->
                activity?.onBackPressedDispatcher?.onBackPressed()
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
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
        } else
            if (needsAudio)
                needsAudio =
                    ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.RECORD_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED

        return !needsCamera && !needsAudio && !needsStorage
    }

    private fun setResultOk(selectedMedia: ArrayList<MiMedia>?) {
        if (isFromCropNext || !config.isCrop) { // the !config.isCrop condition is given as when the crop is been disabled from the main activity, we don't go to the else part.
            /**
             * this will be when calling from the cropNext()
             */
            val intent = Intent().apply {
                putExtra(KeyUtils.SELECTED_MEDIA, selectedMedia)
            }
            activity?.setResult(Activity.RESULT_OK, intent)
            activity?.finish()
            isFromCropNext = false
        } else {
            /**
             * everytime else
             */
            getSelectedAndCapturedImages()
            startCroppingSequence()
        }
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
