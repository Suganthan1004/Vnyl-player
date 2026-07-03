package com.example.vnylplayer.data.repository

import com.example.vnylplayer.data.local.dao.PlaylistDao
import com.example.vnylplayer.data.local.entity.PlaylistEntity
import com.example.vnylplayer.data.local.entity.PlaylistSongCrossRef
import kotlinx.coroutines.flow.Flow

class PlaylistRepository(private val playlistDao: PlaylistDao) {

    val allPlaylists: Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()

    suspend fun createPlaylist(name: String): Long {
        return playlistDao.createPlaylist(PlaylistEntity(name = name))
    }

    suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylist(playlistId)
    }

    suspend fun renamePlaylist(playlistId: Long, newName: String) {
        playlistDao.renamePlaylist(playlistId, newName)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: String) {
        playlistDao.addSongToPlaylist(PlaylistSongCrossRef(playlistId, songId))
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: String) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    fun getSongsForPlaylist(playlistId: Long): Flow<List<String>> {
        return playlistDao.getSongsForPlaylist(playlistId)
    }

    fun getPlaylistById(playlistId: Long): Flow<PlaylistEntity?> {
        return playlistDao.getPlaylistById(playlistId)
    }
}
