package com.lassi.presentation.mediadirectory

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.lifecycle.ViewModelProviders
import com.lassi.R
import com.lassi.common.utils.ColorUtils.getColor
import com.lassi.common.utils.CropUtils
import com.lassi.common.utils.DrawableUtils.changeIconColor
import com.lassi.common.utils.KeyUtils
import com.lassi.common.utils.ToastUtils
import com.lassi.data.media.MiMedia
import com.lassi.domain.common.SafeObserver
import com.lassi.domain.media.LassiConfig
import com.lassi.domain.media.LassiOption
import com.lassi.domain.media.MediaType
import com.lassi.presentation.camera.CameraFragment
import com.lassi.presentation.common.LassiBaseViewModelActivity
import com.lassi.presentation.cropper.CropImage
import com.lassi.presentation.media.SelectedMediaViewModel
import com.lassi.presentation.videopreview.VideoPreviewActivity
import kotlinx.android.synthetic.main.activity_media_picker.*
import java.io.File

class LassiMediaPickerActivity : LassiBaseViewModelActivity<SelectedMediaViewModel>() {
    private var menuDone: MenuItem? = null
    private var menuCamera: MenuItem? = null

    override fun getContentResource() = R.layout.activity_media_picker

    override fun buildViewModel(): SelectedMediaViewModel {
        return ViewModelProviders.of(this)[SelectedMediaViewModel::class.java]
    }

    private val folderViewModel by lazy {
        ViewModelProviders.of(
            this, FolderViewModelFactory(this)
        )[FolderViewModel::class.java]
    }

    override fun initLiveDataObservers() {
        super.initLiveDataObservers()
        viewModel.selectedMediaLiveData.observe(this, SafeObserver(this::handleSelectedMedia))
    }

    override fun initViews() {
        super.initViews()
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
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.ftContainer,
                    FolderFragment.newInstance()
                )
                .commitAllowingStateLoss()
        }
    }

    private fun setThemeAttributes() {
        with(LassiConfig.getConfig()) {
            toolbar.background =
                ColorDrawable(getColor(this@LassiMediaPickerActivity, toolbarColor))
            toolbar.setTitleTextColor(getColor(this@LassiMediaPickerActivity, toolbarResourceColor))

            supportActionBar?.setHomeAsUpIndicator(
                changeIconColor(
                    this@LassiMediaPickerActivity,
                    R.drawable.ic_back_white,
                    toolbarResourceColor
                )
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = getColor(this@LassiMediaPickerActivity, statusBarColor)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.media_picker_menu, menu)
        menuDone = menu?.findItem(R.id.menuDone)
        menuCamera = menu?.findItem(R.id.menuCamera)
        menuDone?.isVisible = false

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

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menuDone?.isVisible = !viewModel.selectedMediaLiveData.value.isNullOrEmpty()
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menuCamera -> initCamera()
            R.id.menuDone -> setSelectedMediaResult()
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setSelectedMediaResult() {
        // Allow crop for single image
        if (LassiConfig.isSingleMediaSelection()) {
            if (LassiConfig.getConfig().mediaType == MediaType.IMAGE) {
                val uri = Uri.fromFile(File(viewModel.selectedMediaLiveData.value!![0].path!!))
                CropUtils.beginCrop(this, uri)
            } else {
                VideoPreviewActivity.startVideoPreview(
                    this,
                    viewModel.selectedMediaLiveData.value!![0].path!!
                )
            }
        } else {
            setResultOk(viewModel.selectedMediaLiveData.value)
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
        menuDone?.isVisible = !selectedMedias.isNullOrEmpty()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE
            && resultCode == Activity.RESULT_OK
        ) {
            if (data != null) {
                if (data.hasExtra(KeyUtils.SELECTED_MEDIA)) {
                    val selectedMedia =
                        data.getSerializableExtra(KeyUtils.SELECTED_MEDIA) as ArrayList<MiMedia>
                    LassiConfig.getConfig().selectedMedias.addAll(selectedMedia)
                    viewModel.addAllSelectedMedia(selectedMedia)
                    folderViewModel.fetchFolders()
                    if (LassiConfig.getConfig().lassiOption == LassiOption.CAMERA_AND_GALLERY) {
                        supportFragmentManager.popBackStack()
                    }
                } else if (data.hasExtra(KeyUtils.MEDIA_PREVIEW)) {
                    val selectedMedia = data.getParcelableExtra<MiMedia>(KeyUtils.MEDIA_PREVIEW)
                    if (LassiConfig.isSingleMediaSelection()) {
                        setResultOk(arrayListOf(selectedMedia))
                    } else {
                        LassiConfig.getConfig().selectedMedias.add(selectedMedia)
                        viewModel.addSelectedMedia(selectedMedia)
                        folderViewModel.fetchFolders()
                        if (LassiConfig.getConfig().lassiOption == LassiOption.CAMERA_AND_GALLERY) {
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
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}