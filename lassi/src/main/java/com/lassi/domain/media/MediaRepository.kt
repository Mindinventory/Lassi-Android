package com.lassi.domain.media

import com.lassi.data.common.Result
import com.lassi.data.media.MiMedia
import com.lassi.data.mediadirectory.Folder
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    suspend fun fetchFolders(): Flow<Result<ArrayList<Folder>>>
    suspend fun fetchDocs(): Flow<Result<ArrayList<MiMedia>>>
}