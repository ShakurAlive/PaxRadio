# PaxRadio - Feature Showcase

## 🎨 Visual Design Overview

### Color Scheme
The app uses a premium dark theme inspired by high-end audio equipment:

```
┌─────────────────────────────────────┐
│   Background: #1A1A1A (Deep Black) │
│   ┌─────────────────────────────┐   │
│   │ Card: #2D2D2D (Dark Gray)  │   │
│   │                             │   │
│   │  Accent: #0066CC (Blue) ●  │   │
│   │                             │   │
│   └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

---

## 📱 Main Screen Layout

```
╔═══════════════════════════════════════╗
║                                       ║
║    ┌──────────────────────────┐      ║
║    │   ● LIVE (pulsing)       │      ║
║    │                          │      ║
║    │     [Station Logo]       │      ║
║    │      80x80 rounded       │      ║
║    │                          │      ║
║    │    "Station Name"        │      ║
║    │    (large, bold)         │      ║
║    │                          │      ║
║    │   "Radio Stream"         │      ║
║    └──────────────────────────┘      ║
║         NOW PLAYING CARD              ║
║                                       ║
║              ╔═══╗                    ║
║              ║ ▶ ║  ← Play/Pause     ║
║              ╚═══╝    (100dp)        ║
║                                       ║
║                                       ║
║              ╭───╮                    ║
║             │ 75% │ ← Volume %       ║
║            ╱   ●   ╲                 ║
║           │    │    │                ║
║            ╲  ─┘   ╱                 ║
║             ╰───╯                    ║
║         VOLUME KNOB (120dp)          ║
║                                       ║
║  ┌─────────────────────────────┐    ║
║  │  📋    [  SPACE  ]    📡    │    ║
║  │ List                    FM   │    ║
║  └─────────────────────────────┘    ║
║         BOTTOM ACTION BAR            ║
╚═══════════════════════════════════════╝
```

---

## 🎛️ Volume Knob - Interactive Design

### Visual Representation
```
        ╭───────────╮
       ╱             ╲
      │    ╭───╮     │
      │   │ 75% │    │   ← Percentage Display
      │    ╰───╯     │
     │               │
     │       ●       │   ← Center Pivot
     │      ╱        │
     │     ╱         │   ← Notch Indicator (rotates)
     │    ╱          │      Points to current position
      │             │
       ╲           ╱
        ╰─────────╯

     [0°]          [270°]
      0%           100%
```

### Interaction
- **Drag**: Touch and move finger in circular motion
- **Rotation**: 0° (bottom) to 270° (clockwise)
- **Visual Feedback**: 
  - Notch rotates smoothly
  - Blue arc shows volume level
  - Percentage updates in real-time
  - Spring animation on release

### Technical Details
```kotlin
// Gesture Detection
detectDragGestures { change, _ ->
    // Calculate angle from touch position
    angle = atan2(touch.y - center.y, touch.x - center.x)
    
    // Convert to 0-270° range
    rotation = normalizeAngle(angle)
    
    // Update volume (0.0 to 1.0)
    volume = rotation / 270f
}

// Spring Animation
animateFloatAsState(
    rotation,
    spring(dampingRatio = 0.8f, stiffness = 300f)
)
```

---

## 📻 Now Playing Card - Anatomy

```
┌──────────────────────────────────────┐
│  ┌────────┐                          │
│  │● LIVE  │  ← Animated pulse badge  │
│  └────────┘     (red, pulsing)       │
│                                       │
│       ╔═══════════╗                  │
│       ║           ║                  │
│       ║  LOGO     ║  ← 80dp rounded  │
│       ║           ║     Shimmer load │
│       ╚═══════════╝     Or mic icon  │
│                                       │
│      Rock FM 101.5  ← Station name   │
│      ──────────────    (28sp bold)   │
│                                       │
│      Radio Stream   ← Metadata       │
│      ─────────────     (gray text)   │
│                                       │
└──────────────────────────────────────┘
   Card: #2D2D2D, 24dp rounded corners
