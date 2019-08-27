package com.lassi.app

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.MimeTypeMap
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
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

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val MEDIA_REQUEST_CODE = 100
    private val selectedMediaAdapter by lazy { SelectedMediaAdapter(this::onItemClicked) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnImagePicker.setOnClickListener(this)
        btnVideoPicker.setOnClickListener(this)
        btnAudioPicker.setOnClickListener(this)
        btnDocPicker.setOnClickListener(this)
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
                    .setStatusBarColor(R.color.colorPrimaryDark)
                    .setToolbarColor(R.color.colorPrimary)
                    .setToolbarResourceColor(android.R.color.white)
                    .setProgressBarColor(R.color.colorAccent)
                    .setCropType(CropImageView.CropShape.OVAL)
                    .setCropAspectRatio(1, 1)
                    .setSupportedFileTypes("jpg", "jpeg", "png", "webp", "gif")
                    .enableFlip()
                    .setMaxFileSize(60000)                  // always enter size in bytes.
                    .showSizeLabel()
                    .enableRotate()
                    .hideFileName()
                    .build()
                startActivityForResult(intent, MEDIA_REQUEST_CODE)
            }
            R.id.btnVideoPicker -> {
                val intent = Lassi(this)
                    .with(LassiOption.CAMERA_AND_GALLERY)
                    .setMaxCount(4)
                    .setGridSize(3)
                    .setMinTime(15)
                    .setMaxTime(30)
                    .setMediaType(MediaType.VIDEO)
                    .setStatusBarColor(R.color.colorPrimaryDark)
                    .setToolbarColor(R.color.colorPrimary)
                    .setToolbarResourceColor(android.R.color.white)
                    .setProgressBarColor(R.color.colorAccent)
                    .setPlaceHolder(R.drawable.ic_video_placeholder)
                    .setErrorDrawable(R.drawable.ic_video_placeholder)
                    .showSizeLabel()
                    .hideFileName()
                    .setSupportedFileTypes("mp4", "mkv", "webm", "avi", "flv", "3gp")
                    .build()
                startActivityForResult(intent, MEDIA_REQUEST_CODE)
            }

            R.id.btnAudioPicker -> {
                val intent = Lassi(this)
                    .setMediaType(MediaType.AUDIO)
                    .setMaxCount(4)
                    .setGridSize(2)
                    .setPlaceHolder(R.drawable.ic_audio_placeholder)
                    .setErrorDrawable(R.drawable.ic_audio_placeholder)
                    .setStatusBarColor(R.color.colorPrimaryDark)
                    .setToolbarColor(R.color.colorPrimary)
                    .setToolbarResourceColor(android.R.color.white)
                    .setProgressBarColor(R.color.colorAccent)
                    .showSizeLabel()
                    .enableRotate()
                    .build()
                startActivityForResult(intent, MEDIA_REQUEST_CODE)
            }
            R.id.btnDocPicker -> {
                val intent = Lassi(this)
                    .setMediaType(MediaType.DOC)
                    .setMaxCount(4)
                    .setGridSize(2)
                    .setPlaceHolder(R.drawable.ic_document_placeholder)
                    .setErrorDrawable(R.drawable.ic_document_placeholder)
                    .setStatusBarColor(R.color.colorPrimaryDark)
                    .setToolbarColor(R.color.colorPrimary)
                    .setToolbarResourceColor(android.R.color.white)
                    .setSupportedFileTypes("pdf", "odt", "doc", "docs", "txt", "ppt", "pptx")
                    .setProgressBarColor(R.color.colorAccent)
                    .build()
                startActivityForResult(intent, MEDIA_REQUEST_CODE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MEDIA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val selectedMedia =
                data?.getSerializableExtra(KeyUtils.SELECTED_MEDIA) as ArrayList<MiMedia>
            selectedMediaAdapter.setList(selectedMedia)
        }
    }

    private fun getMimeType(uri: Uri): String? {
        return if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            contentResolver.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(
                uri.toString()
            )
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                fileExtension.toLowerCase()
            )
        }
    }

    private fun onItemClicked(miMedia: MiMedia) {
        val file = File(miMedia.path)
        val fileUri = FileProvider.getUriForFile(
            this,
            applicationContext.packageName + ".fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, getMimeType(fileUri))
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(Intent.createChooser(intent, "Open file"))
    }
}
