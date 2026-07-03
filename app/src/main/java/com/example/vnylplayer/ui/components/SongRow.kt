package com.example.vnylplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SongRow(
    title: String,
    artist: String,
    duration: String,
    artworkUri: String?,
    showAddButton: Boolean = false,
    showRemoveButton: Boolean = false,
    onClick: () -> Unit,
    onAddClick: () -> Unit = {},
    onRemoveClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CdArtwork(size = 44.dp, isPlaying = false, artworkUri = artworkUri)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground, maxLines = 1)
            Text(artist, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary, maxLines = 1)
        }
        Text(duration, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f))
        
        if (showAddButton) {
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onAddClick, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
            }
        } else if (showRemoveButton) {
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onRemoveClick, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
            }
        }
    }
}
