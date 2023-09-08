package com.lassi.data.media.repository

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import com.lassi.R
import com.lassi.common.extenstions.catch
import com.lassi.common.utils.KeyUtils
import com.lassi.common.utils.Logger
import com.lassi.data.common.Result
import com.lassi.data.database.MediaFileDatabase
import com.lassi.data.media.MiItemMedia
import com.lassi.data.media.MiMedia
import com.lassi.data.media.entity.AlbumCoverPathEntity
import com.lassi.data.media.entity.DurationEntity
import com.lassi.data.media.entity.MediaFileEntity
import com.lassi.domain.media.LassiConfig
import com.lassi.domain.media.MediaRepository
import com.lassi.domain.media.MediaType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.io.File

class MediaRepositoryImpl(private val context: Context) : MediaRepository {
    private var mediaPathToRemove: ArrayList<MediaFileEntity>? = null
    val TAG = MediaRepositoryImpl::class.java.simpleName
    private val minTimeInMillis = LassiConfig.getConfig().minTime * 1000L
    private val maxTimeInMillis = LassiConfig.getConfig().maxTime * 1000L
    private val minFileSize = LassiConfig.getConfig().minFileSize * 1024L
    private val maxFileSize = LassiConfig.getConfig().maxFileSize * 1024L
    private lateinit var mediaDatabase: MediaFileDatabase

    private fun initMediaDb(context: Context) {
        if (!this::mediaDatabase.isInitialized) {
            mediaDatabase = MediaFileDatabase.invoke(context = context)
            Logger.d(TAG, "MEDIA FILE DATABASE Initialized ")
        }
    }

