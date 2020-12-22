package com.lassi.data.media

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import com.lassi.R
import com.lassi.common.utils.KeyUtils
import com.lassi.common.utils.Logger
import com.lassi.data.mediadirectory.Folder
import com.lassi.domain.media.LassiConfig
import com.lassi.domain.media.MediaRepository
import com.lassi.domain.media.MediaType
import io.reactivex.Single
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class MediaRepositoryImpl(private val context: Context) : MediaRepository {

    private val minTimeInMillis = LassiConfig.getConfig().minTime * 1000L
    private val maxTimeInMillis = LassiConfig.getConfig().maxTime * 1000L
    private val fetchedFolders = arrayListOf<Folder>()
    private val folderMap = LinkedHashMap<String, Folder>()
    private val minFileSize = LassiConfig.getConfig().minFileSize * 1024L
    private val maxFileSize = LassiConfig.getConfig().maxFileSize * 1024L

    override fun fetchFolders(): Single<ArrayList<Folder>> {
        val projection = getProjections()
        val cursor = query(projection)
        cursor ?: return Single.error(Throwable())
        folderMap.clear()
        fetchedFolders.clear()
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
                            val albumId = cursor.getString(cursor.getColumnIndex(projection[5]))
                            getAlbumArt(albumId)
                        } else {
                            ""
                        }
                    val duration =
                        if (LassiConfig.getConfig().mediaType == MediaType.VIDEO) {
                            cursor.getLong(cursor.getColumnIndex(projection[4]))
                        } else {
                            0
                        }

                    val file = makeSafeFile(path)
                    if (file != null && file.exists()) {
                        if (LassiConfig.getConfig().mediaType == MediaType.VIDEO ||
                            LassiConfig.getConfig().mediaType == MediaType.AUDIO
                        ) {
                            checkDurationAndAddFileToFolder(
                                bucket,
                                id,
                                name,
                                path,
                                duration,
                                albumCoverPath,
                                size
                            )
                        } else {
                            Logger.e("MediaRepositoryImpl", "$name >> $size")

                            if (isValidFileSize(size)) {
                                addFileToFolder(
                                    bucket,
                                    MiMedia(id, name, path, duration, albumCoverPath)
                                )
                            }
                        }
                    }
                } while (cursor.moveToPrevious())
            }
        } catch (e: Exception) {
            Logger.e("MediaRepositoryImpl", "fetchFolders >> $e")
            return Single.error(e)
        } finally {
            cursor.close()
        }
        fetchedFolders.addAll(folderMap.values)
        return Single.just(fetchedFolders)
    }

    private fun checkDurationAndAddFileToFolder(
        bucket: String?,
        id: Long,
        name: String,
        path: String,
        duration: Long,
        albumCoverPath: String,
        size: Long
    ) {
        if (isValidDuration(duration) && isValidFileSize(size)) {
            addFileToFolder(
                bucket,
                MiMedia(id, name, path, duration, albumCoverPath, size)
            )
        }
    }

    /**
     * check if file size is valid duration
     */
    private fun isValidFileSize(fileSize: Long): Boolean {
        return if (minFileSize > KeyUtils.DEFAULT_FILE_SIZE &&
            maxFileSize > KeyUtils.DEFAULT_FILE_SIZE
        ) {
            fileSize in minFileSize..maxFileSize
        } else if (minFileSize == KeyUtils.DEFAULT_FILE_SIZE &&
            maxFileSize != KeyUtils.DEFAULT_FILE_SIZE
        ) {
            fileSize <= maxFileSize
        } else if (maxFileSize == KeyUtils.DEFAULT_FILE_SIZE &&
            minFileSize != KeyUtils.DEFAULT_FILE_SIZE
        ) {
            minFileSize <= fileSize
        } else {
            true
        }
    }

    /**
     * check if video/audio has valid duration
     */
    private fun isValidDuration(duration: Long): Boolean {
        return if (minTimeInMillis > KeyUtils.DEFAULT_DURATION &&
            maxTimeInMillis > KeyUtils.DEFAULT_DURATION
        ) {
            duration in minTimeInMillis..maxTimeInMillis
        } else if (minTimeInMillis == KeyUtils.DEFAULT_DURATION &&
            maxTimeInMillis != KeyUtils.DEFAULT_DURATION
        ) {
            duration <= maxTimeInMillis
        } else if (maxTimeInMillis == KeyUtils.DEFAULT_DURATION &&
            minTimeInMillis != KeyUtils.DEFAULT_DURATION
        ) {
            minTimeInMillis <= duration
        } else {
            true
        }
    }

    /**
     * Add file to folder
     */
    private fun addFileToFolder(
        bucket: String?,
        miMedia: MiMedia
    ) {
        val bucketName = bucket ?: context.getString(R.string.lassi_all)
        if (isFileTypeSupported(miMedia.path)) {
            var folder = folderMap[bucketName]
            if (folder == null) {
                folder = Folder(bucketName)
                folderMap[bucketName] = folder
            }
            folder.medias.add(miMedia)
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
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.SIZE
            )
            MediaType.VIDEO -> arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.VideoColumns.DURATION,
                MediaStore.Video.VideoColumns.SIZE
            )
            MediaType.AUDIO -> arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.AudioColumns.DURATION,
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

    private fun query(projection: Array<String>): Cursor? {
        return when (LassiConfig.getConfig().mediaType) {
            MediaType.IMAGE -> context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Images.Media.DATE_ADDED
            )
            MediaType.VIDEO -> context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Video.Media.DATE_ADDED
            )
            MediaType.AUDIO -> context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Audio.Media.DATE_ADDED
            )
            MediaType.DOC -> {
                val mimeTypes = mutableListOf<String>()
                LassiConfig.getConfig().supportedFileType.forEach { mimeType ->
                    MimeTypeMap
                        .getSingleton()
                        .getMimeTypeFromExtension(mimeType)?.let {
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
        }
    }

    /**
     * fetch album art for audio files
     */
    private fun getAlbumArt(albumId: String): String {
        var albumCoverPath = ""
        val cursorAlbum = context.contentResolver.query(
            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
            arrayOf(
                MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ALBUM_ART
            ),
            MediaStore.Audio.Albums._ID + "=" + albumId,
            null, null
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

    override fun fetchDocs(): Single<ArrayList<MiMedia>> {
        val projection = getProjections()
        val cursor = query(projection)
        cursor ?: return Single.error(Throwable())
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
            Logger.e("MediaRepositoryImpl", "fetchFolders >> $e")
        } finally {
            cursor.close()
        }
        return Single.just(docs)
    }
}
