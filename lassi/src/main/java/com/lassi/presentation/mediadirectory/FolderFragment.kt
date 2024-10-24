package com.lassi.presentation.mediadirectory


import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.lassi.R
import com.lassi.common.extenstions.hide
import com.lassi.common.extenstions.safeObserve
import com.lassi.common.extenstions.show
import com.lassi.common.utils.KeyUtils
import com.lassi.common.utils.ToastUtils
import com.lassi.common.utils.UriHelper.getCompressFormatForUri
import com.lassi.common.utils.UriHelper.isPhoto
import com.lassi.common.utils.UriHelper.isVideo
import com.lassi.data.common.Response
import com.lassi.data.media.MiItemMedia
import com.lassi.data.media.MiMedia
import com.lassi.databinding.FragmentMediaPickerBinding
import com.lassi.domain.media.LassiConfig
import com.lassi.domain.media.LassiOption
import com.lassi.domain.media.MediaType
import com.lassi.domain.media.MultiLangConfig
import com.lassi.presentation.common.LassiBaseViewModelFragment
import com.lassi.presentation.common.decoration.GridSpacingItemDecoration
import com.lassi.presentation.cropper.BitmapUtils.decodeUriToBitmap
import com.lassi.presentation.cropper.BitmapUtils.writeBitmapToUri
import com.lassi.presentation.cropper.CropImageContract
import com.lassi.presentation.cropper.CropImageContractOptions
import com.lassi.presentation.cropper.CropImageOptions
import com.lassi.presentation.cropper.CropImageView
import com.lassi.presentation.media.MediaFragment
import com.lassi.presentation.mediadirectory.adapter.FolderAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class FolderFragment : LassiBaseViewModelFragment<FolderViewModel, FragmentMediaPickerBinding>(),
    CropImageView.OnSetImageUriCompleteListener, CropImageView.OnCropImageCompleteListener {

    companion object {
        fun newInstance(): FolderFragment {
            return FolderFragment()
        }
    }

    private val pickerTypes =
        setOf(MediaType.PHOTO_PICKER, MediaType.VIDEO_PICKER, MediaType.PHOTO_VIDEO_PICKER)
    private var needsStorage = true

    private val photoPermission = mutableListOf(
        Manifest.permission.READ_MEDIA_IMAGES
    )

    private val list = ArrayList<Uri>()
    private var count = 0
    private val croppedImages = ArrayList<MiMedia>()

    private val cropImage = registerForActivityResult(CropImageContract()) { miMedia ->

        val mediaType = LassiConfig.getConfig().mediaType
        if (miMedia != null) {
            croppedImages.add(miMedia)
            if (++count == list.size) {
                /// called when all the cropping was done.
                setResultOk(croppedImages)
            } else {
                lifecycleScope.launch {
                    val isPhoto =
                        withContext(Dispatchers.IO) {
                            mediaType !=
                                    MediaType.VIDEO_PICKER &&
                                    (mediaType != MediaType.PHOTO_VIDEO_PICKER || list.subList(
                                        count,
                                        list.size
                                    ).any { uri ->
                                        val result = !isVideo(uri, requireContext().contentResolver)
                                        if (!result) {
                                            getMediaPathFromURI(requireContext(), uri)?.let {
                                                croppedImages.add(MiMedia(path = it))
                                            }
                                            count++
                                        }
                                        result
                                    })
                        }

                    if (isPhoto) {
                        withContext(Dispatchers.IO) {
                            getMediaPathFromURI(requireContext(), list[count])?.let { path ->
                                Uri.fromFile(File(path))?.let { uri ->
                                    withContext(Dispatchers.Main) {
                                        croppingOptions(uri)
                                    }
                                }
                            }
                        }
                    } else {
                        // all the cropping was done
                        setResultOk(croppedImages)
                    }
                }
            }
        } else {

            /// when user back from the cropping.
            pickMedia(mediaType = LassiConfig.getConfig().mediaType, mediaPickerLauncher)
        }
    }

    private val photoPermissionAnd14 = mutableListOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    )

    private val vidPermission = mutableListOf(
        Manifest.permission.READ_MEDIA_VIDEO
    )

    private val vidPermissionAnd14 = mutableListOf(
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    )

    private val audioPermission = mutableListOf(
        Manifest.permission.READ_MEDIA_AUDIO
    )

    private val permissionSettingResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            requestPermission()
        }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            if (map.entries.all { it.value }) {
                viewModel.checkInsert()
            } else {
                showPermissionDisableAlert()
            }
        }

    /**
     * This is the approach to store picked media (Video for now) in the DB and rest of the flow
     * would remain folder wise as same as it was before
     */
    private val requestPhotoPickerPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
            // Specifically check for android.permission.READ_MEDIA_VISUAL_USER_SELECTED permission for Android 14 case
            if (map[Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED] == true) {
                viewModel.addPhotoPickerDataInDatabase()
            } else {
                showPermissionDisableAlert()
            }
        }

    /**
     * Video picker in case of Android 14 will be handled here
     * Need to modifty for handling multp
     */

    private val multiPicker =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
            updateMediaList(uris)
        }

    private val singlePicker =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            uri?.let { updateMediaList(listOf(it)) } ?: handleEmptySelection()
        }

    private fun updateMediaList(uris: List<Uri>) {
        list.clear()
        croppedImages.clear()
        list.addAll(uris)
        handleMediaPickerResult(uris)
    }

    private fun handleEmptySelection() {
        Log.d("PhotoPicker", "!@# PHOTO-PICKER:: No media selected")
        activity?.finish()
    }

    private fun handleMediaPickerResult(uris: List<Uri>) {
        val config = LassiConfig.getConfig()
        if (uris.isEmpty()) return handleEmptySelection()

        if (uris.size > config.maxCount) {
            ToastUtils.showToast(requireContext(), config.customLimitExceedingErrorMessage)
            activity?.finish()
            return
        }

        val mediaPaths = uris.mapNotNull { uri ->
            getMediaPathFromURI(requireContext(), uri)?.let { MiMedia(path = it) }
        }

        val isPhoto = determineIfPhoto(uris, mediaPaths, config)

        if (config.isCrop && isPhoto) {
            cropMedia(mediaPaths)
        } else {
            handleCompressionAndResult(ArrayList(mediaPaths), config)
        }
    }

    private fun determineIfPhoto(
        uris: List<Uri>,
        mediaPaths: List<MiMedia>,
        config: LassiConfig
    ): Boolean {
        val mediaType = config.mediaType
        return mediaType != MediaType.VIDEO_PICKER && (mediaType != MediaType.PHOTO_VIDEO_PICKER || uris.any { uri ->
            val isPhoto = !isVideo(uri, requireContext().contentResolver)
            if (!isPhoto) croppedImages.add(mediaPaths[count++])
            isPhoto
        })
    }

    private fun cropMedia(mediaPaths: List<MiMedia>) {
        mediaPaths[count].path?.let { path ->
            Uri.fromFile(File(path))?.let { uri ->
                croppingOptions(uri)
            }
        }
    }

    private fun handleCompressionAndResult(mediaPaths: ArrayList<MiMedia>, config: LassiConfig) {
        if (config.compressionRation != 0) {
            compressMedia(mediaPaths, config)
        } else {
            Log.d("PhotoPicker", "!@# PHOTO-PICKER:: Media paths: $mediaPaths")
            setResultOk(mediaPaths)
        }
    }

    private fun compressMedia(mediaPaths: ArrayList<MiMedia>, config: LassiConfig) {
        mediaPaths.forEachIndexed { index, miMedia ->
            miMedia.path?.let { path ->
                val uri = Uri.fromFile(File(path))
                // Check if the media is a photo before compression
                if (isPhoto(uri, requireContext().contentResolver)) {
                    val compressFormat = getCompressFormatForUri(uri, requireContext())
                    val newUri = writeBitmapToUri(
                        requireContext(),
                        decodeUriToBitmap(requireContext(), uri),
                        compressQuality = config.compressionRation,
                        customOutputUri = null,
                        compressFormat = compressFormat
                    )
                    mediaPaths[index] = miMedia.copy(path = newUri.path)
                }
            }
        }
        setResultOk(mediaPaths)
    }

    private val mediaPickerLauncher =
        if (LassiConfig.getConfig().isMultiPicker) multiPicker else singlePicker

    private fun setResultOk(selectedMedia: ArrayList<MiMedia>?) {
        val intent = Intent().apply {
            putExtra(KeyUtils.SELECTED_MEDIA, selectedMedia)
        }
        activity?.let {
            it.setResult(Activity.RESULT_OK, intent)
            it.finish()
        }
    }

    private val folderAdapter by lazy { FolderAdapter(this::onItemClick) }

    override fun buildViewModel(): FolderViewModel {
        return ViewModelProvider(
            requireActivity(), FolderViewModelFactory(requireActivity())
        )[FolderViewModel::class.java]
    }

    override fun inflateLayout(layoutInflater: LayoutInflater): FragmentMediaPickerBinding {
        return FragmentMediaPickerBinding.inflate(layoutInflater)
    }

    override fun initViews() {
        super.initViews()
        binding.rvMedia.apply {
            setBackgroundColor(LassiConfig.getConfig().galleryBackgroundColor)
            layoutManager = GridLayoutManager(context, LassiConfig.getConfig().gridSize)
            adapter = folderAdapter
            addItemDecoration(GridSpacingItemDecoration(LassiConfig.getConfig().gridSize, 10))
        }
        binding.progressBar.indeterminateDrawable.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                LassiConfig.getConfig().progressBarColor, BlendModeCompat.SRC_ATOP
            )
        requestPermission()
    }

    override fun initLiveDataObservers() {
        super.initLiveDataObservers()
        viewModel.fetchMediaFolderLiveData.safeObserve(viewLifecycleOwner) { response ->
            when (response) {
                is Response.Loading -> {
                    binding.tvNoDataFound.visibility = View.GONE
                    binding.progressBar.show()
                }

                is Response.Success -> {
                    Log.d("FolderFragment", "!@# initLiveDataObservers: ${response.item.size}")
                }

                is Response.Error -> {
                    binding.progressBar.hide()
                    response.throwable.printStackTrace()
                }
            }
        }

        viewModel.getMediaItemList().observe(viewLifecycleOwner) {
            binding.progressBar.hide()
            if (!it.isNullOrEmpty()) {
                folderAdapter.setList(it)
            }
        }

        viewModel.emptyList.observe(viewLifecycleOwner) {
            binding.tvNoDataFound.visibility = if (it) View.VISIBLE else View.GONE
            binding.tvNoDataFound.text = MultiLangConfig.getConfig().noDataFound
        }

        viewModel.fileRemovalCheck.observe(viewLifecycleOwner) { isTrue ->
            if (isTrue) {
                folderAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (LassiConfig.getConfig().mediaType == MediaType.IMAGE) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    needsStorage = needsStorage && ActivityCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                    ) != PackageManager.PERMISSION_GRANTED
                    requestPhotoPickerPermission.launch(photoPermissionAnd14.toTypedArray())
                } else {
                    needsStorage = needsStorage && ActivityCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.READ_MEDIA_IMAGES
                    ) != PackageManager.PERMISSION_GRANTED
                    requestPermission.launch(photoPermission.toTypedArray())
                }
            } else if (LassiConfig.getConfig().mediaType == MediaType.VIDEO) {
                Log.d("TAG", "!@# PHOTO-PICKER:: mediaType == MediaType.VIDEO")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    needsStorage = needsStorage && ActivityCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                    ) != PackageManager.PERMISSION_GRANTED
                    requestPhotoPickerPermission.launch(vidPermissionAnd14.toTypedArray())
                } else {
                    needsStorage = needsStorage && ActivityCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.READ_MEDIA_VIDEO
                    ) != PackageManager.PERMISSION_GRANTED
                    requestPermission.launch(vidPermission.toTypedArray())
                }
            } else if (LassiConfig.getConfig().mediaType in pickerTypes) {
                Log.d("TAG", "!@# PHOTO-PICKER:: mediaType == MediaType.PHOTOPICKER")
                needsStorage = needsStorage && ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                ) != PackageManager.PERMISSION_GRANTED
                Log.d("TAG", "!@# PHOTO-PICKER:: PickVisualMedia.VideoOnly")
                binding.progressBar.show()
                pickMedia(mediaType = LassiConfig.getConfig().mediaType, mediaPickerLauncher)
            } else {
                if (LassiConfig.getConfig().mediaType == MediaType.AUDIO) {
                    needsStorage = needsStorage && ActivityCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.READ_MEDIA_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED
                    requestPermission.launch(audioPermission.toTypedArray())
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (LassiConfig.getConfig().mediaType in pickerTypes) {
                needsStorage = needsStorage && ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                ) != PackageManager.PERMISSION_GRANTED
                Log.d("TAG", "!@# PHOTO-PICKER:: PickVisualMedia.VideoOnly")
                binding.progressBar.show()
                pickMedia(mediaType = LassiConfig.getConfig().mediaType, mediaPickerLauncher)
            } else {
                requestPermission.launch(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                )
            }
        } else {
            if (LassiConfig.getConfig().mediaType in pickerTypes) {
                needsStorage = needsStorage && ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                ) != PackageManager.PERMISSION_GRANTED
                Log.d("TAG", "!@# PHOTO-PICKER:: PickVisualMedia.VideoOnly")
                binding.progressBar.show()
                pickMedia(mediaType = LassiConfig.getConfig().mediaType, mediaPickerLauncher)
            } else {
                requestPermission.launch(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
            }
        }
    }

    private fun pickMedia(
        mediaType: MediaType,
        launcher: ActivityResultLauncher<PickVisualMediaRequest>
    ) {
        val mediaTypeToPick = when (mediaType) {
            MediaType.PHOTO_PICKER -> ActivityResultContracts.PickVisualMedia.ImageOnly
            MediaType.VIDEO_PICKER -> ActivityResultContracts.PickVisualMedia.VideoOnly
            MediaType.PHOTO_VIDEO_PICKER -> ActivityResultContracts.PickVisualMedia.ImageAndVideo
            else -> null
        }

        if (mediaTypeToPick != null) {
            launcher.launch(PickVisualMediaRequest(mediaTypeToPick))
        }
    }

    private fun getMediaPathFromURI(context: Context, uri: Uri): String? {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val fileName = getFileName(context.contentResolver, uri)
        inputStream?.use { input ->
            val outputFile =
                File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), fileName)
            FileOutputStream(outputFile).use { output ->
                val buffer = ByteArray(4 * 1024) // buffer size
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
                return outputFile.absolutePath
            }
        }
        return null
    }

    private fun getFileName(contentResolver: ContentResolver, uri: Uri): String {
        var fileName = "temp_media"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val displayNameIndex =
                    cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                fileName = cursor.getString(displayNameIndex)
            }
        }
        return fileName
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkRemoval()
    }

    private fun onItemClick(bucket: MiItemMedia) {
        activity?.supportFragmentManager?.beginTransaction()?.setCustomAnimations(
            R.anim.right_in, R.anim.right_out, R.anim.right_in, R.anim.right_out
        )?.add(R.id.ftContainer, MediaFragment.getInstance(bucket))
            ?.addToBackStack(MediaFragment::class.java.simpleName)?.commitAllowingStateLoss()
    }

    private fun showPermissionDisableAlert() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            showPermissionAlert(msg = MultiLangConfig.getConfig().storagePermissionRational)
        } else {
            if (LassiConfig.getConfig().mediaType == MediaType.IMAGE) {
                showPermissionAlert(msg = MultiLangConfig.getConfig().readMediaImagesVideoPermissionRational)
            } else if (LassiConfig.getConfig().mediaType == MediaType.VIDEO) {
                showPermissionAlert(msg = MultiLangConfig.getConfig().readMediaImagesVideoPermissionRational)
            } else {
                if (LassiConfig.getConfig().mediaType == MediaType.AUDIO) {
                    showPermissionAlert(msg = MultiLangConfig.getConfig().readMediaAudioPermissionRational)
                }
            }
        }
    }

    private fun showPermissionAlert(msg: String) {
        AlertDialog.Builder(requireContext(), R.style.dialogTheme).apply {
            setMessage(msg)
            setCancelable(false)
            setPositiveButton(MultiLangConfig.getConfig().ok) { _, _ ->
                val intent = Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", activity?.packageName, null)
                }
                permissionSettingResult.launch(intent)
            }
            setNegativeButton(MultiLangConfig.getConfig().cancel) { _, _ ->
                activity?.onBackPressed()
            }
        }.create().apply {
            setCancelable(false)
            show()
            LassiConfig.getConfig().let {
                getButton(DialogInterface.BUTTON_NEGATIVE)?.setTextColor(it.alertDialogNegativeButtonColor)
                getButton(DialogInterface.BUTTON_POSITIVE)?.setTextColor(it.alertDialogPositiveButtonColor)
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.menuCamera)?.isVisible =
            if (LassiConfig.getConfig().mediaType == MediaType.IMAGE
                || LassiConfig.getConfig().mediaType == MediaType.VIDEO
            ) {
                (LassiConfig.getConfig().lassiOption == LassiOption.CAMERA_AND_GALLERY
                        || LassiConfig.getConfig().lassiOption == LassiOption.CAMERA)
            } else {
                false
            }
        menu.findItem(R.id.menuSort)?.isVisible = false
        return super.onPrepareOptionsMenu(menu)
    }

    override fun hasOptionMenu(): Boolean = true

    private fun croppingOptions(
        uri: Uri? = null, includeCamera: Boolean? = false, includeGallery: Boolean? = false
    ) {
        // Start picker to get image for cropping and then use the image in cropping activity.
        cropImage.launch(includeCamera?.let { includeCamera ->
            includeGallery?.let { includeGallery ->
                LassiConfig.getConfig().cropAspectRatio?.x?.let { x ->
                    LassiConfig.getConfig().cropAspectRatio?.y?.let { y ->
                        CropImageOptions(
                            imageSourceIncludeCamera = includeCamera,
                            imageSourceIncludeGallery = includeGallery,
                            cropShape = LassiConfig.getConfig().cropType,
                            showCropOverlay = true,
                            guidelines = CropImageView.Guidelines.ON,
                            multiTouchEnabled = false,
                            aspectRatioX = x,
                            aspectRatioY = y,
                            fixAspectRatio = LassiConfig.getConfig().enableActualCircleCrop,
                            outputCompressQuality = LassiConfig.getConfig().compressionRation
                        )
                    }
                }
            }
        }?.let {
            CropImageContractOptions(
                uri = uri,
                cropImageOptions = it,
            )
        })
    }

    override fun onSetImageUriComplete(view: CropImageView, uri: Uri, error: Exception?) {
        if (error != null) {
            Toast.makeText(activity, "Image load failed: " + error.message, Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun onCropImageComplete(view: CropImageView, result: CropImageView.CropResult) {
        if (result.error != null) {
            Toast.makeText(activity, "Crop failed: ${result.error.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }
}

