package com.example.vnylplayer.data

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.example.vnylplayer.data.local.VnylDatabase
import com.example.vnylplayer.data.local.entity.SongEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MusicRepository(private val context: Context) {

    private val songDao by lazy {
        VnylDatabase.getDatabase(context).songDao()
    }

    suspend fun saveImportedSongs(songs: List<Song>) = withContext(Dispatchers.IO) {

        songDao.insertSongs(
            songs.map { song ->

                SongEntity(
                    songId = song.id,
                    title = song.title,
                    artist = song.artist,
                    albumName = song.albumName,
                    durationMs = song.durationMs ?: 0L,
                    trackNumber = song.trackNumber ?: 0,
                    dateAdded = song.dateAdded ?: 0L,
                    uri = song.uri,
                    artworkUri = song.artworkUri
                )
            }
        )
    }

    /**
     * Replaces Android MediaStore auto-fetching natively isolating the player purely into a Curated mode.
     * Only files explicitly granted File Provider URIs via `ProfileScreen` uploads exist structurally.
     */
    suspend fun getLocalSongs(): List<Song> = withContext(Dispatchers.IO) {
        val importedEntities = songDao.getImportedSongs()
        
        importedEntities.map { entity ->
            Song(
                id = entity.songId,
                title = entity.title,
                artist = entity.artist,
                albumName = entity.albumName,
                durationMs = entity.durationMs,
                trackNumber = entity.trackNumber,
                dateAdded = entity.dateAdded,
                uri = entity.uri,
                artworkUri = entity.artworkUri
            )
        }.sortedBy { it.title.lowercase() }
    }
}