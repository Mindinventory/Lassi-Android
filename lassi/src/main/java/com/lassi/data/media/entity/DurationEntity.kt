package com.lassi.data.media.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import com.lassi.data.media.entity.DurationEntity.Companion.DURATION_ENTITY
import com.lassi.data.media.entity.DurationEntity.Companion.DURATION_MEDIA_ID
import com.lassi.data.media.entity.MediaFileEntity.Companion.MEDIA_ID
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = DURATION_ENTITY,
    foreignKeys = arrayOf(
        ForeignKey(
            entity = MediaFileEntity::class,
            parentColumns = arrayOf(MEDIA_ID),
            childColumns = arrayOf(DURATION_MEDIA_ID),
            onDelete = CASCADE
        )
    )
)
data class DurationEntity(
    @PrimaryKey
    @ColumnInfo(name = DURATION_MEDIA_ID)
    var mediaId: Long,

    @ColumnInfo(name = DURATION_MEDIA_DURATION, defaultValue = "default_media_duration")
    var mediaDuration: Long,

    ) : Parcelable {
    companion object {
        const val DURATION_ENTITY = "duration"
        const val DURATION_MEDIA_ID = "duration_media_id"
        const val DURATION_MEDIA_DURATION = "media_duration"
    }
}