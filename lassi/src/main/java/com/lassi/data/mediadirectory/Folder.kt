package com.lassi.data.mediadirectory

import android.os.Parcelable
import com.lassi.data.media.MiMedia
import kotlinx.parcelize.Parcelize

@Parcelize
data class Folder(var folderName: String?, var medias: ArrayList<MiMedia> = ArrayList()) :
    Parcelable
