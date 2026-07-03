package com.example.vnylplayer.player

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline
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

    // Shuffle Matrix Tracking isolating queue interactions safely over Media3
    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()
    
    // Explicit Dynamic Queue Visibility rendering ExoPlayer bounds visually organically 
    private val _currentQueue = MutableStateFlow<List<Song>>(emptyList())
    val currentQueue: StateFlow<List<Song>> = _currentQueue.asStateFlow()

    private val _currentQueueIndex = MutableStateFlow(0)
    val currentQueueIndex: StateFlow<Int> = _currentQueueIndex.asStateFlow()

    init {
        initializeController()
        loadLibrary() // Proactively fetch persistent Room data eliminating empty state on relaunch
        
        // Asynchronous UI Progress Ticker safely updating both position and derived duration limits natively
        viewModelScope.launch {
            while(true) {
                val p = player
                if (_isPlaying.value || p?.playbackState == androidx.media3.common.Player.STATE_READY) {
                    _progress.value = p?.currentPosition?.coerceAtLeast(0L) ?: 0L
                    
                    val exoDuration = p?.duration ?: 0L
                    if (exoDuration > 0L && _currentSong.value?.durationMs != exoDuration) {
                        _currentSong.value = _currentSong.value?.copy(durationMs = exoDuration)
                    }
                }
                delay(500L) // UI refresh mapped to a tight 500ms pulse for smooth visual sliders
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
                _isShuffleEnabled.value = player?.shuffleModeEnabled ?: false
                refreshQueueState()
                
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
            
            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                super.onTimelineChanged(timeline, reason)
                refreshQueueState()
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
                
                // Track explicitly against dynamic Timeline sequences structurally binding Queue tracking natively!
                refreshQueueState() 
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                super.onShuffleModeEnabledChanged(shuffleModeEnabled)
                _isShuffleEnabled.value = shuffleModeEnabled
                refreshQueueState() // Re-scrambles visual arrays instantly natively syncing algorithms
            }
        })
    }
    
    // Parses raw ExoPlayer timeline limits translating actual algorithmic bounds securely ignoring static iterators!
    private fun refreshQueueState() {
        val p = player ?: return
        val timeline = p.currentTimeline
        if (timeline.isEmpty) {
            _currentQueue.value = emptyList()
            return
        }

        val tempQueue = mutableListOf<Song>()
        val baseLibrary = _songs.value
        var logicalActiveQueueIndex = 0
        var loopCount = 0

        // Always trace iteration from Media3 dynamically depending on toggle state natively
        val shuffleMode = p.shuffleModeEnabled
        var currentIndex = timeline.getFirstWindowIndex(shuffleMode)

        while (currentIndex != C.INDEX_UNSET) {
            val item = p.getMediaItemAt(currentIndex)
            val originalSrc = baseLibrary.find { it.id == item.mediaId }

            if (currentIndex == p.currentMediaItemIndex) {
                logicalActiveQueueIndex = loopCount
            }

            tempQueue.add(
                Song(
                    id = item.mediaId,
                    title = item.mediaMetadata.title?.toString() ?: "Unknown Track",
                    artist = item.mediaMetadata.artist?.toString() ?: "Unknown Artist",
                    durationMs = originalSrc?.durationMs ?: 0L,
                    uri = item.mediaId,
                    artworkUri = item.mediaMetadata.artworkUri?.toString()
                )
            )

            loopCount++
            currentIndex = timeline.getNextWindowIndex(currentIndex, Player.REPEAT_MODE_OFF, shuffleMode)
        }

        _currentQueue.value = tempQueue
        _currentQueueIndex.value = logicalActiveQueueIndex
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

    // High-performance asynchronous queue injection safely bypassing playback interruptions organically!
    fun addToQueue(song: Song) {
        val executionLayer = {
            val mediaItem = MediaItem.Builder()
                .setMediaId(song.uri)
                .setUri(song.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setArtworkUri(if (song.artworkUri != null) android.net.Uri.parse(song.artworkUri) else null)
                        .build()
                )
                .build()
            player?.addMediaItem(mediaItem) // Push to end natively!

            // Reactively spark playback if queue was dead
            if (player?.playbackState == Player.STATE_IDLE) {
                player?.prepare()
                player?.play()
            }
        }

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

    // Deep Media3 Shuffle Engine implicitly securing isolated subset queues flawlessly natively
    fun toggleShuffle() {
        val nextMode = !(_isShuffleEnabled.value)
        _isShuffleEnabled.value = nextMode
        player?.shuffleModeEnabled = nextMode
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
