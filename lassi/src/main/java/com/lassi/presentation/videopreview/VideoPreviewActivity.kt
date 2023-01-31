package com.lassi.presentation.videopreview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.MediaController
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.fragment.app.FragmentActivity
import com.lassi.R
import com.lassi.common.utils.FilePickerUtils
import com.lassi.common.utils.KeyUtils
import com.lassi.common.utils.Logger
import com.lassi.data.media.MiMedia
import com.lassi.domain.media.LassiConfig
import com.lassi.presentation.common.LassiBaseActivity
import com.lassi.presentation.cropper.CropImage
import com.lassi.presentation.cropper.CropImageContract
import kotlinx.android.synthetic.main.activity_video_preview.*
import java.io.File

class VideoPreviewActivity : LassiBaseActivity() {

    private val logTag = VideoPreviewActivity::class.java.simpleName
    private var videoPath: String? = null
    override fun getContentResource() = R.layout.activity_video_preview

    companion object {
        fun startVideoPreview(activity: FragmentActivity?, videoPath: String) {
            val intent = Intent(activity, VideoPreviewActivity::class.java).apply {
                putExtra(KeyUtils.VIDEO_PATH, videoPath)
            }
        }
    }

    override fun initViews() {
        super.initViews()
        toolbar.title = ""
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setThemeAttributes()
        playVideo()
        if (intent == null) {
            finish()
            return
        }
        videoPath = intent.getStringExtra(KeyUtils.VIDEO_PATH)
        val controller = MediaController(this)
        controller.setAnchorView(videoView)
        controller.setMediaPlayer(videoView)
        videoView.setMediaController(controller)
        videoView.setVideoURI(Uri.fromFile(File(videoPath)))
    }

    private fun setThemeAttributes() {
        toolbar.title = ""
        with(LassiConfig.getConfig()) {
            toolbar.background =
                ColorDrawable(toolbarColor)
            toolbar.setTitleTextColor(toolbarResourceColor)
            val upArrow =
                ContextCompat.getDrawable(this@VideoPreviewActivity, R.drawable.ic_back_white)
            upArrow?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                toolbarResourceColor,
                BlendModeCompat.SRC_ATOP
            )
            supportActionBar?.setHomeAsUpIndicator(upArrow)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = statusBarColor
            }
        }
    }

    private val cropImageListener = registerForActivityResult(CropImageContract()) { result ->
        when {
            result.isSuccessful -> {
                Logger.d("AIC-Sample", "Original bitmap: ${result.originalBitmap}")
                Logger.d("AIC-Sample", "Original uri: ${result.originalUri}")
                Logger.d("AIC-Sample", "Output bitmap: ${result.bitmap}")
                Logger.d("AIC-Sample", "Output uri: ${result.getUriFilePath(this)}")
                handleCropImageResult(result.uriContent.toString())
            }
            result is CropImage.CancelledResult -> showErrorMessage("cropping image was cancelled by the user")
            else -> showErrorMessage("cropping image failed")
        }
    }

    private fun showErrorMessage(message: String) {
        Logger.d("AIC-Sample", "Camera error: $message")
        Toast.makeText(this, "!@# Crop failed: $message", Toast.LENGTH_SHORT).show()
    }

    private fun handleCropImageResult(uri: String) {
        Toast.makeText(this, "!@# handleCropImageResult: URI => : $uri", Toast.LENGTH_SHORT).show()
    }

    private fun playVideo() {
        if (videoView.isPlaying) return
        videoView.start()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.video_preview_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            R.id.menuDone -> {
                videoPath?.let {
                    FilePickerUtils.notifyGalleryUpdateNewFile(
                        this,
                        it,
                        "video/mp4",
                        this::onFileScanComplete
                    )
                }
            }
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("Range")
    private fun onFileScanComplete(uri: Uri?, path: String?) {
        uri?.let { returnUri ->
            contentResolver.query(returnUri, null, null, null, null)
        }?.use { cursor ->
            cursor.moveToFirst()
            try {
                val id = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media._ID))
                val name =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME))
                val path =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
                val duration =
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DURATION))
                val miMedia = MiMedia(id, name, path, duration)

                val intent = Intent().apply {
                    putExtra(KeyUtils.MEDIA_PREVIEW, miMedia)
                }
                setResult(Activity.RESULT_OK, intent)
                finish()

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor.close()
            }
        } ?: let {
            path?.let {
                val miMedia = MiMedia(path = it)
                val intent = Intent().apply {
                    putExtra(KeyUtils.MEDIA_PREVIEW, miMedia)
                }
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }
}