    override suspend fun isDbEmpty(): Boolean {
        return try {
            initMediaDb(context)
            mediaDatabase.mediaFileDao().getDataCount(
                mediaType = when (LassiConfig.getConfig().mediaType) {
                    MediaType.IMAGE -> MediaType.IMAGE.value
                    MediaType.VIDEO -> MediaType.VIDEO.value
                    MediaType.AUDIO -> MediaType.AUDIO.value
                    else -> MediaType.IMAGE.value
                }
            )
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun insertMediaData(): Result<Boolean> {
        val resultDeferred = CoroutineScope(IO).async {
            try {
                initMediaDb(context)
                val mediaType = when (LassiConfig.getConfig().mediaType) {
                    MediaType.IMAGE -> MediaType.IMAGE.value
                    MediaType.VIDEO -> MediaType.VIDEO.value
                    MediaType.AUDIO -> MediaType.AUDIO.value
                    else -> MediaType.IMAGE.value
                }
                val latestImgFileDate: Long =
                    mediaDatabase.mediaFileDao().getMaxDateFromMediaTable(mediaType)
                return@async fetchAndInsertMediaHelper(latestImgFileDate)
            } catch (e: Exception) {
                return@async Result.Error(e)
            }
        }
        return resultDeferred.await()
    }

    override suspend fun removeMediaData(allDataList: List<MediaFileEntity>?) {
        CoroutineScope(IO).launch {
            if (allDataList != null) {
                for (mediaFile in allDataList) {
                    val mediaPath: String = mediaFile.mediaPath
                    if (File(mediaPath).exists()) {
                        Log.d(TAG, "!@# removeMediaData: Nothing to remove")
                    } else {
                        //remove particular file, as it doesn't exist
                        initMediaDb(context)
                        mediaDatabase.mediaFileDao().deleteByMediaPath(mediaPath)
                    }
                }
            }
        }
    }

    override suspend fun getAllImgVidMediaFile(): Flow<Result<ArrayList<MediaFileEntity>>> {
        return flow {
            try {
                initMediaDb(context)
                val allData = mediaDatabase.mediaFileDao()
                    .getAllImgVidMediaFile() as ArrayList<MediaFileEntity>
                emit(Result.Success(allData))
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                emit(Result.Error(Throwable()))
            }
        }.catch().flowOn(IO)
    }

    override suspend fun insertAllMediaData(): Result<Boolean> {
        val resultDeferred = CoroutineScope(IO).async {
            try {
                return@async fetchAndInsertMediaHelper(0L)   //Here we don't need to pass Latest date
            } catch (e: Exception) {
                e.printStackTrace()
                return@async Result.Error(e)
            }
        }
        return resultDeferred.await()
    }

    /**
     * Prepare folder listing based on type
     */
    override suspend fun getDataFromDb(): Flow<Result<ArrayList<MiItemMedia>>> {
        return flow {
            initMediaDb(context)
            try {
                val mediaType = when (LassiConfig.getConfig().mediaType) {
                    MediaType.IMAGE -> MediaType.IMAGE.value
                    MediaType.VIDEO -> MediaType.VIDEO.value
                    MediaType.AUDIO -> MediaType.AUDIO.value
                    else -> MediaType.IMAGE.value
                }
                val miItemMediaList = ArrayList<MiItemMedia>()
                mediaDatabase.mediaFileDao().getDistinctBucketList(mediaType)
                    .collect { folderList ->
                        miItemMediaList.clear()
                        folderList.forEach { bucket ->
                            val latestItemPathForBucket: String = mediaDatabase.mediaFileDao()
                                .getLatestItemForBucket(bucket = bucket, mediaType = mediaType)
                            val totalItemSizeForBucket: Long = mediaDatabase.mediaFileDao()
                                .getTotalItemSizeForBucket(bucket = bucket, mediaType = mediaType)
                            miItemMediaList.add(
                                MiItemMedia(
                                    bucket, latestItemPathForBucket, totalItemSizeForBucket
                                )
                            )
                        }
                        emit(Result.Success(miItemMediaList))
                    }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                emit(Result.Error(Throwable()))
            }
        }.catch().flowOn(IO)
    }

    private suspend fun checkDurationAndAddFileToDatabase(
        bucket: String?,
        id: Long,
        name: String,
        path: String,
        duration: Long,
        albumCoverPath: String,
        size: Long,
        dateAdded: Long,
        mediaType: MediaType
    ) {
        if (isValidDuration(duration) && isValidFileSize(size)) {
            addFileToDatabase(
                bucket,
                MiMedia(id, name, path, duration, albumCoverPath, size),
                dateAdded,
                mediaType
            )
        }
    }

    /**
     * check if file size is valid duration
     */
    private fun isValidFileSize(fileSize: Long): Boolean {
        return if (minFileSize > KeyUtils.DEFAULT_FILE_SIZE && maxFileSize > KeyUtils.DEFAULT_FILE_SIZE) {
            fileSize in minFileSize..maxFileSize
        } else if (minFileSize == KeyUtils.DEFAULT_FILE_SIZE && maxFileSize != KeyUtils.DEFAULT_FILE_SIZE) {
            fileSize <= maxFileSize
        } else if (maxFileSize == KeyUtils.DEFAULT_FILE_SIZE && minFileSize != KeyUtils.DEFAULT_FILE_SIZE) {
            minFileSize <= fileSize
        } else {
            true
        }
    }

    /**
     * check if video/audio has valid duration
     */
    private fun isValidDuration(duration: Long): Boolean {
        return if (minTimeInMillis > KeyUtils.DEFAULT_DURATION && maxTimeInMillis > KeyUtils.DEFAULT_DURATION) {
            duration in minTimeInMillis..maxTimeInMillis
        } else if (minTimeInMillis == KeyUtils.DEFAULT_DURATION && maxTimeInMillis != KeyUtils.DEFAULT_DURATION) {
            duration <= maxTimeInMillis
        } else if (maxTimeInMillis == KeyUtils.DEFAULT_DURATION && minTimeInMillis != KeyUtils.DEFAULT_DURATION) {
            minTimeInMillis <= duration
        } else {
            true
        }
    }

    /**
     * Add file to database
     */
    private suspend fun addFileToDatabase(
        bucket: String?,
        miMedia: MiMedia,
        dateAdded: Long,
        mediaType: MediaType,
    ) {
        val bucketName = bucket ?: context.getString(R.string.lassi_all)
        if (isFileTypeSupported(miMedia.path)) {
            CoroutineScope(IO).launch {
                miMedia.path?.let { path ->
                    miMedia.name?.let { mName ->
                        MediaFileEntity(
                            miMedia.id,
                            mName,
                            path,
                            bucketName,
                            miMedia.fileSize,
                            dateAdded,
                            mediaType.value
                        )
                    }
                }?.let { mediaFileEntity ->
                    mediaDatabase.mediaFileDao().insertMediaFile(mediaFileEntity)   //Insert Query
                }

                when (LassiConfig.getConfig().mediaType) {
                    MediaType.AUDIO -> {
                        //to store duration -> DurationEntity
                        mediaDatabase.mediaFileDao()
                            .insertDuration(DurationEntity(miMedia.id, miMedia.duration))
                        //to store albumCover -> AlbumCoverEntity
                        miMedia.thumb?.let { albumArtPath ->
                            AlbumCoverPathEntity(miMedia.id, albumArtPath)
                        }?.let { albumCoverPathEntity ->
                            mediaDatabase.mediaFileDao().insertAlbumCover(albumCoverPathEntity)
                        }
                    }

                    MediaType.VIDEO -> {
                        //to store duration -> DurationEntity
                        mediaDatabase.mediaFileDao()
                            .insertDuration(DurationEntity(miMedia.id, miMedia.duration))
                    }

                    else -> {
                    }
                }
                //to store duration -> DurationEntity
                if (miMedia.duration != 0L) {
                    mediaDatabase.mediaFileDao()
                        .insertDuration(DurationEntity(miMedia.id, miMedia.duration))
                }
                //to store albumCover -> AlbumCoverEntity
                miMedia.thumb?.let { albumArtPath ->
                    AlbumCoverPathEntity(miMedia.id, albumArtPath)
                }?.let { albumCoverPathEntity ->
                    mediaDatabase.mediaFileDao().insertAlbumCover(albumCoverPathEntity)
                }
            }
        }
    }

    /**
     * Check if file type is supported
     */
    private fun isFileTypeSupported(path: String?): Boolean {
        if (path != null) {
            if (LassiConfig.getConfig().supportedFileType.isNotEmpty()) {
                for (fileType in LassiConfig.getConfig().supportedFileType) {
                    if (path.endsWith(fileType, true)) return true
                }
            } else {
                return true
            }
        }
        return false
    }

    /**
     * check if File path is not null
     */
    private fun makeSafeFile(path: String?): File? {
        if (path == null || path.isEmpty()) {
            return null
        }
        return try {
            File(path)
        } catch (ignored: Exception) {
            null
        }
    }

    /**
     * Get List of columns to fetch file info
     */
    private fun getProjections(): Array<String> {
        return when (LassiConfig.getConfig().mediaType) {
            MediaType.IMAGE -> arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,//e.g. image file name
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATE_ADDED
            )

            MediaType.VIDEO -> arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.VideoColumns.DURATION,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.VideoColumns.SIZE
            )

            MediaType.AUDIO -> arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.AudioColumns.DURATION,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.SIZE
            )

            else -> arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.PARENT,
                MediaStore.Files.FileColumns.SIZE
            )
        }
    }

    private fun query(projection: Array<String>, latestImageFileDate: Long): Cursor? {
        return when (LassiConfig.getConfig().mediaType) {
            MediaType.IMAGE -> {
                context.contentResolver.query(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                    } else {
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    },
                    projection,
                    if (latestImageFileDate != 0L) "${MediaStore.Images.Media.DATE_ADDED} > ?" else null,
                    if (latestImageFileDate != 0L) arrayOf(latestImageFileDate.toString()) else null,
                    MediaStore.Images.Media.DATE_ADDED
                )
            }

            MediaType.VIDEO -> context.contentResolver.query(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                } else {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                },
                projection,
                if (latestImageFileDate != 0L) "${MediaStore.Video.Media.DATE_ADDED} > ?" else null,
                if (latestImageFileDate != 0L) arrayOf(latestImageFileDate.toString()) else null,
                MediaStore.Video.Media.DATE_ADDED
            )

            MediaType.AUDIO -> context.contentResolver.query(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Audio.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL_PRIMARY
                    )
                } else {
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                },
                projection,
                if (latestImageFileDate != 0L) "${MediaStore.Audio.Media.DATE_ADDED} > ?" else null,
                if (latestImageFileDate != 0L) arrayOf(latestImageFileDate.toString()) else null,
                MediaStore.Audio.Media.DATE_ADDED
            )

            MediaType.DOC -> {
                val mimeTypes = mutableListOf<String>()
                LassiConfig.getConfig().supportedFileType.forEach { mimeType ->
                    MimeTypeMap.getSingleton().getMimeTypeFromExtension(mimeType)?.let {
                        mimeTypes.add("'$it'")
                    }
                }
                val selectionMimeType =
                    MediaStore.Files.FileColumns.MIME_TYPE + " IN (${mimeTypes.joinToString()})"
                context.contentResolver.query(
                    MediaStore.Files.getContentUri("external"),
                    projection,
                    selectionMimeType,
                    null,
                    MediaStore.Video.Media.DATE_ADDED
                )
            }

            else -> {
                null
            }
        }
    }

    @SuppressLint("Range")
    private suspend fun fetchAndInsertMediaHelper(latestImgFileDate: Long): Result<Boolean> {
        val result: Deferred<Result<Boolean>?> = CoroutineScope(IO).async {
            val projection = getProjections()
            val cursor: Cursor? = query(projection, latestImgFileDate)
            cursor?.let {
                try {
                    if (cursor.moveToLast()) {
                        do {
                            val id = cursor.getLong(cursor.getColumnIndex(projection[0]))
                            val name = cursor.getString(cursor.getColumnIndex(projection[1]))
                            val path = cursor.getString(cursor.getColumnIndex(projection[2]))
                            val bucket = cursor.getString(cursor.getColumnIndex(projection[3]))
                            val size =
                                cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE))
                            val albumCoverPath =
                                if (LassiConfig.getConfig().mediaType == MediaType.AUDIO) {
                                    val albumId =
                                        cursor.getString(cursor.getColumnIndex(projection[6]))
                                    if (albumId != null) {
                                        getAlbumArt(albumId)
                                    } else {
                                        continue
                                    }
                                } else {
                                    ""
                                }
                            val duration =
                                if (LassiConfig.getConfig().mediaType == MediaType.VIDEO || LassiConfig.getConfig().mediaType == MediaType.AUDIO) {
                                    cursor.getLong(cursor.getColumnIndex(projection[4]))
                                } else {
                                    0
                                }

                            val dateAdded =
                                cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED))

                            val file = makeSafeFile(path)

                            initMediaDb(context)

                            if (file != null && file.exists()) {
                                if (LassiConfig.getConfig().mediaType == MediaType.VIDEO || LassiConfig.getConfig().mediaType == MediaType.AUDIO) {
                                    checkDurationAndAddFileToDatabase(
                                        bucket,
                                        id,
                                        name,
                                        path,
                                        duration,
                                        albumCoverPath,
                                        size,
                                        dateAdded,
                                        LassiConfig.getConfig().mediaType
                                    )
                                } else {
                                    if (isValidFileSize(size)) {
                                        addFileToDatabase(
                                            bucket, MiMedia(
                                                id, name, path, duration, thumb = albumCoverPath
                                            ), dateAdded, LassiConfig.getConfig().mediaType
                                        )
                                    }
                                }
                            }
                        } while (cursor.moveToPrevious())
                    }
                    return@async Result.Success(true)
                } catch (e: Exception) {
                    e.printStackTrace()
                    return@async Result.Error(e)
                } finally {
                    cursor.close()
                }
            }
        }
        return result.await()!!
    }

    /**
     * fetch album art for audio files
     */
    @SuppressLint("Range")
    private fun getAlbumArt(albumId: String): String {
        var albumCoverPath = ""
        val cursorAlbum = context.contentResolver.query(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, arrayOf(
                MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART
            ), MediaStore.Audio.Albums._ID + "=" + albumId, null, null
        )

        if (cursorAlbum != null) {
            if (cursorAlbum.count > 0 && cursorAlbum.moveToFirst()) {
                albumCoverPath = cursorAlbum.getString(
                    cursorAlbum.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)
                ) ?: ""
            }
        }
        cursorAlbum?.close()
        return albumCoverPath
    }

    @SuppressLint("Range")
    override suspend fun fetchDocs(): Flow<Result<ArrayList<MiMedia>>> {
        return flow {
            val projection = getProjections()
            val cursor = query(projection, 0L)
            cursor?.let {
                val docs = ArrayList<MiMedia>()
                try {
                    if (cursor.moveToLast()) {
                        do {
                            val id = cursor.getLong(cursor.getColumnIndex(projection[0]))
                            val name = cursor.getString(cursor.getColumnIndex(projection[1]))
                            val path = cursor.getString(cursor.getColumnIndex(projection[2]))
                            docs.add(MiMedia(id, name, path, 0))
                        } while (cursor.moveToPrevious())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    cursor.close()
                }
                emit(Result.Success(docs))
            } ?: emit(Result.Error(Throwable()))
        }.catch().flowOn(IO)
    }
}
