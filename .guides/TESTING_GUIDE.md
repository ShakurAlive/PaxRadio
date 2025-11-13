# PaxRadio - Testing Guide

## 🧪 How to Test the Fixes

### 1. Testing URL Validation

#### Test Invalid Stations:
1. Open `app/src/main/assets/radio_assets/radio.list`
2. Add a test station with empty URL:
   ```
   Test Station|||
   ```
3. Rebuild app: `./gradlew assembleDebug`
4. Install and run app
5. Open station list (bottom left icon)
6. **Expected:** Test Station shows with:
   - Red tinted background
   - Red station name
   - "No stream available" text (red)
   - Error icon (⚠)
7. **Tap the invalid station**
   - Toast appears: "Stream not available for Test Station"
   - Play button stays disabled/gray
   - Card shows "Stream not available" (red text)

---

### 2. Testing Play/Pause Logic

#### With Valid Station:
1. Select any valid station from list
2. Station card shows station name
3. **Tap Play button (▶)**
   - Button changes to Pause (⏸)
   - "LIVE" badge appears and pulses
   - Track info: "Radio Stream"
   - Notification appears
4. **Tap Pause button (⏸)**
   - Button changes to Play (▶)
   - "LIVE" badge disappears
   - Track info: "Paused"
   - Notification shows "Paused"
5. **Tap Play again**
   - Resumes playback
   - All indicators return

#### With Invalid Station:
1. Select station with empty/invalid URL
2. Station appears with red indicators
3. Play button is disabled (gray)
4. **Try to play**
   - Toast: "Stream not available"
   - Nothing plays
   - UI prevents action

---

### 3. Testing MediaSession & Notification

#### Foreground Notification:
1. Play any valid station
2. **Check notification tray**
   - Notification appears with station name
   - "Playing" status
   - Play/Pause button visible
3. **Tap pause in notification**
   - Playback pauses
   - Button changes to Play
   - Status: "Paused"
4. **Tap play in notification**
   - Playback resumes
   - App state syncs

