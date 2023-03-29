package com.lassi.domain.media

import com.lassi.data.common.Result
import com.lassi.data.media.MiItemMedia
import com.lassi.data.media.MiMedia
import com.lassi.data.media.entity.MediaFileEntity
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    suspend fun getDataFromDb(): Flow<Result<ArrayList<MiItemMedia>>>
    suspend fun getAllImgVidMediaFile(): Flow<Result<ArrayList<MediaFileEntity>>>
    suspend fun fetchDocs(): Flow<Result<ArrayList<MiMedia>>>
    suspend fun isDbEmpty(): Boolean
    suspend fun insertMediaData(): Result<Boolean>
    suspend fun insertAllMediaData(): Result<Boolean>
    suspend fun removeMediaData(allDataList: List<MediaFileEntity>?)
}