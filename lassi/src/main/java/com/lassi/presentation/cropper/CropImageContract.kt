package com.lassi.presentation.cropper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import com.lassi.common.utils.KeyUtils
import com.lassi.data.media.MiMedia

/**
 * An [ActivityResultContract] to start an activity that allows the user to crop an image.
 * The UI can be customized using [CropImageOptions].
 * If you do not provide an [CropImageContractOptions.uri] in the input the user will be asked to pick an image before cropping.
 */
class CropImageContract : ActivityResultContract<CropImageContractOptions, MiMedia?>() {
    override fun createIntent(context: Context, input: CropImageContractOptions) =
        Intent(context, CropImageActivity::class.java).apply {
            putExtra(
                CropImage.CROP_IMAGE_EXTRA_BUNDLE,
                Bundle(2).apply {
                    putParcelable(CropImage.CROP_IMAGE_EXTRA_SOURCE, input.uri)
                    putParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS, input.cropImageOptions)
                },
            )
        }

    override fun parseResult(resultCode: Int, intent: Intent?): MiMedia? {
        if (resultCode != Activity.RESULT_OK) return null
        return intent?.getParcelableExtra<MiMedia>(KeyUtils.MEDIA_PREVIEW)
    }
}
