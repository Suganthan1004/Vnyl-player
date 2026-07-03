package com.example.vnylplayer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.vnylplayer.player.SharedPlayerViewModel
import com.example.vnylplayer.ui.navigation.Route
import com.example.vnylplayer.ui.navigation.VnylNavGraph
import androidx.compose.runtime.collectAsState

@Composable
fun MainScaffold(playerViewModel: SharedPlayerViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isPlayerScreen = currentRoute == Route.Player.route
    val isProfileScreen = currentRoute == Route.Profile.route

    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val currentSong by playerViewModel.currentSong.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Main Routing Canvas (Rendered First -> Bottom layer Z=0)
        VnylNavGraph(
            navController = navController,
            playerViewModel = playerViewModel,
            modifier = Modifier.fillMaxSize()
        )

        // Atmospheric Profile & Nav Header (Rendered Second -> Middle layer Z=1)
        AnimatedVisibility(
            visible = !(isPlayerScreen || isProfileScreen),
            enter = fadeIn(tween(600)),
            exit = fadeOut(tween(600)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp, start = 24.dp, end = 24.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT: Seamless Text Navigation (Home | Library)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NavHeaderItem("HOME", currentRoute == Route.Home.route) { navController.navigate(Route.Home.route) }
                    NavHeaderItem("LIBRARY", currentRoute == Route.Library.route) { navController.navigate(Route.Library.route) }
                }

                // RIGHT: Settings Route Action
                IconButton(
                    onClick = { navController.navigate(Route.Profile.route) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // Persistent Floating MiniPlayer with Spatial Fade/Slide (Rendered Last -> Top layer Z=2)
        AnimatedVisibility(
            visible = !isPlayerScreen && currentSong != null,
            enter = fadeIn(tween(800)) + slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(800)
            ),
            exit = fadeOut(tween(800)) + slideOutVertically(
                targetOffsetY = { it / 2 },
                animationSpec = tween(800)
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
        ) {
            currentSong?.let { activeMetadata -> 
                MiniPlayer(
                    title = activeMetadata.title,
                    artist = activeMetadata.artist,
                    artworkUri = activeMetadata.artworkUri,
                    isPlaying = isPlaying,
                    onPlayPauseClick = { playerViewModel.togglePlayPause() },
                    onPlayerClick = { 
                        if (currentRoute != Route.Player.route) {
                            navController.navigate(Route.Player.route) { 
                                launchSingleTop = true 
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun NavHeaderItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, 
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 16.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
            letterSpacing = 4.sp
        )
    }
}
