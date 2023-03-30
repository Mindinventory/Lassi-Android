package com.lassi.data.media.entity

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lassi.data.media.entity.AlbumCoverPathEntity.Companion.ALBUM_COVER_ENTITY
import com.lassi.data.media.entity.AlbumCoverPathEntity.Companion.ALBUM_COVER_MEDIA_ID
import com.lassi.data.media.entity.AlbumCoverPathEntity.Companion.ALBUM_COVER_MEDIA_PATH
import com.lassi.data.media.entity.DurationEntity.Companion.DURATION_ENTITY
import com.lassi.data.media.entity.DurationEntity.Companion.DURATION_MEDIA_DURATION
import com.lassi.data.media.entity.MediaFileEntity.Companion.MEDIA_BUCKET
import com.lassi.data.media.entity.MediaFileEntity.Companion.MEDIA_DATE_ADDED
import com.lassi.data.media.entity.MediaFileEntity.Companion.MEDIA_FILE_ENTITY
import com.lassi.data.media.entity.MediaFileEntity.Companion.MEDIA_ID
import com.lassi.data.media.entity.MediaFileEntity.Companion.MEDIA_NAME
import com.lassi.data.media.entity.MediaFileEntity.Companion.MEDIA_PATH
import com.lassi.data.media.entity.MediaFileEntity.Companion.MEDIA_SIZE
import com.lassi.data.media.entity.MediaFileEntity.Companion.MEDIA_TYPE
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaFileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaFile(mediaFileEntity: MediaFileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDuration(durationEntity: DurationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbumCover(albumCoverPathEntity: AlbumCoverPathEntity)

    @Query("SELECT MAX($MEDIA_DATE_ADDED) as LargestDate FROM $MEDIA_FILE_ENTITY WHERE $MEDIA_TYPE = :mediaType")
    suspend fun getMaxDateFromMediaTable(mediaType: Int): Long

    @Query("SELECT COUNT(*) == 0 FROM $MEDIA_FILE_ENTITY WHERE $MEDIA_TYPE = :mediaType")
    suspend fun getDataCount(mediaType: Int): Boolean

    @Query("SELECT EXISTS(SELECT * FROM $MEDIA_FILE_ENTITY WHERE $MEDIA_PATH = :mediaPath)")
    suspend fun checkMediaFileExistence(mediaPath: String): Boolean

    @Query("SELECT DISTINCT $MEDIA_BUCKET FROM $MEDIA_FILE_ENTITY WHERE $MEDIA_TYPE = :mediaType")
    fun getDistinctBucketList(mediaType: Int): Flow<List<String>>

    @Query("SELECT MAX($MEDIA_DATE_ADDED) as LargestDate FROM $MEDIA_FILE_ENTITY WHERE $MEDIA_BUCKET = :bucket")
    suspend fun getLatestDateForBucket(bucket: String): Long

    @Query("SELECT $MEDIA_PATH as LatestItem FROM $MEDIA_FILE_ENTITY WHERE $MEDIA_BUCKET = :bucket  AND $MEDIA_TYPE = :mediaType AND $MEDIA_DATE_ADDED = (SELECT MAX($MEDIA_DATE_ADDED) as LargestDate FROM $MEDIA_FILE_ENTITY WHERE $MEDIA_BUCKET = :bucket)")
    suspend fun getLatestItemForBucket(bucket: String, mediaType: Int): String

    @Query("SELECT COUNT($MEDIA_ID) FROM $MEDIA_FILE_ENTITY WHERE $MEDIA_BUCKET = :bucket AND $MEDIA_TYPE = :mediaType")
    suspend fun getTotalItemSizeForBucket(bucket: String, mediaType: Int): Long

    @Query(
        "SELECT $MEDIA_FILE_ENTITY.$MEDIA_ID as mediaId, $MEDIA_FILE_ENTITY.$MEDIA_NAME as mediaName, " +
                "$MEDIA_FILE_ENTITY.$MEDIA_PATH as mediaPath, $MEDIA_FILE_ENTITY.$MEDIA_SIZE as mediaSize, $DURATION_ENTITY.$DURATION_MEDIA_DURATION as mediaDuration, $ALBUM_COVER_ENTITY.$ALBUM_COVER_MEDIA_PATH as mediaAlbumCoverPath" +
                " FROM $MEDIA_FILE_ENTITY" +
                " INNER JOIN $DURATION_ENTITY" +
                " ON $MEDIA_FILE_ENTITY.$MEDIA_ID = $DURATION_ENTITY.duration_media_id" +
                " INNER JOIN  $ALBUM_COVER_ENTITY" +
                " ON $MEDIA_FILE_ENTITY.$MEDIA_ID = $ALBUM_COVER_ENTITY.$ALBUM_COVER_MEDIA_ID" +
                " WHERE $MEDIA_BUCKET = :bucket AND $MEDIA_TYPE = :mediaType"
    )
    fun getSelectedMediaFile(bucket: String, mediaType: Int): List<SelectedMediaModel>

    @Query(
        "SELECT $MEDIA_FILE_ENTITY.$MEDIA_ID as mediaId, $MEDIA_FILE_ENTITY.$MEDIA_NAME as mediaName, " +
                "$MEDIA_FILE_ENTITY.$MEDIA_PATH as mediaPath, $MEDIA_FILE_ENTITY.$MEDIA_SIZE as mediaSize, $DURATION_ENTITY.$DURATION_MEDIA_DURATION as mediaDuration, $ALBUM_COVER_ENTITY.$ALBUM_COVER_MEDIA_PATH as mediaAlbumCoverPath" +
                " FROM $MEDIA_FILE_ENTITY" +
                " INNER JOIN $DURATION_ENTITY" +
                " ON $MEDIA_FILE_ENTITY.$MEDIA_ID = $DURATION_ENTITY.duration_media_id" +
                " INNER JOIN  $ALBUM_COVER_ENTITY" +
                " ON $MEDIA_FILE_ENTITY.$MEDIA_ID = $ALBUM_COVER_ENTITY.$ALBUM_COVER_MEDIA_ID" +
                " WHERE $MEDIA_BUCKET = :bucket AND $MEDIA_TYPE = :mediaType ORDER BY CASE WHEN :isAsc = 1 THEN $MEDIA_DATE_ADDED END ASC, CASE WHEN :isAsc = 0 THEN $MEDIA_DATE_ADDED END DESC")
    fun getSelectedSortedMediaFile(bucket: String, isAsc: Int, mediaType: Int): List<SelectedMediaModel>

    @Query("SELECT * FROM $MEDIA_FILE_ENTITY WHERE $MEDIA_BUCKET = :bucket AND $MEDIA_TYPE = :mediaType")
    fun getSelectedImageMediaFile(bucket: String, mediaType: Int): List<MediaFileEntity>

    @Query("SELECT * FROM $MEDIA_FILE_ENTITY WHERE $MEDIA_BUCKET = :bucket AND $MEDIA_TYPE = :mediaType ORDER BY CASE WHEN :isAsc = 1 THEN media_date_added END ASC, CASE WHEN :isAsc = 0 THEN media_date_added END DESC")
    fun getSelectedSortedImageMediaFile(
        bucket: String,
        isAsc: Int,
        mediaType: Int
    ): List<MediaFileEntity>

    @Query("SELECT * FROM $MEDIA_FILE_ENTITY WHERE $MEDIA_TYPE = 1 OR $MEDIA_TYPE = 2 OR $MEDIA_TYPE = 3")
    fun getAllImgVidMediaFile(): List<MediaFileEntity>

    @Query("DELETE FROM $MEDIA_FILE_ENTITY WHERE $MEDIA_PATH = :mediaPath")
    fun deleteByMediaPath(mediaPath: String)
}