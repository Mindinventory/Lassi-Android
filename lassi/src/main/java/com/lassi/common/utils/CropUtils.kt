package com.lassi.common.utils

import android.net.Uri
import androidx.fragment.app.FragmentActivity
import com.lassi.domain.media.LassiConfig
import com.lassi.presentation.cropper.CropImage
import com.lassi.presentation.cropper.CropImageView

object CropUtils {
    fun beginCrop(activity: FragmentActivity, source: Uri) {
        with(LassiConfig.getConfig()) {
            CropImage.activity(source)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAllowFlipping(false)
                .setAllowRotation(false)
                .setOutputCompressQuality(70)
                .setCropShape(cropType)
                .setAspectRatio(cropAspectRatio)
                .setOutputUri(source)
                .setAllowRotation(enableRotateImage)
                .setAllowFlipping(enableFlipImage)
                .start(activity)
        }
    }
}