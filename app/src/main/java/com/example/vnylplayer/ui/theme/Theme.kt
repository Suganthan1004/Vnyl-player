package com.example.vnylplayer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = LuxuryCrimson,
    secondary = MutedSlate,
    background = Obsidian,
    surface = DeepCharcoal,
    onPrimary = Obsidian,
    onSecondary = Obsidian,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
)

@Composable
fun VnylPlayerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
