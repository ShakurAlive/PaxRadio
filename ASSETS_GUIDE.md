# PaxRadio - Asset Configuration Guide

## Overview

PaxRadio uses an asset-based configuration system for radio stations. This allows you to easily add, remove, or modify stations without recompiling the app.

## Directory Structure

```
app/src/main/assets/
└── radio_assets/
    ├── radio.list          # Station configuration file
    └── logos/              # Station logo images
        ├── rock_fm.png
        ├── jazz_radio.png
        └── ...
```

## Station Configuration File

### File Location
`app/src/main/assets/radio_assets/radio.list`

### Format
Each line represents one radio station with pipe-separated values:
```
Station Name|Stream URL|Logo Filename
```

### Example
```
# PaxRadio Station List
# Lines starting with # are comments and will be ignored

Rock FM|http://stream.example.com/rock|rock_fm.png
Jazz Radio|http://stream.example.com/jazz|jazz_radio.png
News 24/7|http://stream.example.com/news|news_24.png
Pop Hits|http://stream.example.com/pop|pop_hits.png
Classic Hits|http://stream.example.com/classic|classic.png
```

### Field Descriptions

1. **Station Name** (Required)
   - The display name shown in the UI
   - Can contain spaces and special characters
   - Example: `Rock FM`, `Jazz Radio 101.5`, `Classical Music`

2. **Stream URL** (Required)
   - The direct streaming URL
   - Supported formats: MP3, AAC, HLS, DASH
   - Example: `http://stream.example.com/rock.mp3`
   - Example: `https://radio.station.com/live/stream`

3. **Logo Filename** (Optional)
   - Name of the logo file in the `logos/` directory
   - If omitted, a default microphone icon will be used
   - Supported formats: PNG, JPG, JPEG
   - Example: `rock_fm.png`, `jazz.jpg`

## Adding Station Logos

### Image Requirements
- **Format**: PNG (recommended) or JPG
- **Size**: 512x512 pixels or larger (recommended)
- **Aspect Ratio**: Square (1:1) recommended
- **Max File Size**: 2MB per image (recommended)

### Steps to Add Logos

1. Prepare your logo image (512x512 px, PNG format)

2. Copy the image to:
   ```
   app/src/main/assets/radio_assets/logos/
   ```

3. Note the exact filename (case-sensitive)

4. Update `radio.list` with the filename in the third column:
   ```
   Rock FM|http://stream.url|rock_fm.png
   ```

5. Rebuild the app or sync assets

### Example Logo Files
```
logos/
├── rock_fm.png
├── jazz_radio.png
├── news_24.png
├── pop_hits.jpg
└── classic.png
```

## Common Use Cases

### Adding a New Station

1. Open `radio.list`
2. Add a new line at the end:
   ```
   Smooth Jazz|http://jazz.stream.com/live|smooth_jazz.png
   ```
3. Add the logo file `smooth_jazz.png` to `logos/` directory
4. Rebuild the app

### Removing a Station

1. Open `radio.list`
2. Delete the entire line for the station
3. (Optional) Delete the logo file from `logos/`
4. Rebuild the app

### Updating a Stream URL

1. Open `radio.list`
2. Find the station line
3. Replace the URL (second column):
   ```
   Rock FM|http://new-stream-url.com/rock|rock_fm.png
   ```
4. Rebuild the app

### Station Without Logo

If you don't have a logo, just omit the third column or use two pipes:
```
Talk Radio|http://talk.stream.com/live|
```
or
```
Talk Radio|http://talk.stream.com/live
```

The app will show a default microphone icon.

## Finding Streaming URLs

### Popular Sources
1. **Internet Radio Directories**
   - Radio Browser (radio-browser.info)
   - TuneIn (tunein.com)
   - Radio.net
   - Streema

2. **Direct from Station Websites**
   - Look for "Listen Live" or "Stream URL"
   - Right-click on the play button and inspect the network requests
   - Check the station's mobile app

3. **Common URL Patterns**
   - `http://stream.radio.com:8000/stream.mp3`
   - `https://listen.radio.com/live/aac`
   - `http://icecast.server.com:8080/radio128`

### Testing Stream URLs

Use VLC Media Player or a similar tool to test URLs before adding them:
1. Open VLC
2. Media → Open Network Stream
3. Paste the URL
4. If it plays, the URL is valid

## Troubleshooting

### Station Not Showing
- Check that `radio.list` is properly formatted
- Ensure no extra spaces around the pipe separators
- Verify the file is saved as UTF-8

### Logo Not Loading
- Verify the filename matches exactly (case-sensitive)
- Check the file is in `radio_assets/logos/` directory
- Ensure the image format is PNG or JPG
- Try with a smaller file size

### Stream Not Playing
- Test the URL in VLC or a web browser
- Check if the stream requires authentication
- Verify the stream format is supported by ExoPlayer
- Check internet connectivity

### App Shows Default Stations
- The app falls back to default stations if `radio.list` is not found
- Check that assets are included in the build
- Verify file path: `app/src/main/assets/radio_assets/radio.list`

## Advanced Configuration

### Comments in radio.list
```
# This is a comment line - will be ignored
# Station format: Name|URL|Logo

Rock FM|http://stream.com/rock|rock.png
# Jazz Radio|http://stream.com/jazz|jazz.png  ← This line is disabled
```

### Multiple Stations, Same Logo
You can reuse the same logo file for multiple stations:
```
Rock Classics|http://rock1.com|rock.png
Modern Rock|http://rock2.com|rock.png
Hard Rock|http://rock3.com|rock.png
```

### High-Quality Streams
For high-quality streams, just use the appropriate URL:
```
HD Jazz|http://stream.com/jazz-320k.mp3|jazz.png
CD Quality Classical|http://stream.com/classical-flac|classical.png
```

## Example Complete Configuration

**radio.list:**
```
# PaxRadio Station List
# Format: Station Name|Stream URL|Logo Filename

# Music Stations
Rock FM|http://stream.rockfm.com/live|rock_fm.png
Smooth Jazz|http://jazz.streaming.com/smooth|jazz.png
Classical 24/7|http://classical.radio.com:8000/live|classical.png
Pop Hits|http://stream.pophits.com/128|pop.png

# News & Talk
BBC World Service|http://stream.bbc.com/worldservice|bbc.png
NPR News|http://npr.streaming.org/news|npr.png

# Electronic
Chillout Lounge|http://chillout.stream.com/ambient|chillout.png
Electronic Beats|http://electronic.fm/live|electronic.png
```

**logos/ directory:**
```
rock_fm.png (512x512)
jazz.png (512x512)
classical.png (512x512)
pop.png (512x512)
bbc.png (512x512)
npr.png (512x512)
chillout.png (512x512)
electronic.png (512x512)
```

## Building with Assets

After modifying assets, rebuild the app:

```bash
# Clean build
./gradlew clean assembleDebug

# Or just build
./gradlew assembleDebug
```

Assets are automatically packaged into the APK during the build process.

## Notes

- Changes to `radio.list` or logos require rebuilding the app
- The app reads the file once at startup
- Invalid lines in `radio.list` are skipped
- Maximum recommended stations: 50 (for performance)
- Total logo file size should stay under 20MB (for APK size)

## Support

For issues or questions, refer to the main README.md or check the source code in:
- `RadioStationParser.kt` - Asset parsing logic
- `StreamingViewModel.kt` - Station loading
- `NowPlayingCard.kt` - Logo display

