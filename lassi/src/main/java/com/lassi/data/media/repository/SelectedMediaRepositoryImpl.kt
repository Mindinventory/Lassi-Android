package com.lassi.data.media.repository

import android.content.Context
import com.lassi.common.extenstions.catch
import com.lassi.common.utils.Logger
import com.lassi.data.common.Result
import com.lassi.data.database.MediaFileDatabase
import com.lassi.data.media.MiMedia
import com.lassi.data.media.entity.MediaFileEntity
import com.lassi.data.media.entity.SelectedMediaModel
import com.lassi.domain.media.LassiConfig
import com.lassi.domain.media.MediaType
import com.lassi.domain.media.SelectedMediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class SelectedMediaRepositoryImpl(private val context: Context) : SelectedMediaRepository {
    val TAG = SelectedMediaRepositoryImpl::class.java.simpleName
    private lateinit var mediaDatabase: MediaFileDatabase
    private val miMediaFileEntityList = ArrayList<MiMedia>()

    override suspend fun getSelectedMediaData(bucket: String): Flow<Result<ArrayList<MiMedia>>> {
        return flow {
            initMediaDb(context)
            try {
                miMediaFileEntityList.clear()

                val mediaType = when (LassiConfig.getConfig().mediaType) {
                    MediaType.IMAGE -> MediaType.IMAGE.value
                    MediaType.VIDEO -> MediaType.VIDEO.value
                    MediaType.AUDIO -> MediaType.AUDIO.value
                    else -> MediaType.IMAGE.value
                }

                val selectedImageMediaItemList: List<MediaFileEntity>
                val selectedMediaItemList: List<SelectedMediaModel>
                if (mediaType == MediaType.IMAGE.value) {
                    selectedImageMediaItemList =
                        mediaDatabase.mediaFileDao().getSelectedImageMediaFile(bucket, mediaType)
                    selectedImageMediaItemList.forEach { selectedMediaModel ->
                        miMediaFileEntityList.add(
                            MiMedia(
                                id = selectedMediaModel.mediaId,
                                name = selectedMediaModel.mediaName,
                                path = selectedMediaModel.mediaPath,
                                fileSize = selectedMediaModel.mediaSize,
                            )
                        )
                    }
                } else {
                    selectedMediaItemList =
                        mediaDatabase.mediaFileDao().getSelectedMediaFile(bucket, mediaType)
                    selectedMediaItemList.forEach { selectedMediaModel ->
                        miMediaFileEntityList.add(
                            MiMedia(
                                id = selectedMediaModel.mediaId,
                                name = selectedMediaModel.mediaName,
                                path = selectedMediaModel.mediaPath,
                                fileSize = selectedMediaModel.mediaSize,
                                duration = selectedMediaModel.mediaDuration,
                                thumb = selectedMediaModel.mediaAlbumCoverPath,
                            )
                        )
                    }
                }
                emit(Result.Success(miMediaFileEntityList))
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                emit(Result.Error(Throwable()))
            }
        }.catch().flowOn(Dispatchers.IO)
    }

    private fun initMediaDb(context: Context) {
        if (!this::mediaDatabase.isInitialized) {
            mediaDatabase = MediaFileDatabase.invoke(context = context)
            Logger.d(TAG, "MEDIA FILE DATABASE Initialized ")
        }
    }

    override suspend fun getSortedDataFromDb(
        bucket: String,
        isAsc: Int,
        mediaType: MediaType
    ): Flow<Result<ArrayList<MiMedia>>> {
        return flow {
            initMediaDb(context)
            try {
                val mediaTypeValue = when (mediaType) {
                    MediaType.IMAGE -> MediaType.IMAGE.value
                    MediaType.VIDEO -> MediaType.VIDEO.value
                    MediaType.AUDIO -> MediaType.AUDIO.value
                    else -> MediaType.IMAGE.value
                }

                miMediaFileEntityList.clear()

                if (mediaTypeValue == MediaType.IMAGE.value) {
                    val sortedImageMediaItemList: List<MediaFileEntity> =
                        mediaDatabase.mediaFileDao()
                            .getSelectedSortedImageMediaFile(bucket, isAsc, mediaTypeValue)
                    sortedImageMediaItemList.forEach { selectedMediaModel ->
                        miMediaFileEntityList.add(
                            MiMedia(
                                id = selectedMediaModel.mediaId,
                                name = selectedMediaModel.mediaName,
                                path = selectedMediaModel.mediaPath,
                                fileSize = selectedMediaModel.mediaSize,
                            )
                        )
                    }
                } else {
                    val sortedMediaItemList: List<SelectedMediaModel> =
                        mediaDatabase.mediaFileDao()
                            .getSelectedSortedMediaFile(bucket, isAsc, mediaTypeValue)
                    sortedMediaItemList.forEach { selectedMediaModel ->
                        miMediaFileEntityList.add(
                            MiMedia(
                                id = selectedMediaModel.mediaId,
                                name = selectedMediaModel.mediaName,
                                path = selectedMediaModel.mediaPath,
                                fileSize = selectedMediaModel.mediaSize,
                                duration = selectedMediaModel.mediaDuration,
                                thumb = selectedMediaModel.mediaAlbumCoverPath,
                            )
                        )
                    }
                }
                emit(Result.Success(miMediaFileEntityList))
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                emit(Result.Error(Throwable()))
            }
        }.catch().flowOn(Dispatchers.IO)
    }
}