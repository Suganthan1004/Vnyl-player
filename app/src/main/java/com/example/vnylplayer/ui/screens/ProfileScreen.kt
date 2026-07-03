package com.example.vnylplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.clickable
import com.example.vnylplayer.data.Song
import com.example.vnylplayer.player.SharedPlayerViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    playerViewModel: SharedPlayerViewModel,
    onNavigateBack: () -> Unit = {}
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Implements user request: 'select local audio files, scan metadata, extract: title, artist'
    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            coroutineScope.launch {
                val retriever = MediaMetadataRetriever()
                val extractedSongs = uris.mapNotNull { uri ->

                    runCatching {

                        val retriever = MediaMetadataRetriever()

                        context.contentResolver.takePersistableUriPermission(
                            uri,
                            android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )

                        retriever.setDataSource(context, uri)

                        val title =
                            retriever.extractMetadata(
                                MediaMetadataRetriever.METADATA_KEY_TITLE
                            ) ?: "Unknown File"

                        val artist =
                            retriever.extractMetadata(
                                MediaMetadataRetriever.METADATA_KEY_ARTIST
                            ) ?: "Unknown Artist"

                        val duration =
                            retriever.extractMetadata(
                                MediaMetadataRetriever.METADATA_KEY_DURATION
                            )?.toLongOrNull() ?: 1L

                        // SECURE EMBEDDED MP3 ALBUM ART EXTRACTION 
                        // Bypasses unreliable MediaStore heuristics entirely extracting pure physical metadata directly from bytes!
                        var finalArtworkUri: String? = null
                        val rawArtworkBytes = retriever.embeddedPicture
                        if (rawArtworkBytes != null) {
                            val coversDir = java.io.File(context.filesDir, "extracted_covers")
                            if (!coversDir.exists()) coversDir.mkdirs()
                            
                            val artworkHash = rawArtworkBytes.contentHashCode()
                            // Safe file system mappings using standard hashing averting duplication
                            val persistentArtworkFile = java.io.File(coversDir, "cover_$artworkHash.png")
                            if (!persistentArtworkFile.exists()) {
                                persistentArtworkFile.writeBytes(rawArtworkBytes)
                            }
                            finalArtworkUri = android.net.Uri.fromFile(persistentArtworkFile).toString()
                        }

                        retriever.release()

                        Song(
                            id = uri.toString(),
                            title = title,
                            artist = artist,
                            durationMs = duration,
                            uri = uri.toString(),
                            artworkUri = finalArtworkUri // Physically injected bypassing null values natively
                        )

                    }.getOrNull()
                }
                retriever.release()
                
                // Directly pass newly fetched vectors into background SQLite schemas securely
                playerViewModel.importSongs(extractedSongs)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF050505), 
                        MaterialTheme.colorScheme.background 
                    )
                )
            )
            .systemBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                }
                Text(
                    text = "SETTINGS",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Profile Core
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("S", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.displayMedium)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Suganthan", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
            }

            Spacer(modifier = Modifier.height(64.dp))

            // Actions Block
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                
                // Import Component (Global Softened Radii enforced -> 24.dp)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { audioPicker.launch(arrayOf("audio/*")) }, // Launch media picker
                    color = Color(0xFF050505)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Audiotrack, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Import Local Audio", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Notifications
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF050505))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Playback Notifications", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
                    
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                            uncheckedTrackColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}
