package com.lassi.app

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.webkit.MimeTypeMap
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.lassi.app.adapter.SelectedMediaAdapter
import com.lassi.app.databinding.ActivityMainBinding
import com.lassi.common.utils.KeyUtils
import com.lassi.data.media.MiMedia
import com.lassi.domain.media.LassiOption
import com.lassi.domain.media.MediaType
import com.lassi.domain.media.SortingOption
import com.lassi.presentation.builder.Lassi
import com.lassi.presentation.common.decoration.GridSpacingItemDecoration
import com.lassi.presentation.cropper.CropImageView
import java.io.File
import java.util.Locale

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private var _binding: ActivityMainBinding? = null
    protected val binding get() = _binding!!

    private val selectedMediaAdapter by lazy { SelectedMediaAdapter(this::onItemClicked) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        binding.also {
            setContentView(it.root)
            it.btnImagePicker.setOnClickListener(this)
            it.btnVideoPicker.setOnClickListener(this)
            it.btnAudioPicker.setOnClickListener(this)
            it.btnDocPicker.setOnClickListener(this)
            it.btnImageCapture.setOnClickListener(this)
            it.btnVideoCapture.setOnClickListener(this)
            it.btnDocumentSystemIntent.setOnClickListener(this)
            it.rvSelectedMedia.adapter = selectedMediaAdapter
            it.rvSelectedMedia.addItemDecoration(GridSpacingItemDecoration(2, 10))
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnImagePicker -> {
                val intent = Lassi(this)
                    .with(LassiOption.CAMERA_AND_GALLERY)
                    .setMaxCount(1)
                    .setAscSort(SortingOption.ASCENDING)
                    .setGridSize(2)
                    .setPlaceHolder(R.drawable.ic_image_placeholder)
                    .setErrorDrawable(R.drawable.ic_image_placeholder)
                    .setSelectionDrawable(R.drawable.ic_checked_media)
                    .setStatusBarColor(R.color.colorPrimaryDark)
                    .setToolbarColor(R.color.colorPrimary)
                    .setToolbarResourceColor(android.R.color.white)
                    .setAlertDialogNegativeButtonColor(R.color.cherry_red)
                    .setAlertDialogPositiveButtonColor(R.color.emerald_green)
                    .setProgressBarColor(R.color.colorAccent)
                    .setGalleryBackgroundColor(R.color.colorGrey)
                    .setCropType(CropImageView.CropShape.OVAL)
                    .setCropAspectRatio(1, 1)
                    .setCompressionRatio(10)
                    .setMinFileSize(0)
                    .setMaxFileSize(10000)
                    .enableActualCircleCrop()
                    .setSupportedFileTypes("jpg", "jpeg", "png", "webp", "gif")
                    .enableFlip()
                    .enableRotate()
                    .build()
                receiveData.launch(intent)

            }

            R.id.btnVideoPicker -> {
                val intent = Lassi(this)
                    .with(LassiOption.CAMERA_AND_GALLERY)
                    .setMaxCount(4)
                    .setGridSize(3)
                    .setMinTime(5)
                    .setMaxTime(30)
                    .setMinFileSize(0)
                    .setMaxFileSize(20000)
                    .setMediaType(MediaType.VIDEO)
                    .setStatusBarColor(R.color.colorPrimaryDark)
                    .setToolbarColor(R.color.colorPrimary)
                    .setToolbarResourceColor(android.R.color.white)
                    .setAlertDialogNegativeButtonColor(R.color.cherry_red)
                    .setAlertDialogPositiveButtonColor(R.color.emerald_green)
                    .setProgressBarColor(R.color.colorAccent)
                    .setGalleryBackgroundColor(R.color.colorGrey)
                    .setPlaceHolder(R.drawable.ic_video_placeholder)
                    .setErrorDrawable(R.drawable.ic_video_placeholder)
                    .setSelectionDrawable(R.drawable.ic_checked_media)
                    .setSupportedFileTypes("mp4", "mkv", "webm", "avi", "flv", "3gp")
                    .build()
                receiveData.launch(intent)
            }

            R.id.btnAudioPicker -> {
                val intent = Lassi(this)
                    .setMediaType(MediaType.AUDIO)
                    .setMaxCount(4)
                    .setGridSize(2)
                    .setPlaceHolder(R.drawable.ic_audio_placeholder)
                    .setErrorDrawable(R.drawable.ic_audio_placeholder)
                    .setSelectionDrawable(R.drawable.ic_checked_media)
                    .setStatusBarColor(R.color.colorPrimaryDark)
                    .setToolbarColor(R.color.colorPrimary)
                    .setToolbarResourceColor(android.R.color.white)
                    .setProgressBarColor(R.color.colorAccent)
                    .setGalleryBackgroundColor(R.color.colorGrey)
                    .build()
                receiveData.launch(intent)
            }

            R.id.btnDocPicker -> {
                requestPermissionForDocument()
            }

            R.id.btnDocumentSystemIntent -> {
                val intent = Lassi(this)
                    .setMediaType(MediaType.FILE_TYPE_WITH_SYSTEM_VIEW)
                    .setSupportedFileTypes(
                        "jpg",
                        "jpeg",
                        "png",
                        "webp",
                        "gif",
                        "mp4",
                        "mkv",
                        "webm",
                        "avi",
                        "flv",
                        "3gp",
                        "pdf",
                        "odt",
                        "doc",
                        "docs",
                        "docx",
                        "txt",
                        "ppt",
                        "pptx",
                        "rtf",
                        "xlsx",
                        "xls"
                    )
                    .setMaxCount(3)
                    .setCustomLimitExceedingErrorMessage(R.string.error_exceed_msg)
                    .build()
                receiveData.launch(intent)
            }

            R.id.btnImageCapture -> {
                val intent = Lassi(this)
                    .with(LassiOption.CAMERA)
                    .setMaxCount(1)
                    .setGridSize(2)
                    .setPlaceHolder(R.drawable.ic_image_placeholder)
                    .setErrorDrawable(R.drawable.ic_image_placeholder)
                    .setSelectionDrawable(R.drawable.ic_checked_media)
                    .setStatusBarColor(R.color.colorPrimaryDark)
                    .setToolbarColor(R.color.colorPrimary)
                    .setToolbarResourceColor(android.R.color.white)
                    .setProgressBarColor(R.color.colorAccent)
                    .setGalleryBackgroundColor(R.color.colorGrey)
                    .setAlertDialogNegativeButtonColor(R.color.cherry_red)
                    .setAlertDialogPositiveButtonColor(R.color.emerald_green)
                    .setMediaType(MediaType.IMAGE)
                    .setCropType(CropImageView.CropShape.OVAL)
                    .setCropAspectRatio(1, 1)
                    .setCompressionRatio(0)
                    .setMinFileSize(0)
                    .setMaxFileSize(1000000)
                    .enableActualCircleCrop()
                    .setSupportedFileTypes("jpg", "jpeg", "png", "webp", "gif")
                    .enableFlip()
                    .enableRotate()
                    .build()
                receiveData.launch(intent)
            }

            R.id.btnVideoCapture -> {
                val intent = Lassi(this)
                    .with(LassiOption.CAMERA)
                    .setMaxCount(1)
                    .setGridSize(3)
                    .setMinTime(5)
                    .setMaxTime(30)
                    .setPlaceHolder(R.drawable.ic_image_placeholder)
                    .setErrorDrawable(R.drawable.ic_image_placeholder)
                    .setSelectionDrawable(R.drawable.ic_checked_media)
                    .setStatusBarColor(R.color.colorPrimaryDark)
                    .setToolbarColor(R.color.colorPrimary)
                    .setMediaType(MediaType.VIDEO)
                    .setToolbarResourceColor(android.R.color.white)
                    .setAlertDialogNegativeButtonColor(R.color.cherry_red)
                    .setAlertDialogPositiveButtonColor(R.color.emerald_green)
                    .setProgressBarColor(R.color.colorAccent)
                    .setGalleryBackgroundColor(R.color.colorGrey)
                    .setCropType(CropImageView.CropShape.OVAL)
                    .setCropAspectRatio(1, 1)
                    .setCompressionRatio(0)
                    .setMinFileSize(0)
                    .setMaxFileSize(10000)
                    .enableActualCircleCrop()
                    .setSupportedFileTypes("jpg", "jpeg", "png", "webp", "gif")
                    .enableFlip()
                    .enableRotate()
                    .build()
                receiveData.launch(intent)
            }
        }
    }

    private fun launchDocPicker() {
        val intent = Lassi(this)
            .setMediaType(MediaType.DOC)
            .setMaxCount(4)
            .setGridSize(2)
            .setPlaceHolder(R.drawable.ic_document_placeholder)
            .setErrorDrawable(R.drawable.ic_document_placeholder)
            .setSelectionDrawable(R.drawable.ic_checked_media)
            .setStatusBarColor(R.color.colorPrimaryDark)
            .setToolbarColor(R.color.colorPrimary)
            .setToolbarResourceColor(android.R.color.white)
            .setAlertDialogNegativeButtonColor(R.color.cherry_red)
            .setAlertDialogPositiveButtonColor(R.color.emerald_green)
            .setGalleryBackgroundColor(R.color.colorGrey)
            .setSupportedFileTypes(
                "pdf",
                "odt",
                "doc",
                "docs",
                "docx",
                "txt",
                "ppt",
                "pptx",
                "rtf",
                "xlsx",
                "xls"
            )
            .setProgressBarColor(R.color.colorAccent)
            .build()
        receiveData.launch(intent)
    }

    private val receiveData =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val selectedMedia =
                    it.data?.getSerializableExtra(KeyUtils.SELECTED_MEDIA) as ArrayList<MiMedia>

                if (selectedMedia.isNotEmpty()) {
                    binding.ivEmpty.isVisible = selectedMedia.isEmpty()
                    selectedMediaAdapter.setList(selectedMedia)
                }
            }
        }

    private fun getMimeType(uri: Uri): String? {
        return if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            contentResolver.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(fileExtension.lowercase(Locale.getDefault()))
        }
    }

    private fun onItemClicked(miMedia: MiMedia) {
        miMedia.path?.let {
            val intent = Intent(Intent.ACTION_VIEW)
            if (miMedia.doesUri) {
                val uri = Uri.parse(it)
                intent.setDataAndType(uri, getMimeType(uri))
            } else {
                val file = File(it)
                val fileUri = FileProvider.getUriForFile(
                    this,
                    applicationContext.packageName + ".fileprovider", file
                )
                intent.setDataAndType(fileUri, getMimeType(fileUri))
            }

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(intent, "Open file"))
        }
    }

    /**
     *   If Android device SDK is >= 30 and wants to access document (only for choose the non media file)
     *   then ask for "android.permission.MANAGE_EXTERNAL_STORAGE" permission
     */
    private fun requestPermissionForDocument() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                if (Environment.isExternalStorageManager()) {
                    launchDocPicker()
                } else {
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                        intent.addCategory("android.intent.category.DEFAULT")
                        intent.data = Uri.parse(
                            String.format("package:%s", applicationContext?.packageName)
                        )
                        mPermissionSettingResult.launch(intent)
                    } catch (e: Exception) {
                        val intent = Intent()
                        intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                        mPermissionSettingResult.launch(intent)
                    }
                }
            }

            else -> {
                launchDocPicker()
            }
        }
    }

    private val mPermissionSettingResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            requestPermissionForDocument()
        }
}
