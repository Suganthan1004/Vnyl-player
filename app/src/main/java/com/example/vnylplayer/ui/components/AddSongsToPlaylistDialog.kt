package com.example.vnylplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.vnylplayer.data.Song
import com.example.vnylplayer.player.PlaylistViewModel

@Composable
fun AddSongsToPlaylistDialog(
    playlistId: Long,
    allSongs: List<Song>,
    playlistViewModel: PlaylistViewModel,
    onDismiss: () -> Unit
) {
    // Dynamically retrieve the subtracted sequence of songs!
    val availableSongs by playlistViewModel
        .getAvailableSongsForPlaylist(playlistId, allSongs)
        .collectAsState(initial = emptyList())

    fun formatMs(ms: Long): String {
        val totalSeconds = ms / 1000
        val m = totalSeconds / 60
        val s = totalSeconds % 60
        return String.format("%02d:%02d", m, s)
    }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(32.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1B181C).copy(alpha = 0.98f),
                            Color(0xFF2C0B12).copy(alpha = 0.8f) // Subtle burgundy drop-off
                        )
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                
                // HEADER
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ADD SONGS",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${availableSongs.size} Available Tracks",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("CANCEL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        }

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            elevation = null,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("DONE", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // DYNAMIC SUBTRACTION LIST
                if (availableSongs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(bottom = 100.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No additional tracks available.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 16.dp)
                    ) {
                        items(availableSongs, key = { it.id }) { song ->
                            SongRow(
                                title = song.title,
                                artist = song.artist,
                                duration = formatMs(song.durationMs),
                                artworkUri = song.artworkUri,
                                showAddButton = true, // EXPLICITLY SET TRUE IN SELECTION MODE ONLY
                                onClick = {}, // No playback in this mode
                                onAddClick = {
                                    // Instantly triggers Room Insertion -> Re-evaluates getAvailableSongsForPlaylist -> Song vanishes gracefully!
                                    playlistViewModel.addSongToPlaylist(playlistId, song.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
