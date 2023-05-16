package com.lassi.data.mediadirectory

import android.os.Parcelable
import com.lassi.data.media.MiMedia
import kotlinx.parcelize.Parcelize

@Parcelize
data class FolderBucket(
    var bucketName: String?,
    var totalItems: ArrayList<MiMedia> = ArrayList(),
    var lastImagePath: String?
) : Parcelable
