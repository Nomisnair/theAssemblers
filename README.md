# ZombieThumb 🧟👍

**On-device anti-doomscrolling companion** — detects zombie-scroll patterns using interaction physics, never reads your content.

## What it does

ZombieThumb monitors **how** you scroll, not **what** you scroll. When it detects a doomscrolling pattern, it sends a compassionate notification suggesting a break — with the exact meters of feed you've scrolled and how long you've been at it.

### The 4 Biomarkers

| Biomarker | What it detects | Weight |
|---|---|---|
| **Flick Rhythm** | Steady 1.2-2.0s swipe cadence (reading = irregular 4-6s pauses) | 35% |
| **Consumption Ratio** | ~98% vertical flings, <2% taps/interactions | 30% |
| **Bedtime Slump** | Phone overhead (~85°) + dark room (~0 lux) + stillness | 20% |
| **Feed Mileage** | Cumulative scroll distance in meters + session duration | 15% |

Combined into a **Trance Score (0-100%)** via EMA-smoothed weighted formula.

## Privacy

ZombieThumb is **100% on-device** with **zero network access**:

- ✅ Scroll velocity, cadence, distance
- ✅ Tap frequency, device tilt, ambient light
- ✅ Foreground app package name
- 🚫 **NEVER** reads screen content, text, messages, or any personal info
- 🚫 **NEVER** takes screenshots or records the screen
- 🚫 **NEVER** connects to the internet (permission explicitly removed)
- 🚫 **NEVER** syncs to any cloud

The Accessibility Service runs with `canRetrieveWindowContent="false"` and `canPerformGestures="false"`.

## Setup

### Prerequisites
- Android Studio Hedgehog or newer
- Android SDK 34
- JDK 17

### Build & Run
```bash
# Clone and open in Android Studio
# Set your SDK path in local.properties:
# sdk.dir=C:\\Users\\YourName\\AppData\\Local\\Android\\Sdk

./gradlew assembleDebug

# Install on device/emulator
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Enable Permissions

1. **Accessibility Service**: Settings → Accessibility → ZombieThumb → Enable
   - On Android 13+ sideloaded APKs: Settings → Apps → ZombieThumb → ⋮ → "Allow restricted settings" first
2. **Notifications**: Grant when prompted (Android 13+), or Settings → Apps → ZombieThumb → Notifications

### Install the Lightweight AI Model (Optional)

The app supports lightweight on-device AI models (like Falcon 1B or Gemma 2B) to generate dynamic, compassionate messages. If you don't install a model, the app seamlessly falls back to handwritten templates (which take up zero space and work perfectly).

To use a lightweight model like Falcon 1B (~700MB) instead of the heavy Gemma model:

```bash
# Download falcon-rw-1b-int4.bin (or gemma-2b-it-gpu-int4.bin)
# Place it in the app's files directory:
adb push falcon-rw-1b-int4.bin /data/data/com.zombiethumb/files/
```

If the model file is missing or inference takes >1.5 seconds, the app uses template messages.

## How to Demo

See [DEMO.md](DEMO.md) for a complete 60-second demo script.

### Quick Start
1. Open the app → Enable monitoring toggle
2. Open TikTok/Instagram/YouTube
3. Rapidly swipe through the feed (~25 flicks in ~18 seconds)
4. A notification appears: "You've scrolled X meters of feed..."

### Demo Mode
Enable **Demo Mode** in Settings for:
- Lowered trance threshold (50% instead of 85%)
- Short cooldown (15 seconds instead of 10 minutes)
- ~25 rapid flicks triggers a notification in ~18 seconds

### Debug Panel
Long-press the "ZombieThumb" title on the Home screen to open the Debug Panel showing live:
- Flick interval, interaction ratio, tilt, lux
- Feed mileage, trance score
- All biomarker scores updating in real time
- "Simulate Trance" button for instant demo

## Architecture

```
com.zombiethumb
├── data/               # Sensors, accessibility, Room DB, DataStore
├── domain/             # TranceEngine, biomarkers (pure Kotlin, unit-tested)
├── service/            # MonitoringService, NotificationHelper, Gemma
└── ui/                 # Compose screens, ViewModels, theme
```

### Tech Stack
- Kotlin, Jetpack Compose (Material 3)
- Coroutines + Flow
- Room (local only), DataStore for settings
- MediaPipe LLM Inference (Gemma 2B, optional)
- Min SDK 29, Target SDK 34

## Known Limitations

1. **Gemma model loading**: The 1.3GB model file must be manually pushed to the device. The app works fully without it via template messages.
2. **Emulator sensor accuracy**: Accelerometer/gyroscope readings on emulators are limited. Use Demo Mode or the Simulate Trance button for testing.
3. **Accessibility service on Android 13+**: Sideloaded APKs require manually enabling "Allow restricted settings" before the accessibility service can be toggled on.
4. **Package visibility**: The app monitors a hardcoded list of social media package names. Users can toggle individual apps in Settings.
5. **No custom notification icon**: Uses a system drawable as placeholder.

## License

Hackathon project — MIT License.
