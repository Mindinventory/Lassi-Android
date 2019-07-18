package com.lassi.domain.media

import com.lassi.data.media.MiMedia
import com.lassi.data.mediadirectory.Folder
import io.reactivex.Single

interface MediaRepository {
    fun fetchFolders(): Single<ArrayList<Folder>>
    fun fetchDocs(): Single<ArrayList<MiMedia>>
}