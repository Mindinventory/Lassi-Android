package com.lassi.data.media.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lassi.data.media.entity.MediaFileEntity.Companion.MEDIA_FILE_ENTITY
import com.lassi.data.media.entity.MediaFileEntity.Companion.MEDIA_ID
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = MEDIA_FILE_ENTITY, indices = [Index(value = [MEDIA_ID], unique = true)])
data class MediaFileEntity(

    @PrimaryKey
    @ColumnInfo(name = MEDIA_ID)
    var mediaId: Long,

    @ColumnInfo(name = MEDIA_NAME)
    var mediaName: String,

    @ColumnInfo(name = MEDIA_PATH)
    var mediaPath: String,

    @ColumnInfo(name = MEDIA_BUCKET, defaultValue = "default_media_bucket")
    var mediaBucket: String,

    @ColumnInfo(name = MEDIA_SIZE)
    var mediaSize: Long,

    @ColumnInfo(name = MEDIA_DATE_ADDED)
    var mediaDateAdded: Long,

    @ColumnInfo(name = MEDIA_TYPE)
    var mediaType: Int,

) : Parcelable {
    companion object {
        const val MEDIA_FILE_ENTITY = "media"
        const val MEDIA_ID = "media_id"
        const val MEDIA_NAME = "media_name"
        const val MEDIA_PATH = "media_path"
        const val MEDIA_BUCKET = "media_bucket"
        const val MEDIA_SIZE = "media_size"
        const val MEDIA_TYPE = "media_type"
        const val MEDIA_DATE_ADDED = "media_date_added"
    }
}
