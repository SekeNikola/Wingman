# Wingman Launcher — Architecture Document

## 1. Project Overview

Wingman is a fully functional Android launcher app styled as a retro-futuristic handheld terminal. It replaces the standard Android home screen and presents a list-based, cursor-driven UI rendered entirely in Jetpack Compose, with no Material Design components, no icon grids, and no standard Android chrome.

---

## 2. Technology Stack

| Concern | Choice | Rationale |
|---|---|---|
| Language | Kotlin | Required per brief; idiomatic for Compose |
| UI Toolkit | Jetpack Compose (no XML) | Declarative, state-driven, enables custom drawing |
| Architecture Pattern | MVVM + Unidirectional Data Flow | Clean separation; Compose works naturally with StateFlow |
| Navigation | State-based (no Fragments, no NavHost) | Required per brief; single sealed class controls screen |
| Dependency Injection | Hilt | Standard Jetpack DI, minimal boilerplate |
| Async | Kotlin Coroutines + StateFlow | Lifecycle-safe, Compose-friendly reactive streams |
| Local DB | Room | Stores organizer notes/tasks; typed, compile-safe |
| Audio | Android MediaPlayer (wrapped in a repository) | Simple mock music playback; no external dependency needed |
| Font | Press Start 2P (Google Fonts / bundled asset) | Pixel-art monospaced, matches visual target |
| Custom Drawing | Compose Canvas + Modifier | CRT scanlines, noise grain, glow — drawn without bitmaps |
| Launcher Registration | AndroidManifest HOME intent filter | Required to appear as selectable launcher |
| Testing | JUnit 4 + Compose UI Test + MockK | Unit tests on ViewModels/repositories, UI tests on screens |
| Min SDK | 26 (Android 8.0) | Covers broad mid-range device base |
| Target SDK | 35 | Latest stable at time of writing |

---

## 3. Launcher Registration

`AndroidManifest.xml` must include on `MainActivity`:

```xml
<intent-filter>
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.HOME" />
    <category android:name="android.intent.category.DEFAULT" />
</intent-filter>
```

`launchMode="singleTask"` prevents duplicate instances. `taskAffinity=""` prevents the launcher from being included in the app task stack.

---

## 4. Module & Folder Structure

