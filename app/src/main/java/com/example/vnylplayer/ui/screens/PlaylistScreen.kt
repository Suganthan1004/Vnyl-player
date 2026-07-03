package com.example.vnylplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
fun PlaylistScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF050505), 
                        MaterialTheme.colorScheme.background 
                    ),
                    radius = 2000f
                )
            )
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "PLAYLIST / ARTIST PROFILE",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
            letterSpacing = 2.sp
        )
    }
}
