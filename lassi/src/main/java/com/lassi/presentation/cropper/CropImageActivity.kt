package com.lassi.presentation.cropper

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.lassi.R
import com.lassi.common.utils.*
import com.lassi.data.media.MiMedia
import com.lassi.domain.media.LassiConfig
import com.lassi.presentation.mediadirectory.LassiMediaPickerActivity
import kotlinx.android.synthetic.main.crop_image_activity.*
import java.io.File
import java.io.IOException

/**
 * Built-in activity for image cropping.<br></br>
 * Use [CropImage.activity] to create a builder to start this activity.
 */
open class CropImageActivity : AppCompatActivity(), CropImageView.OnSetImageUriCompleteListener,
    CropImageView.OnCropImageCompleteListener {
    private val logTag = CropImageActivity::class.java.simpleName
    /**
     * The crop image view library widget used in the activity
     */

    /**
     * Persist URI image to crop URI if specific permissions are required
     */
    private var cropImageUri: Uri? = null

    /**
     * the options that were set for the crop image
     */
    private var cropImageOptions: CropImageOptions? = null

    /**
     * Get Android uri to save the cropped image into.<br></br>
     * Use the given in options or create a temp file.
     */
    private val outputUri: Uri
        get() {
            var outputUri: Uri? = cropImageOptions?.outputUri
            if (outputUri == null || outputUri == Uri.EMPTY) {
                try {
                    val ext =
                        when {
                            cropImageOptions?.outputCompressFormat == Bitmap.CompressFormat.JPEG -> ".jpg"
                            cropImageOptions?.outputCompressFormat == Bitmap.CompressFormat.PNG -> ".png"
                            else -> ".webp"
                        }
                    outputUri = Uri.fromFile(File.createTempFile("cropped", ext, cacheDir))
                } catch (e: IOException) {
                    throw RuntimeException("Failed to create temp file for output image", e)
                }

            }
            return outputUri!!
        }

    @SuppressLint("NewApi")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.crop_image_activity)
        toolbar.title = ""
        setSupportActionBar(toolbar)
        val bundle = intent.getBundleExtra(CropImage.CROP_IMAGE_EXTRA_BUNDLE)
        cropImageUri = bundle.getParcelable(CropImage.CROP_IMAGE_EXTRA_SOURCE)
        cropImageOptions = bundle.getParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS)

        if (savedInstanceState == null) {
            if (cropImageUri == null || cropImageUri == Uri.EMPTY) {
                if (CropImage.isExplicitCameraPermissionRequired(this)) {
                    // request permissions and handle the result in onRequestPermissionsResult()
                    requestPermissions(
                        arrayOf(Manifest.permission.CAMERA),
                        CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE
                    )
                } else {
                    CropImage.startPickImageActivity(this)
                }
            } else if (CropImage.isReadExternalStoragePermissionsRequired(this, cropImageUri!!)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE
                )
            } else {
                // no permissions required or already grunted, can start crop image activity
                cropImageView.setImageUriAsync(cropImageUri)
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setThemeAttributes()
    }

    private fun setThemeAttributes() {
        with(LassiConfig.getConfig()) {
            toolbar.background =
                ColorDrawable(ColorUtils.getColor(this@CropImageActivity, toolbarColor))
            toolbar.setTitleTextColor(
                ColorUtils.getColor(this@CropImageActivity, toolbarResourceColor)
            )
            val upArrow =
                ContextCompat.getDrawable(this@CropImageActivity, R.drawable.ic_back_white)
            upArrow?.setColorFilter(
                ColorUtils.getColor(this@CropImageActivity, toolbarResourceColor),
                PorterDuff.Mode.SRC_ATOP
            )
            supportActionBar?.setHomeAsUpIndicator(upArrow)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor =
                    ColorUtils.getColor(this@CropImageActivity, statusBarColor)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        cropImageView!!.setOnSetImageUriCompleteListener(this)
        cropImageView!!.setOnCropImageCompleteListener(this)
    }

    override fun onStop() {
        super.onStop()
        cropImageView!!.setOnSetImageUriCompleteListener(null)
        cropImageView!!.setOnCropImageCompleteListener(null)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.crop_image_menu, menu)
        val menuRotateLeft = menu.findItem(R.id.crop_image_menu_rotate_left)
        menuRotateLeft?.icon = DrawableUtils.changeIconColor(
            this@CropImageActivity,
            R.drawable.ic_crop_image_menu_rotate_left,
            LassiConfig.getConfig().toolbarResourceColor
        )

        val menuRotateRight = menu.findItem(R.id.crop_image_menu_rotate_right)
        menuRotateRight?.icon = DrawableUtils.changeIconColor(
            this@CropImageActivity,
            R.drawable.ic_crop_image_menu_rotate_right,
            LassiConfig.getConfig().toolbarResourceColor
        )

        val menuFlip = menu.findItem(R.id.crop_image_menu_flip)
        menuFlip?.icon = DrawableUtils.changeIconColor(
            this@CropImageActivity,
            R.drawable.ic_crop_image_menu_flip,
            LassiConfig.getConfig().toolbarResourceColor
        )

        val menuDone = menu.findItem(R.id.crop_image_menu_crop)
        menuDone?.icon = DrawableUtils.changeIconColor(
            this@CropImageActivity,
            R.drawable.ic_done_white,
            LassiConfig.getConfig().toolbarResourceColor
        )

        if (!cropImageOptions!!.allowRotation) {
            menu.removeItem(R.id.crop_image_menu_rotate_left)
            menu.removeItem(R.id.crop_image_menu_rotate_right)
        } else if (cropImageOptions!!.allowCounterRotation) {
            menu.findItem(R.id.crop_image_menu_rotate_left).isVisible = true
        }

        if (!cropImageOptions!!.allowFlipping) {
            menu.removeItem(R.id.crop_image_menu_flip)
        }

        if (cropImageOptions!!.cropMenuCropButtonTitle != null) {
            menu.findItem(R.id.crop_image_menu_crop).title =
                cropImageOptions!!.cropMenuCropButtonTitle
        }

        var cropIcon: Drawable? = null
        try {
            if (cropImageOptions!!.cropMenuCropButtonIcon != 0) {
                cropIcon =
                    ContextCompat.getDrawable(this, cropImageOptions!!.cropMenuCropButtonIcon)
                menu.findItem(R.id.crop_image_menu_crop).icon = cropIcon
            }
        } catch (e: Exception) {
            Log.w("AIC", "Failed to read menu crop drawable", e)
        }

        if (cropImageOptions!!.activityMenuIconColor != 0) {
            updateMenuItemIconColor(
                menu, R.id.crop_image_menu_rotate_left, cropImageOptions!!.activityMenuIconColor
            )
            updateMenuItemIconColor(
                menu, R.id.crop_image_menu_rotate_right, cropImageOptions!!.activityMenuIconColor
            )
            updateMenuItemIconColor(
                menu,
                R.id.crop_image_menu_flip,
                cropImageOptions!!.activityMenuIconColor
            )
            if (cropIcon != null) {
                updateMenuItemIconColor(
                    menu,
                    R.id.crop_image_menu_crop,
                    cropImageOptions!!.activityMenuIconColor
                )
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.crop_image_menu_crop) {
            cropImage()
            return true
        }
        if (item.itemId == R.id.crop_image_menu_rotate_left) {
            rotateImage(-cropImageOptions!!.rotationDegrees)
            return true
        }
        if (item.itemId == R.id.crop_image_menu_rotate_right) {
            rotateImage(cropImageOptions!!.rotationDegrees)
            return true
        }
        if (item.itemId == R.id.crop_image_menu_flip_horizontally) {
            cropImageView!!.flipImageHorizontally()
            return true
        }
        if (item.itemId == R.id.crop_image_menu_flip_vertically) {
            cropImageView!!.flipImageVertically()
            return true
        }
        if (item.itemId == android.R.id.home) {
            setResultCancel()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResultCancel()
    }

    @SuppressLint("NewApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                // User cancelled the picker. We don't have anything to crop
                setResultCancel()
            }

            if (resultCode == Activity.RESULT_OK) {
                cropImageUri = CropImage.getPickImageResultUri(this, data)

                // For API >= 23 we need to check specifically that we have permissions to read external
                // storage.
                if (CropImage.isReadExternalStoragePermissionsRequired(this, cropImageUri!!)) {
                    // request permissions and handle the result in onRequestPermissionsResult()
                    requestPermissions(
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE
                    )
                } else {
                    // no permissions required or already grunted, can start crop image activity
                    cropImageView!!.setImageUriAsync(cropImageUri)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (cropImageUri != null
                && grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                // required permissions granted, start crop image activity
                cropImageView!!.setImageUriAsync(cropImageUri)
            } else {
                Toast.makeText(this, R.string.crop_image_activity_no_permissions, Toast.LENGTH_LONG)
                    .show()
                setResultCancel()
            }
        }

        if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            // Irrespective of whether camera permission was given or not, we show the picker
            // The picker will not add the camera intent if permission is not available
            CropImage.startPickImageActivity(this)
        }
    }

    override fun onSetImageUriComplete(view: CropImageView, uri: Uri, error: Exception?) {
        if (error == null) {
            if (cropImageOptions!!.initialCropWindowRectangle != null) {
                cropImageView!!.cropRect = cropImageOptions!!.initialCropWindowRectangle
            }
            if (cropImageOptions!!.initialRotation > -1) {
                cropImageView!!.rotatedDegrees = cropImageOptions!!.initialRotation
            }
        } else {
            setResult(null, error, 1)
        }
    }

    override fun onCropImageComplete(view: CropImageView, result: CropImageView.CropResult) {
        setResult(result.uri, result.error, result.sampleSize)
    }

// region: Private methods

    /**
     * Execute crop image and save the result tou output uri.
     */
    private fun cropImage() {
        if (cropImageOptions!!.noOutputImage) {
            setResult(null, null, 1)
        } else {
            val outputUri = outputUri
            cropImageView!!.saveCroppedImageAsync(
                outputUri,
                cropImageOptions!!.outputCompressFormat,
                cropImageOptions!!.outputCompressQuality,
                cropImageOptions!!.outputRequestWidth,
                cropImageOptions!!.outputRequestHeight,
                cropImageOptions!!.outputRequestSizeOptions
            )
        }
    }

    /**
     * Rotate the image in the crop image view.
     */
    private fun rotateImage(degrees: Int) {
        cropImageView!!.rotateImage(degrees)
    }

    /**
     * Result with cropped image data or error if failed.
     */
    private fun setResult(uri: Uri?, error: Exception?, sampleSize: Int) {
        uri?.path?.let {
            FilePickerUtils.notifyGalleryUpdateNewFile(
                this,
                it,
                onFileScanComplete = this::onFileScanComplete
            )
        }
//        startActivity(getResultIntent(uri, error, sampleSize))
    }

    private fun onFileScanComplete(uri: Uri) {
        uri.let { returnUri ->
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

                val intent = Intent().apply {
                    putExtra(KeyUtils.MEDIA_PREVIEW, miMedia)
                }
                setResult(Activity.RESULT_OK, intent)
                finish()
            } catch (e: Exception) {
                Logger.e(logTag, "onNewIntent $e")
            } finally {
                cursor.close()
            }
        }
    }

    /**
     * Cancel of cropping activity.
     */
    private fun setResultCancel() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    /**
     * Get intent instance to be used for the result of this activity.
     */
    protected fun getResultIntent(uri: Uri?, error: Exception?, sampleSize: Int): Intent {
        val result = CropImage.ActivityResult(
            cropImageView!!.imageUri,
            uri,
            error,
            cropImageView!!.cropPoints,
            cropImageView!!.cropRect,
            cropImageView!!.rotatedDegrees,
            cropImageView!!.wholeImageRect,
            sampleSize
        )
        val intent = Intent(this, LassiMediaPickerActivity::class.java)
        intent.putExtras(getIntent())
        intent.putExtra(CropImage.CROP_IMAGE_EXTRA_RESULT, result)
        return intent
    }

    /**
     * Update the color of a specific menu item to the given color.
     */
    private fun updateMenuItemIconColor(menu: Menu, itemId: Int, color: Int) {
        val menuItem = menu.findItem(itemId)
        if (menuItem != null) {
            val menuItemIcon = menuItem.icon
            if (menuItemIcon != null) {
                try {
                    menuItemIcon.mutate()
                    menuItemIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
                    menuItem.icon = menuItemIcon
                } catch (e: Exception) {
                    Log.w("AIC", "Failed to update menu item color", e)
                }

            }
        }
    }
// endregion
}
