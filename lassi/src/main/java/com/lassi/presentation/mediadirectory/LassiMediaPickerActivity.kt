package com.lassi.presentation.mediadirectory

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.lifecycle.ViewModelProviders
import com.lassi.R
import com.lassi.common.utils.ColorUtils.getColor
import com.lassi.common.utils.DrawableUtils.changeIconColor
import com.lassi.common.utils.KeyUtils
import com.lassi.common.utils.ToastUtils
import com.lassi.data.media.MiMedia
import com.lassi.domain.common.SafeObserver
import com.lassi.domain.media.LassiConfig
import com.lassi.presentation.builder.Lassi
import com.lassi.presentation.camera.CameraActivity
import com.lassi.presentation.common.LassiBaseViewModelActivity
import com.lassi.presentation.cropper.CropImage
import com.lassi.presentation.media.SelectedMediaViewModel
import kotlinx.android.synthetic.main.toolbar.*

class LassiMediaPickerActivity : LassiBaseViewModelActivity<SelectedMediaViewModel>() {
    private var menuDone: MenuItem? = null
    private var menuCamera: MenuItem? = null

    override fun getContentResource() = R.layout.activity_media_picker

    override fun buildViewModel(): SelectedMediaViewModel {
        return ViewModelProviders.of(this)[SelectedMediaViewModel::class.java]
    }

    private val miFolderViewModel by lazy {
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
        with(LassiConfig.getConfig()) {
            toolbar.title =
                String.format(
                    getString(R.string.selected_items),
                    selectedMedias.size,
                    maxCount
                )
        }
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setThemeAttributes()
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.ftContainer,
                FolderFragment.newInstance()
            )
            .commitAllowingStateLoss()
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
        /*val intent = Intent().apply {
            putParcelableArrayListExtra(
                KeyUtils.SELECTED_MEDIA,
                viewModel.selectedMediaLiveData.value
            )
        }
        setResult(Activity.RESULT_OK, intent)*/

        Lassi.selectedMediaCallback?.onMediaSelected(viewModel.selectedMediaLiveData.value)
        finish()
    }

    private fun initCamera() {
        if (viewModel.selectedMediaLiveData.value?.size == LassiConfig.getConfig().maxCount) {
            ToastUtils.showToast(this, R.string.already_selected_max_items)
        } else {
            startActivityForResult(
                Intent(this, CameraActivity::class.java),
                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE
            )
        }
    }

    private fun handleSelectedMedia(selectedMedias: ArrayList<MiMedia>) {
        toolbar.title = String.format(
            getString(R.string.selected_items),
            selectedMedias.size,
            LassiConfig.getConfig().maxCount
        )
        menuDone?.isVisible = !selectedMedias.isNullOrEmpty()
    }


    override fun onNewIntent(data: Intent?) {
        super.onNewIntent(data)
        if (data != null) {
            if (data.hasExtra(KeyUtils.SELECTED_MEDIA)) {
                val miMedia = data.getParcelableExtra<MiMedia>(KeyUtils.SELECTED_MEDIA)
                LassiConfig.getConfig().selectedMedias.add(miMedia)
                viewModel.addSelectedMedia(miMedia)
                miFolderViewModel.fetchFolders()
            }
        }
    }
}