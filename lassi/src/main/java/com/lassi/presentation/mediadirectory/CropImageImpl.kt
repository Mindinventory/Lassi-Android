package com.lassi.presentation.mediadirectory

import android.net.Uri
import com.lassi.presentation.cropper.CropImageActivity
import com.lassi.presentation.cropper.CropImageView

abstract class CropImageImpl: CropImageActivity() {
    override fun onSetImageUriComplete(view: CropImageView, uri: Uri, error: Exception?) {
        super.onSetImageUriComplete(view, uri, error)
    }

    override fun onCropImageComplete(view: CropImageView, result: CropImageView.CropResult) {
        super.onCropImageComplete(view, result)
    }

}