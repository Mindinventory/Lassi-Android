package com.lassi.presentation.builder

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.lassi.common.utils.KeyUtils
import com.lassi.domain.media.LassiConfig
import com.lassi.domain.media.LassiOption
import com.lassi.domain.media.MediaType
import com.lassi.domain.media.SortingOption
import com.lassi.presentation.cameraview.controls.AspectRatio
import com.lassi.presentation.cropper.CropImageView
import com.lassi.presentation.mediadirectory.LassiMediaPickerActivity

class Lassi(private val context: Context) {

    private var lassiConfig = LassiConfig()

    /**
     * Limit max item selection
     */
    fun setMaxCount(maxCount: Int): Lassi {
        // handle negative input
        lassiConfig.maxCount = if (maxCount < 0) {
            KeyUtils.DEFAULT_MEDIA_COUNT
        } else {
            maxCount
        }
        return this
    }

    /**
     * Default sorting
     * 1 - Ascending
     * 0 - Descending
     */
    fun setAscSort(ascFlag: SortingOption): Lassi {
        lassiConfig.ascFlag = when (ascFlag) {
            SortingOption.ASCENDING -> KeyUtils.ASCENDING_ORDER
            SortingOption.DESCENDING -> KeyUtils.DESCENDING_ORDER
        }
        return this
    }

    /**
     * Set item grid size (>= 2 or <=4)
     */
    fun setGridSize(gridSize: Int): Lassi {
        lassiConfig.gridSize = when {
            gridSize < KeyUtils.DEFAULT_GRID_SIZE -> KeyUtils.DEFAULT_GRID_SIZE
            gridSize > KeyUtils.MAX_GRID_SIZE -> KeyUtils.MAX_GRID_SIZE
            else -> gridSize
        }
        return this
    }

    /**
     * Media type (MediaType.IMAGE, MediaType.VIDEO, MediaType.AUDIO, MediaType.DOC)
     */
    fun setMediaType(mediaType: MediaType): Lassi {
        lassiConfig.mediaType = mediaType
        return this
    }

    /**
     * Allow Media picket to capture/record from camera while multiple media selection
     */
    fun with(lassiOption: LassiOption): Lassi {
        lassiConfig.lassiOption = lassiOption
        return this
    }

    /**
     * Filter videos by min time in seconds (only for MediaType.VIDEO)
     */
    fun setMinTime(minTime: Long): Lassi {
        // handle negative input
        lassiConfig.minTime = if (minTime > 0) {
            minTime
        } else {
            KeyUtils.DEFAULT_DURATION
        }
        return this
    }

    /**
     * Filter videos by max time in seconds (only for MediaType.VIDEO)
     */
    fun setMaxTime(maxTime: Long): Lassi {
        // handle negative input
        lassiConfig.maxTime = if (maxTime > 0) {
            maxTime
        } else {
            KeyUtils.DEFAULT_DURATION
        }
        return this
    }

    /**
     * Add comma separated supported files types ex. png, jpeg
     */
    fun setSupportedFileTypes(vararg fileTypes: String): Lassi {
        lassiConfig.supportedFileType = fileTypes.toMutableList()
        return this
    }

    /**
     * Set toolbar color resource
     */
    fun setToolbarColor(@ColorRes toolbarColor: Int): Lassi {
        lassiConfig.toolbarColor = ContextCompat.getColor(context, toolbarColor)
        return this
    }

    /**
     * Set toolbar color hex
     */
    fun setToolbarColor(toolbarColor: String): Lassi {
        lassiConfig.toolbarColor = Color.parseColor(toolbarColor)
        return this
    }

    /**
     * Set statusBar color resource (Only applicable for >= Lollipop)
     */
    fun setStatusBarColor(@ColorRes statusBarColor: Int): Lassi {
        lassiConfig.statusBarColor = ContextCompat.getColor(context, statusBarColor)
        return this
    }

    /**
     * Set statusBar color hex (Only applicable for >= Lollipop)
     */
    fun setStatusBarColor(statusBarColor: String): Lassi {
        lassiConfig.statusBarColor = Color.parseColor(statusBarColor)
        return this
    }

    /**
     * Set toolbar color resource
     */
    fun setToolbarResourceColor(@ColorRes toolbarResourceColor: Int): Lassi {
        lassiConfig.toolbarResourceColor = ContextCompat.getColor(context, toolbarResourceColor)
        return this
    }

    /**
     * Set toolbar color hex
     */
    fun setToolbarResourceColor(toolbarResourceColor: String): Lassi {
        lassiConfig.toolbarResourceColor = Color.parseColor(toolbarResourceColor)
        return this
    }

