package com.example.vnylplayer.player

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.vnylplayer.data.Song
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.example.vnylplayer.data.MusicRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SharedPlayerViewModel(application: Application) : AndroidViewModel(application) {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    var player: Player? = null
        private set
        
    private val repository = MusicRepository(application)

    // Core MediaStore Library Flows dynamically mapping raw system arrays
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _albums = MutableStateFlow<List<String>>(emptyList())
    val albums: StateFlow<List<String>> = _albums.asStateFlow()

    private val _artists = MutableStateFlow<List<String>>(emptyList())
    val artists: StateFlow<List<String>> = _artists.asStateFlow()

    private val _recentlyPlayed = MutableStateFlow<List<Song>>(emptyList())
    val recentlyPlayed: StateFlow<List<Song>> = _recentlyPlayed.asStateFlow()

    // Native State Flows abstracting ExoPlayer logic beautifully into Jetpack Compose UI
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()
    
    private val _progress = MutableStateFlow(0L)
    val progress: StateFlow<Long> = _progress.asStateFlow()

    init {
        initializeController()
        
        // Asynchronous UI Progress Ticker (Saves extreme battery compared to raw listener loops)
        viewModelScope.launch {
            while(true) {
                if (_isPlaying.value) {
                    _progress.value = player?.currentPosition ?: 0L
                }
                delay(1000L) // UI refresh mapped to a relaxed 1-second pulse for cinematic smoothness without drain
            }
        }
    }
    
    fun importSongs(songs: List<Song>) {
        viewModelScope.launch {
            repository.saveImportedSongs(songs)
            loadLibrary() // refresh library mapping the new metadata dynamically
        }
    }
    
    // Exposed to Activity layer for forced reload after fetching Android dangerous permissions
    fun loadLibrary() {
        viewModelScope.launch {
            val localSongs = repository.getLocalSongs()
            _songs.value = localSongs
            
            // Derive unique collections directly avoiding secondary SQL query overhead
            _albums.value = localSongs.mapNotNull { it.albumName }.distinct().sorted()
            _artists.value = localSongs.map { it.artist }.distinct().sorted()
            
            // Safely mapping a pseudo-recently played list dynamically for now
            _recentlyPlayed.value = localSongs.shuffled().take(10)
        }
    }

    private fun initializeController() {
        val applicationContext = getApplication<Application>().applicationContext
        val sessionToken = SessionToken(
            applicationContext,
            ComponentName(applicationContext, VnylPlaybackService::class.java)
        )

        controllerFuture = MediaController.Builder(applicationContext, sessionToken).buildAsync()
        controllerFuture?.addListener(
            {
                player = controllerFuture?.get()
                setupPlayerListener()
                
                // Safely reconnect UI tracking bridging the gap from background service persistence
                _isPlaying.value = player?.isPlaying == true
                player?.currentMediaItem?.mediaMetadata?.let { meta ->
                    _currentSong.value = Song(
                        id = player?.currentMediaItem?.mediaId ?: "",
                        title = meta.title?.toString() ?: "Unknown Track",
                        artist = meta.artist?.toString() ?: "Unknown Artist",
                        durationMs = player?.duration ?: 0L,
                        uri = player?.currentMediaItem?.mediaId ?: "",
                        artworkUri = meta.artworkUri?.toString()
                    )
                }
            },
            MoreExecutors.directExecutor()
        )
    }

    private fun setupPlayerListener() {
        player?.addListener(object : Player.Listener { // Listen for external playback changes (Background, lockscreen)
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                // Bridge raw ExoPlayer item mapping back into our precise Song domain model
                mediaItem?.mediaMetadata?.let { meta ->
                    _currentSong.value = Song(
                        id = mediaItem.mediaId,
                        title = meta.title?.toString() ?: "Unknown Track",
                        artist = meta.artist?.toString() ?: "Unknown Artist",
                        durationMs = player?.duration ?: 0L,
                        uri = mediaItem.mediaId, 
                        artworkUri = meta.artworkUri?.toString()
                    )
                } ?: run {
                    _currentSong.value = null
                }
            }
        })
    }
    
    // Safely translates a local library array block into the ExoPlayer internal queue system natively
    fun playQueue(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty() || startIndex < 0 || startIndex >= songs.size) return
        
        val executionLayer = {
            val mediaItems = songs.map { song ->
                MediaItem.Builder()
                    .setMediaId(song.uri)
                    .setUri(song.uri)
                    .setMediaMetadata( // Hardcode metadata to prevent ExoPlayer from overwriting it poorly on some OEM OS
                        MediaMetadata.Builder()
                            .setTitle(song.title)
                            .setArtist(song.artist)
                            .setArtworkUri(if (song.artworkUri != null) android.net.Uri.parse(song.artworkUri) else null)
                            .build()
                    )
                    .build()
            }
            
            player?.setMediaItems(mediaItems, startIndex, C.TIME_UNSET)
            player?.prepare()
            player?.play()
        }
        
        // Stabilize Player initialization blocking preventing NullPointerExceptions across UI actions
        if (player != null) {
            executionLayer()
        } else {
            controllerFuture?.addListener({ executionLayer() }, MoreExecutors.directExecutor())
        }
    }

    fun togglePlayPause() {
        if (player?.isPlaying == true) {
            player?.pause()
        } else {
            player?.play()
        }
    }

    fun skipToNext() {
        player?.seekToNextMediaItem()
    }

    fun skipToPrevious() {
        player?.seekToPreviousMediaItem()
    }
    
    // Seek action wired for Slider tracking
    fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
        _progress.value = positionMs
    }

    override fun onCleared() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
        super.onCleared()
    }
}
