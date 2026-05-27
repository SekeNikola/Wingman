# ARCHITECTURE.md — Wingman Android Launcher

## Overview

Single-Activity Android launcher. No fragments. No Material Design. Feels like a dedicated OS on retro handheld hardware. Kotlin + Jetpack Compose throughout.

---

## Stack Decisions

| Concern | Decision | Rationale |
|---|---|---|
| Language | Kotlin | Project requirement |
| UI | Jetpack Compose | No XML, declarative, animatable |
| Architecture | MVVM + UDF | Clean state flow, testable ViewModels |
| Navigation | Sealed class state machine | No Compose Navigation lib — avoids back-stack complexity for a launcher. State lives in a single ViewModel. |
| DI | Manual (no Hilt) | Launcher is tiny; Hilt adds weight without benefit at this scale |
| Storage | DataStore Preferences | Settings persistence only. No Room needed for dummy data. |
| Sound | SoundPool | Low-latency for UI click/scroll SFX. Loaded at boot. |
| Font | Press Start 2P via assets | Bundled .ttf, loaded as custom FontFamily — no Google Fonts network dep |
| Effects | Canvas + Compose drawWithContent | Scanlines, grain, glow — all GPU composited, no bitmaps |
| Input | Custom InputEvent sealed class | Abstracted from raw KeyEvent — decoupled from hardware |

---

## Folder / File Structure

Standard Android project layout. Package name: `com.wingman.launcher`

```
wingman/
├── app/
│   ├── build.gradle.kts
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml            ← HOME intent-filter, LAUNCHER category
│           ├── assets/
│           │   └── fonts/
│           │       └── press_start_2p.ttf
│           ├── res/
│           │   └── raw/
│           │       ├── sfx_click.ogg
│           │       ├── sfx_scroll.ogg
│           │       ├── sfx_boot.ogg
│           │       └── sfx_back.ogg
│           └── java/com/wingman/launcher/
│               ├── MainActivity.kt
│               │
│               ├── data/
│               │   ├── model/
│               │   │   ├── NavigationState.kt       ← sealed class
│               │   │   ├── InputEvent.kt            ← sealed class
│               │   │   ├── MenuSection.kt           ← enum
│               │   │   ├── ScanFile.kt
│               │   │   ├── OrganizerTask.kt
│               │   │   ├── TutorialEntry.kt
│               │   │   ├── MusicTrack.kt
│               │   │   └── SettingsState.kt
│               │   ├── repository/
│               │   │   ├── ScansRepository.kt
│               │   │   ├── OrganizerRepository.kt
│               │   │   ├── TutorialsRepository.kt
│               │   │   ├── MusicRepository.kt
│               │   │   └── SettingsRepository.kt   ← wraps DataStore
│               │   └── source/
│               │       └── DummyData.kt             ← all stub data
│               │
│               ├── input/
│               │   ├── InputMapper.kt               ← KeyEvent → InputEvent
│               │   └── InputHandler.kt              ← dispatches InputEvents to ViewModel
│               │
│               ├── sound/
│               │   └── SoundEngine.kt               ← SoundPool wrapper, play/release
│               │
│               ├── ui/
│               │   ├── theme/
│               │   │   ├── Color.kt
│               │   │   ├── Type.kt
│               │   │   └── Theme.kt                 ← WingmanTheme composable
│               │   │
│               │   ├── effects/
│               │   │   ├── Scanlines.kt             ← Modifier.scanlines()
│               │   │   ├── Glow.kt                  ← Modifier.terminalGlow()
│               │   │   └── Grain.kt                 ← Modifier.noiseGrain()
│               │   │
│               │   ├── components/
│               │   │   ├── TopBar.kt                ← username + battery/signal
│               │   │   ├── MenuRow.kt               ← single menu item row
│               │   │   ├── PixelIcon.kt             ← Canvas-drawn pixel icons
│               │   │   ├── ActiveRowHighlight.kt    ← animated amber bar + diagonal stripes
│               │   │   └── TerminalText.kt          ← styled Text with optional flicker
│               │   │
│               │   ├── screens/
│               │   │   ├── BootScreen.kt
│               │   │   ├── HomeScreen.kt            ← main menu list
│               │   │   ├── ScansScreen.kt
│               │   │   ├── OrganizerScreen.kt
│               │   │   ├── TutorialsScreen.kt
│               │   │   ├── MusicScreen.kt
│               │   │   └── SettingsScreen.kt
│               │   │
│               │   └── WingmanApp.kt                ← root composable, nav state switch
│               │
│               └── viewmodel/
│                   ├── MainViewModel.kt             ← nav state, active index, input dispatch
│                   ├── ScansViewModel.kt
│                   ├── OrganizerViewModel.kt
│                   ├── TutorialsViewModel.kt
│                   ├── MusicViewModel.kt
│                   └── SettingsViewModel.kt
│
├── build.gradle.kts
└── gradle/
    └── libs.versions.toml
```

