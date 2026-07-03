package com.example.vnylplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MiniPlayer(
    title: String,
    artist: String,
    artworkUri: String?,
    isPlaying: Boolean,
    progressRatio: Float,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onPlayerClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxWidth()
            // Ambient shadow using deep drop-shadow settings softened (32.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
            .clip(RoundedCornerShape(32.dp))
            // Subtle translucent glass layer
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xE6141416), // 90% opacity deep charcoal
                        Color(0xF2070709)  // 95% opacity obsidian
                    )
                )
            )
            .clickable { onPlayerClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .padding(bottom = 2.dp), // Snug padding for elegance escaping edge bar
            verticalAlignment = Alignment.CenterVertically
        ) {
        // Integrate Global CD Graphic system
        com.example.vnylplayer.ui.components.CdArtwork(
            size = 48.dp,
            isPlaying = isPlaying,
            artworkUri = artworkUri
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground, maxLines = 1)
            Text(artist, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary, maxLines = 1)
        }
        
        IconButton(onClick = onPreviousClick) {
            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = MaterialTheme.colorScheme.onBackground)
        }
        
        IconButton(onClick = onPlayPauseClick) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Play/Pause",
                tint = primaryColor,
                modifier = Modifier
                    .size(40.dp)
                    .drawBehind {
                        // Very subtle ambient ring around play button
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.08f),
                            radius = size.width / 2
                        )
                    }
                    .padding(8.dp)
            )
        }
        
        IconButton(onClick = onNextClick) {
            Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = MaterialTheme.colorScheme.onBackground)
        }
    }
    
    // Explicit Tactile Edge Tracker naturally synchronized globally!
    Box(
        modifier = Modifier
            .fillMaxWidth(if (progressRatio > 0f) progressRatio else 0.001f) // Prevent 0-width collapse
            .height(3.dp) // Thicker presence structurally
            .align(Alignment.BottomStart)
            // Glowing transparent crimson flush with the layout bounds organically!
            .background(Color(0xFFE63946)) 
    )
}}
