package com.lassi.data.media

import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.lassi.common.utils.KeyUtils
import com.lassi.common.utils.Logger
import com.lassi.data.mediadirectory.Folder
import com.lassi.domain.media.LassiConfig
import com.lassi.domain.media.MediaRepository
import com.lassi.domain.media.MediaType
import io.reactivex.Single
import java.io.File
import java.util.*

class MediaDataRepository(private val context: Context) : MediaRepository {
    private val minTimeInMillis = LassiConfig.getConfig().minTime * 1000L
    private val maxTimeInMillis = LassiConfig.getConfig().maxTime * 1000L
    private val fetchedFolders = arrayListOf<Folder>()
    private val folderMap = LinkedHashMap<String, Folder>()

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
                    val duration =
                        if (LassiConfig.getConfig().mediaType == MediaType.VIDEO) {
                            cursor.getLong(cursor.getColumnIndex(projection[4]))
                        } else {
                            0
                        }

                    val file = makeSafeFile(path)
                    if (file != null && file.exists()) {
                        if (LassiConfig.getConfig().mediaType == MediaType.VIDEO) {
                            if (minTimeInMillis > KeyUtils.DEFAULT_VIDEO_DURATION && maxTimeInMillis > KeyUtils.DEFAULT_VIDEO_DURATION) {
                                if (duration in minTimeInMillis..maxTimeInMillis) {
                                    addFileToFolder(
                                        bucket,
                                        MiMedia(id, name, path, duration)
                                    )
                                }
                            } else if (minTimeInMillis == KeyUtils.DEFAULT_VIDEO_DURATION && maxTimeInMillis != KeyUtils.DEFAULT_VIDEO_DURATION) {
                                if (duration <= maxTimeInMillis) {
                                    addFileToFolder(
                                        bucket,
                                        MiMedia(id, name, path, duration)
                                    )
                                }
                            } else if (maxTimeInMillis == KeyUtils.DEFAULT_VIDEO_DURATION && minTimeInMillis != KeyUtils.DEFAULT_VIDEO_DURATION) {
                                if (minTimeInMillis <= duration) {
                                    addFileToFolder(
                                        bucket,
                                        MiMedia(id, name, path, duration)
                                    )
                                }
                            } else {
                                addFileToFolder(
                                    bucket,
                                    MiMedia(id, name, path, duration)
                                )
                            }
                        } else {
                            addFileToFolder(bucket, MiMedia(id, name, path, duration))
                        }
                    }
                } while (cursor.moveToPrevious())
            }
        } catch (e: Exception) {
            Logger.e("MediaDataRepository", "fetchFolders >> $e")
            return Single.error(e)
        } finally {
            cursor.close()
        }
        fetchedFolders.addAll(folderMap.values)
        return Single.just(fetchedFolders)
    }

    /**
     * Add file to folder
     */
    private fun addFileToFolder(
        bucket: String,
        miMedia: MiMedia
    ) {
        if (isFileTypeSupported(miMedia.path)) {
            var folder = folderMap[bucket]
            if (folder == null) {
                folder = Folder(bucket)
                folderMap[bucket] = folder
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
        return if (LassiConfig.getConfig().mediaType == MediaType.IMAGE) {
            arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME
            )
        } else {
            arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.VideoColumns.DURATION
            )
        }
    }

    private fun query(projection: Array<String>): Cursor? {
        return if (LassiConfig.getConfig().mediaType == MediaType.IMAGE) {
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Images.Media.DATE_ADDED
            )
        } else {
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Video.Media.DATE_ADDED
            )
        }
    }
}