package com.lassi.data.media.entity

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SelectedMediaModel(
    @SerializedName("mediaId")
    var mediaId: Long,
    @SerializedName("mediaName")
    var mediaName: String,
    @SerializedName("mediaPath")
    var mediaPath: String,
    @SerializedName("mediaSize")
    var mediaSize: Long,
    @SerializedName("mediaDuration")
    var mediaDuration: Long,
    @SerializedName("mediaAlbumCoverPath")
    var mediaAlbumCoverPath: String,
) : Parcelable
