package com.lassi.domain.media

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MultiLangConfig(
    var ok: String = "",
    var cancel: String = "",
    var lassiAll: String = "",
    var cropImageMenuRotateLeft: String = "",
    var cropImageMenuRotateRight: String = "",
    var cropImageMenuFlip: String = "",
    var cropImageMenuFlipHorizontally: String = "",
    var cropImageMenuFlipVertically: String = "",
    var pickImageIntentChooserTitle: String = "",
    var cropImageActivityNoPermissions: String = "",
    var cropImageActivityTitle: String = "",
    var camera: String = "",
    var sort: String = "",
    var done: String = "",
    var cameraAudioStoragePermissionRational: String = "",
    var cameraStoragePermissionRational: String = "",
    var cameraAudioPermissionRational: String = "",
    var cameraPermissionRational: String = "",
    var storagePermissionRational: String = "",
    var readMediaImagesVideoPermissionRational: String = "",
    var readMediaAudioPermissionRational: String = "",
    var alreadySelectedMaxItems: String = "",
    var errorExceedMsg: String = "",
    var defaultExceedErrorMsg: String = "",
    var noDataFound: String = "",
    var sortByDate: String = "",
    var sortAscending: String = "",
    var sortDescending: String = "",
    var icRotateLeft24: String = "",
    var icRotateRight24: String = "",
    var cropImageMenuCrop: String = "",
    var icFlip24: String = "",
    var icFlip24Horizontally: String = "",
    var icFlip24Vertically: String = "",
    var pickImageChooserTitle: String = "",
    var pickImageCamera: String = "",
    var pickImageGallery: String = "",
    var mainActionRotate: String = "",
    var mainActionCrop: String = ""
) : Parcelable {
    companion object {
        private var multiLangConfig = MultiLangConfig()

        fun setMultiLangConfig(config: MultiLangConfig) {
            multiLangConfig.apply {
                ok = config.ok
                cancel = config.cancel
                lassiAll = config.lassiAll
                cropImageMenuRotateLeft = config.cropImageMenuRotateLeft
                cropImageMenuRotateRight = config.cropImageMenuRotateRight
                cropImageMenuFlip = config.cropImageMenuFlip
                cropImageMenuFlipHorizontally = config.cropImageMenuFlipHorizontally
                cropImageMenuFlipVertically = config.cropImageMenuFlipVertically
                pickImageIntentChooserTitle = config.pickImageIntentChooserTitle
                cropImageActivityNoPermissions = config.cropImageActivityNoPermissions
                cropImageActivityTitle = config.cropImageActivityTitle
                camera = config.camera
                sort = config.sort
                done = config.done
                cameraAudioStoragePermissionRational = config.cameraAudioStoragePermissionRational
                cameraStoragePermissionRational = config.cameraStoragePermissionRational
                cameraAudioPermissionRational = config.cameraAudioPermissionRational
                cameraPermissionRational = config.cameraPermissionRational
                storagePermissionRational = config.storagePermissionRational
                readMediaImagesVideoPermissionRational =
                    config.readMediaImagesVideoPermissionRational
                readMediaAudioPermissionRational = config.readMediaAudioPermissionRational
                alreadySelectedMaxItems = config.alreadySelectedMaxItems
                errorExceedMsg = config.errorExceedMsg
                defaultExceedErrorMsg = config.defaultExceedErrorMsg
                noDataFound = config.noDataFound
                sortByDate = config.sortByDate
                sortAscending = config.sortAscending
                sortDescending = config.sortDescending
                icRotateLeft24 = config.icRotateLeft24
                icRotateRight24 = config.icRotateRight24
                cropImageMenuCrop = config.cropImageMenuCrop
                icFlip24 = config.icFlip24
                icFlip24Horizontally = config.icFlip24Horizontally
                icFlip24Vertically = config.icFlip24Vertically
                pickImageChooserTitle = config.pickImageChooserTitle
                pickImageCamera = config.pickImageCamera
                pickImageGallery = config.pickImageGallery
                mainActionRotate = config.mainActionRotate
                mainActionCrop = config.mainActionCrop
            }
        }

        fun getConfig(): MultiLangConfig {
            return multiLangConfig
        }
    }
}