---

## Color System

All values are exact matches to the reference device image.

```
// ui/theme/Color.kt

BG_PRIMARY       = #0A0C0A   // near-black with slight green cast — main background
BG_SURFACE       = #111411   // slightly lighter — row backgrounds
BG_TOPBAR        = #0D0F0D   // top bar background

AMBER_PRIMARY    = #E8820C   // active highlight bar, active text, arrow cursor
AMBER_GLOW       = #F5A030   // glow corona around active item (lower opacity)
AMBER_DIM        = #7A4406   // dimmed amber for inactive decorative elements

GREEN_PRIMARY    = #4CAF50   // inactive menu text (pixel green, not Material green)
GREEN_MUTED      = #2E5C30   // secondary text, status indicators, dim labels
GREEN_DIM        = #1A3320   // barely visible tint for separators

SCANLINE_TINT    = #00000033 // semi-transparent black for scanline overlay
GRAIN_TINT       = #FFFFFF08 // near-invisible white noise layer

WHITE_PIXEL      = #D4D8D0   // brightest text (username, headings)
CURSOR_ARROW     = #E8820C   // ">" glyph, same as AMBER_PRIMARY
```

---

## Typography Plan

Font: **Press Start 2P** (Google Fonts open license)
- Bundled as `assets/fonts/press_start_2p.ttf` — zero network dependency, works offline
- Loaded via `FontFamily(Font(R.font.press_start_2p))` using a font resource alias in `res/font/`

```
// ui/theme/Type.kt

TerminalLarge    — 14sp, LetterSpacing 0.1em  → section titles, boot lines
TerminalBody     — 10sp, LetterSpacing 0.05em → menu items, list rows
TerminalSmall    —  8sp, LetterSpacing 0.05em → metadata, timestamps, status
TerminalMicro    —  6sp, LetterSpacing 0em    → pixel icons label, battery %
```

All text: ALL_CAPS enforced at the composable level via `text.uppercase()`.
Line height: 1.6× font size minimum (pixel fonts need breathing room).

---

## Navigation Architecture

Single sealed class. MainViewModel holds `val navState: StateFlow<NavigationState>`.

```
NavigationState
├── Boot                         // animated boot sequence, auto-advances
├── Home(selectedIndex: Int)     // main menu, index 0-4
├── Scans
├── Organizer
├── Tutorials
├── Music
└── Settings
```

Transitions:
- `Boot` → `Home(0)` automatically after boot animation completes
- `Home` + Enter → target section state
- Any section + Back → `Home(lastIndex)`
- `Home` + Back → no-op (launcher root, swallow the event)

WingmanApp.kt: `when(navState)` switch renders the correct screen composable. No Compose Navigation, no NavHost, no back stack library.

---

## Input Handling Architecture

Goal: zero coupling between hardware input and business logic.

