package com.lassi.presentation.builder

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.lassi.R
import com.lassi.common.utils.KeyUtils
import com.lassi.domain.media.LassiConfig
import com.lassi.domain.media.LassiOption
import com.lassi.domain.media.MediaType
import com.lassi.domain.media.MultiLangConfig
import com.lassi.domain.media.SortingOption
import com.lassi.presentation.cameraview.controls.AspectRatio
import com.lassi.presentation.cropper.CropImageView
import com.lassi.presentation.mediadirectory.LassiMediaPickerActivity

class Lassi(private val context: Context) {

    private var multiLangConfig = MultiLangConfig()
    private var lassiConfig = LassiConfig()

    init {
        getMultiLngBuilder()
    }

    /**
     * Multi-language support
     */
    fun getMultiLngBuilder(
        setOkLbl: String? = null,
        setCancelLbl: String? = null,
        setLassiAllLbl: String? = null,
        setCropImageMenuRotateLeftLbl: String? = null,
        setCropImageMenuRotateRightLbl: String? = null,
        setCropImageMenuFlipLbl: String? = null,
        setCropImageMenuFlipHorizontallyLbl: String? = null,
        setCropImageMenuFlipVerticallyLbl: String? = null,
        setPickImageIntentChooserTitleLbl: String? = null,
        setCropImageActivityNoPermissionsLbl: String? = null,
        setCropImageActivityTitleLbl: String? = null,
        setCameraLbl: String? = null,
        setSortLbl: String? = null,
        setDoneLbl: String? = null,
        setCameraAudioStoragePermissionRationalLbl: String? = null,
        setCameraStoragePermissionRationalLbl: String? = null,
        setCameraAudioPermissionRationalLbl: String? = null,
        setCameraPermissionRationalLbl: String? = null,
        setStoragePermissionRationalLbl: String? = null,
        setReadMediaImagesVideoPermissionRationalLbl: String? = null,
        setReadMediaAudioPermissionRationalLbl: String? = null,
        setAlreadySelectedMaxItemsLbl: String? = null,
        setErrorExceedMsgLbl: String? = null,
        setDefaultExceedErrorMsgLbl: String? = null,
        setNoDataFoundLbl: String? = null,
        setSortByDateLbl: String? = null,
        setSortAscendingLbl: String? = null,
        setSortDescendingLbl: String? = null,
        setIcRotateLeft24Lbl: String? = null,
        setIcRotateRight24Lbl: String? = null,
        setCropImageMenuCropLbl: String? = null,
        setIcFlip24Lbl: String? = null,
        setIcFlip24HorizontallyLbl: String? = null,
        setIcFlip24VerticallyLbl: String? = null,
        setPickImageChooserTitleLbl: String? = null,
        setPickImageCameraLbl: String? = null,
        setPickImageGalleryLbl: String? = null,
        setMainActionRotateLbl: String? = null,
        setMainActionCropLbl: String? = null,
    ): Lassi {
        multiLangConfig.ok = setOkLbl ?: context.getString(R.string.ok)
        multiLangConfig.cancel = setCancelLbl ?: context.getString(R.string.cancel)
        multiLangConfig.lassiAll = setLassiAllLbl ?: context.getString(R.string.lassi_all)
        multiLangConfig.cropImageMenuRotateLeft =
            setCropImageMenuRotateLeftLbl ?: context.getString(R.string.crop_image_menu_rotate_left)
        multiLangConfig.cropImageMenuRotateRight = setCropImageMenuRotateRightLbl
            ?: context.getString(R.string.crop_image_menu_rotate_right)
        multiLangConfig.cropImageMenuFlip =
            setCropImageMenuFlipLbl ?: context.getString(R.string.crop_image_menu_flip)
        multiLangConfig.cropImageMenuFlipHorizontally = setCropImageMenuFlipHorizontallyLbl
            ?: context.getString(R.string.crop_image_menu_flip_horizontally)
        multiLangConfig.cropImageMenuFlipVertically = setCropImageMenuFlipVerticallyLbl
            ?: context.getString(R.string.crop_image_menu_flip_vertically)
        multiLangConfig.pickImageIntentChooserTitle = setPickImageIntentChooserTitleLbl
            ?: context.getString(R.string.pick_image_intent_chooser_title)
        multiLangConfig.cropImageActivityNoPermissions = setCropImageActivityNoPermissionsLbl
            ?: context.getString(R.string.crop_image_activity_no_permissions)
        multiLangConfig.cropImageActivityTitle =
            setCropImageActivityTitleLbl ?: context.getString(R.string.crop_image_activity_title)
        multiLangConfig.camera = setCameraLbl ?: context.getString(R.string.camera)
        multiLangConfig.sort = setSortLbl ?: context.getString(R.string.sort)
        multiLangConfig.done = setDoneLbl ?: context.getString(R.string.done)
        multiLangConfig.cameraAudioStoragePermissionRational =
            setCameraAudioStoragePermissionRationalLbl
                ?: context.getString(R.string.camera_audio_storage_permission_rational)
        multiLangConfig.cameraStoragePermissionRational = setCameraStoragePermissionRationalLbl
            ?: context.getString(R.string.camera_storage_permission_rational)
        multiLangConfig.cameraAudioPermissionRational = setCameraAudioPermissionRationalLbl
            ?: context.getString(R.string.camera_audio_permission_rational)
        multiLangConfig.cameraPermissionRational =
            setCameraPermissionRationalLbl ?: context.getString(R.string.camera_permission_rational)
        multiLangConfig.storagePermissionRational = setStoragePermissionRationalLbl
            ?: context.getString(R.string.storage_permission_rational)
        multiLangConfig.readMediaImagesVideoPermissionRational =
            setReadMediaImagesVideoPermissionRationalLbl
                ?: context.getString(R.string.read_media_images_video_permission_rational)
        multiLangConfig.readMediaAudioPermissionRational = setReadMediaAudioPermissionRationalLbl
            ?: context.getString(R.string.read_media_audio_permission_rational)
        multiLangConfig.alreadySelectedMaxItems =
            setAlreadySelectedMaxItemsLbl ?: context.getString(R.string.already_selected_max_items)
        multiLangConfig.errorExceedMsg =
            setErrorExceedMsgLbl ?: context.getString(R.string.error_exceed_msg)
        multiLangConfig.defaultExceedErrorMsg =
            setDefaultExceedErrorMsgLbl ?: context.getString(R.string.default_exceed_error_msg)
        multiLangConfig.noDataFound = setNoDataFoundLbl ?: context.getString(R.string.no_data_found)
        multiLangConfig.sortByDate = setSortByDateLbl ?: context.getString(R.string.sort_by_date)
        multiLangConfig.sortAscending =
            setSortAscendingLbl ?: context.getString(R.string.sort_ascending)
        multiLangConfig.sortDescending =
            setSortDescendingLbl ?: context.getString(R.string.sort_descending)
        multiLangConfig.icRotateLeft24 =
            setIcRotateLeft24Lbl ?: context.getString(R.string.ic_rotate_left_24)
        multiLangConfig.icRotateRight24 =
            setIcRotateRight24Lbl ?: context.getString(R.string.ic_rotate_right_24)
        multiLangConfig.cropImageMenuCrop =
            setCropImageMenuCropLbl ?: context.getString(R.string.crop_image_menu_crop)
        multiLangConfig.icFlip24 = setIcFlip24Lbl ?: context.getString(R.string.ic_flip_24)
        multiLangConfig.icFlip24Horizontally =
            setIcFlip24HorizontallyLbl ?: context.getString(R.string.ic_flip_24_horizontally)
        multiLangConfig.icFlip24Vertically =
            setIcFlip24VerticallyLbl ?: context.getString(R.string.ic_flip_24_vertically)
        multiLangConfig.pickImageChooserTitle =
            setPickImageChooserTitleLbl ?: context.getString(R.string.pick_image_chooser_title)
        multiLangConfig.pickImageCamera =
            setPickImageCameraLbl ?: context.getString(R.string.pick_image_camera)
        multiLangConfig.pickImageGallery =
            setPickImageGalleryLbl ?: context.getString(R.string.pick_image_gallery)
        multiLangConfig.mainActionRotate =
            setMainActionRotateLbl ?: context.getString(R.string.main_action_rotate)
        multiLangConfig.mainActionCrop =
            setMainActionCropLbl ?: context.getString(R.string.main_action_crop)

        MultiLangConfig.setMultiLangConfig(multiLangConfig)
        return this
    }

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
     * Set sorting checked state radio button color resource
     */
    @SuppressLint("ResourceAsColor")
    fun setSortingCheckedRadioButtonColor(@ColorRes color: Int): Lassi {
        lassiConfig.sortingCheckedRadioButtonColor = color
        return this
    }

    /**
     * Set sorting unchecked state radio button color resource
     */
    @SuppressLint("ResourceAsColor")
    fun setSortingUncheckedRadioButtonColor(@ColorRes color: Int): Lassi {
        lassiConfig.sortingUncheckedRadioButtonColor = color
        return this
    }

    /**
     * Set sorting checked state radio button color resource
     */
    @SuppressLint("ResourceAsColor")
    fun setSortingCheckedTextColor(@ColorRes color: Int): Lassi {
        lassiConfig.sortingCheckedTextColor = color
        return this
    }

    /**
     * Set sorting unchecked state radio button color resource
     */
    @SuppressLint("ResourceAsColor")
    fun setSortingUncheckedTextColor(@ColorRes color: Int): Lassi {
        lassiConfig.sortingUncheckedTextColor = color
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

    fun enableMultiSelection(): Lassi {
        lassiConfig.isMultiPicker = true
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
        lassiConfig.compressionRatio = compression
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
    fun setCustomLimitExceedingErrorMessage(errorMessage: String): Lassi {
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
