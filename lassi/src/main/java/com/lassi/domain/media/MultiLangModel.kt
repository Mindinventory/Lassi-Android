package com.lassi.domain.media

import android.content.Context
import com.lassi.R
import com.lassi.domain.media.MultiLangModel.Common.cancel
import com.lassi.domain.media.MultiLangModel.Common.lassiAll
import com.lassi.domain.media.MultiLangModel.Common.ok
import com.lassi.domain.media.MultiLangModel.CropImage.cropImageActivityNoPermissions
import com.lassi.domain.media.MultiLangModel.CropImage.cropImageActivityTitle
import com.lassi.domain.media.MultiLangModel.CropImage.cropImageMenuFlip
import com.lassi.domain.media.MultiLangModel.CropImage.cropImageMenuFlipHorizontally
import com.lassi.domain.media.MultiLangModel.CropImage.cropImageMenuFlipVertically
import com.lassi.domain.media.MultiLangModel.CropImage.cropImageMenuRotateLeft
import com.lassi.domain.media.MultiLangModel.CropImage.cropImageMenuRotateRight
import com.lassi.domain.media.MultiLangModel.CropImage.pickImageIntentChooserTitle
import com.lassi.domain.media.MultiLangModel.ErrorOrAlertMessage.alreadySelectedMaxItems
import com.lassi.domain.media.MultiLangModel.ErrorOrAlertMessage.defaultExceedErrorMsg
import com.lassi.domain.media.MultiLangModel.ErrorOrAlertMessage.errorExceedMsg
import com.lassi.domain.media.MultiLangModel.ErrorOrAlertMessage.noDataFound
import com.lassi.domain.media.MultiLangModel.ImageActions.cropImageMenuCrop
import com.lassi.domain.media.MultiLangModel.ImageActions.icFlip24
import com.lassi.domain.media.MultiLangModel.ImageActions.icFlip24Horizontally
import com.lassi.domain.media.MultiLangModel.ImageActions.icFlip24Vertically
import com.lassi.domain.media.MultiLangModel.ImageActions.icRotateLeft24
import com.lassi.domain.media.MultiLangModel.ImageActions.icRotateRight24
import com.lassi.domain.media.MultiLangModel.ImageActions.mainActionCrop
import com.lassi.domain.media.MultiLangModel.ImageActions.mainActionRotate
import com.lassi.domain.media.MultiLangModel.ImageActions.pickImageCamera
import com.lassi.domain.media.MultiLangModel.ImageActions.pickImageChooserTitle
import com.lassi.domain.media.MultiLangModel.ImageActions.pickImageGallery
import com.lassi.domain.media.MultiLangModel.MediaPermission.cameraAudioPermissionRational
import com.lassi.domain.media.MultiLangModel.MediaPermission.cameraAudioStoragePermissionRational
import com.lassi.domain.media.MultiLangModel.MediaPermission.cameraPermissionRational
import com.lassi.domain.media.MultiLangModel.MediaPermission.cameraStoragePermissionRational
import com.lassi.domain.media.MultiLangModel.MediaPermission.readMediaAudioPermissionRational
import com.lassi.domain.media.MultiLangModel.MediaPermission.readMediaImagesVideoPermissionRational
import com.lassi.domain.media.MultiLangModel.MediaPermission.storagePermissionRational
import com.lassi.domain.media.MultiLangModel.MediaPickerMenu.camera
import com.lassi.domain.media.MultiLangModel.MediaPickerMenu.done
import com.lassi.domain.media.MultiLangModel.MediaPickerMenu.sort
import com.lassi.domain.media.MultiLangModel.Sorting.sortAscending
import com.lassi.domain.media.MultiLangModel.Sorting.sortByDate
import com.lassi.domain.media.MultiLangModel.Sorting.sortDescending

object MultiLangModel {
    object Common {
        var ok: String = ""
        var cancel: String = ""
        var lassiAll: String = ""

