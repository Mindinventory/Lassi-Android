package com.lassi.data.media.repository

import android.content.Context
import com.lassi.common.extenstions.catch
import com.lassi.common.utils.Logger
import com.lassi.data.common.Result
import com.lassi.data.database.MediaFileDatabase
import com.lassi.data.media.MiMedia
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
                if (mediaType == MediaType.IMAGE.value) {
                    mediaDatabase.mediaFileDao().getSelectedImageMediaFile(bucket, mediaType)
                        .collect { selectedMediaFileEntity ->
                            miMediaFileEntityList.clear()
                            selectedMediaFileEntity.forEach { selectedMediaModel ->
                                miMediaFileEntityList.add(
                                    MiMedia(
                                        id = selectedMediaModel.mediaId,
                                        name = selectedMediaModel.mediaName,
                                        path = selectedMediaModel.mediaPath,
                                        fileSize = selectedMediaModel.mediaSize,
                                    )
                                )
                            }
                            emit(Result.Success(miMediaFileEntityList))
                        }

                } else {
                    mediaDatabase.mediaFileDao().getSelectedMediaFile(bucket, mediaType)
                        .collect { selectedMediaFileEntity ->
                            miMediaFileEntityList.clear()
                            selectedMediaFileEntity.forEach { selectedMediaModel ->
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
                            emit(Result.Success(miMediaFileEntityList))
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
}