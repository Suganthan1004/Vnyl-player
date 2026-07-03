package com.example.vnylplayer.player

// Interface outlining ExoPlayer or similar integration state
interface PlayerState {
    val isPlaying: Boolean
    val currentPosition: Long
    fun play()
    fun pause()
    fun seekTo(position: Long)
}
