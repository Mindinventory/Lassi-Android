package com.lassi.data.media

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MiItemMedia(
    var bucketName: String? = null,
    var latestItemPathForBucket: String? = null,
    var totalItemSizeForBucket: Long = 0L
) : Parcelable
