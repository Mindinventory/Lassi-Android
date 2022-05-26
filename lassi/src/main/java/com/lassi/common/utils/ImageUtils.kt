package com.lassi.common.utils

import com.lassi.data.media.MiMedia
import com.lassi.domain.media.LassiConfig
import com.lassi.domain.media.MediaType

object ImageUtils {
    fun getThumb(miMedia: MiMedia): String? {
        return if (LassiConfig.getConfig().mediaType == MediaType.AUDIO) {
            miMedia.thumb
        } else {
            miMedia.path
        }
    }
}
