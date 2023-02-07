package com.lassi.presentation.videopreview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.MediaController
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.lassi.R
import com.lassi.common.utils.FilePickerUtils
import com.lassi.common.utils.KeyUtils
import com.lassi.common.utils.Logger
import com.lassi.data.common.StartVideoContract
import com.lassi.data.media.MiMedia
import com.lassi.domain.media.LassiConfig
import com.lassi.domain.media.LassiOption
import com.lassi.presentation.common.LassiBaseActivity
import com.lassi.presentation.cropper.CropImage
import com.lassi.presentation.cropper.CropImageContract
import com.lassi.presentation.media.SelectedMediaViewModel
import com.lassi.presentation.mediadirectory.FolderViewModel
import com.lassi.presentation.mediadirectory.FolderViewModelFactory
import com.lassi.presentation.mediadirectory.LassiMediaPickerActivity
import com.lassi.presentation.mediadirectory.SelectedMediaViewModelFactory
import kotlinx.android.synthetic.main.activity_video_preview.*
import java.io.File
import java.util.ArrayList

class VideoPreviewActivity : LassiBaseActivity() {

    private val logTag = VideoPreviewActivity::class.java.simpleName
    private var videoPath: String? = null
    override fun getContentResource() = R.layout.activity_video_preview

    private val viewModel by lazy {
        ViewModelProvider(
            this, SelectedMediaViewModelFactory(this)
        )[SelectedMediaViewModel::class.java]
    }

    private val folderViewModel by lazy {
        ViewModelProvider(
            this, FolderViewModelFactory(this)
        )[FolderViewModel::class.java]
    }

/*    private val startVideoContract = registerForActivityResult(StartVideoContract()) { data ->
        with(data) {
            if (this!= null) {
                if (this.hasExtra(KeyUtils.SELECTED_MEDIA)) {
                    val selectedMedia = this.getSerializableExtra(KeyUtils.SELECTED_MEDIA) as ArrayList<MiMedia>
                    Logger.d("LASSI", "!@# registerForActivityResult Media size 390 => ${selectedMedia.size}")
                    LassiConfig.getConfig().selectedMedias.addAll(selectedMedia)
                    viewModel.addAllSelectedMedia(selectedMedia)
                    folderViewModel.checkInsert()
                    if (LassiConfig.getConfig().lassiOption == LassiOption.CAMERA_AND_GALLERY || LassiConfig.getConfig().lassiOption == LassiOption.GALLERY) {
                        supportFragmentManager.popBackStack()
                    }
                } else if (this.hasExtra(KeyUtils.MEDIA_PREVIEW)) {
                    val selectedMedia = this.getParcelableExtra<MiMedia>(KeyUtils.MEDIA_PREVIEW)
                    Logger.d("LASSI", "!@# registerForActivityResult Media path 85 => ${selectedMedia?.path}")
                    if (LassiConfig.isSingleMediaSelection()) {
                        setResultOk(arrayListOf(selectedMedia!!))
                    } else {
                        LassiConfig.getConfig().selectedMedias.add(selectedMedia!!)
                        viewModel.addSelectedMedia(selectedMedia)
                        folderViewModel.checkInsert()
                        if (LassiConfig.getConfig().lassiOption == LassiOption.CAMERA_AND_GALLERY || LassiConfig.getConfig().lassiOption == LassiOption.GALLERY) {
                            supportFragmentManager.popBackStack()
                        }
                    }
                }
            }
        }
    }

    private fun setResultOk(selectedMedia: ArrayList<MiMedia>?) {
        val intent = Intent().apply {
            putExtra(KeyUtils.SELECTED_MEDIA, selectedMedia)
        }
        Logger.d(
            "LASSI",
            "!@# VideoPreviewActivity selectedMedia size 96 => ${selectedMedia?.size}"
        )
        setResult(Activity.RESULT_OK, intent)
        finish()
    }*/

/*    init {
        startVideoPreview()
    }
    open fun startVideoPreview(activity: FragmentActivity? = null, videoPath: String = "") {
        Log.d("TAG", "!@# startVideoPreview: videoPath => $videoPath")
        activity?.let {
            val intent = Intent(activity, VideoPreviewActivity::class.java).apply {
                putExtra(KeyUtils.VIDEO_PATH, videoPath)
            }
            if (videoPath  != "") {
//            activity?.startActivityForResult(intent, 203)
                startVideoContract.launch(videoPath)
                StartVideoContract().parseResult(203, intent)
            }
        }
    }*/

    companion object {
        fun startVideoPreview(activity: FragmentActivity?, videoPath: String) {
            Log.d("TAG", "!@# startVideoPreview: videoPath => $videoPath")
            val intent = Intent(activity, VideoPreviewActivity::class.java).apply {
                putExtra(KeyUtils.VIDEO_PATH, videoPath)
            }
//            activity?.startActivityForResult(intent, 203)

            StartVideoContract().parseResult(203, intent)
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
        Log.d("TAG", "!@# initViews: videoPath => $videoPath")
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