```
Layer 1 — Hardware
  MainActivity.onKeyDown(keyCode, event)
  MainActivity.onGenericMotionEvent(event)       ← rotary/scroll wheel future

Layer 2 — InputMapper (pure function, no state)
  KeyEvent → InputEvent sealed class
  Maps: DPAD_UP, DPAD_DOWN, ENTER, BACK, VOLUME_UP/DOWN (scroll fallback)

Layer 3 — InputHandler
  Receives InputEvent, routes to active ViewModel
  Handles hold-to-accelerate timer (repeat delay 300ms, repeat rate 80ms)

Layer 4 — ViewModel
  Accepts InputEvent, mutates NavigationState or section-specific state
  No reference to Android KeyEvent anywhere in ViewModel
```

Touch input: composables receive standard tap/swipe gestures directly. Swipe up/down on HomeScreen maps to the same selection change as DPAD. This keeps touch as a parallel path, not a replacement.

Future rotary encoder: add `InputEvent.RotaryScroll(delta: Float)` to the sealed class and map `onGenericMotionEvent` → `InputMapper`. ViewModels need zero changes.

---

## Boot Sequence Plan

Duration: ~3.5 seconds total. Auto-advance to Home.

```
0.0s  — Black screen
0.3s  — Top bar fades in: "WINGMAN OS v1.0"
0.8s  — Cursor blink starts
1.0s  — Line 1 types out: "INITIALIZING SYSTEM..."
1.6s  — Line 2: "LOADING MODULES..."
2.2s  — Line 3: "ACCESSING LAUNCHER..."
2.8s  — Line 4: "OK" (amber, bold)
3.0s  — Screen flash (single frame white flash, like CRT power-on)
3.2s  — Crossfade to HomeScreen
3.5s  — Boot complete, sfx_boot.ogg plays at 0.0s
```

Implementation: `BootScreen` uses `LaunchedEffect` with `delay()` steps updating a `bootLines: List<String>` state. When complete, calls `viewModel.onBootComplete()`.

---

## Sound Effects Plan

Engine: `SoundPool` (max 4 simultaneous streams).
All files: OGG Vorbis, mono, ~44100Hz, <1 second each.
Loaded in `SoundEngine.init()` called from `MainActivity.onCreate()`.

| ID | File | Trigger |
|---|---|---|
| SFX_CLICK | sfx_click.ogg | Enter key / tap to open section |
| SFX_SCROLL | sfx_scroll.ogg | DPAD up/down, each step |
| SFX_BOOT | sfx_boot.ogg | BootScreen start |
| SFX_BACK | sfx_back.ogg | Back key, return to Home |

Volume: all at 0.6f default. SettingsState.effectsEnabled gates all playback.
`SoundEngine` is a singleton object, referenced by MainViewModel and section ViewModels.

---

## Section Screen Data Models

### SCANS
List of dummy "scanned files." Feels like a filesystem scanner log.
```
ScanFile(id, filename, sizeKb, timestamp, status: ScanStatus)
ScanStatus: enum { CLEAN, FLAGGED, QUARANTINED }
```

### ORGANIZER
Task/note list. Minimal but styled.
```
OrganizerTask(id, title, body, priority: Priority, isDone)
Priority: enum { HIGH, NORMAL, LOW }
```

### TUTORIALS
Static text entries. Scrollable doc viewer.
```
TutorialEntry(id, title, content, tags: List<String>)
```

### MUSIC
Mock player. List + now-playing state.
```
MusicTrack(id, title, artist, durationSeconds)
MusicPlayerState(currentTrack, isPlaying, progressSeconds, tracks: List<MusicTrack>)
```

### SETTINGS
Toggle-based settings. Persisted via DataStore.
```
SettingsState(
  effectsEnabled: Boolean,        // scanlines + grain
  glowEnabled: Boolean,           // glow on active item
  flickerEnabled: Boolean,        // subtle screen flicker
  themeIntensity: Float,          // 0.0–1.0, controls effect strength
  soundEnabled: Boolean,
  username: String                // displayed in TopBar
)
```