```

### Animation Details

**LIVE Badge Pulse:**
```
Scale: 1.0x → 1.15x → 1.0x
Duration: 800ms
Easing: FastOutSlowInEasing
Repeat: Infinite, Reverse mode
```

**Logo Loading States:**
```
1. Loading:  [⟳ Spinner]  (CircularProgressIndicator)
2. Success:  [Logo Image] (Crossfade transition)
3. Error:    [🎤 Mic]     (Fallback icon)
```

---

## 📋 Station Selector - Bottom Sheet

```
╔═══════════════════════════════════════╗
║  Select Station                       ║
║  ═══════════════════════════════      ║
║                                       ║
║  ┌─────────────────────────────┐     ║
║  │ [Logo] Rock FM              │ ✓   ║
║  │        Rock classics        │     ║
║  └─────────────────────────────┘     ║
║                                       ║
║  ┌─────────────────────────────┐     ║
║  │ [Logo] Jazz Radio           │     ║
║  │        Smooth jazz          │     ║
║  └─────────────────────────────┘     ║
║                                       ║
║  ┌─────────────────────────────┐     ║
║  │ [Logo] News 24/7            │     ║
║  │        Global news          │     ║
║  └─────────────────────────────┘     ║
║                                       ║
║         [Scrollable list...]         ║
║                                       ║
╚═══════════════════════════════════════╝
```

### Features
- Tap any station → Plays immediately
- Current station highlighted with blue tint
- Equalizer icon (≋) shows playing station
- 56dp logos with 12dp rounded corners
- Scrollable for many stations
- Dismisses on selection or background tap

---

## 📡 FM Radio Mode

```
╔═══════════════════════════════════════╗
║                                       ║
║         ╭───────────╮                 ║
║        ╱             ╲                ║
║       │   ╭───────╮   │              ║
║       │  │ 87.5   │  │  ← Freq marks ║
║      │   │   ⋮    │   │              ║
║      │   │  99.5  │   │              ║
║      │   │   ⋮    │   │              ║
║      │   │ 108.0 │   │              ║
║       │  ╰───────╯   │              ║
║       │       ↑      │  ← Needle     ║
║        ╲     │      ╱     (blue)     ║
║         ╰────┼────╯                  ║
║              ●      ← Center pivot   ║
║                                       ║
║         99.5 MHz    ← Large display  ║
║                                       ║
║  ──────────◉────────  ← Slider       ║
║  87.5          108.0                 ║
║                                       ║
║  ┌─────────────────────────────┐    ║
║  │    SCAN STATIONS            │    ║
║  └─────────────────────────────┘    ║
║                                       ║
║  Note: FM playback simulated         ║
║                                       ║
╚═══════════════════════════════════════╝
```

### Headphones Warning
```
┌──────────────────────────────────────┐
│  ⚠️ Headphones Required              │
│                                       │
│  Please plug in wired headphones     │
│  to use FM Radio                     │
│                                       │
│  [Recheck]                           │
└──────────────────────────────────────┘
  Orange/amber card (#663300)
```

---

## 🎵 Asset Configuration System

### File Structure
```
app/src/main/assets/
└── radio_assets/
    ├── radio.list              ← Station config
    └── logos/
        ├── rock_fm.png         ← Station logos
        ├── jazz_radio.png
        ├── news_24.png
        └── ...
```

### radio.list Format
```
# Comment lines start with #
Station Name|Stream URL|logo_filename.png
─────────────────────────────────────────
Rock FM|http://stream.com/rock|rock_fm.png
Jazz Radio|http://jazz.com/live|jazz_radio.png
News 24/7|http://news.com/stream|news_24.png
```

### Loading Flow
```
App Startup
    ↓
RadioStationParser.parseFromAssets()
    ↓
Read radio.list from assets
    ↓
Parse each line (split by '|')
    ↓
Create RadioStation objects
    ↓
Load into ViewModel
    ↓
Display in UI
```

---

## 🎯 Interaction Flow

### Play a Station
```
User taps → Opens         → User selects → Station plays
list icon   station sheet   station        immediately
   📋          ↓               ↓              ▶
            [Sheet]        [Station]      [Playing]
                          highlights      shows LIVE
```

### Adjust Volume
```
User touches → Drags in    → Knob rotates → Volume
knob          circle motion   smoothly      changes
   👆            ↻              🎛️           🔊
            Spring           Blue arc      0-100%
            animation       indicates
```

### Switch to FM
```
User taps → Checks        → Shows dial  → User tunes
FM icon     headphones      or warning    frequency
   📡          ↓              ↓             🔍
           [Check]        [FM Mode]      [Tune]
```

---

## 🔊 Audio Engine

### ExoPlayer Configuration
```
ExoPlayer.Builder(context)
    .setAudioAttributes(
        AudioAttributes.Builder()
            .setUsage(USAGE_MEDIA)
            .setContentType(AUDIO_CONTENT_TYPE_MUSIC)
            .build()
    )
    .setHandleAudioBecomingNoisy(true)
    .build()
```

### Playback Flow
```
Stream URL
    ↓
MediaItem.fromUri(url)
    ↓
exoPlayer.setMediaItem()
    ↓
exoPlayer.prepare()
    ↓
exoPlayer.play()
    ↓
[Background Service]
    ↓
Foreground Notification
```

---

## 🎨 Animation Showcase

### 1. LIVE Badge Pulse
```
Scale Animation:
  ●     →    ◉    →    ●
 1.0x       1.15x      1.0x
  ↓          ↓          ↓
 [800ms  transition  reverse]
```

### 2. Volume Knob Rotation
```
Touch → Drag → Spring → Smooth Rotation
                   ↓
           dampingRatio: 0.8
           stiffness: 300
                   ↓
         Natural feel with bounce
```

### 3. Play Button Scale
```
Paused:  ▶  (scale 0.95x)
          ↓
Playing: ⏸  (scale 1.0x)
```

### 4. FM Needle Movement
```
Frequency change:
  87.5 MHz  →  99.5 MHz  →  108.0 MHz
     ↓            ↓             ↓
   [Needle rotates smoothly]
```

---

## 🎯 State Management

### Radio Mode States
```
┌─────────────────────────────────────┐
│  No Station Selected                │
│  - Play button disabled (gray)      │
│  - "Select Station" text            │
└─────────────────────────────────────┘
              ↓ [Select station]
┌─────────────────────────────────────┐
│  Station Selected (Not Playing)     │
│  - Play button enabled (blue)       │
│  - Station info shown               │
│  - No LIVE badge                    │
└─────────────────────────────────────┘
              ↓ [Tap play]
┌─────────────────────────────────────┐
│  Playing                            │
│  - Pause button shown               │
│  - LIVE badge pulsing               │
│  - Volume knob active               │
└─────────────────────────────────────┘
```

### FM Mode States
```
┌─────────────────────────────────────┐
│  No Headphones                      │
│  - Warning card shown               │
│  - Controls disabled                │
└─────────────────────────────────────┘
              ↓ [Plug headphones]
┌─────────────────────────────────────┐
│  Headphones Connected               │
│  - Frequency dial active            │
│  - Slider enabled                   │
│  - Scan button enabled              │
└─────────────────────────────────────┘
```

---

## 📊 Performance Optimizations

### Lazy Loading
- Station list uses LazyColumn (only renders visible items)
- Images loaded asynchronously with Coil
- Crossfade transitions prevent flickering

### State Hoisting
- ViewModels manage state
- Composables are stateless UI
- State flows for reactive updates

### Canvas Optimizations
- Volume knob drawn with Canvas (hardware accelerated)
- FM dial uses single Canvas (no nested composables)
- Efficient gesture detection

---

## 🎁 Bonus Features

### Material 3 Integration
- Dynamic color scheme
- Elevation system (tonal)
- Modern component designs
- Ripple effects throughout

### Accessibility
- Content descriptions on all icons
- Semantic labels for screen readers
- High contrast dark theme
- Large touch targets (48dp minimum)

### Error Handling
- Fallback to default stations if assets missing
- Fallback to mic icon if logo missing
- Toast messages for stream errors
- Headphones detection with retry

---

## 🌟 Design Philosophy

**Inspired by:** High-end stereo equipment and analog audio gear

**Key Principles:**
1. **Minimalism** - Single screen, no clutter
2. **Tactile** - Rotatable knob, touchable controls
3. **Premium** - Dark theme, smooth animations
4. **Intuitive** - Obvious controls, clear feedback
5. **Analog Feel** - Physical-style interactions

**Result:** A radio app that feels like controlling real audio equipment! 🎛️📻

---

**PaxRadio - Where digital meets analog in perfect harmony.** ✨

