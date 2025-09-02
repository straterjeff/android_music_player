package com.example.android_music_player.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.android_music_player.data.PlaybackState
import com.example.android_music_player.ui.components.PlayerControls
import com.example.android_music_player.ui.components.SongListItem
import com.example.android_music_player.viewmodel.MusicPlayerViewModel

/**
 * Main music player screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerScreen(
    viewModel: MusicPlayerViewModel = viewModel()
) {
    val songs by viewModel.songs.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var showPlayerControls by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    
    // Show player controls when a song is selected
    LaunchedEffect(playerState.currentSong) {
        showPlayerControls = playerState.currentSong != null
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Music Player") },
            actions = {
                IconButton(onClick = { isSearchExpanded = !isSearchExpanded }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
                IconButton(onClick = { viewModel.refreshSongs() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        )
        
        // Search bar
        if (isSearchExpanded) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    if (it.isBlank()) {
                        viewModel.refreshSongs()
                    } else {
                        viewModel.searchSongs(it)
                    }
                },
                label = { Text("Search songs...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
        
        if (showPlayerControls) {
            // Player Controls Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                PlayerControls(
                    playerState = playerState,
                    onPlayPause = { viewModel.togglePlayPause() },
                    onStop = { 
                        viewModel.stop()
                        showPlayerControls = false
                    },
                    onSkipNext = { viewModel.skipToNext() },
                    onSkipPrevious = { viewModel.skipToPrevious() },
                    onSeek = { position -> viewModel.seekTo(position) },
                    onShuffleToggle = { viewModel.setShuffleEnabled(!playerState.isShuffleEnabled) },
                    onRepeatToggle = { viewModel.setRepeatEnabled(!playerState.isRepeatEnabled) },
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        // Songs List
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when {
                isLoading -> {
                    // Loading state
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading songs...")
                    }
                }
                
                songs.isEmpty() -> {
                    // Empty state
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LibraryMusic,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No songs found",
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Make sure you have audio files on your device and have granted storage permissions.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
                
                else -> {
                    // Songs list
                    val listState = rememberLazyListState()
                    
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        item {
                            // Songs count header
                            Text(
                                text = "${songs.size} song${if (songs.size != 1) "s" else ""} found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        
                        items(
                            items = songs,
                            key = { song -> song.id }
                        ) { song ->
                            SongListItem(
                                song = song,
                                onSongClick = { clickedSong ->
                                    viewModel.playPlaylist(songs, songs.indexOf(clickedSong))
                                    showPlayerControls = true
                                },
                                isCurrentlyPlaying = playerState.currentSong?.id == song.id && 
                                    playerState.playbackState == PlaybackState.PLAYING
                            )
                        }
                    }
                }
            }
        }
    }
}
