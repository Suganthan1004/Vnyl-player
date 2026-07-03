package com.example.vnylplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vnylplayer.player.SharedPlayerViewModel
import com.example.vnylplayer.player.PlaylistViewModel
import com.example.vnylplayer.ui.components.CreatePlaylistDialog
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.TextButton
import androidx.compose.foundation.horizontalScroll
import com.example.vnylplayer.data.Song
import com.example.vnylplayer.ui.components.SongRow
import com.example.vnylplayer.ui.components.AddToPlaylistDialog

@Composable
fun LibraryScreen(playerViewModel: SharedPlayerViewModel, playlistViewModel: PlaylistViewModel, onPlaylistClick: (Long) -> Unit) {
    val allSongs by playerViewModel.songs.collectAsState()
    val albums by playerViewModel.albums.collectAsState()
    val artists by playerViewModel.artists.collectAsState()
    
    val playlists by playlistViewModel.allPlaylists.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var songToAdd by remember { mutableStateOf<Song?>(null) }

    fun formatMs(ms: Long): String {
        val totalSeconds = ms / 1000
        val m = totalSeconds / 60
        val s = totalSeconds % 60
        return String.format("%02d:%02d", m, s)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF131518), 
                        MaterialTheme.colorScheme.background 
                    ),
                    radius = 2000f
                )
            )
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 120.dp, top = 40.dp)
        ) {
            Text(
                text = "LIBRARY",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))
            
            // Sub-category grouping explicitly outlining user request bounds
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text("PLAYLISTS", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary, letterSpacing = 2.sp)
                TextButton(onClick = { showDialog = true }) {
                    Text("+ NEW", color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            if (playlists.isEmpty()) {
                Text("Tap + NEW to construct a persistent custom Room database set.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary.copy(0.6f), modifier = Modifier.padding(horizontal = 24.dp))
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    playlists.forEach { playlist ->
                        PlaylistCard(
                            playlistId = playlist.playlistId,
                            title = playlist.name, 
                            playlistViewModel = playlistViewModel,
                            allSongs = allSongs,
                            onClick = { onPlaylistClick(playlist.playlistId) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))

            Text("ALL SONGS (${allSongs.size})", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary, letterSpacing = 2.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(16.dp))
            
            Column {
                if (allSongs.isEmpty()) {
                    Text("No local tracks found. Import dynamically via Settings.", modifier = Modifier.padding(horizontal = 24.dp), color = MaterialTheme.colorScheme.secondary)
                } else {
                    allSongs.forEach { song ->
                        SongRow(
                            title = song.title,
                            artist = song.artist,
                            duration = formatMs(song.durationMs),
                            artworkUri = song.artworkUri,
                            showAddButton = false, // Defaults to purely play bounds natively until an explicit Select mode renders!
                            onClick = { playerViewModel.playQueue(allSongs, allSongs.indexOf(song)) },
                            onAddClick = { songToAdd = song }
                        )
                    }
                }
            }
        }
        
        songToAdd?.let { selectedSong ->
            AddToPlaylistDialog(
                playlists = playlists,
                onDismiss = { songToAdd = null },
                onPlaylistSelected = { playlistId ->
                    playlistViewModel.addSongToPlaylist(playlistId, selectedSong.id)
                    songToAdd = null
                }
            )
        }
        
        if (showDialog) {
            CreatePlaylistDialog(
                onDismiss = { showDialog = false },
                onSubmit = { name ->
                    playlistViewModel.createPlaylist(name)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
private fun PlaylistCard(playlistId: Long, title: String, playlistViewModel: PlaylistViewModel, allSongs: List<Song>, onClick: () -> Unit) {
    val assignedSongs by playlistViewModel.getSongsForPlaylist(playlistId).collectAsState(initial = emptyList())
    val actualCount = allSongs.count { it.id in assignedSongs }
    
    Column(
        horizontalAlignment = androidx.compose.ui.Alignment.Start, 
        modifier = Modifier.clickable(onClick = onClick).width(140.dp)
    ) {
        com.example.vnylplayer.ui.components.CdArtwork(
            size = 140.dp,
            isPlaying = false,
            artworkUri = null // Room entities explicitly don't hold art directly unless bound natively.
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground, maxLines = 1)
        Text(text = "$actualCount tracks", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary, maxLines = 1)
    }
}
