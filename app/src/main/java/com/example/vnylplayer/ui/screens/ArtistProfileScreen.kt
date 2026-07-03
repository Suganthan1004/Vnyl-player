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
import com.example.vnylplayer.player.SharedPlayerViewModel
import com.example.vnylplayer.ui.components.SongRow

@Composable
fun ArtistProfileScreen(
    artistName: String,
    playerViewModel: SharedPlayerViewModel
) {
    val librarySource by playerViewModel.songs.collectAsState()

    // Strictly isolates rendering exclusively to songs matching this exact artist.
    val artistSongs = librarySource.filter { song ->
        song.artist.equals(artistName, ignoreCase = true)
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
                .padding(top = 40.dp)
        ) {
            Text(
                text = artistName.uppercase(),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${artistSongs.size} Tracks",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (artistSongs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tracks bound to this artist locally.",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 120.dp)
                ) {
                    items(artistSongs) { song ->
                        val index = artistSongs.indexOf(song)
                        SongRow(
                            title = song.title,
                            artist = song.artist,
                            duration = formatMs(song.durationMs),
                            artworkUri = song.artworkUri,
                            showAddButton = false,
                            showRemoveButton = false, // Strictly not a playlist, tracks can't be "removed" from a static artist
                            onClick = {
                                playerViewModel.playQueue(
                                    artistSongs,
                                    index
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
