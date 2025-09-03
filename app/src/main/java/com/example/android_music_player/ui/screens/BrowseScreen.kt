package com.example.android_music_player.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.android_music_player.data.BrowseCategory
import com.example.android_music_player.data.CategoryItem
import com.example.android_music_player.data.PlaybackState
import com.example.android_music_player.ui.components.CategoryCard
import com.example.android_music_player.ui.components.SongListItem
import com.example.android_music_player.viewmodel.MusicPlayerViewModel

/**
 * Screen for browsing music by categories (Artists, Albums, Genres, etc.)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(
    category: BrowseCategory,
    onBackClick: () -> Unit,
    onCategoryItemClick: (CategoryItem) -> Unit = {},
    viewModel: MusicPlayerViewModel = viewModel()
) {
    val categories by viewModel.categories.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val categoryItems = categories[category] ?: emptyList()
    val categoryName = getCategoryDisplayName(category)
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    text = categoryName,
                    fontWeight = FontWeight.Medium
                ) 
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )
        
        // Content
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
                        Text("Loading $categoryName...")
                    }
                }
                
                categoryItems.isEmpty() -> {
                    // Empty state
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No $categoryName found",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "Try adding some music to your device",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                else -> {
                    // Categories list
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(
                            items = categoryItems,
                            key = { it.id }
                        ) { categoryItem ->
                            CategoryCard(
                                categoryItem = categoryItem,
                                onClick = onCategoryItemClick
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Screen for displaying songs within a specific category item
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySongsScreen(
    category: BrowseCategory,
    itemId: String,
    itemName: String,
    onBackClick: () -> Unit,
    viewModel: MusicPlayerViewModel = viewModel()
) {
    val playerState by viewModel.playerState.collectAsState()
    val songs = viewModel.getCategorySongs(category, itemId)
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(
                    text = itemName,
                    fontWeight = FontWeight.Medium
                ) 
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )
        
        // Songs list
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when {
                songs.isEmpty() -> {
                    // Empty state
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No songs found",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
                
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        item {
                            // Header with song count
                            Text(
                                text = "${songs.size} song${if (songs.size != 1) "s" else ""}",
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

/**
 * Get display name for browse categories
 */
private fun getCategoryDisplayName(category: BrowseCategory): String {
    return when (category) {
        BrowseCategory.ALL_SONGS -> "All Songs"
        BrowseCategory.ARTISTS -> "Artists"
        BrowseCategory.ALBUMS -> "Albums"
        BrowseCategory.GENRES -> "Genres"
        BrowseCategory.PLAYLISTS -> "Playlists"
        BrowseCategory.RECENTLY_ADDED -> "Recently Added"
        BrowseCategory.RECENTLY_PLAYED -> "Recently Played"
        BrowseCategory.FAVORITES -> "Favorites"
    }
}
