package com.example.vnylplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.vnylplayer.player.SharedPlayerViewModel
import com.example.vnylplayer.player.PlaylistViewModel
import com.example.vnylplayer.data.Song
import com.example.vnylplayer.ui.components.SongRow
import com.example.vnylplayer.ui.components.AddToPlaylistDialog

@Composable
fun HomeScreen(
    playerViewModel: SharedPlayerViewModel,
    playlistViewModel: PlaylistViewModel,
    onPlaylistClick: (Long) -> Unit = {},
    onSeeAllSongsClick: () -> Unit = {},
    onArtistClick: (String) -> Unit = {},
    onNavigateToPlayer: () -> Unit = {}
) {
    val recentlyPlayed by playerViewModel.recentlyPlayed.collectAsState()
    val artists by playerViewModel.artists.collectAsState()
    val playlists by playlistViewModel.allPlaylists.collectAsState()
    val allLibrarySongs by playerViewModel.songs.collectAsState() // Elevated globally solving scope leakage natively!
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
                        Color(0xFF050505), // Void black atmospheric core
                        MaterialTheme.colorScheme.background // True pitch edge
                    ),
                    radius = 1800f
                )
            )
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 120.dp, top = 40.dp) // space for MiniPlayer and Header
        ) {
            
            Text(
                text = "DISCOVER",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            SectionTitle("RECENTLY PLAYED")
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (playlists.isEmpty()) {
                    AlbumCard(title = "Night Drive", artist = "Retro Syntax", null, onClick = { onPlaylistClick(1L) })
                    AlbumCard(title = "Lunar Phase", artist = "OLED Heights", null, onClick = { onPlaylistClick(2L) })
                } else {
                    playlists.forEach { playlist ->
                        HomePlaylistCard(
                            playlistId = playlist.playlistId,
                            title = playlist.name,
                            playlistViewModel = playlistViewModel,
                            allSongs = allLibrarySongs,
                            onClick = { onPlaylistClick(playlist.playlistId) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            SectionTitleWithAction("RECENTLY PLAYED SONGS", "See All →", onActionClick = onSeeAllSongsClick)
            Spacer(modifier = Modifier.height(16.dp))
            Column {
                if (recentlyPlayed.isEmpty()) {
                    SongRow("Signal Processing", "Analog Dreams", "04:12", null, showAddButton = false, onClick = {}, onAddClick = {})
                    SongRow("Neon Resonance", "Retro Syntax", "03:45", null, showAddButton = false, onClick = {}, onAddClick = {})
                } else {
                    recentlyPlayed.take(5).forEach { song ->
                        SongRow(
                            title = song.title,
                            artist = song.artist,
                            duration = formatMs(song.durationMs),
                            artworkUri = song.artworkUri,
                            showAddButton = false, // Clean UI rendering purely for play sequences on Home
                            onClick = { 
                                if (playerViewModel.currentSong.value?.id == song.id) {
                                    onNavigateToPlayer()
                                } else {
                                    playerViewModel.playQueue(recentlyPlayed, recentlyPlayed.indexOf(song)) 
                                }
                            },
                            onAddClick = { songToAdd = song },
                            onAddToQueueClick = { playerViewModel.addToQueue(song) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
            
            SectionTitle("ARTISTS")
            Spacer(modifier = Modifier.height(24.dp)) // A bit more spacious for cinematic portraits
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp) // Premium spacing
            ) {
                val topArtists = artists.take(5)
                if (topArtists.isEmpty()) {
                    ArtistCard("Retro Syntax", null, onClick = { onArtistClick("Retro Syntax") })
                    ArtistCard("Cinematic Audio", null, onClick = { onArtistClick("Cinematic Audio") })
                } else {
                    topArtists.forEach { artistName ->
                        // Query the first track structurally containing valid imagery bridging them visually onto the category
                        val artistArtwork = allLibrarySongs.firstOrNull { it.artist == artistName && it.artworkUri != null }?.artworkUri
                        ArtistCard(name = artistName, artworkUri = artistArtwork, onClick = { onArtistClick(artistName) })
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
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.secondary,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(horizontal = 24.dp)
    )
}

@Composable
private fun SectionTitleWithAction(title: String, actionText: String, onActionClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
            letterSpacing = 2.sp
        )
        Text(
            text = actionText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            letterSpacing = 1.sp,
            modifier = Modifier
                .clickable(onClick = onActionClick)
                .padding(vertical = 4.dp, horizontal = 8.dp) // Touch target
        )
    }
}

@Composable
private fun AlbumCard(title: String, artist: String, artworkUri: String?, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.Start, 
        modifier = Modifier.clickable(onClick = onClick).width(140.dp)
    ) {
        com.example.vnylplayer.ui.components.CdArtwork(
            size = 140.dp,
            isPlaying = false,
            artworkUri = artworkUri
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground, maxLines = 1)
        Text(text = artist, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary, maxLines = 1)
    }
}

@Composable
private fun HomePlaylistCard(playlistId: Long, title: String, playlistViewModel: PlaylistViewModel, allSongs: List<Song>, onClick: () -> Unit) {
    val assignedSongs by playlistViewModel.getSongsForPlaylist(playlistId).collectAsState(initial = emptyList())
    val actualCount = allSongs.count { it.id in assignedSongs }
    
    Column(
        horizontalAlignment = Alignment.Start, 
        modifier = Modifier.clickable(onClick = onClick).width(140.dp)
    ) {
        com.example.vnylplayer.ui.components.CdArtwork(
            size = 140.dp,
            isPlaying = false,
            artworkUri = null,
            isPlaylist = true
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground, maxLines = 1)
        Text(text = "$actualCount Tracks", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary, maxLines = 1)
    }
}

@Composable
private fun ArtistCard(name: String, artworkUri: String?, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).width(120.dp)
    ) {
        if (artworkUri != null) {
            AsyncImage(
                model = android.net.Uri.parse(artworkUri),
                contentDescription = "Artist Image for $name",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color(0x0AFFFFFF), CircleShape)
            )
        } else {
            // Cinematic fallback gradient explicitly bound for metadata-less Artists natively
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF0A0A0A), Color(0xFF050505))
                        )
                    )
                    .border(1.dp, Color(0x0AFFFFFF), CircleShape)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1 // Limit overflow intrinsically
        )
    }
}