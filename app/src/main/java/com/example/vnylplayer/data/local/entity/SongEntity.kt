package com.example.vnylplayer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey
    val songId: String,
    val title: String,
    val artist: String,
    val albumName: String? = null,
    val durationMs: Long,
    val trackNumber: Int = 0,
    val dateAdded: Long = System.currentTimeMillis(),
    val uri: String,
    val artworkUri: String? = null
)
