package com.example.vnylplayer.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.vnylplayer.data.local.dao.PlaylistDao
import com.example.vnylplayer.data.local.dao.SongDao
import com.example.vnylplayer.data.local.entity.PlaylistEntity
import com.example.vnylplayer.data.local.entity.PlaylistSongCrossRef
import com.example.vnylplayer.data.local.entity.SongEntity

@Database(entities = [PlaylistEntity::class, PlaylistSongCrossRef::class, SongEntity::class], version = 2, exportSchema = false)
abstract class VnylDatabase : RoomDatabase() {
    
    abstract fun playlistDao(): PlaylistDao
    abstract fun songDao(): SongDao

    companion object {
        @Volatile
        private var INSTANCE: VnylDatabase? = null

        fun getDatabase(context: Context): VnylDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VnylDatabase::class.java,
                    "vnyl_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
