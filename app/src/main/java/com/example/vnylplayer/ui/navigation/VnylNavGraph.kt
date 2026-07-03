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
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun VnylNavGraph(
    navController: NavHostController,
    playerViewModel: SharedPlayerViewModel,
    pagerState: PagerState,
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
            val coroutineScope = rememberCoroutineScope()
            // Bound Spatial Pager structurally encapsulating core top-level nodes flawlessly
            HorizontalPager(state = pagerState) { page ->
                when (page) {
                    0 -> HomeScreen(
                        playerViewModel = playerViewModel,
                        playlistViewModel = playlistViewModel,
                        onPlaylistClick = { id -> navController.navigate("playlist/$id") },
                        onSeeAllSongsClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                        onArtistClick = { artistName -> 
                            val encoded = java.net.URLEncoder.encode(artistName, "UTF-8")
                            navController.navigate("artist/$encoded") 
                        },
                        onNavigateToPlayer = { navController.navigate(Route.Player.route) }
                    )
                    1 -> LibraryScreen(
                        playerViewModel = playerViewModel, 
                        playlistViewModel = playlistViewModel,
                        onPlaylistClick = { id -> navController.navigate("playlist/$id") },
                        onNavigateToPlayer = { navController.navigate(Route.Player.route) }
                    ) 
                }
            }
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
            PlaylistDetailScreen(
                playlistId = id, 
                playerViewModel = playerViewModel, 
                playlistViewModel = playlistViewModel,
                onNavigateToPlayer = { navController.navigate(Route.Player.route) }
            ) 
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
            ArtistProfileScreen(
                artistName = decodedName, 
                playerViewModel = playerViewModel,
                onNavigateToPlayer = { navController.navigate(Route.Player.route) }
            )
        }
    }
}
