package com.example.vnylplayer.ui.navigation

sealed class Route(val route: String) {
    object Home : Route("home")
    object Library : Route("library")
    object Search : Route("search")
    object Player : Route("player")
    object Playlist : Route("playlist")
    object Profile : Route("profile")
    object Artist : Route("artist")
}
