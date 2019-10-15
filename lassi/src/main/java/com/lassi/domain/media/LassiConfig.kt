package com.lassi.domain.media

import android.graphics.Color
import android.os.Parcelable
import androidx.annotation.DrawableRes
import com.lassi.R
import com.lassi.common.utils.KeyUtils
import com.lassi.data.media.MiMedia
import com.lassi.presentation.cameraview.controls.AspectRatio
import com.lassi.presentation.cropper.CropImageView
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LassiConfig(
    var toolbarColor: Int = Color.parseColor(KeyUtils.BLACK),
    var statusBarColor: Int = Color.parseColor(KeyUtils.BLACK),
    var toolbarResourceColor: Int = Color.parseColor(KeyUtils.WHITE),
    var progressBarColor: Int = Color.parseColor(KeyUtils.BLACK),
    @DrawableRes var placeHolder: Int = R.drawable.ic_image_placeholder,
    @DrawableRes var errorDrawable: Int = R.drawable.ic_image_placeholder,
    var selectedMedias: ArrayList<MiMedia> = ArrayList(),
    var mediaType: MediaType = MediaType.IMAGE,
    var maxCount: Int = KeyUtils.DEFAULT_MEDIA_COUNT,
    var gridSize: Int = KeyUtils.DEFAULT_GRID_SIZE,
    var lassiOption: LassiOption = LassiOption.CAMERA_AND_GALLERY,
    var minTime: Long = KeyUtils.DEFAULT_VIDEO_DURATION,
    var maxTime: Long = KeyUtils.DEFAULT_VIDEO_DURATION,
    var cropType: CropImageView.CropShape = CropImageView.CropShape.RECTANGLE,
    var supportedFileType: MutableList<String> = mutableListOf(),
    var cropAspectRatio: AspectRatio? = null,
    var enableFlipImage: Boolean = false,
    var enableRotateImage: Boolean = false,
    var enableActualCircleCrop: Boolean = false
) : Parcelable {
    companion object {

        private var mediaPickerConfig = LassiConfig()

        fun setConfig(lassiConfig: LassiConfig) {
            this.mediaPickerConfig.apply {
                toolbarColor = lassiConfig.toolbarColor
                statusBarColor = lassiConfig.statusBarColor
                toolbarResourceColor = lassiConfig.toolbarResourceColor
                progressBarColor = lassiConfig.progressBarColor
                selectedMedias = lassiConfig.selectedMedias
                mediaType = lassiConfig.mediaType
                maxCount = lassiConfig.maxCount
                gridSize = lassiConfig.gridSize
                lassiOption = lassiConfig.lassiOption
                minTime = lassiConfig.minTime
                maxTime = lassiConfig.maxTime
                placeHolder = lassiConfig.placeHolder
                errorDrawable = lassiConfig.errorDrawable
                cropType = lassiConfig.cropType
                supportedFileType = lassiConfig.supportedFileType
                cropAspectRatio = lassiConfig.cropAspectRatio
                enableFlipImage = lassiConfig.enableFlipImage
                enableRotateImage = lassiConfig.enableRotateImage
                enableActualCircleCrop = lassiConfig.enableActualCircleCrop
            }
        }

        fun getConfig(): LassiConfig {
            return mediaPickerConfig
        }

        fun isSingleMediaSelection(): Boolean {
            return (mediaPickerConfig.maxCount == 1 || mediaPickerConfig.lassiOption == LassiOption.CAMERA)
        }
    }
}