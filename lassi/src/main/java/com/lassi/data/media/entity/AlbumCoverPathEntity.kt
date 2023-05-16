package com.lassi.data.media.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.lassi.data.media.entity.AlbumCoverPathEntity.Companion.ALBUM_COVER_ENTITY
import com.lassi.data.media.entity.AlbumCoverPathEntity.Companion.ALBUM_COVER_MEDIA_ID
import com.lassi.data.media.entity.MediaFileEntity.Companion.MEDIA_ID
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = ALBUM_COVER_ENTITY,
    foreignKeys = arrayOf(
        ForeignKey(
            entity = MediaFileEntity::class,
            parentColumns = arrayOf(MEDIA_ID),
            childColumns = arrayOf(ALBUM_COVER_MEDIA_ID),
            onDelete = ForeignKey.CASCADE
        )
    )
)
data class AlbumCoverPathEntity(
    @PrimaryKey
    @ColumnInfo(name = ALBUM_COVER_MEDIA_ID)
    var mediaId: Long,

    @ColumnInfo(name = ALBUM_COVER_MEDIA_PATH, defaultValue = "default_media_album_cover_path")
    var mediaAlbumCoverPath: String,

    ) : Parcelable {
    companion object {
        const val ALBUM_COVER_ENTITY = "album_cover"
        const val ALBUM_COVER_MEDIA_ID = "album_cover_media_id"
        const val ALBUM_COVER_MEDIA_PATH = "album_cover_path"
    }
}