package com.lassi.presentation.mediadirectory

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.webkit.MimeTypeMap
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.lassi.R
import com.lassi.common.extenstions.getFileName
import com.lassi.common.extenstions.getFileSize
import com.lassi.common.utils.DrawableUtils.changeIconColor
import com.lassi.common.utils.FilePickerUtils.getFilePathFromUri
import com.lassi.common.utils.KeyUtils
import com.lassi.common.utils.Logger
import com.lassi.common.utils.ToastUtils
import com.lassi.data.media.MiMedia
import com.lassi.domain.common.SafeObserver
import com.lassi.domain.media.LassiConfig
import com.lassi.domain.media.LassiOption
import com.lassi.domain.media.MediaType
import com.lassi.presentation.camera.CameraFragment
import com.lassi.presentation.common.LassiBaseViewModelActivity
import com.lassi.presentation.docs.DocsFragment
import com.lassi.presentation.media.SelectedMediaViewModel
import com.livefront.bridge.Bridge
import com.livefront.bridge.SavedStateHandler
import io.reactivex.annotations.NonNull
import io.reactivex.annotations.Nullable
import kotlinx.android.synthetic.main.activity_media_picker.*
import java.io.File

class LassiMediaPickerActivity : LassiBaseViewModelActivity<SelectedMediaViewModel>() {
    private var menuDone: MenuItem? = null
    private var menuCamera: MenuItem? = null
    private val getContent =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uri ->
            uri?.let { uris ->
                val list = ArrayList<MiMedia>()
                uris.map { uri ->
                    val miMedia = MiMedia()
                    miMedia.name = getFileName(uri)
                    miMedia.doesUri = false
                    miMedia.fileSize = getFileSize(uri)
                    miMedia.path = getFilePathFromUri(this, uri, true)
                    list.add(miMedia)
                }
                setResultOk(list)
            }
        }

    override fun getContentResource() = R.layout.activity_media_picker

    override fun buildViewModel(): SelectedMediaViewModel {
        return ViewModelProvider(
            this,
            SelectedMediaViewModelFactory(this)
        )[SelectedMediaViewModel::class.java]
    }

    override fun initLiveDataObservers() {
        super.initLiveDataObservers()
        viewModel.selectedMediaLiveData.observe(this, SafeObserver(this::handleSelectedMedia))
    }

    override fun initViews() {
        super.initViews()
        Bridge.initialize(applicationContext, object : SavedStateHandler {
            override fun saveInstanceState(@NonNull target: Any, @NonNull state: Bundle) {
            }

            override fun restoreInstanceState(@NonNull target: Any, @Nullable state: Bundle?) {
            }
        })
        setToolbarTitle(LassiConfig.getConfig().selectedMedias)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setThemeAttributes()
        initiateFragment()
    }

    private fun setToolbarTitle(selectedMedias: ArrayList<MiMedia>) {
        val maxCount = LassiConfig.getConfig().maxCount
        if (maxCount > 1) {
            toolbar.title = String.format(
                getString(R.string.selected_items),
                selectedMedias.size,
                maxCount
            )
        } else {
            toolbar.title = ""
        }
    }

    private fun initiateFragment() {
        if (LassiConfig.getConfig().lassiOption == LassiOption.CAMERA) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.ftContainer,
                    CameraFragment()
                )
                .commitAllowingStateLoss()
        } else {
            LassiConfig.getConfig().mediaType.let { mediaType ->
                when (mediaType) {
                    MediaType.DOC -> {
                        supportFragmentManager.beginTransaction()
                            .replace(
                                R.id.ftContainer,
                                DocsFragment()
                            )
                            .commitAllowingStateLoss()
                    }
                    MediaType.FILE_TYPE_WITH_SYSTEM_VIEW -> {
                        browseFile()
                    }
                    else -> {
                        supportFragmentManager.beginTransaction()
                            .replace(
                                R.id.ftContainer,
                                FolderFragment.newInstance()
                            )
                            .commitAllowingStateLoss()
                    }
                }
            }
        }
    }

    private fun browseFile() {
        val mimeTypesList = ArrayList<String>()
        LassiConfig.getConfig().supportedFileType.forEach { mimeType ->
            MimeTypeMap
                .getSingleton()
                .getMimeTypeFromExtension(mimeType)?.let {
                    mimeTypesList.add(it)
                }
        }
        var mMimeTypeArray = arrayOf<String>()
        mMimeTypeArray = mimeTypesList.toArray(mMimeTypeArray)
        getContent.launch(mMimeTypeArray)
    }

    private fun setThemeAttributes() {
        with(LassiConfig.getConfig()) {
            toolbar.background =
                ColorDrawable(toolbarColor)
            toolbar.setTitleTextColor(toolbarResourceColor)
            supportActionBar?.setHomeAsUpIndicator(
                changeIconColor(
                    this@LassiMediaPickerActivity,
                    R.drawable.ic_back_white,
                    toolbarResourceColor
                )
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = statusBarColor
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.media_picker_menu, menu)
        menuDone = menu.findItem(R.id.menuDone)
        menuCamera = menu.findItem(R.id.menuCamera)
        menuDone?.isVisible = false
        menuCamera?.isVisible = false

        menuDone?.icon = changeIconColor(
            this@LassiMediaPickerActivity,
            R.drawable.ic_done_white,
            LassiConfig.getConfig().toolbarResourceColor
        )
        menuCamera?.icon = changeIconColor(
            this@LassiMediaPickerActivity,
            R.drawable.ic_camera_white,
            LassiConfig.getConfig().toolbarResourceColor
        )
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menuCamera?.isVisible =
            (LassiConfig.getConfig().lassiOption == LassiOption.CAMERA || LassiConfig.getConfig().lassiOption == LassiOption.CAMERA_AND_GALLERY)
        menuDone?.isVisible = !viewModel.selectedMediaLiveData.value.isNullOrEmpty()
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuCamera -> initCamera()
            R.id.menuDone -> setSelectedMediaResult()
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setSelectedMediaResult() {
        // Allow crop for single image
        when (LassiConfig.getConfig().mediaType) {
            MediaType.IMAGE, MediaType.VIDEO, MediaType.AUDIO, MediaType.DOC -> {
                setResultOk(viewModel.selectedMediaLiveData.value)
            }
            else -> {
            }
        }
    }

    private fun initCamera() {
        if (viewModel.selectedMediaLiveData.value?.size == LassiConfig.getConfig().maxCount) {
            ToastUtils.showToast(this, R.string.already_selected_max_items)
        } else {
            supportFragmentManager.beginTransaction()
                .add(R.id.ftContainer, CameraFragment())
                .addToBackStack(CameraFragment::class.java.simpleName)
                .commitAllowingStateLoss()
        }
    }

    private fun handleSelectedMedia(selectedMedias: ArrayList<MiMedia>) {
        setToolbarTitle(selectedMedias)
        menuDone?.isVisible = selectedMedias.isNotEmpty()
    }

    private fun setResultOk(selectedMedia: ArrayList<MiMedia>?) {
        val intent = Intent().apply {
            putExtra(KeyUtils.SELECTED_MEDIA, selectedMedia)
        }
        Logger.d(
            "LASSI",
            "!@# LassiMediaPickerActivity selectedMedia size 417 => ${selectedMedia?.size}"
        )
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    companion object {
        const val DATE_FORMAT = "yyyyMMdd_HHmmss"
        const val FILE_NAMING_PREFIX = "JPEG_"
        const val FILE_NAMING_SUFFIX = "_"
        const val FILE_FORMAT = ".jpg"
    }
}
