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
    var toolbarColor: Int = Color.BLACK,
    var statusBarColor: Int = Color.BLACK,
    var toolbarResourceColor: Int = Color.WHITE,
    var progressBarColor: Int = Color.BLACK,
    @DrawableRes var placeHolder: Int = R.drawable.ic_image_placeholder,
    @DrawableRes var errorDrawable: Int = R.drawable.ic_image_placeholder,
    var selectedMedias: ArrayList<MiMedia> = ArrayList(),
    var mediaType: MediaType = MediaType.IMAGE,
    var maxCount: Int = KeyUtils.DEFAULT_MEDIA_COUNT,
    var gridSize: Int = KeyUtils.DEFAULT_GRID_SIZE,
    var lassiOption: LassiOption = LassiOption.CAMERA_AND_GALLERY,
    var minTime: Long = KeyUtils.DEFAULT_DURATION,
    var maxTime: Long = KeyUtils.DEFAULT_DURATION,
    var cropType: CropImageView.CropShape = CropImageView.CropShape.RECTANGLE,
    var supportedFileType: MutableList<String> = mutableListOf(),
    var cropAspectRatio: AspectRatio? = null,
    var enableFlipImage: Boolean = false,
    var enableRotateImage: Boolean = false,
    var enableActualCircleCrop: Boolean = false,
    var compressionRation: Int = 0,
    var minFileSize: Long = KeyUtils.DEFAULT_FILE_SIZE,
    var maxFileSize: Long = KeyUtils.DEFAULT_FILE_SIZE,
    var isCrop: Boolean = true
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
                compressionRation = lassiConfig.compressionRation
                minFileSize = lassiConfig.minFileSize
                maxFileSize = lassiConfig.maxFileSize
                isCrop = lassiConfig.isCrop
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