```
app/
├── src/
│   ├── main/
│   │   ├── AndroidManifest.xml
│   │   ├── assets/
│   │   │   └── fonts/
│   │   │       └── press_start_2p.ttf
│   │   ├── res/
│   │   │   └── raw/
│   │   │       ├── sfx_click.ogg
│   │   │       └── sfx_scroll.ogg
│   │   └── java/com/wingman/launcher/
│   │       │
│   │       ├── MainActivity.kt               ← single activity entry point
│   │       │
│   │       ├── di/
│   │       │   ├── AppModule.kt              ← Hilt module: DB, repos
│   │       │   └── AudioModule.kt            ← Hilt module: audio repo
│   │       │
│   │       ├── data/
│   │       │   ├── db/
│   │       │   │   ├── WingmanDatabase.kt    ← Room database
│   │       │   │   ├── dao/
│   │       │   │   │   ├── NoteDao.kt
│   │       │   │   │   └── TaskDao.kt
│   │       │   │   └── entity/
│   │       │   │       ├── NoteEntity.kt
│   │       │   │       └── TaskEntity.kt
│   │       │   ├── model/                    ← Domain models (shared interface layer)
│   │       │   │   ├── MenuItem.kt
│   │       │   │   ├── Note.kt
│   │       │   │   ├── Task.kt
│   │       │   │   ├── ScanFile.kt
│   │       │   │   ├── TrackInfo.kt
│   │       │   │   ├── SettingsState.kt
│   │       │   │   └── SystemStatus.kt
│   │       │   ├── repository/
│   │       │   │   ├── OrganizerRepository.kt
│   │       │   │   ├── ScansRepository.kt
│   │       │   │   ├── MusicRepository.kt
│   │       │   │   └── SettingsRepository.kt
│   │       │   └── source/
│   │       │       ├── FakeScansDataSource.kt   ← in-memory dummy scan files
│   │       │       ├── FakeMusicDataSource.kt   ← in-memory dummy tracks
│   │       │       └── PrefsDataSource.kt       ← SharedPreferences wrapper
│   │       │
│   │       ├── viewmodel/
│   │       │   ├── HomeViewModel.kt
│   │       │   ├── ScansViewModel.kt
│   │       │   ├── OrganizerViewModel.kt
│   │       │   ├── MusicViewModel.kt
│   │       │   └── SettingsViewModel.kt
│   │       │
│   │       ├── ui/
│   │       │   ├── navigation/
│   │       │   │   └── AppDestination.kt     ← sealed class for screen state
│   │       │   ├── theme/
│   │       │   │   ├── Color.kt
│   │       │   │   ├── Typography.kt
│   │       │   │   └── WingmanTheme.kt
│   │       │   ├── components/
│   │       │   │   ├── TopBar.kt
│   │       │   │   ├── MenuRow.kt
│   │       │   │   ├── PixelIcon.kt
│   │       │   │   ├── ScanlineOverlay.kt
│   │       │   │   ├── GlowBox.kt
│   │       │   │   ├── NoiseOverlay.kt
│   │       │   │   ├── BootScreen.kt
│   │       │   │   └── StatusIndicators.kt
│   │       │   ├── input/
│   │       │   │   └── InputHandler.kt       ← unified keyboard/scroll/touch router
│   │       │   ├── screen/
│   │       │   │   ├── HomeScreen.kt
│   │       │   │   ├── ScansScreen.kt
│   │       │   │   ├── OrganizerScreen.kt
│   │       │   │   ├── TutorialsScreen.kt
│   │       │   │   ├── MusicScreen.kt
│   │       │   │   └── SettingsScreen.kt
│   │       │   └── WingmanApp.kt             ← root composable, owns nav state
│   │       │
│   │       └── util/
│   │           ├── AudioPlayer.kt            ← MediaPlayer wrapper
│   │           └── Extensions.kt
│   │
│   ├── test/                                 ← JUnit unit tests
│   │   └── java/com/wingman/launcher/
│   │       ├── viewmodel/
│   │       └── repository/
│   │
│   └── androidTest/                          ← Compose UI tests
│       └── java/com/wingman/launcher/
│           ├── HomeScreenTest.kt
│           └── NavigationTest.kt
│
├── build.gradle.kts
└── proguard-rules.pro
```

---

## 5. Navigation Architecture

Navigation is controlled by a single `AppDestination` sealed class held as a `StateFlow` in a root-level `NavigationState` object owned by `WingmanApp`. No Jetpack Navigation component is used.

Screen transitions:
- `WingmanApp` observes `currentDestination: StateFlow<AppDestination>`
- A `when` expression renders the appropriate screen composable
- Back navigation is handled by `BackHandler` in each sub-screen, which pops back to `Home`
- The boot sequence (`BootDestination`) transitions automatically after its animation completes

Transition animations are driven by `AnimatedContent` keyed on the destination, using custom enter/exit specs that feel like a terminal mode-switch (fade + vertical clip).

---

## 6. State Management

| Layer | Mechanism |
|---|---|
| UI State | `StateFlow<UiState>` in each ViewModel |
| Navigation | `MutableStateFlow<AppDestination>` in WingmanApp |
| Selection cursor | `mutableStateOf<Int>` local to each screen composable |
| Settings persistence | `SharedPreferences` via `PrefsDataSource`, exposed as `Flow<SettingsState>` |
| Organizer data | Room → `Flow<List<NoteEntity>>` → mapped to domain model in repo |
| Music playback | `MusicViewModel` wraps `AudioPlayer`, exposes `PlayerState` |

---

## 7. Visual Effects Implementation

### Scanline Overlay
A full-screen `Canvas` composable drawn on top of all content using `Modifier.zIndex`. Draws horizontal semi-transparent lines every N pixels. Alpha driven by `SettingsState.effectIntensity`.

