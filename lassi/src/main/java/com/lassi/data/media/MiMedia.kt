package com.lassi.data.media

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MiMedia(
    var id: Long = 0,
    var name: String? = null,
    var path: String? = null,
    var duration: Long = 0L,
    var thumb: String? = null,
    var fileSize: Long = 0L,
    var doesUri:Boolean = false
) : Parcelable
