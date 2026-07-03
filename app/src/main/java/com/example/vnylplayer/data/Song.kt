package com.example.vnylplayer.data

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val albumName: String? = null,
    val durationMs: Long,
    val trackNumber: Int? = null,
    val dateAdded: Long? = null,
    val uri: String,
    val artworkUri: String? = null // Mapped to Android's albumart content provider. Handled by Coil gracefully.
)
