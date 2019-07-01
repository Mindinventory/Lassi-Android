package com.lassi.presentation.videopreview

import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.MediaController
import androidx.core.content.ContextCompat
import com.lassi.R
import com.lassi.common.utils.ColorUtils
import com.lassi.common.utils.FilePickerUtils
import com.lassi.common.utils.KeyUtils
import com.lassi.common.utils.Logger
import com.lassi.data.media.MiMedia
import com.lassi.domain.media.LassiConfig
import com.lassi.domain.media.LassiOption
import com.lassi.presentation.cameraview.controls.VideoResult
import com.lassi.presentation.common.LassiBaseActivity
import com.lassi.presentation.mediadirectory.LassiMediaPickerActivity
import kotlinx.android.synthetic.main.activity_video_preview.*
import kotlinx.android.synthetic.main.toolbar.*

class VideoPreviewActivity : LassiBaseActivity() {

    private val logTag = VideoPreviewActivity::class.java.simpleName

    override fun getContentResource() = R.layout.activity_video_preview

    override fun initViews() {
        super.initViews()
        toolbar.title = ""
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setThemeAttributes()

        playVideo()

        if (videoResult == null) {
            finish()
            return
        }

        val controller = MediaController(this)
        controller.setAnchorView(videoView)
        controller.setMediaPlayer(videoView)
        videoView.setMediaController(controller)
        videoView.setVideoURI(Uri.fromFile(videoResult?.file))
    }

    private fun setThemeAttributes() {
        toolbar.title = ""
        with(LassiConfig.getConfig()) {
            toolbar.background =
                ColorDrawable(ColorUtils.getColor(this@VideoPreviewActivity, toolbarColor))
            toolbar.setTitleTextColor(
                ColorUtils.getColor(
                    this@VideoPreviewActivity,
                    toolbarResourceColor
                )
            )
            val upArrow =
                ContextCompat.getDrawable(this@VideoPreviewActivity, R.drawable.ic_back_white)
            upArrow?.setColorFilter(
                ColorUtils.getColor(this@VideoPreviewActivity, toolbarResourceColor),
                PorterDuff.Mode.SRC_ATOP
            )
            supportActionBar?.setHomeAsUpIndicator(upArrow)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor =
                    ColorUtils.getColor(this@VideoPreviewActivity, statusBarColor)
            }
        }
    }

    private fun playVideo() {
        if (videoView.isPlaying) return
        videoView.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isChangingConfigurations) {
            setVideoResult(null)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.video_preview_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menuDone -> {
                videoResult?.file?.path?.let {
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

    private fun onFileScanComplete(uri: Uri) {
        uri.let { returnUri ->
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

                if (LassiConfig.getConfig().lassiOption == LassiOption.CAMERA) {
                    val selectedMedia = ArrayList<MiMedia>().apply {
                        add(miMedia)
                    }
                    val intent = Intent().apply {
                        putExtra(KeyUtils.SELECTED_MEDIA, selectedMedia)
                    }
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                } else {
                    val intent = Intent(this, LassiMediaPickerActivity::class.java).apply {
                        putExtra(KeyUtils.SELECTED_MEDIA, miMedia)
                    }
                    startActivity(intent)
                }
            } catch (e: Exception) {
                Logger.e(logTag, "onNewIntent $e")
            } finally {
                cursor.close()
            }
        }
    }

    companion object {

        private var videoResult: VideoResult? = null

        fun setVideoResult(result: VideoResult?) {
            videoResult = result
        }
    }
}
