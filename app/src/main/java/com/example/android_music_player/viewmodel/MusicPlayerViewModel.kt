package com.example.android_music_player.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.android_music_player.data.MusicScanner
import com.example.android_music_player.data.PlayerState
import com.example.android_music_player.data.PlaybackState
import com.example.android_music_player.data.Song
import com.example.android_music_player.service.MusicService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing music player state and operations
 */
class MusicPlayerViewModel(application: Application) : AndroidViewModel(application) {
    
    private val context = getApplication<Application>()
    private val musicScanner = MusicScanner(context)
    
    private var mediaController: MediaController? = null
    private var controllerFuture: ListenableFuture<MediaController>? = null
    
    // State flows
    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()
    
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _currentPlaylist = MutableStateFlow<List<Song>>(emptyList())
    val currentPlaylist: StateFlow<List<Song>> = _currentPlaylist.asStateFlow()
    
    private val _currentSongIndex = MutableStateFlow(0)
    val currentSongIndex: StateFlow<Int> = _currentSongIndex.asStateFlow()
    
    // Player listener for state changes
    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            updatePlaybackState(playbackState)
        }
        
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            val state = if (isPlaying) PlaybackState.PLAYING else PlaybackState.PAUSED
            _playerState.value = _playerState.value.copy(playbackState = state)
        }
        
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            mediaItem?.let {
                updateCurrentSong(it.mediaId)
            }
        }
    }
    
    init {
        initializeMediaController()
        startPositionUpdates()
        loadSongs()
    }
    
    private fun initializeMediaController() {
        val sessionToken = SessionToken(context, android.content.ComponentName(context, MusicService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
            mediaController?.addListener(playerListener)
        }, MoreExecutors.directExecutor())
    }
    
    private fun loadSongs() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val scannedSongs = musicScanner.scanForAudioFiles()
                _songs.value = scannedSongs
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun startPositionUpdates() {
        viewModelScope.launch {
            while (true) {
                mediaController?.let { controller ->
                    if (controller.isPlaying) {
                        val currentPosition = controller.currentPosition
                        val duration = controller.duration
                        val currentSong = getCurrentSong()
                        
                        _playerState.value = _playerState.value.copy(
                            currentPosition = currentPosition,
                            duration = duration,
                            currentSong = currentSong,
                            playbackState = if (controller.isPlaying) PlaybackState.PLAYING else PlaybackState.PAUSED
                        )
                    }
                }
                delay(1000) // Update every second
            }
        }
    }
    
    private fun getCurrentSong(): Song? {
        val playlist = _currentPlaylist.value
        val index = _currentSongIndex.value
        return if (playlist.isNotEmpty() && index in playlist.indices) {
            playlist[index]
        } else null
    }
    
    /**
     * Play a specific song
     */
    fun playSong(song: Song) {
        val mediaItem = MediaItem.Builder()
            .setUri(song.uri)
            .setMediaId(song.id.toString())
            .build()
        
        mediaController?.setMediaItem(mediaItem)
        mediaController?.prepare()
        mediaController?.play()
        
        _currentPlaylist.value = listOf(song)
        _currentSongIndex.value = 0
        updatePlayerState(PlaybackState.PLAYING, song)
    }
    
    /**
     * Play a list of songs starting from a specific index
     */
    fun playPlaylist(playlist: List<Song>, startIndex: Int = 0) {
        if (playlist.isNotEmpty() && startIndex in playlist.indices) {
            val mediaItems = playlist.map { song ->
                MediaItem.Builder()
                    .setUri(song.uri)
                    .setMediaId(song.id.toString())
                    .build()
            }
            
            mediaController?.setMediaItems(mediaItems, startIndex, 0L)
            mediaController?.prepare()
            mediaController?.play()
            
            _currentPlaylist.value = playlist
            _currentSongIndex.value = startIndex
            updatePlayerState(PlaybackState.PLAYING, playlist[startIndex])
        }
    }
    
    /**
     * Toggle play/pause
     */
    fun togglePlayPause() {
        mediaController?.let { controller ->
            if (controller.isPlaying) {
                controller.pause()
            } else {
                controller.play()
            }
        }
    }
    
    /**
     * Stop playback
     */
    fun stop() {
        mediaController?.stop()
        _playerState.value = _playerState.value.copy(
            playbackState = PlaybackState.STOPPED,
            currentPosition = 0L
        )
    }
    
    /**
     * Skip to next track
     */
    fun skipToNext() {
        mediaController?.let { controller ->
            if (controller.hasNextMediaItem()) {
                controller.seekToNext()
                val playlist = _currentPlaylist.value
                val currentIndex = _currentSongIndex.value
                if (currentIndex < playlist.size - 1) {
                    _currentSongIndex.value = currentIndex + 1
                }
            }
        }
    }
    
    /**
     * Skip to previous track
     */
    fun skipToPrevious() {
        mediaController?.let { controller ->
            if (controller.hasPreviousMediaItem()) {
                controller.seekToPrevious()
                val playlist = _currentPlaylist.value
                val currentIndex = _currentSongIndex.value
                if (currentIndex > 0) {
                    _currentSongIndex.value = currentIndex - 1
                }
            }
        }
    }
    
    /**
     * Seek to a specific position
     */
    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
        _playerState.value = _playerState.value.copy(currentPosition = positionMs)
    }
    
    /**
     * Set shuffle mode
     */
    fun setShuffleEnabled(enabled: Boolean) {
        mediaController?.shuffleModeEnabled = enabled
        _playerState.value = _playerState.value.copy(isShuffleEnabled = enabled)
    }
    
    /**
     * Set repeat mode
     */
    fun setRepeatEnabled(enabled: Boolean) {
        val repeatMode = if (enabled) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
        mediaController?.repeatMode = repeatMode
        _playerState.value = _playerState.value.copy(isRepeatEnabled = enabled)
    }
    
    /**
     * Search songs
     */
    fun searchSongs(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val searchResults = musicScanner.searchSongs(query)
                _songs.value = searchResults
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Refresh song list
     */
    fun refreshSongs() {
        loadSongs()
    }
    
    private fun updatePlayerState(playbackState: PlaybackState, song: Song) {
        _playerState.value = _playerState.value.copy(
            playbackState = playbackState,
            currentSong = song
        )
    }
    
    private fun updatePlaybackState(playbackState: Int) {
        val state = when (playbackState) {
            Player.STATE_IDLE -> PlaybackState.STOPPED
            Player.STATE_BUFFERING -> PlaybackState.LOADING
            Player.STATE_READY -> if (mediaController?.isPlaying == true) PlaybackState.PLAYING else PlaybackState.PAUSED
            Player.STATE_ENDED -> PlaybackState.STOPPED
            else -> PlaybackState.STOPPED
        }
        _playerState.value = _playerState.value.copy(playbackState = state)
    }
    
    private fun updateCurrentSong(mediaId: String?) {
        mediaId?.let { id ->
            val songId = id.toLongOrNull()
            val playlist = _currentPlaylist.value
            val song = playlist.find { it.id == songId }
            song?.let {
                _playerState.value = _playerState.value.copy(currentSong = it)
                _currentSongIndex.value = playlist.indexOf(it)
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        mediaController?.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}
