package com.lassi.presentation.videopreview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.MediaController
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.lassi.R
import com.lassi.common.utils.FilePickerUtils
import com.lassi.common.utils.KeyUtils
import com.lassi.data.media.MiMedia
import com.lassi.databinding.ActivityVideoPreviewBinding
import com.lassi.domain.media.LassiConfig
import com.lassi.presentation.common.LassiBaseActivity
import java.io.File

class VideoPreviewActivity : LassiBaseActivity<ActivityVideoPreviewBinding>() {
    private var videoPath: String? = null

    override fun inflateLayout(layoutInflater: LayoutInflater): ActivityVideoPreviewBinding {
        return ActivityVideoPreviewBinding.inflate(layoutInflater)
    }

    override fun initViews() {
        super.initViews()
        binding.toolbar.title = ""
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setThemeAttributes()
        playVideo()
        if (intent == null) {
            finish()
            return
        }
        videoPath = intent.getStringExtra(KeyUtils.VIDEO_PATH)
        val controller = MediaController(this)
        binding.videoView.apply {
            controller.setAnchorView(this)
            controller.setMediaPlayer(this)
            setMediaController(controller)
            setVideoURI(Uri.fromFile(File(videoPath)))
        }
    }

    private fun setThemeAttributes() {
        binding.toolbar.title = ""
        with(LassiConfig.getConfig()) {
            binding.toolbar.background =
                ColorDrawable(toolbarColor)
            binding.toolbar.setTitleTextColor(toolbarResourceColor)
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

    private fun playVideo() {
        if (binding.videoView.isPlaying) return
        binding.videoView.start()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.video_preview_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
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
