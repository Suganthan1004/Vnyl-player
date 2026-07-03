package com.example.vnylplayer.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.vnylplayer.ui.screens.LibraryScreen
import com.example.vnylplayer.ui.screens.PlayerScreen
import com.example.vnylplayer.ui.screens.PlaylistDetailScreen
import com.example.vnylplayer.ui.screens.SearchScreen
import com.example.vnylplayer.ui.screens.HomeScreen
import com.example.vnylplayer.ui.screens.ArtistProfileScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vnylplayer.player.PlaylistViewModel
import com.example.vnylplayer.player.SharedPlayerViewModel

@Composable
fun VnylNavGraph(
    navController: NavHostController,
    playerViewModel: SharedPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val playlistViewModel: PlaylistViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Route.Home.route,
        modifier = modifier,
        enterTransition = { fadeIn(tween(800)) },
        exitTransition = { fadeOut(tween(800)) },
        popEnterTransition = { fadeIn(tween(800)) },
        popExitTransition = { fadeOut(tween(800)) }
    ) {
        composable(Route.Home.route) { 
            HomeScreen(
                playerViewModel = playerViewModel,
                playlistViewModel = playlistViewModel,
                onPlaylistClick = { id -> navController.navigate("playlist/$id") },
                onSeeAllSongsClick = { navController.navigate(Route.Library.route) },
                onArtistClick = { artistName -> 
                    val encoded = java.net.URLEncoder.encode(artistName, "UTF-8")
                    navController.navigate("artist/$encoded") 
                }
            )
        }
        composable(Route.Library.route) { 
            LibraryScreen(
                playerViewModel = playerViewModel, 
                playlistViewModel = playlistViewModel,
                onPlaylistClick = { id -> navController.navigate("playlist/$id") }
            ) 
        }
        composable(Route.Search.route) { SearchScreen() }
        composable(Route.Player.route) { 
            PlayerScreen(
                playerViewModel = playerViewModel, 
                onNavigateBack = { navController.popBackStack() }
            ) 
        }
        composable("playlist/{playlistId}") { backStackEntry -> 
            val id = backStackEntry.arguments?.getString("playlistId")?.toLongOrNull() ?: 1L
            PlaylistDetailScreen(id, playerViewModel, playlistViewModel) 
        }
        composable(Route.Profile.route) { 
            com.example.vnylplayer.ui.screens.ProfileScreen(
                playerViewModel = playerViewModel, 
                onNavigateBack = { navController.popBackStack() }
            ) 
        }
        composable("artist/{artistName}") { backStackEntry ->
            val rawName = backStackEntry.arguments?.getString("artistName") ?: "Unknown Artist"
            val decodedName = java.net.URLDecoder.decode(rawName, "UTF-8")
            ArtistProfileScreen(decodedName, playerViewModel)
        }
    }
}
