# PaxRadio - Quick Reference Card

## 🚀 Quick Start

### Installation
```bash
# Build the APK
./gradlew assembleDebug

# APK Location
app/build/outputs/apk/debug/app-debug.apk
```

### First Launch
1. Grant permissions (Internet, Notifications)
2. App loads with default stations
3. Tap list icon → select station → tap play

---

## 🎨 Color Codes

| Element | Color | Hex Code |
|---------|-------|----------|
| Background | Deep Black | `#1A1A1A` |
| Cards | Dark Gray | `#2D2D2D` |
| Accent | Deep Blue | `#0066CC` |
| Text | White | `#FFFFFF` |
| Secondary | Light Gray | `#B0B0B0` |

---

## 📁 File Locations

### Source Code
```
MainActivity.kt              → Main app screen
VolumeKnob.kt               → Analog knob component
NowPlayingCard.kt           → Station display card
StationSelectorSheet.kt     → Station list modal
FmModeScreen.kt             → FM radio interface
RadioStationParser.kt       → Asset file parser
StreamingViewModel.kt       → Radio logic
FmRadioViewModel.kt         → FM logic
Theme.kt                    → Dark color scheme
RadioPlayer.kt              → ExoPlayer wrapper
PlayerService.kt            → Background service
```

### Assets
```
assets/radio_assets/radio.list     → Station config
assets/radio_assets/logos/         → Station logos
```

### Documentation
```
README.md                   → Project overview
ASSETS_GUIDE.md            → Asset configuration
IMPLEMENTATION_SUMMARY.md  → Complete implementation
FEATURES.md                → Visual feature guide
```

---

## 🎛️ Component Sizes

| Component | Size | Details |
|-----------|------|---------|
| Volume Knob | 120dp | Canvas, rotatable 0-270° |
| Play Button | 100dp | Circular, icon 60dp |
| Play Icon | 60dp | Pause/Play circle icons |
| Station Logo (Card) | 80dp | Rounded 16dp corners |
| Station Logo (List) | 56dp | Rounded 12dp corners |
| Now Playing Card | Full width | 24dp padding, 24dp corners |
| FM Dial | 280dp | Canvas with needle |
| Bottom Bar | 80dp height | Black background |
| Bottom Icons | 48dp | Touch targets |

---

## 🎨 Animations

| Animation | Duration | Type | Details |
|-----------|----------|------|---------|
| LIVE Badge | 800ms | Scale | 1.0x ↔ 1.15x, infinite |
| Volume Knob | Dynamic | Spring | dampingRatio: 0.8, stiffness: 300 |
| Play Button | Dynamic | Spring | Scale on state change |
| FM Needle | Dynamic | Smooth | Follows frequency |
| Card Elevation | - | Tonal | Material 3 elevation |
| Image Load | 300ms | Crossfade | Coil default |

---

## 📋 radio.list Format

```
# Comments start with #
Station Name|Stream URL|logo_filename.png

# Examples:
Rock FM|http://stream.rock.com/live|rock_fm.png
Jazz Radio|https://jazz.com/stream|jazz_radio.png
News 24|http://news.com:8000/stream|news.png
```

**Rules:**
- One station per line
- Pipe-separated (|) values
- Logo filename is optional
- Comments start with #
- Blank lines ignored

---

## 🔧 Key Dependencies

```gradle
// Compose & Material 3
androidx.compose.material3:material3
androidx.compose.material:material-icons-extended

// Media Playback
androidx.media3:media3-exoplayer:1.3.1
androidx.media3:media3-session:1.3.1
androidx.media3:media3-ui:1.3.1

// Image Loading
io.coil-kt:coil-compose:2.6.0

// Dependency Injection
com.google.dagger:hilt-android:2.51.1

// Navigation
androidx.navigation:navigation-compose
androidx.hilt:hilt-navigation-compose
```

---

## 🎯 Gesture Controls

### Volume Knob
```
Touch knob → Drag in circle → Volume adjusts
   👆           ↻                🔊
         (0° to 270° rotation)
```

### Station Selection
```
Tap list icon → Tap station → Plays immediately
    📋             🎵              ▶
```

### FM Tuning
```
Drag slider ← → Adjust frequency
    ◉              📡
 (87.5-108 MHz)
```

---

## 📱 Screen Modes