        fun setOk(value: String) {
            ok = value
        }
        fun setCancel(value: String) {
            cancel = value
        }
        fun setLassiAll(value: String) {
            lassiAll = value
        }
    }
    object CropImage {
        var cropImageMenuRotateLeft: String = ""
        var cropImageMenuRotateRight: String = ""
        var cropImageMenuFlip: String = ""
        var cropImageMenuFlipHorizontally: String = ""
        var cropImageMenuFlipVertically: String = ""
        var pickImageIntentChooserTitle: String = ""
        var cropImageActivityNoPermissions: String = ""
        var cropImageActivityTitle: String = ""
        fun setCropImageMenuRotateLeft(value: String) {
            cropImageMenuRotateLeft = value
        }
        fun setCropImageMenuRotateRight(value: String) {
            cropImageMenuRotateRight = value
        }
        fun setCropImageMenuFlip(value: String) {
            cropImageMenuFlip = value
        }
        fun setCropImageMenuFlipHorizontally(value: String) {
            cropImageMenuFlipHorizontally = value
        }
        fun setCropImageMenuFlipVertically(value: String) {
            cropImageMenuFlipVertically = value
        }
        fun setPickImageIntentChooserTitle(value: String) {
            pickImageIntentChooserTitle = value
        }
        fun setCropImageActivityNoPermissions(value: String) {
            cropImageActivityNoPermissions = value
        }
        fun setCropImageActivityTitle(value: String) {
            cropImageActivityTitle = value
        }
    }
    object MediaPickerMenu {
        var camera: String = ""
        var sort: String = ""
        var done: String = ""

        fun setCamera(value: String) {
            camera = value
        }
        fun setSort(value: String) {
            sort = value
        }
        fun setDone(value: String) {
            done = value
        }
    }
    object MediaPermission {
        var cameraAudioStoragePermissionRational: String = ""
        var cameraStoragePermissionRational: String = ""
        var cameraAudioPermissionRational: String = ""
        var cameraPermissionRational: String = ""
        var storagePermissionRational: String = ""
        var readMediaImagesVideoPermissionRational: String = ""
        var readMediaAudioPermissionRational: String = ""

        fun setCameraAudioStoragePermissionRational(value: String) {
            cameraAudioStoragePermissionRational = value
        }
        fun setCameraStoragePermissionRational(value: String) {
            cameraStoragePermissionRational = value
        }
        fun setCameraAudioPermissionRational(value: String) {
            cameraAudioPermissionRational = value
        }
        fun setCameraPermissionRational(value: String) {
            cameraPermissionRational = value
        }
        fun setStoragePermissionRational(value: String) {
            storagePermissionRational = value
        }
        fun setReadMediaImagesVideoPermissionRational(value: String) {
            readMediaImagesVideoPermissionRational = value
        }
        fun setReadMediaAudioPermissionRational(value: String) {
            readMediaAudioPermissionRational = value
        }
    }
    object ErrorOrAlertMessage {
        var alreadySelectedMaxItems: String = ""
        var errorExceedMsg: String = ""
        var defaultExceedErrorMsg: String = ""
        var noDataFound: String = ""

        fun setAlreadySelectedMaxItems(value: String) {
            alreadySelectedMaxItems = value
        }
        fun setErrorExceedMsg(value: String) {
            errorExceedMsg = value
        }
        fun setDefaultExceedErrorMsg(value: String) {
            defaultExceedErrorMsg = value
        }
        fun setNoDataFound(value: String) {
            noDataFound = value
        }
    }
    object Sorting {
        var sortByDate: String = ""
        var sortAscending: String = ""
        var sortDescending: String = ""

        fun setSortByDate(value: String) {
            sortByDate = value
        }
        fun setSortAscending(value: String) {
            sortAscending = value
        }
        fun setSortDescending(value: String) {
            sortDescending = value
        }
    }
    object ImageActions {
        var icRotateLeft24: String = ""
        var icRotateRight24: String = ""
        var cropImageMenuCrop: String = ""
        var icFlip24: String = ""
        var icFlip24Horizontally: String = ""
        var icFlip24Vertically: String = ""
        var pickImageChooserTitle: String = ""
        var pickImageCamera: String = ""
        var pickImageGallery: String = ""
        var mainActionRotate: String = ""
        var mainActionCrop: String = ""

        fun setIcRotateLeft24(value: String) {
            icRotateLeft24 = value
        }
        fun setIcRotateRight24(value: String) {
            icRotateRight24 = value
        }
        fun setCropImageMenuCrop(value: String) {
            cropImageMenuCrop = value
        }
        fun setIcFlip24(value: String) {
            icFlip24 = value
        }
        fun setIcFlip24Horizontally(value: String) {
            icFlip24Horizontally = value
        }
        fun setIcFlip24Vertically(value: String) {
            icFlip24Vertically = value
        }
        fun setPickImageChooserTitle(value: String) {
            pickImageChooserTitle = value
        }
        fun setPickImageCamera(value: String) {
            pickImageCamera = value
        }
        fun setPickImageGallery(value: String) {
            pickImageGallery = value
        }
        fun setMainActionRotate(value: String) {
            mainActionRotate = value
        }
        fun setMainActionCrop(value: String) {
            mainActionCrop = value
        }
    }

