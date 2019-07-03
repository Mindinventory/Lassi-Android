package com.lassi.presentation.builder

import android.content.Context
import android.content.Intent
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.lassi.common.utils.KeyUtils
import com.lassi.domain.media.LassiConfig
import com.lassi.domain.media.LassiOption
import com.lassi.domain.media.MediaType
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
        lassiConfig.maxCount =
            if (maxCount < 0) KeyUtils.DEFAULT_MEDIA_COUNT else maxCount
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
     * Media type (MediaType.IMAGE, MediaType.VIDEO)
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
        lassiConfig.minTime = minTime
        return this
    }

    /**
     * Filter videos by max time in seconds (only for MediaType.VIDEO)
     */
    fun setMaxTime(maxTime: Long): Lassi {
        lassiConfig.maxTime = maxTime
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
     * Set toolbar color
     */
    fun setToolbarColor(@ColorRes toolbarColor: Int): Lassi {
        lassiConfig.toolbarColor = toolbarColor
        return this
    }

    /**
     * Set statusBar color (Only applicable for >= Lollipop)
     */
    fun setStatusBarColor(@ColorRes statusBarColor: Int): Lassi {
        lassiConfig.statusBarColor = statusBarColor
        return this
    }

    /**
     * Set toolbar resource color
     */
    fun setToolbarResourceColor(@ColorRes toolbarResourceColor: Int): Lassi {
        lassiConfig.toolbarResourceColor = toolbarResourceColor
        return this
    }

    /**
     * Set progressbar color
     */
    fun setProgressBarColor(@ColorRes progressBarColor: Int): Lassi {
        lassiConfig.progressBarColor = progressBarColor
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
     * Set crop type
     */
    fun setCropType(cropType: CropImageView.CropShape): Lassi {
        lassiConfig.cropType = cropType
        return this
    }

    /**
     * Set crop Aspect ratio
     */
    fun setCropAspectRatio(x: Int, y: Int): Lassi {
        lassiConfig.cropAspectRatio = AspectRatio.of(x, y)
        return this
    }

    /**
     * Start LassiMediaPickerActivity with config
     */
    fun build(): Intent {
        LassiConfig.setConfig(lassiConfig)
        return Intent(context, LassiMediaPickerActivity::class.java)
    }
}
