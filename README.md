# 🎵 jPod - Android Music Player

A feature-rich, modern Android music player built with **Kotlin** and **Jetpack Compose**. jPod provides a comprehensive music library experience with advanced categorization, playlist management, and professional-grade audio playback inspired by classic portable music players.

## ✨ Features

### 🎵 **Core Music Playback**
- **High-quality audio playback** using Media3 ExoPlayer
- **Background playback** with media session support
- **Full playback controls**: Play, Pause, Stop, Skip Next/Previous
- **Seek functionality** with interactive progress bar
- **Shuffle and Repeat modes**
- **Audio focus management** and notification controls

### 📂 **Smart Music Organization**
- **Multi-format support**: MP3, WAV, WMA, FLAC, and more
- **Automatic library scanning** from device storage
- **Rich metadata display**: Title, Artist, Album, Genre, Year, Track Number
- **Album artwork** with fallback icons
- **Recently played tracking**

### 🗂️ **Category Browsing**
Browse your music collection by:
- **🎤 Artists** - Organized by performing artist
- **💿 Albums** - Grouped by album with artist information
- **🎵 Genres** - Musical genres (Pop, Rock, Jazz, Country, etc.)
- **⭐ Favorites** - Your liked songs
- **🕐 Recently Played** - Recently listened tracks
- **📅 Recently Added** - Newest songs on device

### 📋 **Advanced Playlist Management**
- **Create custom playlists** with names and descriptions
- **Add/Remove songs** from any playlist
- **Multiple playlist membership** - songs can belong to multiple playlists
- **Playlist reordering** - drag and drop song organization
- **Persistent storage** - playlists survive app restarts
- **Built-in Favorites** playlist with one-tap favoriting

### 🔍 **Search & Discovery**
- **Comprehensive search** across title, artist, album, and genre
- **Quick category filters** with material design chips
- **Instant results** as you type

### 🎨 **Modern UI/UX**
- **Material Design 3** with beautiful animations
- **Dark/Light theme support** (system-based)
- **Intuitive navigation** with clear visual hierarchy
- **Responsive design** for different screen sizes
- **Accessibility support** with proper content descriptions

## 📱 **Screenshots & UI**

### Main Screen
- Clean song list with album artwork
- Category filter chips for quick browsing
- Search functionality
- Player controls (when playing)

### Category Browsing
- Artist/Album/Genre cards with song counts
- Drill-down navigation to view category contents
- Beautiful category icons and metadata

### Playlist Management
- Create and manage custom playlists
- Add/remove songs with intuitive UI
- Playlist overview with song counts and descriptions

## 🛠️ **Technical Stack**

- **Language**: Kotlin 2.0.21
- **UI Framework**: Jetpack Compose with Material Design 3
- **Audio Engine**: Media3 ExoPlayer
- **Architecture**: MVVM with StateFlow
- **Persistence**: SharedPreferences with kotlinx.serialization
- **Image Loading**: Coil for album artwork
- **Permissions**: Accompanist Permissions
- **Navigation**: Jetpack Navigation Compose

## 🏗️ **Build Instructions**

### Prerequisites
- **Android Studio** Hedgehog (2023.1.1) or later
- **Java 17** or later
- **Android SDK** with minimum API 24 (Android 7.0)
- **Target SDK**: API 35 (Android 14)

### Setup
1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd android_music_player
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory

3. **Sync the project**
   - Android Studio will automatically sync Gradle dependencies
   - Wait for the sync to complete

4. **Build the project**
   ```bash
   ./gradlew build
   ```

5. **Run on device/emulator**
   - Connect an Android device or start an emulator
   - Click "Run" in Android Studio or use:
   ```bash
   ./gradlew installDebug
   ```

### Building APK
```bash
# Debug APK
./gradlew assembleDebug

# Release APK (requires signing)
./gradlew assembleRelease
```

## 🎵 **Testing with Sample Music**

### ADB Setup for File Transfer