    //Setting data initially
    fun initializeDefaultValues(context: Context) {
        ok = context.getString(R.string.ok)
        cancel = context.getString(R.string.cancel)
        lassiAll = context.getString(R.string.lassi_all)

        cropImageMenuRotateLeft = context.getString(R.string.crop_image_menu_rotate_left)
        cropImageMenuRotateRight = context.getString(R.string.crop_image_menu_rotate_right)
        cropImageMenuFlip = context.getString(R.string.crop_image_menu_flip)
        cropImageMenuFlipHorizontally = context.getString(R.string.crop_image_menu_flip_horizontally)
        cropImageMenuFlipVertically = context.getString(R.string.crop_image_menu_flip_vertically)
        pickImageIntentChooserTitle = context.getString(R.string.pick_image_intent_chooser_title)
        cropImageActivityNoPermissions = context.getString(R.string.crop_image_activity_no_permissions)
        cropImageActivityTitle = context.getString(R.string.crop_image_activity_title)

        camera = context.getString(R.string.camera)
        sort = context.getString(R.string.sort)
        done = context.getString(R.string.done)

        cameraAudioStoragePermissionRational = context.getString(R.string.camera_audio_storage_permission_rational)
        cameraStoragePermissionRational = context.getString(R.string.camera_storage_permission_rational)
        cameraAudioPermissionRational = context.getString(R.string.camera_audio_permission_rational)
        cameraPermissionRational = context.getString(R.string.camera_permission_rational)
        storagePermissionRational = context.getString(R.string.storage_permission_rational)
        readMediaImagesVideoPermissionRational = context.getString(R.string.read_media_images_video_permission_rational)
        readMediaAudioPermissionRational = context.getString(R.string.read_media_audio_permission_rational)

        alreadySelectedMaxItems = context.getString(R.string.already_selected_max_items)
        errorExceedMsg = context.getString(R.string.error_exceed_msg)
        defaultExceedErrorMsg = context.getString(R.string.default_exceed_error_msg)
        noDataFound = context.getString(R.string.no_data_found)

        sortByDate = context.getString(R.string.sort_by_date)
        sortAscending = context.getString(R.string.sort_ascending)
        sortDescending = context.getString(R.string.sort_descending)

        icRotateLeft24 = context.getString(R.string.ic_rotate_left_24)
        icRotateRight24 = context.getString(R.string.ic_rotate_right_24)
        cropImageMenuCrop = context.getString(R.string.crop_image_menu_crop)
        icFlip24 = context.getString(R.string.ic_flip_24)
        icFlip24Horizontally = context.getString(R.string.ic_flip_24_horizontally)
        icFlip24Vertically = context.getString(R.string.ic_flip_24_vertically)
        pickImageChooserTitle = context.getString(R.string.pick_image_chooser_title)
        pickImageCamera = context.getString(R.string.pick_image_camera)
        pickImageGallery = context.getString(R.string.pick_image_gallery)
        mainActionRotate = context.getString(R.string.main_action_rotate)
        mainActionCrop = context.getString(R.string.main_action_crop)
    }
}


/*
data class MultiLangModel(
    val ok: String,
    val cancel: String,
    val selectedItems: String,
    val directoryWithItemCount: String,
    val lassiAll: String,
    val cropImageMenuRotateLeft: String,
    val cropImageMenuRotateRight: String,
    val cropImageMenuFlip: String,
    val cropImageMenuFlipHorizontally: String,
    val cropImageMenuFlipVertically: String,
    val pickImageIntentChooserTitle: String,
    val cropImageActivityNoPermissions: String,
    val cropImageActivityTitle: String,
    val camera: String,
    val sort: String,
    val done: String,
    val alreadySelectedMaxItems: String,
    val cameraAudioStoragePermissionRational: String,
    val cameraStoragePermissionRational: String,
    val cameraAudioPermissionRational: String,
    val cameraPermissionRational: String,
    val storagePermissionRational: String,
    val readMediaImagesVideoPermissionRational: String,
    val readMediaAudioPermissionRational: String,
//    val minVideoRecordingTimeError: String,
    val errorExceedMsg: String,
    val defaultExceedErrorMsg: String,
    val icRotateLeft24: String,
    val icRotateRight24: String,
    val cropImageMenuCrop: String,
    val icFlip24: String,
    val icFlip24Horizontally: String,
    val icFlip24Vertically: String,
    val pickImageChooserTitle: String,
    val pickImageCamera: String,
    val pickImageGallery: String,
    val mainActionRotate: String,
    val mainActionCrop: String,
    val noDataFound: String,
    val sortByDate: String,
    val sortAscending: String,
    val sortDescending: String,
//    val sortingOptions: Array<String>
)*/
