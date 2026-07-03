package com.example.vnylplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import com.example.vnylplayer.player.PlaylistViewModel
import com.example.vnylplayer.player.SharedPlayerViewModel
import com.example.vnylplayer.ui.components.SongRow
import com.example.vnylplayer.ui.components.AddSongsToPlaylistDialog

@Composable
fun PlaylistDetailScreen(
    playlistId: Long,
    playerViewModel: SharedPlayerViewModel,
    playlistViewModel: PlaylistViewModel,
    onNavigateToPlayer: () -> Unit = {}
) {

    val playlistSongsIds by playlistViewModel
        .getSongsForPlaylist(playlistId)
        .collectAsState(initial = emptyList())

    val targetPlaylist by playlistViewModel
        .getPlaylistById(playlistId)
        .collectAsState(initial = null)

    var showAddSongsDialog by remember { mutableStateOf(false) }

    val librarySource by playerViewModel
        .songs
        .collectAsState()

    val activeSongs =
        librarySource.filter { song ->
            playlistSongsIds.contains(song.id)
        }

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
                        Color(0xFF050505),
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
                .padding(top = 40.dp)
        ) {

            Text(
                text = targetPlaylist?.name?.uppercase() ?: "PLAYLIST",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${activeSongs.size} Tracks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                if (activeSongs.isNotEmpty()) {
                    val isRunningThisPlaylist = playerViewModel.currentSong.collectAsState().value?.let { activeSongs.contains(it) } == true
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .clickable { playerViewModel.playQueue(activeSongs, 0) }
                            .background(
                                brush = Brush.linearGradient(
                                    colors = if (isRunningThisPlaylist) listOf(
                                        Color(0xCCFF0033), // Brighter crimson while active playing
                                        Color(0x99550011)
                                    ) else listOf(
                                        Color(0x88990011), // Deep obsidian glassmorphic crimson
                                        Color(0x44440008)
                                    )
                                )
                            )
                            .border(
                                1.dp,
                                if (isRunningThisPlaylist) Color(0x44FF6666) else Color(0x22FF3333),
                                RoundedCornerShape(24.dp)
                            )
                            .padding(horizontal = 24.dp, vertical = 10.dp)
                    ) {
                        if (isRunningThisPlaylist) {
                            Text(
                                text = "PLAYING",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFFFEEEE),
                                letterSpacing = 2.sp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Playlist",
                                tint = Color(0xFFFFEEEE)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (activeSongs.isEmpty()) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 120.dp),

                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "No tracks inside this sequence currently.",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

            } else {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 120.dp)
                ) {

                    items(activeSongs) { song ->

                        val index = activeSongs.indexOf(song)

                        SongRow(
                            title = song.title,
                            artist = song.artist,
                            duration = formatMs(song.durationMs),
                            artworkUri = song.artworkUri,
                            showAddButton = false,
                            showOverflowMenu = true, // Enables contextual 3-dot dropdown natively instead of abrupt destructive X hooks!
                            onRemoveClick = { playlistViewModel.removeSongFromPlaylist(playlistId, song.id) }, // Passed dynamically into Context MenuItem
                            onClick = {
                                if (playerViewModel.currentSong.value?.id == song.id) {
                                    onNavigateToPlayer()
                                } else {
                                    playerViewModel.playQueue(
                                        activeSongs,
                                        index
                                    )
                                }
                            },
                            onAddToQueueClick = { playerViewModel.addToQueue(song) }
                        )
                    }
                }
            }
        }
        
        FloatingActionButton(
            onClick = { showAddSongsDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 120.dp, end = 24.dp),
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = androidx.compose.foundation.shape.CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 4.dp, 
                pressedElevation = 8.dp
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Songs")
        }

        if (showAddSongsDialog) {
            AddSongsToPlaylistDialog(
                playlistId = playlistId,
                allSongs = librarySource,
                playlistViewModel = playlistViewModel,
                onDismiss = { showAddSongsDialog = false }
            )
        }
    }
}