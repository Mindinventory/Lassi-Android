package com.lassi.domain.media

import com.lassi.data.common.Result
import com.lassi.data.media.MiMedia
import kotlinx.coroutines.flow.Flow

interface SelectedMediaRepository {
    suspend fun getSelectedMediaData(bucket: String): Flow<Result<ArrayList<MiMedia>>>
    suspend fun getSortedDataFromDb(
        bucket: String,
        isAsc: Int,
        mediaType: MediaType
    ): Flow<Result<ArrayList<MiMedia>>>
}