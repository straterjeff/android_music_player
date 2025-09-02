package com.example.android_music_player.data

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Utility class to scan for audio files on the device
 */
class MusicScanner(private val context: Context) {
    
    private val contentResolver: ContentResolver = context.contentResolver
    
    /**
     * Scan for all audio files on the device
     */
    suspend fun scanForAudioFiles(): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.ALBUM_ID
        )
        
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} = 1"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
        
        val cursor: Cursor? = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            sortOrder
        )
        
        cursor?.use { cur ->
            val idColumn = cur.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cur.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cur.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cur.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cur.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = cur.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val dateAddedColumn = cur.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val sizeColumn = cur.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val mimeTypeColumn = cur.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val albumIdColumn = cur.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            
            while (cur.moveToNext()) {
                val id = cur.getLong(idColumn)
                val title = cur.getString(titleColumn) ?: "Unknown Title"
                val artist = cur.getString(artistColumn) ?: "Unknown Artist"
                val album = cur.getString(albumColumn) ?: "Unknown Album"
                val duration = cur.getLong(durationColumn)
                val data = cur.getString(dataColumn) ?: ""
                val dateAdded = cur.getLong(dateAddedColumn)
                val size = cur.getLong(sizeColumn)
                val mimeType = cur.getString(mimeTypeColumn) ?: ""
                val albumId = cur.getLong(albumIdColumn)
                
                // Create content URI for the audio file
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                
                // Create album art URI
                val albumArtUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    albumId
                )
                
                val song = Song(
                    id = id,
                    title = title,
                    artist = artist,
                    album = album,
                    duration = duration,
                    uri = contentUri,
                    albumArt = albumArtUri,
                    dateAdded = dateAdded,
                    size = size,
                    mimeType = mimeType
                )
                
                songs.add(song)
            }
        }
        
        songs
    }
    
    /**
     * Get songs by artist
     */
    suspend fun getSongsByArtist(artist: String): List<Song> = withContext(Dispatchers.IO) {
        scanForAudioFiles().filter { it.artist.equals(artist, ignoreCase = true) }
    }
    
    /**
     * Get songs by album
     */
    suspend fun getSongsByAlbum(album: String): List<Song> = withContext(Dispatchers.IO) {
        scanForAudioFiles().filter { it.album.equals(album, ignoreCase = true) }
    }
    
    /**
     * Search songs by title
     */
    suspend fun searchSongs(query: String): List<Song> = withContext(Dispatchers.IO) {
        scanForAudioFiles().filter { 
            it.title.contains(query, ignoreCase = true) ||
            it.artist.contains(query, ignoreCase = true) ||
            it.album.contains(query, ignoreCase = true)
        }
    }
}
