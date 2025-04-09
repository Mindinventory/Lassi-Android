package com.lassi.domain.media

import android.graphics.Color
import android.os.Parcelable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.lassi.R
import com.lassi.common.utils.KeyUtils
import com.lassi.common.utils.KeyUtils.ERROR_EXCEEDING_MSG
import com.lassi.data.media.MiMedia
import com.lassi.presentation.cameraview.controls.AspectRatio
import com.lassi.presentation.cropper.CropImageView
import kotlinx.parcelize.Parcelize

@Parcelize
data class LassiConfig(
    var toolbarColor: Int = Color.BLACK,
    var statusBarColor: Int = Color.BLACK,
    var toolbarResourceColor: Int = Color.WHITE,
    var progressBarColor: Int = Color.BLACK,
    var galleryBackgroundColor: Int = Color.WHITE,
    @ColorRes var sortingCheckedRadioButtonColor: Int = R.color.darkYellow,
    @ColorRes var sortingUncheckedRadioButtonColor: Int = R.color.blackTrans50,
    @ColorRes var sortingCheckedTextColor: Int = R.color.darkYellow,
    @ColorRes var sortingUncheckedTextColor: Int = R.color.blackTrans50,
    @DrawableRes var placeHolder: Int = R.drawable.ic_image_placeholder,
    @DrawableRes var errorDrawable: Int = R.drawable.ic_image_placeholder,
    @DrawableRes var selectionDrawable: Int = R.drawable.ic_checked_media,
    var selectedMedias: ArrayList<MiMedia> = ArrayList(),
    var mediaType: MediaType = MediaType.IMAGE,
    var maxCount: Int = KeyUtils.DEFAULT_MEDIA_COUNT,
    var ascFlag: Int = KeyUtils.DEFAULT_ORDER,
    var gridSize: Int = KeyUtils.DEFAULT_GRID_SIZE,
    var lassiOption: LassiOption = LassiOption.PICKER,
    var minTime: Long = KeyUtils.DEFAULT_DURATION,
    var maxTime: Long = KeyUtils.DEFAULT_DURATION,
    var cropType: CropImageView.CropShape = CropImageView.CropShape.RECTANGLE,
    var supportedFileType: MutableList<String> = mutableListOf(),
    var cropAspectRatio: AspectRatio? = AspectRatio.of(1, 1),
    var enableFlipImage: Boolean = false,
    var enableRotateImage: Boolean = false,
    var enableActualCircleCrop: Boolean = false,
    var compressionRatio: Int = 0,
    var minFileSize: Long = KeyUtils.DEFAULT_FILE_SIZE,
    var maxFileSize: Long = KeyUtils.DEFAULT_FILE_SIZE,
    var isCrop: Boolean = true,
    var alertDialogNegativeButtonColor: Int = Color.BLACK,
    var alertDialogPositiveButtonColor: Int = Color.BLACK,
    var customLimitExceedingErrorMessage: String = ERROR_EXCEEDING_MSG,
    var isMultiPicker : Boolean = false
) : Parcelable {
    companion object {

        private var mediaPickerConfig = LassiConfig()

        fun setConfig(lassiConfig: LassiConfig) {
            this.mediaPickerConfig.apply {
                toolbarColor = lassiConfig.toolbarColor
                statusBarColor = lassiConfig.statusBarColor
                toolbarResourceColor = lassiConfig.toolbarResourceColor
                progressBarColor = lassiConfig.progressBarColor
                galleryBackgroundColor = lassiConfig.galleryBackgroundColor
                sortingCheckedRadioButtonColor = lassiConfig.sortingCheckedRadioButtonColor
                sortingUncheckedRadioButtonColor = lassiConfig.sortingUncheckedRadioButtonColor
                sortingCheckedTextColor = lassiConfig.sortingCheckedTextColor
                sortingUncheckedTextColor = lassiConfig.sortingUncheckedTextColor
                selectedMedias = lassiConfig.selectedMedias
                mediaType = lassiConfig.mediaType
                maxCount = lassiConfig.maxCount
                ascFlag = lassiConfig.ascFlag
                gridSize = lassiConfig.gridSize
                lassiOption = lassiConfig.lassiOption
                minTime = lassiConfig.minTime
                maxTime = lassiConfig.maxTime
                placeHolder = lassiConfig.placeHolder
                errorDrawable = lassiConfig.errorDrawable
                selectionDrawable = lassiConfig.selectionDrawable
                cropType = lassiConfig.cropType
                supportedFileType = lassiConfig.supportedFileType
                cropAspectRatio = lassiConfig.cropAspectRatio
                enableFlipImage = lassiConfig.enableFlipImage
                enableRotateImage = lassiConfig.enableRotateImage
                enableActualCircleCrop = lassiConfig.enableActualCircleCrop
                compressionRatio = lassiConfig.compressionRatio
                minFileSize = lassiConfig.minFileSize
                maxFileSize = lassiConfig.maxFileSize
                isCrop = lassiConfig.isCrop
                alertDialogNegativeButtonColor = lassiConfig.alertDialogNegativeButtonColor
                alertDialogPositiveButtonColor = lassiConfig.alertDialogPositiveButtonColor
                customLimitExceedingErrorMessage = lassiConfig.customLimitExceedingErrorMessage
                isMultiPicker = lassiConfig.isMultiPicker
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
