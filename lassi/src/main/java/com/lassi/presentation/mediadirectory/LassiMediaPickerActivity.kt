package com.lassi.presentation.mediadirectory

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.webkit.MimeTypeMap
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.lassi.R
import com.lassi.common.extenstions.getFileName
import com.lassi.common.extenstions.getFileSize
import com.lassi.common.extenstions.show
import com.lassi.common.utils.DrawableUtils.changeIconColor
import com.lassi.common.utils.FilePickerUtils.getFilePathFromUri
import com.lassi.common.utils.KeyUtils
import com.lassi.common.utils.Logger
import com.lassi.common.utils.ToastUtils
import com.lassi.common.utils.UriHelper.getCompressFormatForUri
import com.lassi.data.media.MiMedia
import com.lassi.databinding.ActivityMediaPickerBinding
import com.lassi.domain.common.SafeObserver
import com.lassi.domain.media.LassiConfig
import com.lassi.domain.media.LassiOption
import com.lassi.domain.media.MediaType
import com.lassi.domain.media.MultiLangConfig
import com.lassi.presentation.camera.CameraFragment
import com.lassi.presentation.common.LassiBaseViewModelActivity
import com.lassi.presentation.cropper.BitmapUtils.decodeUriToBitmap
import com.lassi.presentation.cropper.BitmapUtils.writeBitmapToUri
import com.lassi.presentation.cropper.CropImageContract
import com.lassi.presentation.cropper.CropImageContractOptions
import com.lassi.presentation.cropper.CropImageOptions
import com.lassi.presentation.cropper.CropImageView
import com.lassi.presentation.docs.DocsFragment
import com.lassi.presentation.media.SelectedMediaViewModel
import com.livefront.bridge.Bridge
import com.livefront.bridge.SavedStateHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class LassiMediaPickerActivity :
    LassiBaseViewModelActivity<SelectedMediaViewModel, ActivityMediaPickerBinding>() {
    private var menuDone: MenuItem? = null
    private var menuCamera: MenuItem? = null
    private var menuSort: MenuItem? = null
    private var croppedMediaList: ArrayList<MiMedia> = ArrayList()
    private val config = LassiConfig.getConfig()

    private val cropImage = registerForActivityResult(CropImageContract()) { miMedia ->
        miMedia?.let {
            croppedMediaList.add(miMedia)
            if (croppedMediaList.size == config.selectedMedias.size) {
                setResultOk(croppedMediaList)
            } else {
                val nextMediaIndex = croppedMediaList.size
                val uri = Uri.fromFile(config.selectedMedias[nextMediaIndex].path?.let { File(it) })
                uri?.let { croppingOptions(uri = uri) }
            }
        }
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uri ->
            uri?.let { uris ->
                if (config.mediaType == MediaType.FILE_TYPE_WITH_SYSTEM_VIEW && uris.size > config.maxCount) {
                    ToastUtils.showToast(
                        this@LassiMediaPickerActivity,
                        config.customLimitExceedingErrorMessage
                    )
                    finish()
                } else {
                    binding.progressBar.indeterminateDrawable.colorFilter =
                        BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                            config.progressBarColor, BlendModeCompat.SRC_ATOP
                        )
                    binding.progressBar.show()

                    lifecycleScope.launch {
                        val resultList: ArrayList<MiMedia> = withContext(Dispatchers.IO) {
                            uris.map { uri ->
                                MiMedia().apply {
                                    name = getFileName(uri)
                                    doesUri = false
                                    fileSize = getFileSize(uri)
                                    path =
                                        getFilePathFromUri(this@LassiMediaPickerActivity, uri, true)
                                    Log.d("TAG", "!@# SLOWER MEDIA ITEM: $name")
                                }
                            } as ArrayList<MiMedia>
                        }

                        withContext(Dispatchers.Main) {
                            setResultOk(resultList)
                        }
                    }
                }
            }
        }

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

    override fun inflateLayout(layoutInflater: LayoutInflater): ActivityMediaPickerBinding {
        return ActivityMediaPickerBinding.inflate(layoutInflater)
    }

    override fun initViews() {
        super.initViews()
        config.selectedMedias.clear()
        Bridge.initialize(applicationContext, object : SavedStateHandler {
            override fun saveInstanceState(target: Any, state: Bundle) {
            }

            override fun restoreInstanceState(target: Any, state: Bundle?) {
            }
        })
        setToolbarTitle(config.selectedMedias)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setThemeAttributes()
        initiateFragment()

        // Gives the text of the status bar dark color
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = true

        // this thing ensures that the padding removed for the edge-to-edge support is not overridden again
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // giving the padding according to the edge-to-edge support.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_layout_media_picker)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setBackgroundColor(config.statusBarColor)
            view.setPadding(
                view.paddingLeft,
                systemBars.top,
                view.paddingRight,
                systemBars.bottom
            )
            insets
        }
    }

    private fun setToolbarTitle(selectedMedias: ArrayList<MiMedia>) {
        val maxCount = config.maxCount
        if (maxCount > 1) {
            binding.toolbar.title = String.format(
                getString(R.string.selected_items),
                selectedMedias.size.toString(),
                maxCount.toString()
            )
        } else {
            binding.toolbar.title = ""
        }
    }

    private fun initiateFragment() {
        if (config.lassiOption == LassiOption.CAMERA) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.ftContainer,
                    CameraFragment()
                )
                .commitAllowingStateLoss()
        } else {
            config.mediaType.let { mediaType ->
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
        config.supportedFileType.forEach { mimeType ->
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
        with(config) {
            binding.toolbar.background =
                ColorDrawable(toolbarColor)
            binding.toolbar.setTitleTextColor(toolbarResourceColor)
            supportActionBar?.setHomeAsUpIndicator(
                changeIconColor(
                    this@LassiMediaPickerActivity,
                    R.drawable.ic_back_white,
                    toolbarResourceColor
                )
            )
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = statusBarColor
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.media_picker_menu, menu)
        menuDone = menu.findItem(R.id.menuDone)
        menuCamera = menu.findItem(R.id.menuCamera)
        menuSort = menu.findItem(R.id.menuSort)
        menuDone?.isVisible = false
        menuCamera?.isVisible = false
        menuSort?.isVisible = true
        with(config) {
            menuDone?.icon = changeIconColor(
                this@LassiMediaPickerActivity,
                R.drawable.ic_done_white, toolbarResourceColor
            )
            menuCamera?.icon = changeIconColor(
                this@LassiMediaPickerActivity,
                R.drawable.ic_camera_white, toolbarResourceColor
            )
            menuSort?.icon = changeIconColor(
                this@LassiMediaPickerActivity,
                R.drawable.ic_sorting_foreground, toolbarResourceColor
            )
            return super.onCreateOptionsMenu(menu)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menuCamera?.isVisible =
            (config.lassiOption == LassiOption.CAMERA ||
                    config.lassiOption == LassiOption.CAMERA_AND_GALLERY)
        menuDone?.isVisible = !viewModel.selectedMediaLiveData.value.isNullOrEmpty()
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuCamera -> initCamera()
            R.id.menuDone -> setSelectedMediaResult()
            android.R.id.home -> onBackPressedDispatcher.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setSelectedMediaResult() {
        // Allow crop for single image
        config.apply {
            when (mediaType) {
                MediaType.IMAGE, MediaType.VIDEO, MediaType.AUDIO, MediaType.DOC -> {
                    if (mediaType == MediaType.IMAGE && isCrop) {
                        val uri = Uri.fromFile(selectedMedias.first().path?.let { File(it) })
                        uri?.let {
                            croppingOptions(uri = uri)
                        }
                    } else {
                        viewModel.selectedMediaLiveData.value?.let {
                            if (MediaType.IMAGE == mediaType && compressionRatio != 0) {
                                compressMedia(it)
                            } else {
                                setResultOk(it)
                            }
                        }
                    }
                }

                else -> {
                }
            }
        }
    }

    fun compressMedia(mediaPaths: ArrayList<MiMedia>) {
        mediaPaths.forEachIndexed { index, miMedia ->
            miMedia.path?.let { path ->
                val uri = Uri.fromFile(File(path))
                val compressFormat = getCompressFormatForUri(uri, this)
                val newUri = writeBitmapToUri(
                    this,
                    decodeUriToBitmap(this, uri),
                    compressQuality = config.compressionRatio,
                    customOutputUri = null,
                    compressFormat = compressFormat
                )
                mediaPaths[index] = miMedia.copy(path = newUri.path)
            }
        }
        setResultOk(mediaPaths)
    }


    private fun initCamera() {
        if (viewModel.selectedMediaLiveData.value?.size == config.maxCount) {
            ToastUtils.showToast(this, MultiLangConfig.getConfig().alreadySelectedMaxItems)
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
        config.selectedMedias.clear()
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun croppingOptions(
        uri: Uri? = null, includeCamera: Boolean? = false, includeGallery: Boolean? = false,
    ) {
        // Start picker to get image for cropping and then use the image in cropping activity.
        cropImage.launch(includeCamera?.let {
            includeGallery?.let { includeGallery ->
                config.cropAspectRatio?.x?.let { x ->
                    config.cropAspectRatio?.y?.let { y ->
                        CropImageOptions(
                            imageSourceIncludeCamera = includeCamera,
                            imageSourceIncludeGallery = includeGallery,
                            cropShape = config.cropType,
                            showCropOverlay = true,
                            guidelines = CropImageView.Guidelines.ON,
                            multiTouchEnabled = false,
                            aspectRatioX = x,
                            aspectRatioY = y,
                            fixAspectRatio = config.enableActualCircleCrop,
                            outputCompressQuality = LassiConfig.getConfig().compressionRatio
                        )
                    }
                }
            }
        }?.let {
            CropImageContractOptions(
                uri = uri,
                cropImageOptions = it,
            )
        })
    }
}
