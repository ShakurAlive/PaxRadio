# PaxRadio

A modern Android radio streaming app with analog-style controls, built with Kotlin and Jetpack Compose using Material 3 design.

## 🎨 Design

**Dark Theme**: Deep black (#1A1A1A) background with dark gray (#2D2D2D) cards and deep blue (#0066CC) accents for a premium, minimalist look.

## ✨ Features

### Radio Streaming Mode
- **Now Playing Card**: Large, rounded card displaying current station with animated "LIVE" badge
- **Station Logos**: Loaded from assets with shimmer loading effect (fallback to mic icon)
- **Analog Volume Knob**: Interactive rotatable knob (0° to 270°) with smooth spring animations
  - Metallic gray design with notch indicator
  - Real-time volume percentage display
  - Touch-drag gesture control
- **Play/Pause Button**: Large circular button with ripple effects
- **Station Selector**: Bottom sheet modal with station list and logos

### FM Radio Mode
- **Analog Frequency Dial**: Visual needle indicator (87.5-108 MHz)
- **Frequency Slider**: Fine-tune frequency with smooth controls
- **Headphones Detection**: Prompts user to plug in wired headphones
- **Station Scanning**: Auto-scan feature to find local stations
- **Note**: FM playback simulated on devices without hardware support

### Asset-Based Configuration
- **radio.list**: Text file in `assets/radio_assets/` with format:
  ```
  Station Name|https://stream.url|logo_filename.png
  ```
- **Logo Directory**: `assets/radio_assets/logos/` for station logo images
- **Dynamic Loading**: Stations loaded at app startup with fallback to defaults

## 🛠 Tech Stack

- **UI**: Jetpack Compose with Material 3
- **Theme**: Custom dark gray/black color scheme
- **Architecture**: MVVM with ViewModels
- **DI**: Hilt/Dagger
- **Media Playback**: ExoPlayer (Media3)
- **Image Loading**: Coil
- **Animations**: Spring animations, infinite transitions, gesture detection
- **Min SDK**: 29 (Android 10)
- **Target SDK**: 36

## 📁 Project Structure

```
app/src/main/
├── assets/
│   └── radio_assets/
│       ├── radio.list          # Station configuration
│       └── logos/              # Station logo images
├── java/com/example/paxradio/
│   ├── data/
│   │   ├── RadioStation.kt
│   │   └── RadioStationParser.kt
│   ├── di/
│   │   └── PlayerModule.kt
│   ├── player/
│   │   ├── RadioPlayer.kt
│   │   └── PlayerService.kt
│   ├── ui/
│   │   ├── components/
│   │   │   ├── VolumeKnob.kt       # Analog volume knob
│   │   │   ├── NowPlayingCard.kt   # Main display card
│   │   │   └── StationSelectorSheet.kt
│   │   ├── fm/
│   │   │   ├── FmModeScreen.kt     # FM Radio UI
│   │   │   └── FmRadioViewModel.kt
│   │   ├── streaming/
│   │   │   └── StreamingViewModel.kt
│   │   ├── theme/
│   │   │   └── Theme.kt            # Dark theme colors
│   │   └── MainActivity.kt         # Single-screen app
│   └── PaxRadioApp.kt
```

## 🚀 Building

```bash
./gradlew clean :app:assembleDebug
```

The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

## 📝 Configuration

### Adding Radio Stations

1. Edit `app/src/main/assets/radio_assets/radio.list`:
   ```
   Rock FM|http://stream.example.com/rock|rock_fm.png
   Jazz Radio|http://stream.example.com/jazz|jazz_radio.png
   ```

2. Add station logos to `app/src/main/assets/radio_assets/logos/`:
   - Format: PNG or JPG
   - Recommended size: 512x512 pixels
   - Filename must match the third column in radio.list

### Custom Theme Colors

Edit `app/src/main/java/com/example/paxradio/ui/theme/Theme.kt`:
```kotlin
val DarkBackground = Color(0xFF1A1A1A)  // Main background
val CardBackground = Color(0xFF2D2D2D)  // Card surfaces
val DeepBlue = Color(0xFF0066CC)        // Accent color
```

## 🎮 Controls

- **Volume Knob**: Drag in circular motion to adjust volume (0-100%)
- **Play/Pause**: Tap the large circular button
- **Station List**: Tap list icon (bottom left)
- **FM Mode**: Tap FM icon (bottom right)
- **Station Select**: Tap any station in the selector sheet

## 📱 Permissions

- `INTERNET` - Stream radio stations
- `FOREGROUND_SERVICE` - Background playback
- `FOREGROUND_SERVICE_MEDIA_PLAYBACK` - Media playback service
- `WAKE_LOCK` - Keep device awake during playback
- `POST_NOTIFICATIONS` - Show playback notifications

## 🎯 Key Components

### VolumeKnob.kt
Custom Canvas-based rotatable volume control with:
- Gesture detection for drag control
- Spring animations for smooth rotation
- Metallic gradient styling
- Real-time percentage display

### NowPlayingCard.kt
Elevated card showing current station with:
- Animated "LIVE" pulse badge
- Station logo with shimmer loading
- Station name and metadata
- Rounded corners with dark theme

### RadioStationParser.kt
Parses `radio.list` file from assets:
- Line-by-line parsing with format validation
- Fallback to default stations if file missing
- Logo path resolution

## 🔧 Dependencies

All dependencies configured in `app/build.gradle.kts`:
- Material 3 Compose
- ExoPlayer 2.19.1
- Coil for image loading
- Material Icons Extended
- Hilt for dependency injection

## 📄 License

This is a demonstration project for learning purposes.

Modern Android radio streaming app skeleton using Kotlin + Jetpack Compose (Material 3), Media3 ExoPlayer, Hilt DI, Navigation, Coil.

## Added Source Structure
- Application class with Hilt: `PaxRadioApp.kt`
- DI module: `di/PlayerModule.kt`
- Player wrapper + service: `player/RadioPlayer.kt`, `player/PlayerService.kt`
- Data model: `data/RadioStation.kt`
- ViewModels: `ui/streaming/StreamingViewModel.kt`, `ui/fm/FmRadioViewModel.kt`
- Compose UI: streaming & fm screens, player bar, theme, navigation enum, `MainActivity`.

## Pending Manual Steps
The build.gradle and manifest edits did not apply automatically. Please manually:

### 1. Update `gradle/libs.versions.toml`
Add plugin aliases for Hilt and kapt:
```
[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-kapt = { id = "org.jetbrains.kotlin.kapt", version.ref = "kotlin" }
hilt-android = { id = "com.google.dagger.hilt.android", version = "2.52" }
```
(Optional: add versions and libraries for compose/media3/coil.)

### 2. Replace `app/build.gradle.kts` content:
Use snippet from conversation adding Compose, Hilt, Media3 dependencies and enabling compose build features.

### 3. Replace `app/src/main/AndroidManifest.xml`:
Insert permissions and register `MainActivity` and `PlayerService`.

### 4. Sync Gradle
```
./gradlew.bat clean build
```
(Windows)

After updates, project should compile (streams are placeholder URLs).

## Next Enhancements
- Add network connectivity check (ConnectivityManager)
- Add error handling & notifications actions
- Persist scanned FM stations (SharedPreferences)
- Integrate real FM tuner API where available

## Minimum SDK
Set to 26 per requirement.

## Disclaimer
FM functionality simulated; real hardware integration requires vendor API.

