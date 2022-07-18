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
import com.lassi.common.utils.KeyUtils
import com.lassi.data.media.MiMedia
import com.lassi.domain.media.LassiOption
import com.lassi.domain.media.MediaType
import com.lassi.presentation.builder.Lassi
import com.lassi.presentation.common.decoration.GridSpacingItemDecoration
import com.lassi.presentation.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val selectedMediaAdapter by lazy { SelectedMediaAdapter(this::onItemClicked) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnImagePicker.setOnClickListener(this)
        btnVideoPicker.setOnClickListener(this)
        btnAudioPicker.setOnClickListener(this)
        btnDocPicker.setOnClickListener(this)
        btnDocumentSystemIntent.setOnClickListener(this)
        rvSelectedMedia.adapter = selectedMediaAdapter
        rvSelectedMedia.addItemDecoration(GridSpacingItemDecoration(2, 10))
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnImagePicker -> {
                val intent = Lassi(this)
                    .with(LassiOption.CAMERA_AND_GALLERY)
                    .setMaxCount(4)
                    .setGridSize(2)
                    .setPlaceHolder(R.drawable.ic_image_placeholder)
                    .setErrorDrawable(R.drawable.ic_image_placeholder)
                    .setSelectionDrawable(R.drawable.ic_checked_media)
                    .setStatusBarColor(R.color.colorPrimaryDark)
                    .setToolbarColor(R.color.colorPrimary)
                    .setToolbarResourceColor(android.R.color.white)
                    .setProgressBarColor(R.color.colorAccent)
                    .setCropType(CropImageView.CropShape.OVAL)
                    .setCropAspectRatio(1, 1)
                    .setCompressionRation(10)
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
                    .setMaxCount(1)
                    .setGridSize(3)
                    .setMinTime(5)
                    .setMaxTime(30)
                    .setMinFileSize(0)
                    .setMaxFileSize(2000)
                    .setMediaType(MediaType.VIDEO)
                    .setStatusBarColor(R.color.colorPrimaryDark)
                    .setToolbarColor(R.color.colorPrimary)
                    .setToolbarResourceColor(android.R.color.white)
                    .setProgressBarColor(R.color.colorAccent)
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
                if (!selectedMedia.isNullOrEmpty()) {
                    ivEmpty.isVisible = selectedMedia.isEmpty()
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