### Radio Mode (Default)
- Now Playing Card (top)
- Play/Pause Button (center)
- Volume Knob (bottom center)
- Bottom bar: List | Space | FM

### FM Mode
- Frequency Dial (analog)
- Frequency Display (large)
- Slider Control
- Scan Button
- Headphones warning (if needed)

**Toggle:** Tap FM icon in bottom bar

---

## 🔊 Audio States

| State | Play Button | LIVE Badge | Volume Knob |
|-------|-------------|------------|-------------|
| No Station | ▶ (gray) | Hidden | Active |
| Selected | ▶ (blue) | Hidden | Active |
| Playing | ⏸ (blue) | Pulsing | Active |

---

## 🎨 Theme Customization

Edit `ui/theme/Theme.kt`:

```kotlin
// Change these values:
val DarkBackground = Color(0xFF1A1A1A)  // Main BG
val CardBackground = Color(0xFF2D2D2D)  // Cards
val DeepBlue = Color(0xFF0066CC)        // Accents

// Apply to:
private val DarkColorScheme = darkColorScheme(
    primary = DeepBlue,
    background = DarkBackground,
    surface = CardBackground,
    // ...
)
```

---

## 🐛 Common Issues

### Stations not loading
✅ Check `radio.list` exists in assets
✅ Verify format (pipe-separated)
✅ Rebuild app after changes

### Logos not showing
✅ Check filename matches (case-sensitive)
✅ Verify logo is in `logos/` folder
✅ Use PNG or JPG format
✅ Rebuild app

### Stream not playing
✅ Test URL in VLC player first
✅ Check internet connection
✅ Verify URL is direct stream
✅ Check ExoPlayer compatibility

### FM mode empty
✅ Plug in wired headphones
✅ Tap "Refresh" button
✅ Headphones required as antenna

---

## 📊 Build Commands

```bash
# Clean build
./gradlew clean assembleDebug

# Build only
./gradlew assembleDebug

# Install on device
./gradlew installDebug

# Build release
./gradlew assembleRelease

# Run tests
./gradlew test
```

---

## 🔑 Permissions Required

```xml
INTERNET                        → Stream radio
FOREGROUND_SERVICE              → Background play
FOREGROUND_SERVICE_MEDIA_PLAYBACK → Media service
WAKE_LOCK                       → Keep awake
POST_NOTIFICATIONS              → Show notification
```

Auto-granted at runtime (except notifications on Android 13+)

---

## 📈 Performance Tips

1. **Logos**: Keep under 1MB each, 512x512px
2. **Stations**: Max 50 for smooth scrolling
3. **URLs**: Use HTTPS when possible
4. **Format**: Prefer MP3 or AAC streams

---

## 🎓 Architecture Summary

```
┌─────────────────────────────────────┐
│          MainActivity               │
│  (Single Activity, Compose UI)      │
└────────────┬────────────────────────┘
             │
    ┌────────┴────────┐
    ↓                 ↓
StreamingVM      FmRadioVM
    ↓                 ↓
RadioPlayer    AudioManager
    ↓
ExoPlayer
    ↓
PlayerService
```

**Pattern:** MVVM with Hilt DI
**UI:** 100% Jetpack Compose
**State:** StateFlow for reactive updates

---

## 🎁 Bonus Features

✅ Material 3 design system
✅ Dark theme optimized
✅ Smooth spring animations
✅ Gesture-based volume control
✅ Asset-based configuration
✅ Background playback
✅ Notification controls
✅ Logo fallback system
✅ Headphones detection
✅ FM mode simulation

---

## 📞 Support Checklist

Before asking for help:
- [ ] Read README.md
- [ ] Check ASSETS_GUIDE.md
- [ ] Verify radio.list format
- [ ] Test stream URL in VLC
- [ ] Rebuild after asset changes
- [ ] Check logcat for errors

---

## 🌟 Quick Tips

💡 **Tip 1:** Test stream URLs in VLC before adding
💡 **Tip 2:** Use square logos (512x512) for best results
💡 **Tip 3:** Comment out stations with # to disable
💡 **Tip 4:** Volume knob works best with circular drag
💡 **Tip 5:** Rebuild app after any asset changes

---

**Version:** 1.0
**Build:** Successful ✅
**Status:** Production Ready 🚀

---

*PaxRadio - Modern Radio, Analog Feel* 🎛️📻

