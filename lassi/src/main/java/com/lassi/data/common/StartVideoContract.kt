package com.lassi.data.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.lassi.common.utils.KeyUtils
import com.lassi.data.media.MiMedia
import com.lassi.presentation.videopreview.VideoPreviewActivity

class StartVideoContract : ActivityResultContract<String?, MiMedia?>() {
    override fun createIntent(context: Context, videoPath: String?): Intent {
        val intent = Intent(context, VideoPreviewActivity::class.java)
        intent.putExtra(KeyUtils.VIDEO_PATH, videoPath)
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): MiMedia? {
        if (resultCode != Activity.RESULT_OK) return null
        return intent?.getParcelableExtra<MiMedia>(KeyUtils.MEDIA_PREVIEW)
    }
}