package com.lassi.common.utils

import android.net.Uri
import androidx.fragment.app.FragmentActivity
import com.lassi.domain.media.LassiConfig
import com.lassi.presentation.cropper.CropImage
import com.lassi.presentation.cropper.CropImageView

object CropUtils {
    fun beginCrop(activity: FragmentActivity, source: Uri) {
        val lassiConfig = LassiConfig.getConfig()
        CropImage.activity(source)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAllowFlipping(false)
            .setAllowRotation(false)
            .setOutputCompressQuality(70)
            .setCropShape(lassiConfig.cropType)
            .setAspectRatio(lassiConfig.cropAspectRatio)
            .setOutputUri(source)
            .start(activity)
    }
}