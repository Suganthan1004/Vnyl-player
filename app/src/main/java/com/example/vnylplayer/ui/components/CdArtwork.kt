package com.example.vnylplayer.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import coil.compose.AsyncImage

@Composable
fun CdArtwork(
    size: Dp,
    isPlaying: Boolean = false,
    artworkUri: String? = null,
    modifier: Modifier = Modifier
) {
    // Rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "disc_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .size(size)
            .rotate(if (isPlaying) rotation else 0f),
        contentAlignment = Alignment.Center
    ) {
        // Outer CD Disc (Metallic Grooves)
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(size.toPx() / 2, size.toPx() / 2)
            val radius = size.toPx() / 2

            // Base Disc color
            drawCircle(color = Color(0xFF141416), radius = radius, center = center)

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

            // Inner Artwork Label Component
        Box(
            modifier = Modifier
                .size(size * 0.45f) // The CD label takes 45% of the radius
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF231F20), Color(0xFF131114))
                    )
                ) // Placeholder fallback for missing artwork
        ) {
            
            // Raw Android file Image binding via Coil
            if (artworkUri != null) {
                AsyncImage(
                    model = artworkUri,
                    contentDescription = "Album Artwork Layer",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // CD center spindle hole punch
            Box(
                modifier = Modifier
                    .size(size * 0.1f)
                    .clip(CircleShape)
                    .background(Color(0xFF0F0F11)) // Punches out to background color conceptually
                    .align(Alignment.Center)
            )
        }
    }
}