    /**
     * Set progressbar color resource
     */
    fun setProgressBarColor(@ColorRes progressBarColor: Int): Lassi {
        lassiConfig.progressBarColor = ContextCompat.getColor(context, progressBarColor)
        return this
    }

    /**
     * Set gallery background color resource
     */
    fun setGalleryBackgroundColor(@ColorRes color: Int): Lassi {
        lassiConfig.galleryBackgroundColor = ContextCompat.getColor(context, color)
        return this
    }

    /**
     * Set progressbar color hex
     */
    fun setProgressBarColor(progressBarColor: String): Lassi {
        lassiConfig.progressBarColor = Color.parseColor(progressBarColor)
        return this
    }

    /**
     * Set place holder to grid items
     */
    fun setPlaceHolder(@DrawableRes placeHolder: Int): Lassi {
        lassiConfig.placeHolder = placeHolder
        return this
    }

    /**
     * Set error drawable to grid items
     */
    fun setErrorDrawable(@DrawableRes errorDrawable: Int): Lassi {
        lassiConfig.errorDrawable = errorDrawable
        return this
    }

    /**
     * Set selection drawable to grid items
     */
    fun setSelectionDrawable(@DrawableRes selectionDrawable: Int): Lassi {
        lassiConfig.selectionDrawable = selectionDrawable
        return this
    }

    /**
     * Set crop type(only for MediaType.Image and Single Image Selection)
     */
    fun setCropType(cropType: CropImageView.CropShape): Lassi {
        lassiConfig.cropType = cropType
        return this
    }

    /**
     * Set crop (only for MediaType.Image and Single Image Selection)
     */
    fun disableCrop(): Lassi {
        lassiConfig.isCrop = false
        return this
    }

    /**
     * Set crop Aspect ratio
     */
    fun setCropAspectRatio(x: Int, y: Int): Lassi {
        val aspectX = if (x < 0) 1 else x
        val aspectY = if (y < 0) 1 else y
        lassiConfig.cropAspectRatio = AspectRatio.of(aspectX, aspectY)
        return this
    }

    /**
     * Enable flip image option while image cropping
     */
    fun enableFlip(): Lassi {
        lassiConfig.enableFlipImage = true
        return this
    }

    /**
     * Enable rotate image option while image cropping
     */
    fun enableRotate(): Lassi {
        lassiConfig.enableRotateImage = true
        return this
    }

    /**
     * Enable actual circular crop (only for MediaType.Image and CropImageView.CropShape.OVAL)
     */
    fun enableActualCircleCrop(): Lassi {
        lassiConfig.enableActualCircleCrop = true
        return this
    }

    /**
     * Set compression ration between 0 to 100 (Only for single image selection)
     */
    fun setCompressionRatio(compressionRation: Int): Lassi {
        val compression = if (compressionRation > 100) {
            100
        } else {
            compressionRation
        }
        lassiConfig.compressionRation = compression
        return this
    }

    /**
     * Set minimum file size in KB
     */
    fun setMinFileSize(fileSize: Long): Lassi {
        if (fileSize > 0) {
            lassiConfig.minFileSize = fileSize
        }
        return this
    }

    /**
     * Set maximum file size in KB
     */
    fun setMaxFileSize(fileSize: Long): Lassi {
        if (fileSize > 0) {
            lassiConfig.maxFileSize = fileSize
        }
        return this
    }

    /**
     * Set color for the Negative button of the Alert dialog
     */
    fun setAlertDialogNegativeButtonColor(@ColorRes negativeBtnColor: Int): Lassi {
        lassiConfig.alertDialogNegativeButtonColor =
            ContextCompat.getColor(context, negativeBtnColor)
        return this
    }

    /**
     * Set color for the Positive button of the Alert dialog
     */
    fun setAlertDialogPositiveButtonColor(@ColorRes positiveBtnColor: Int): Lassi {
        lassiConfig.alertDialogPositiveButtonColor =
            ContextCompat.getColor(context, positiveBtnColor)
        return this
    }

    /**
     * To set custom error message when picked items exceeds the defined maxCount
     */
    fun setCustomLimitExceedingErrorMessage(errorMessage: Int): Lassi {
        lassiConfig.customLimitExceedingErrorMessage = errorMessage
        return this
    }

    /**
     * Create intent for LassiMediaPickerActivity with config
     */
    fun build(): Intent {
        LassiConfig.setConfig(lassiConfig)
        return Intent(context, LassiMediaPickerActivity::class.java)
    }
}
