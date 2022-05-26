package com.lassi.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lassi.data.media.entity.AlbumCoverPathEntity
import com.lassi.data.media.entity.DurationEntity
import com.lassi.data.media.entity.MediaFileDao
import com.lassi.data.media.entity.MediaFileEntity

@Database(
    entities = arrayOf(
        MediaFileEntity::class,
        DurationEntity::class,
        AlbumCoverPathEntity::class
    ), version = 1, exportSchema = false
)
abstract class MediaFileDatabase : RoomDatabase() {
    abstract fun mediaFileDao(): MediaFileDao

    companion object {
        @Volatile
        private var INSTANCE: MediaFileDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = INSTANCE ?: synchronized(LOCK) {
            buildDatabase(context).also {
                INSTANCE = it
            }
        }

        private fun buildDatabase(context: Context): MediaFileDatabase = Room.databaseBuilder(
            context, MediaFileDatabase::class.java, "media_file_database"
        ).build()
    }
}