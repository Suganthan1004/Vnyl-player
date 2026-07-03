package com.example.vnylplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.border
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

    val targetPlaylist by playlistViewModel
        .getPlaylistById(playlistId)
        .collectAsState(initial = null)

    val selectedSongs = remember { mutableStateListOf<Song>() } // Native Multi-Select tracking array bounds

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
                            Color(0xFF050505).copy(alpha = 0.98f),
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
                            .fillMaxWidth()
                            .weight(1f) // Binds layout strictly up letting bottom bar nest dynamically natively!
                    ) {
                        items(availableSongs, key = { it.id }) { song ->
                            val isSelected = selectedSongs.contains(song)
                            SongRow(
                                title = song.title,
                                artist = song.artist,
                                duration = formatMs(song.durationMs),
                                artworkUri = song.artworkUri,
                                showAddButton = true, // EXPLICITLY SET TRUE IN SELECTION MODE ONLY
                                isSelected = isSelected, // Pushes UI state checks dynamically directly down natively
                                onClick = {}, // No playback in this mode natively
                                onAddClick = {
                                    // Toggles selection checkmark natively bounding batches
                                    if (isSelected) {
                                        selectedSongs.remove(song)
                                    } else {
                                        selectedSongs.add(song)
                                    }
                                }
                            )
                        }
                    }
                }

                // BOTTOM ACTION BAR OVERRIDE LOOP
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF000000).copy(alpha = 0.98f)) // Absolute Obsidian bounding baseline natively isolating
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("CANCEL", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                    }

                    Button(
                        onClick = {
                            selectedSongs.forEach { batchSong ->
                                playlistViewModel.addSongToPlaylist(playlistId, batchSong.id)
                            }
                            onDismiss() // Close the UI cleanly when batch inserts flawlessly execute natively
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedSongs.isNotEmpty()) Color(0xFF8B0000).copy(alpha = 0.9f) else Color(0xFF0A0A0A)
                        ),
                        enabled = selectedSongs.isNotEmpty(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (selectedSongs.isNotEmpty()) "ADD SELECTED (${selectedSongs.size})" else "ADD SELECTED",
                            color = if (selectedSongs.isNotEmpty()) Color.White else MaterialTheme.colorScheme.onBackground.copy(alpha=0.5f),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}
