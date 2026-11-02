# XRecorder - Screen Recorder & Video Editor 📹

A modern, high-performance, offline Native Android Screen Recorder and Video Editor app built with **Kotlin** and **Jetpack Compose (Material 3)**.

Designed after the iconic InShot XRecorder app with its signature dark studio aesthetic, electric orange accents, floating control ball, and video editing studio.

---

## ✨ Features

- 🔴 **One-Tap Screen Recording:**
  - Fullscreen **3... 2... 1... Countdown** before recording begins
  - Live elapsed time tracker (`00:03:45`) with blinking recording dot
  - Foreground Service notification with Pause / Resume / Stop actions
- 🟠 **Draggable Floating Ball:**
  - Circular floating widget that hovers on screen
  - Expandable radial menu for instant Record, Pause, Screenshot, and Settings
- ✂️ **In-App Video Trimmer:**
  - Cut and trim videos with millisecond precision
  - Dual start and end marker range sliders with real-time length and size calculation
- 📺 **In-App Video Player:**
  - Fast playback scrubber, timecodes, 1080P / FPS badges
  - Quick share, rename, and delete actions
- 🖼️ **Screenshot Capture & Gallery:**
  - Instant one-tap screen grabs
  - Categorized gallery (All, Videos, Screenshots)
- ⚙️ **Customizable Studio Settings:**
  - **Resolution:** 1080p (FHD), 720p (HD), 540p, 480p
  - **Frame Rate (FPS):** 60 FPS, 50 FPS, 30 FPS, 24 FPS
  - **Audio Source:** Mute, Microphone, Internal Audio, Internal & Mic
  - **Countdown:** Off, 3s, 5s, 10s
  - **Shake to Stop:** Gently shake device to finish capture
  - **Floating Ball Toggle:** Enable or hide anytime

---

## 🚀 Building & Exporting APK

### 1. Automated via GitHub Actions
This repository includes a ready-to-run GitHub Actions workflow (`.github/workflows/build-apk.yml`):
1. Push this repository to GitHub.
2. Go to the **Actions** tab on GitHub.
3. The **Build Android APK** workflow will automatically run, compile the project, and attach the downloadable `XRecorder-Debug-APK` under **Artifacts**!

### 2. Manual Command Line Build
To build the APK locally:
```bash
./gradlew assembleDebug
```
The compiled APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🛠️ Tech Stack & Architecture
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Architecture:** MVVM (Model-View-ViewModel) with StateFlow
- **Media & Image Loading:** Coil Compose
- **Service:** Android Foreground Service with Notification Channels
- **Min SDK:** 24 (Android 7.0+)
- **Target SDK:** 36 (Android 15+)
