package com.lassi.presentation.cropper

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.net.toUri
import com.lassi.R
import com.lassi.common.utils.FilePickerUtils
import com.lassi.common.utils.KeyUtils
import com.lassi.common.utils.Logger
import com.lassi.data.media.MiMedia
import com.lassi.databinding.CropImageActivityBinding
import com.lassi.presentation.cropper.utils.getUriForFile
import java.io.File

open class CropImageActivity : AppCompatActivity(), CropImageView.OnSetImageUriCompleteListener,
    CropImageView.OnCropImageCompleteListener {
    private val logTag = CropImageActivity::class.java.simpleName
    /**
     * The crop image view library widget used in the activity
     */

    /** Persist URI image to crop URI if specific permissions are required. */
    private var cropImageUri: Uri? = null

    /** The options that were set for the crop image*/
    private lateinit var cropImageOptions: CropImageOptions

    /** The crop image view library widget used in the activity. */
    private var cropImageView: CropImageView? = null
    private lateinit var binding: CropImageActivityBinding
    private var latestTmpUri: Uri? = null
    private val pickImageGallery =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            onPickImageResult(uri)
        }

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            onPickImageResult(latestTmpUri)
        } else {
            onPickImageResult(null)
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CropImageActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCropImageView(binding.cropImageView)
        val bundle = intent.getBundleExtra(CropImage.CROP_IMAGE_EXTRA_BUNDLE)
        cropImageUri = bundle?.parcelable(CropImage.CROP_IMAGE_EXTRA_SOURCE)
        cropImageOptions =
            bundle?.parcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS) ?: CropImageOptions()

        if (savedInstanceState == null) {
            if (cropImageUri == null || cropImageUri == Uri.EMPTY) {
                when {
                    cropImageOptions.showIntentChooser -> showIntentChooser()
                    cropImageOptions.imageSourceIncludeGallery &&
                            cropImageOptions.imageSourceIncludeCamera ->
                        showImageSourceDialog(::openSource)
                    cropImageOptions.imageSourceIncludeGallery ->
                        pickImageGallery.launch("image/*")
                    cropImageOptions.imageSourceIncludeCamera ->
                        openCamera()
                    else -> finish()
                }
            } else {
                cropImageView?.setImageUriAsync(cropImageUri)
            }
        } else {
            latestTmpUri = savedInstanceState.getString(BUNDLE_KEY_TMP_URI)?.toUri()
        }

        setCustomizations()

        onBackPressedDispatcher.addCallback {
            setResultCancel()
        }
    }


    private fun setCustomizations() {
        cropImageOptions.activityBackgroundColor.let { activityBackgroundColor ->
            binding.root.setBackgroundColor(activityBackgroundColor)
        }
    }

    private fun showIntentChooser() {
        val ciIntentChooser = CropImageIntentChooser(
            activity = this,
            callback = object : CropImageIntentChooser.ResultCallback {
                override fun onSuccess(uri: Uri?) {
                    onPickImageResult(uri)
                }

                override fun onCancelled() {
                    setResultCancel()
                }
            },
        )
        cropImageOptions.let { options ->
            options.intentChooserTitle
                ?.takeIf { title ->
                    title.isNotBlank()
                }
                ?.let { icTitle ->
                    ciIntentChooser.setIntentChooserTitle(icTitle)
                }
            options.intentChooserPriorityList
                ?.takeIf { appPriorityList -> appPriorityList.isNotEmpty() }
                ?.let { appsList ->
                    ciIntentChooser.setupPriorityAppsList(appsList)
                }
            val cameraUri: Uri? = if (options.imageSourceIncludeCamera) getTmpFileUri() else null
            ciIntentChooser.showChooserIntent(
                includeCamera = options.imageSourceIncludeCamera,
                includeGallery = options.imageSourceIncludeGallery,
                cameraImgUri = cameraUri,
            )
        }
    }

    private fun openSource(source: Source) {
        when (source) {
            Source.CAMERA -> openCamera()
            Source.GALLERY -> pickImageGallery.launch("image/*")
        }
    }

    private fun openCamera() {
        getTmpFileUri().let { uri ->
            latestTmpUri = uri
            takePicture.launch(uri)
        }
    }

    private fun getTmpFileUri(): Uri {
        val tmpFile = File.createTempFile("tmp_image_file", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        return getUriForFile(this, tmpFile)
    }

    /**
     * This method show the dialog for user source choice, it is an open function so can be overridden
     * and customised with the app layout if you need.
     */
    open fun showImageSourceDialog(openSource: (Source) -> Unit) {
        AlertDialog.Builder(this)
            .setCancelable(false)
            .setOnKeyListener { _, keyCode, keyEvent ->
                if (keyCode == KeyEvent.KEYCODE_BACK && keyEvent.action == KeyEvent.ACTION_UP) {
                    setResultCancel()
                    finish()
                }
                true
            }
            .setTitle(R.string.pick_image_chooser_title)
            .setItems(
                arrayOf(
                    getString(R.string.pick_image_camera),
                    getString(R.string.pick_image_gallery),
                ),
            ) { _, position -> openSource(if (position == 0) Source.CAMERA else Source.GALLERY) }
            .show()
    }

    public override fun onStart() {
        super.onStart()
        cropImageView?.setOnSetImageUriCompleteListener(this)
        cropImageView?.setOnCropImageCompleteListener(this)
    }

    public override fun onStop() {
        super.onStop()
        cropImageView?.setOnSetImageUriCompleteListener(null)
        cropImageView?.setOnCropImageCompleteListener(null)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(BUNDLE_KEY_TMP_URI, latestTmpUri.toString())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (cropImageOptions.skipEditing) return true
        menuInflater.inflate(R.menu.crop_image_menu, menu)

        if (!cropImageOptions.allowRotation) {
            menu.removeItem(R.id.ic_rotate_left_24)
            menu.removeItem(R.id.ic_rotate_right_24)
        } else if (cropImageOptions.allowCounterRotation) {
            menu.findItem(R.id.ic_rotate_left_24).isVisible = true
        }

        if (!cropImageOptions.allowFlipping) menu.removeItem(R.id.ic_flip_24)

        if (cropImageOptions.cropMenuCropButtonTitle != null) {
            menu.findItem(R.id.crop_image_menu_crop).title =
                cropImageOptions.cropMenuCropButtonTitle
        }

        var cropIcon: Drawable? = null
        try {
            if (cropImageOptions.cropMenuCropButtonIcon != 0) {
                cropIcon = ContextCompat.getDrawable(this, cropImageOptions.cropMenuCropButtonIcon)
                menu.findItem(R.id.crop_image_menu_crop).icon = cropIcon
            }
        } catch (e: Exception) {
            Log.w("AIC", "Failed to read menu crop drawable", e)
        }

        if (cropImageOptions.activityMenuIconColor != 0) {
            updateMenuItemIconColor(
                menu,
                R.id.ic_rotate_left_24,
                cropImageOptions.activityMenuIconColor,
            )
            updateMenuItemIconColor(
                menu,
                R.id.ic_rotate_right_24,
                cropImageOptions.activityMenuIconColor,
            )
            updateMenuItemIconColor(menu, R.id.ic_flip_24, cropImageOptions.activityMenuIconColor)

            if (cropIcon != null) {
                updateMenuItemIconColor(
                    menu,
                    R.id.crop_image_menu_crop,
                    cropImageOptions.activityMenuIconColor,
                )
            }
        }
        cropImageOptions.activityMenuTextColor?.let { menuItemsTextColor ->
            val menuItemIds = listOf(
                R.id.ic_rotate_left_24,
                R.id.ic_rotate_right_24,
                R.id.ic_flip_24,
                R.id.ic_flip_24_horizontally,
                R.id.ic_flip_24_vertically,
                R.id.crop_image_menu_crop,
            )
            for (itemId in menuItemIds) {
                updateMenuItemTextColor(menu, itemId, menuItemsTextColor)
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.crop_image_menu_crop -> {
            cropImage()
            true
        }
        R.id.ic_rotate_left_24 -> {
            rotateImage(-cropImageOptions.rotationDegrees)
            true
        }
        R.id.ic_rotate_right_24 -> {
            rotateImage(cropImageOptions.rotationDegrees)
            true
        }
        R.id.ic_flip_24_horizontally -> {
            cropImageView?.flipImageHorizontally()
            true
        }
        R.id.ic_flip_24_vertically -> {
            cropImageView?.flipImageVertically()
            true
        }
        android.R.id.home -> {
            setResultCancel()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    protected open fun onPickImageResult(resultUri: Uri?) {
        when (resultUri) {
            null -> setResultCancel()
            else -> {
                cropImageUri = resultUri
                cropImageView?.setImageUriAsync(cropImageUri)
            }
        }
    }

    override fun onSetImageUriComplete(view: CropImageView, uri: Uri, error: Exception?) {
        if (error == null) {
            if (cropImageOptions.initialCropWindowRectangle != null) {
                cropImageView?.cropRect = cropImageOptions.initialCropWindowRectangle
            }

            if (cropImageOptions.initialRotation > 0) {
                cropImageView?.rotatedDegrees = cropImageOptions.initialRotation
            }

            if (cropImageOptions.skipEditing) {
                cropImage()
            }
        } else {
            setResult(null, error, 1)
        }
    }

    override fun onCropImageComplete(view: CropImageView, result: CropImageView.CropResult) {
        Logger.d("LASSI", "!@# CropImageActivity onCropImageComplete() 364 => ${result.uriContent}")
        setResult(result.uriContent, result.error, result.sampleSize)
    }

    /**
     * Execute crop image and save the result tou output uri.
     */
    open fun cropImage() {
        if (cropImageOptions.noOutputImage) {
            setResult(null, null, 1)
        } else {
            cropImageView?.croppedImageAsync(
                saveCompressFormat = cropImageOptions.outputCompressFormat,
                saveCompressQuality = cropImageOptions.outputCompressQuality,
                reqWidth = cropImageOptions.outputRequestWidth,
                reqHeight = cropImageOptions.outputRequestHeight,
                options = cropImageOptions.outputRequestSizeOptions,
                customOutputUri = cropImageOptions.customOutputUri,
            )
        }
    }

    /**
     * When extending this activity, please set your own ImageCropView
     */
    open fun setCropImageView(cropImageView: CropImageView) {
        this.cropImageView = cropImageView
    }

    /**
     * Rotate the image in the crop image view.
     */
    open fun rotateImage(degrees: Int) {
        cropImageView?.rotateImage(degrees)
    }

    /**
     * Result with cropped image data or error if failed.
     */
    open fun setResult(uri: Uri?, error: Exception?, sampleSize: Int) {
        Logger.d("LASSI", "!@# CropImageActivity setResult() 404 => ${uri?.path}")
        uri?.path?.let {
            FilePickerUtils.notifyGalleryUpdateNewFile(
                this,
                filePath = it,
                onFileScanComplete = this::onFileScanComplete
            )
        }
    }

    private fun onFileScanComplete(uri: Uri?, imagePath: String?) {
        Logger.d(
            "LASSI",
            "!@# CropImageActivity setResult() 420 uri => $uri, imagePath => $imagePath"
        )
        uri?.let { returnUri ->
            Logger.d("LASSI", "!@# CropImageActivity setResult() 422 => $returnUri")
            contentResolver.query(returnUri, null, null, null, null)
        }?.use { cursor ->
            cursor.moveToFirst()
            try {
                val id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID))
                val name =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))
                val path =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
                val miMedia = MiMedia(id, name, path, 0)
                Logger.d("CropUtils", "Check URI already exist $path")
                val intent = Intent().apply {
                    putExtra(KeyUtils.MEDIA_PREVIEW, miMedia)
                }
                setResult(Activity.RESULT_OK, intent)
                finish()
            } catch (e: Exception) {
                Logger.e(logTag, "onFileScanComplete $e")
            } finally {
                cursor.close()
            }
        } ?: let {
            imagePath?.let {
                Logger.d("CropUtils", "Check URI already path is $it")
                val miMedia = MiMedia(path = it, doesUri = false)
                val intent = Intent().apply {
                    putExtra(KeyUtils.MEDIA_PREVIEW, miMedia)
                }
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }

    /**
     * Cancel of cropping activity.
     */
    open fun setResultCancel() {
        setResult(RESULT_CANCELED)
        finish()
    }

    /**
     * Get intent instance to be used for the result of this activity.
     */
    open fun getResultIntent(uri: Uri?, error: Exception?, sampleSize: Int): Intent {
        val result = CropImage.ActivityResult(
            originalUri = cropImageView?.imageUri,
            uriContent = uri,
            error = error,
            cropPoints = cropImageView?.cropPoints,
            cropRect = cropImageView?.cropRect,
            rotation = cropImageView?.rotatedDegrees ?: 0,
            wholeImageRect = cropImageView?.wholeImageRect,
            sampleSize = sampleSize,
        )
        val intent = Intent()
        intent.extras?.let(intent::putExtras)
        intent.putExtra(CropImage.CROP_IMAGE_EXTRA_RESULT, result)
        return intent
    }

    /**
     * Update the color of a specific menu item to the given color.
     */
    open fun updateMenuItemIconColor(menu: Menu, itemId: Int, color: Int) {
        val menuItem = menu.findItem(itemId)
        if (menuItem != null) {
            val menuItemIcon = menuItem.icon
            if (menuItemIcon != null) {
                try {
                    menuItemIcon.apply {
                        mutate()
                        colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                            color,
                            BlendModeCompat.SRC_ATOP,
                        )
                    }
                    menuItem.icon = menuItemIcon
                } catch (e: Exception) {
                    Log.w("AIC", "Failed to update menu item color", e)
                }
            }
        }
    }

    /**
     * Update the color of a specific menu item to the given color.
     */
    open fun updateMenuItemTextColor(menu: Menu, itemId: Int, color: Int) {
        val menuItem = menu.findItem(itemId)
        val menuTitle = menuItem.title
        if (menuTitle?.isNotBlank() == true) {
            try {
                val spannableTitle: Spannable = SpannableString(menuTitle)
                spannableTitle.setSpan(
                    ForegroundColorSpan(color),
                    0,
                    spannableTitle.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
                menuItem.title = spannableTitle
            } catch (e: Exception) {
                Log.w("AIC", "Failed to update menu item color", e)
            }
        }
    }

    enum class Source { CAMERA, GALLERY }

    private companion object {

        const val BUNDLE_KEY_TMP_URI = "bundle_key_tmp_uri"
    }
}
