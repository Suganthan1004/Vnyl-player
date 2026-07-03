package com.example.vnylplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vnylplayer.player.SharedPlayerViewModel
import com.example.vnylplayer.ui.components.CdArtwork

@Composable
fun PlayerScreen(
    playerViewModel: SharedPlayerViewModel,
    onNavigateBack: () -> Unit
) {

    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val currentSong by playerViewModel.currentSong.collectAsState()
    val progress by playerViewModel.progress.collectAsState()

    // Safely enforce Slider bounds. Empty/Null tracks physically crash Compose if valueRange equates to 0f..0f
    val safeDuration = (currentSong?.durationMs ?: 1L).coerceAtLeast(1L)
    val safeProgress = progress.toFloat().coerceIn(0f, safeDuration.toFloat())

    fun formatMs(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        return String.format("%02d:%02d", minutes, seconds)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF181516),
                        MaterialTheme.colorScheme.background
                    ),
                    radius = 2000f
                )
            )
            .systemBarsPadding()
    ) {

        // BACK BUTTON
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Minimize Player",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 60.dp),

            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "NOW PLAYING",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // CD ARTWORK
            CdArtwork(
                size = 320.dp,
                isPlaying = isPlaying,
                artworkUri = currentSong?.artworkUri
            )

            Spacer(modifier = Modifier.height(48.dp))

            // SONG INFO
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val hasSong = currentSong != null

                Text(
                    text = if (hasSong) currentSong!!.title else "No Song Playing",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = if (hasSong) currentSong!!.artist else "Start a track to continue",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // PROGRESS SECTION
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {

                Slider(
                    value = safeProgress,
                    onValueChange = {
                        playerViewModel.seekTo(it.toLong())
                    },
                    valueRange = 0f..safeDuration.toFloat(),
                    enabled = currentSong != null,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = formatMs(progress.coerceAtMost(safeDuration)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Text(
                        text = formatMs(safeDuration),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // PLAYER CONTROLS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // PREVIOUS
                IconButton(
                    onClick = { playerViewModel.skipToPrevious() },
                    enabled = currentSong != null
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = if (currentSong != null) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha=0.2f),
                        modifier = Modifier.size(32.dp)
                    )
                }

                // PLAY / PAUSE
                IconButton(
                    onClick = { playerViewModel.togglePlayPause() },
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (currentSong != null) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha=0.2f))
                        .padding(16.dp),
                    enabled = currentSong != null
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector =
                                if (isPlaying) {
                                    Icons.Default.Pause
                                } else {
                                    Icons.Default.PlayArrow
                                },
                            contentDescription = "Play/Pause",
                            tint = if (currentSong != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha=0.3f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // NEXT
                IconButton(
                    onClick = { playerViewModel.skipToNext() },
                    enabled = currentSong != null
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = if (currentSong != null) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha=0.2f),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}