### Noise / Grain Overlay
A `Canvas` composable that generates a small random-pixel bitmap each frame (or uses a pre-baked noise texture from `res/drawable`). Drawn with low alpha. Intensity controlled by settings.

### Glow Effect
Implemented via nested `Box` with `Modifier.blur()` (API 31+) or a manual multi-layer shadow approach for older APIs. Active menu row gets a warm orange aura.

### Pixel Font
`Press Start 2P` loaded via `FontFamily(Font(R.font.press_start_2p))` and registered in `WingmanTheme` typography. All `Text` composables use `WingmanTheme.typography`.

### Boot Screen
A coroutine-driven sequence composable that displays lines of fake terminal text with delays, then transitions to `Home`. Text typed character by character using a `LaunchedEffect`.

### Flicker Animation
A looping `InfiniteTransition` drives a very slight alpha oscillation (0.97f–1.0f) on the root composable to simulate CRT flicker.

---

## 8. Input Handling

`InputHandler` is a Compose modifier extension that intercepts:
- `KeyEvent.KEYCODE_DPAD_UP` / `KEYCODE_DPAD_DOWN` → move selection
- `KeyEvent.KEYCODE_DPAD_CENTER` / `KEYCODE_ENTER` → activate
- `KeyEvent.KEYCODE_BACK` / `KEYCODE_ESCAPE` → navigate back
- Scroll wheel delta → mapped to up/down selection movement with stepped feel

Touch is handled natively through `.clickable` with `indication = null` (ripple disabled) following the two-tap pattern:
1. First tap on unselected row → selects it
2. Tap on already-selected row → activates it

Keyboard repeat acceleration: a `LaunchedEffect` with a short delay loop detects held key state and increases scroll speed after 300 ms hold threshold.

---

## 9. Audio

Sound effects (`sfx_click.ogg`, `sfx_scroll.ogg`) are short clips stored in `res/raw/`. `AudioPlayer` wraps `SoundPool` for low-latency playback. Triggered on selection change and activation events.

Music section uses `MediaPlayer` playing a dummy track. `MusicRepository` exposes play/pause/next/previous as suspend functions. Playback state flows back as `PlayerState`.

---

## 10. Data Layer Summary

| Data Source | Storage | Notes |
|---|---|---|
| Organizer (notes/tasks) | Room SQLite | Persisted, editable |
| Scans | In-memory `FakeScansDataSource` | Static dummy file list |
| Music tracks | In-memory `FakeMusicDataSource` | Static track metadata |
| Settings | SharedPreferences | Effect intensity, theme variant |
| System status (battery, signal) | Android `BatteryManager` + `TelephonyManager` | Polled on resume |

---

## 11. Performance Considerations

- Use `Column` (not `LazyColumn`) for the main 5-item menu — tight control over animation and selection state without recycler overhead at this size.
- Sub-screens with longer lists (Scans, Organizer) use `LazyColumn` with stable keys to minimize recomposition.
- Noise overlay uses a pre-baked 64x64 pixel noise bitmap tiled via `DrawScope.drawImage` rather than generating random pixels per frame.
- All `animateColorAsState` and `animateFloatAsState` use `tween` specs with short durations (80–150 ms) to feel snappy but not instant.
- Avoid allocations inside `drawWithContent` / Canvas callbacks.

---

## 12. Build Configuration

```
compileSdk = 35
minSdk = 26
targetSdk = 35

dependencies:
  - androidx.compose.bom (latest stable)
  - androidx.activity:activity-compose
  - androidx.lifecycle:lifecycle-viewmodel-compose
  - androidx.room:room-runtime + room-ktx + room-compiler (kapt/ksp)
  - com.google.dagger:hilt-android + hilt-compiler
  - androidx.hilt:hilt-navigation-compose (only for ViewModel scoping, not navigation)
  - androidx.datastore:datastore-preferences (alternative to SharedPreferences if preferred)
  - io.mockk:mockk (test)
  - androidx.compose.ui:ui-test-junit4 (androidTest)
```
