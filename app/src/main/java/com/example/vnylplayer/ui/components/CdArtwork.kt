package com.example.vnylplayer.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import kotlinx.coroutines.isActive
import coil.compose.AsyncImage
import com.example.vnylplayer.R

@Composable
fun CdArtwork(
    size: Dp,
    isPlaying: Boolean = false,
    artworkUri: String? = null,
    isPlaylist: Boolean = false,
    currentProgressMs: Long = 0L,
    totalDurationMs: Long = 0L,
    onScrub: ((Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var currentScrubProgress by remember { mutableLongStateOf(0L) }
    
    val degreesPerMs = 360f / 10000f // 10 seconds per rotation

    // Ultra smooth 120fps rotation frame buffer natively decoupling from ExoPlayer constraints
    LaunchedEffect(isPlaying, isDragging) {
        if (isPlaying && !isDragging) {
            var lastTime = withFrameNanos { it }
            while (isActive) {
                val currentTime = withFrameNanos { it }
                val deltaMs = (currentTime - lastTime) / 1_000_000f
                rotationAngle = (rotationAngle + (deltaMs * degreesPerMs)) % 360f
                lastTime = currentTime
            }
        }
    }

    LaunchedEffect(artworkUri) {
        rotationAngle = 0f // Resets physics strictly matching raw new physical vinyl structures
    }

    val updatedProgress by rememberUpdatedState(currentProgressMs)
    val updatedDuration by rememberUpdatedState(totalDurationMs)
    val updatedOnScrub by rememberUpdatedState(onScrub)

    val dragModifier = if (onScrub != null) {
        Modifier.pointerInput(Unit) {
            val center = androidx.compose.ui.geometry.Offset(this.size.width / 2f, this.size.height / 2f)
            var lastAngle = 0f

            detectDragGestures(
                onDragStart = { offset ->
                    isDragging = true
                    currentScrubProgress = updatedProgress
                    lastAngle = Math.toDegrees(kotlin.math.atan2((offset.y - center.y).toDouble(), (offset.x - center.x).toDouble())).toFloat()
                },
                onDragEnd = { isDragging = false },
                onDragCancel = { isDragging = false },
                onDrag = { change, _ ->
                    val touchAngle = Math.toDegrees(kotlin.math.atan2((change.position.y - center.y).toDouble(), (change.position.x - center.x).toDouble())).toFloat()
                    var deltaAngle = touchAngle - lastAngle

                    // Wrap mathematical vectors organically handling coordinate axis flips
                    if (deltaAngle > 180f) deltaAngle -= 360f
                    else if (deltaAngle < -180f) deltaAngle += 360f

                    rotationAngle = (rotationAngle + deltaAngle) % 360f
                    lastAngle = touchAngle

                    val deltaMs = (deltaAngle / 360f) * 10000f
                    currentScrubProgress = (currentScrubProgress + deltaMs.toLong()).coerceIn(0L, maxOf(0L, updatedDuration))
                    updatedOnScrub?.invoke(currentScrubProgress) // Natively pushes the temporal bindings to ExoPlayer!
                }
            )
        }
    } else Modifier

    Box(
        modifier = modifier
            .size(size)
            .then(dragModifier)
            .rotate(rotationAngle),
        contentAlignment = Alignment.Center
    ) {
        if (isPlaylist) {
            // PLAYLIST MODE: Photorealistic CD Disc Background
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.cd_artwork),
                contentDescription = "Playlist CD Core",
                modifier = Modifier.size(size),
                contentScale = ContentScale.Crop
            )

            // Optional center artwork for playlists
            if (!artworkUri.isNullOrBlank()) {
                AsyncImage(
                    model = android.net.Uri.parse(artworkUri),
                    contentDescription = "Playlist Artwork Layer",
                    modifier = Modifier
                        .size(size * 0.33f)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(size * 0.33f)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                )
            }
        } else {
            // TRACK MODE: Outer CD Disc (Metallic Grooves Vinyl)
            Canvas(modifier = Modifier.size(size)) { // Explicitly mapping absolute strict geometric bounds preventing canvas stretching anomalies
                val center = Offset(size.toPx() / 2, size.toPx() / 2)
                val radius = size.toPx() / 2

                // Base Disc color
                drawCircle(color = Color(0xFF050505), radius = radius, center = center)

                // Shiny structural metallic ring gradient simulating physical CD glare
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0x33FFFFFF),
                            Color.Transparent,
                            Color(0x11FFFFFF),
                            Color.Transparent,
                            Color(0x33FFFFFF)
                        ),
                        center = center
                    ),
                    radius = radius,
                    center = center
                )

                // Disc physical grooves (Subtle concentric micro-circles)
                for (i in 2..10) {
                    drawCircle(
                        color = Color(0x0AFFFFFF),
                        radius = radius * (1f - (i * 0.05f)),
                        center = center,
                        style = Stroke(width = 1f)
                    )
                }
            }

            // Layer 2: Center Circular Cutout / Album Art
            if (!artworkUri.isNullOrBlank()) {
                AsyncImage(
                    model = android.net.Uri.parse(artworkUri), // Force explicitly converting string models properly resolving SAF endpoints natively 
                    contentDescription = "Album Artwork Layer",
                    modifier = Modifier
                        .size(size * 0.45f) // Dedicated center artwork size mapped securely
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Fallback RED matte Placeholder (Replacing legacy gradients)
                Box(
                    modifier = Modifier
                        .size(size * 0.45f)
                        .clip(CircleShape)
                        .background(Color(0xFF8B0000)) // Dark Scarlet Red (Matte Solid)
                )
            }

            // Layer 3: CD center spindle hole punch
            Box(
                modifier = Modifier
                    .size(size * 0.1f)
                    .clip(CircleShape)
                    .background(Color(0xFF0F0F11)) // Punches out to background color conceptually
            )
        }
    }
}