#### Lock Screen Controls:
1. Play a station
2. Lock device (press power button)
3. Wake device (don't unlock)
4. **Check lock screen**
   - Media controls visible
   - Station name shown
   - Play/Pause works
5. **Test controls**
   - Pause from lock screen works
   - Play from lock screen works
   - App state stays synced

---

### 4. Testing Headset Button

#### Prerequisites:
- Wired or Bluetooth headphones

#### Test Steps:
1. Connect headphones
2. Play a station
3. **Press headset play/pause button**
   - Playback pauses
4. **Press button again**
   - Playback resumes
5. **Disconnect headphones**
   - Playback should pause (audio becoming noisy handling)

---

### 5. Testing State Transitions

#### All State Scenarios:

**Idle → Playing:**
1. Open app (Idle state)
2. Select station
3. Tap Play
4. **Expected:** Playing state, LIVE badge, notification

**Playing → Paused:**
1. While playing
2. Tap Pause
3. **Expected:** Paused state, no LIVE badge, "Paused" text

**Paused → Playing:**
1. While paused
2. Tap Play
3. **Expected:** Resumes playback, LIVE badge returns

**Playing → Error → Playing:**
1. Simulate network disconnect during playback
2. **Expected:** Error state, "Playback error" (red)
3. Reconnect network
4. Tap Play
5. **Expected:** Retries and plays

**NoStream State:**
1. Select invalid station
2. Try to play
3. **Expected:** NoStream state, Toast message, disabled button

---

### 6. Testing Station Switching

1. Play station A
2. **Select station B while A is playing**
   - Station A stops cleanly
   - Station B starts playing
   - No overlap or glitches
3. **Notification updates**
   - Shows station B name
4. **Now Playing card updates**
   - Shows station B info

---

### 7. Testing Volume Control

1. Play any station
2. **Drag volume knob**
   - Volume changes smoothly
   - Knob rotates 0° to 270°
   - Percentage updates
3. **Set to 0%**
   - Silent but still playing
4. **Set to 100%**
   - Maximum volume

---

### 8. Testing Background Playback

1. Play a station
2. **Press Home button**
   - App goes to background
   - Playback continues
   - Notification stays visible
3. **Open other apps**
   - Playback continues
4. **Lock device**
   - Playback continues
5. **Return to app**
   - UI state matches playback

---

### 9. Testing Error Scenarios

#### Invalid URL Format:
1. Add station: `Bad Station|htp://invalid|logo.png`
2. **Expected:** Red tint, not playable

#### Empty URL:
1. Add station: `Empty Station||logo.png`
2. **Expected:** Red tint, Toast on select

#### Network Error:
1. Play station
2. Turn off WiFi/Data
3. **Expected:** Error state after timeout
4. Turn on network
5. Tap Play to retry

#### Missing Station:
1. Don't select any station
2. Tap Play button
3. **Expected:** Toast "Please select a station first"

---

### 10. Testing FM Mode Toggle

1. **Tap FM icon (bottom right)**
   - Switches to FM mode
   - Shows frequency dial
2. **Check headphones detection**
   - If no headphones: warning shown
   - If headphones: dial active
3. **Tap Station List icon**
   - Returns to radio mode
   - Maintains state

---

## 🎯 Expected Results Summary

| Test | Expected Result | Pass/Fail |
|------|----------------|-----------|
| Invalid URL rejected | Red tint + Toast | ⬜ |
| Play button disabled for invalid | Gray button | ⬜ |
| Valid station plays | LIVE badge + notification | ⬜ |
| Pause works | Paused state + updated UI | ⬜ |
| Resume works | Returns to playing | ⬜ |
| Notification shows | With play/pause | ⬜ |
| Lock screen controls | Visible and functional | ⬜ |
| Headset button | Play/Pause works | ⬜ |
| Station switching | Clean transition | ⬜ |
| Background playback | Continues playing | ⬜ |
| Volume control | Smooth adjustment | ⬜ |
| Error handling | Toast + error state | ⬜ |
| State transitions | All states work | ⬜ |
| FM mode toggle | Switches correctly | ⬜ |

---

## 🐛 Common Issues & Solutions

### Issue: Notification not showing
**Solution:** Grant notification permission in Settings

### Issue: Lock screen controls missing
**Solution:** 
- Check notification permission
- Ensure service is running
- Restart app

### Issue: Headset button not working
**Solution:**
- Check Bluetooth/wired connection
- Some headphones may not support media buttons
- Try different headphones

### Issue: Station won't play
**Solution:**
- Check URL validity (http:// or https://)
- Test URL in browser/VLC
- Check internet connection
- Look for red tint (invalid indicator)

### Issue: App crashes on play
**Solution:**
- Check logcat for errors
- Verify ExoPlayer dependency
- Ensure service is declared in manifest

---

## 📊 Performance Checks

### Memory:
- Monitor with Android Studio Profiler
- Should stay under 100MB for audio streaming

### Battery:
- Should use <5% per hour with screen off
- Foreground service optimized

### Network:
- Typical: 128kbps = ~1MB per minute
- Monitor data usage in Settings

---

## 🔍 Debug Commands

### Check if service is running:
```bash
adb shell dumpsys activity services | findstr PaxRadio
```

### Check notifications:
```bash
adb shell dumpsys notification | findstr PaxRadio
```

### View logs:
```bash
adb logcat | findstr "RadioPlayer\|RadioPlaybackService"
```

### Clear app data:
```bash
adb shell pm clear com.example.paxradio
```

---

## ✅ Final Checklist

Before considering testing complete:

- [ ] All 14 tests passed
- [ ] No crashes observed
- [ ] All states transition correctly
- [ ] Notification works properly
- [ ] Lock screen controls functional
- [ ] Invalid stations clearly marked
- [ ] Error messages user-friendly
- [ ] Background playback stable
- [ ] Memory usage acceptable
- [ ] Battery drain reasonable

---

## 🎉 Success Criteria

**The app is working correctly if:**
1. ✅ Invalid stations show red tint and can't play
2. ✅ Valid stations play without issues
3. ✅ Play/Pause toggle works in all scenarios
4. ✅ Notification appears with working controls
5. ✅ Lock screen shows media controls
6. ✅ Headset button controls playback
7. ✅ Background playback continues
8. ✅ Station switching is clean
9. ✅ Error states show clear messages
10. ✅ No crashes or ANRs

---

*Happy Testing! 🧪*