To test jPod with your own music files, you'll need **ADB (Android Debug Bridge)** to transfer MP3s to the emulator or device.

#### Installing ADB

**On macOS (recommended):**
```bash
# Install via Homebrew
brew install android-platform-tools
```

**On Windows:**
1. Download [Android Platform Tools](https://developer.android.com/studio/releases/platform-tools)
2. Extract and add to your system PATH
3. Or use Android Studio's built-in ADB: `~/Library/Android/sdk/platform-tools/`

**On Linux:**
```bash
# Ubuntu/Debian
sudo apt install android-tools-adb

# Arch Linux
sudo pacman -S android-tools
```

#### Verify ADB Installation
```bash
adb version
# Should show: Android Debug Bridge version X.X.X
```

### Transferring Music Files

#### 1. Check Connected Devices
```bash
adb devices
# Should show your emulator or device listed
```

#### 2. Transfer Your Music Files
```bash
# Extract your music files locally first
unzip your_music.zip -d ~/jPod_test_music/

# Transfer to artist-specific directory in Music folder (current proven approach)
adb push ~/jPod_test_music/"Artist Name - Album Name"/ /sdcard/Music/"Artist Name"/

# For multiple devices (emulator + physical device):
# Emulator:
adb -s emulator-5554 push ~/jPod_test_music/"Artist Name - Album Name"/ /sdcard/Music/"Artist Name"/

# Physical device (replace DEVICE_ID with your device ID from 'adb devices'):
adb -s DEVICE_ID push ~/jPod_test_music/"Artist Name - Album Name"/ /sdcard/Music/"Artist Name"/
```

#### 3. Trigger Media Scanner
After transferring files, trigger Android's media scanner to detect them:
```bash
# Clear MediaStore cache for reliable indexing (recommended)
adb shell pm clear com.android.providers.media

# Scan artist directory  
adb shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///storage/emulated/0/Music/"Artist Name"

# For multiple devices:
adb -s emulator-5554 shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///storage/emulated/0/Music/"Artist Name"
adb -s DEVICE_ID shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///storage/emulated/0/Music/"Artist Name"
```

#### 5. Install and Test jPod
```bash
# Install the app
./gradlew installDebug

# Launch jPod and grant permissions when prompted
# Your music files should appear automatically!
```

### jPod Music Directory Structure
**jPod uses artist-specific directories within the Music folder** for reliable MediaStore compatibility:

```
/sdcard/Music/
├── Dave Hause/
│   ├── 01 A Knife In The Mud.mp3
│   ├── 02 Cellmates.mp3
│   ├── 03 Look Alive.mp3
│   └── ... (more songs)
├── [Future Artist]/
│   └── [Artist songs]
└── [System retains other app audio in separate directories]
```

**Current working structure (proven on multiple devices):**
```bash
# Transfer music using ADB
adb push /path/to/music/ /sdcard/Music/[ArtistName]/

# Force MediaStore to recognize files as music
adb shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///storage/emulated/0/Music/[ArtistName]
```

**Benefits of this structure:**
- ✅ **Guaranteed MediaStore compatibility**: Works reliably across different Android versions
- ✅ **Clean artist-based filtering**: jPod shows only specified artists, not system audio
- ✅ **Simple management**: Easy to add new artists by creating new directories
- ✅ **Proven stability**: Battle-tested on both emulator and physical devices

### Testing Features
Once music is loaded, test these jPod capabilities:
- ✅ **Playback Controls**: Play, pause, skip, seek
- ✅ **Categories**: Browse by Artist, Album, Genre  
- ✅ **Search**: Find songs by title, artist, album
- ✅ **Playlists**: Create and manage custom playlists
- ✅ **Favorites**: Star/unstar songs
- ✅ **Recently Played**: Check playback history

### Troubleshooting
- **No music appears**: Check permissions and trigger media scanner
- **ADB not found**: Ensure platform-tools are in your PATH
- **Device not detected**: Enable USB Debugging on physical devices
- **Transfer fails**: Check available storage space on device

## 📋 **Permissions Required**

The app requires the following permissions:

### Storage Permissions
- **READ_MEDIA_AUDIO** (Android 13+) - Access audio files
- **READ_EXTERNAL_STORAGE** (Android 12 and below) - Legacy storage access

### Media Playback
- **FOREGROUND_SERVICE** - Background music playback
- **FOREGROUND_SERVICE_MEDIA_PLAYBACK** - Media-specific foreground service
- **WAKE_LOCK** - Prevent device sleep during playback

## 🎯 **Usage Instructions**

### First Launch
1. **Grant permissions** when prompted for storage access
2. The app will **automatically scan** your device for music files
3. **Browse your library** using category chips or the main song list

### Playing Music
1. **Tap any song** to start playback immediately
2. **Tap a category** (Artist, Album, Genre) to browse organized content
3. **Use player controls** that appear when music is playing
4. **Seek within tracks** using the progress bar

### Managing Favorites
1. **Tap the heart icon** on any song to add to favorites
2. **Access favorites** via the Favorites chip or Playlists screen
3. **Remove favorites** by tapping the filled heart icon

### Creating Playlists
1. **Tap the playlist icon** in the top toolbar
2. **Tap the + button** to create a new playlist
3. **Enter playlist name** and optional description
4. **Add songs** from the main library or category views

### Search Functionality
1. **Tap the search icon** in the top toolbar
2. **Type your query** - searches title, artist, album, and genre
3. **Tap any result** to play or view details

### Browsing by Categories
1. **Tap category chips** on the main screen (Artists, Albums, etc.)
2. **Browse organized lists** with song counts and descriptions
3. **Tap any category item** to view its songs
4. **Play entire categories** or individual tracks

## 🏛️ **Architecture Overview**

### Data Layer
- **`Song.kt`** - Core data model with metadata
- **`Playlist.kt`** - Playlist and category definitions
- **`MusicScanner.kt`** - MediaStore integration for file discovery
- **`PlaylistManager.kt`** - Playlist persistence and operations

### UI Layer (Jetpack Compose)
- **`MusicPlayerScreen.kt`** - Main interface with player controls
- **`BrowseScreen.kt`** - Category browsing interface
- **`PlaylistScreen.kt`** - Playlist management interface
- **Reusable components** for songs, categories, and controls

### Business Logic
- **`MusicPlayerViewModel.kt`** - State management and business logic
- **`MusicService.kt`** - Background audio playbook with Media3

### Key Features Implementation
- **State Management**: StateFlow for reactive UI updates
- **Media Session**: Integration with system media controls
- **Persistence**: JSON serialization for playlist storage
- **Performance**: Lazy loading and efficient list rendering

## 🔮 **Future Enhancements**

- **Equalizer** with preset and custom settings
- **Sleep timer** for bedtime listening
- **Crossfade** between tracks
- **Lyrics display** integration
- **Cloud sync** for playlists across devices
- **Custom themes** and color schemes
- **Advanced search filters**
- **Song statistics** and listening analytics

## 🐛 **Troubleshooting**

### Common Issues
1. **No songs found**: Ensure music files are in standard locations and formats
2. **Permission denied**: Grant storage permissions in Android Settings
3. **Playback issues**: Check if audio files are corrupted or unsupported format
4. **App crashes**: Ensure device meets minimum requirements (API 24+)

### Supported Audio Formats
- MP3, WAV, FLAC, AAC, OGG Vorbis, WMA
- Most common audio codecs via Media3 ExoPlayer

## 📄 **License**

This project is open source and available under the [MIT License](LICENSE).

## 🤝 **Contributing**

Contributions are welcome! Please feel free to submit pull requests or open issues for bugs and feature requests.

---

**jPod - Built with ❤️ using Kotlin and Jetpack Compose**
