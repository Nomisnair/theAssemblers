# Snapout Demo Script (60 seconds) 🧘‍♀️📱

## Pre-Demo Setup (do before going on stage)

1. Install the APK on a real phone (not emulator — sensors work better)
2. Enable Accessibility Service: Settings → Accessibility → Snapout → On
3. Grant notification permission when prompted
4. Open Snapout app → Settings → **Enable Demo Mode**
5. Confirm: threshold is 50%, cooldown is 15 seconds
6. Open Snapout Home → Turn on the **Monitoring** toggle
7. Have TikTok or Instagram installed and logged in

---

## The Demo (60 seconds)

### 0:00 — Introduction (10s)
> "Snapout detects when you're doomscrolling — not by reading your screen, but by measuring the *physics* of how you scroll. It's 100% on-device, zero internet, zero cloud."

### 0:10 — Show the Privacy Screen (5s)
- Open the app → Privacy tab
- Point out: "We track scroll speed, tilt angle, ambient light — but NEVER text, messages, or screen content."

### 0:15 — Show the Debug Panel (5s)
- Go to Home → long-press "Snapout" title
- Show the Debug Panel with all live telemetry values at zero
- "These numbers will update in real time as I scroll."

### 0:20 — Open TikTok/Instagram (5s)
- Switch to TikTok or Instagram
- "Watch what happens when I start doomscrolling..."

### 0:25 — Rapid Flick Demo (18s)
- **Rapidly swipe through the feed** — about 25 quick, steady flicks
- Maintain a rhythm of roughly one swipe every 0.7-1.5 seconds
- Don't tap, like, or interact — just scroll
- Keep going for about 18 seconds

### 0:43 — Notification Appears! (7s)
- A heads-up notification should appear within ~18-20 seconds:
  > "You've scrolled 30 meters of TikTok in 18 seconds. Time for a short break."
- Point it out: "Snapout detected the doomscroll pattern and sent a gentle nudge."
- Show the notification actions: "Take a break" and "5 more minutes"

### 0:50 — Show Live Numbers (5s)
- Switch back to Snapout → Debug Panel
- Show live values: flick interval (~1.4s), interaction ratio (~2%), meters scrolled, trance score (>50%)
- "These are the biomarkers updating in real time."

### 0:55 — The Pitch (5s)
> "Snapout measures the *physics* of scrolling, not the content. All data stays on your device. It's your digital wellness companion that respects your privacy."

---

## Backup: Simulate Trance Button

If the live demo doesn't trigger naturally (emulator, bad timing):
1. Open Debug Panel (long-press Home title)
2. Press **"🧟 Simulate Trance"** button
3. A notification appears instantly with simulated telemetry
4. Explain: "This is what the notification looks like with real data — the demo uses actual sensor readings, but we can simulate for reliability."

## Troubleshooting

| Problem | Fix |
|---------|-----|
| No notification after 20s | Check: Is monitoring ON? Is Demo Mode ON? Is the accessibility service enabled? |
| Accessibility service won't toggle | Android 13+ sideloaded: Settings → Apps → Snapout → ⋮ → Allow restricted settings |
| Notification shows but no heads-up | Check notification channel: Settings → Apps → Snapout → Notifications → "Take a break" should be high priority |
| Trance score stays at 0% | Make sure you're scrolling in a monitored app (TikTok, Instagram, YouTube, etc.) |
