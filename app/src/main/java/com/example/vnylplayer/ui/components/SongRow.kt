package com.example.vnylplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.RoundedCornerShape
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
    isSelected: Boolean = false,
    showRemoveButton: Boolean = false, // Deprecated globally
    showOverflowMenu: Boolean = false,
    onClick: () -> Unit,
    onAddClick: () -> Unit = {},
    onRemoveClick: () -> Unit = {},
    onAddToQueueClick: (() -> Unit)? = null // Bind queue dynamics cleanly
) {
    var overflowExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (isSelected) Color(0x11FF0033) else Color.Transparent) // Crimson selected row ambient mapping natively
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
                if (isSelected) {
                    Icon(Icons.Default.Check, contentDescription = "Selected", tint = Color(0xFFFF5555)) // Deep crimson checkbox checked state natively
                } else {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                }
            }
        } else if (showOverflowMenu || onAddToQueueClick != null || showRemoveButton) {
            Spacer(modifier = Modifier.width(8.dp))
            Box {
                IconButton(onClick = { overflowExpanded = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = MaterialTheme.colorScheme.secondary)
                }

                DropdownMenu(
                    expanded = overflowExpanded,
                    onDismissRequest = { overflowExpanded = false },
                    modifier = Modifier
                        .background(Color(0xFF050505).copy(alpha = 0.95f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 8.dp)
                ) {
                    if (onAddToQueueClick != null) {
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    "Add to Queue", 
                                    color = MaterialTheme.colorScheme.onBackground, 
                                    style = MaterialTheme.typography.labelLarge 
                                ) 
                            },
                            onClick = {
                                overflowExpanded = false
                                onAddToQueueClick()
                            }
                        )
                    }

                    if (showOverflowMenu || showRemoveButton) {
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    "Remove from Playlist", 
                                    color = Color(0xFFFF5555), // Crimson destructive accent purely contextualized
                                    style = MaterialTheme.typography.labelLarge 
                                ) 
                            },
                            onClick = {
                                overflowExpanded = false
                                onRemoveClick()
                            }
                        )
                    }
                }
            }
        }
    }
}
