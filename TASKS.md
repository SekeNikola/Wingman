# TASKS.md — Wingman Android Launcher Build Sequence

Tasks are sequenced foundation-first. No task should begin before its dependencies are complete.

---

## PHASE 1 — Foundation

### T01
- **title**: Project scaffold + Manifest
- **owner**: data_layer
- **file_paths**:
  - `app/build.gradle.kts`
  - `build.gradle.kts`
  - `gradle/libs.versions.toml`
  - `app/src/main/AndroidManifest.xml`
- **acceptance_criteria**:
  - Project builds and installs to device
  - App appears in launcher picker (HOME intent-filter + DEFAULT + LAUNCHER categories present)
  - Press Start 2P font file placed at `app/src/main/res/font/press_start_2p.xml` font resource alias
  - DataStore dependency declared
  - SoundPool available (no extra dep needed, stdlib)
- **dependencies**: none

---

### T02
- **title**: Color + Typography theme
- **owner**: ui
- **file_paths**:
  - `app/src/main/java/com/wingman/launcher/ui/theme/Color.kt`
  - `app/src/main/java/com/wingman/launcher/ui/theme/Type.kt`
  - `app/src/main/java/com/wingman/launcher/ui/theme/Theme.kt`
  - `app/src/main/assets/fonts/press_start_2p.ttf`
- **acceptance_criteria**:
  - All hex values from ARCHITECTURE.md Color System are declared as named constants
  - Press Start 2P loads correctly at all 4 size tiers (TerminalLarge, TerminalBody, TerminalSmall, TerminalMicro)
  - WingmanTheme composable wraps content with correct background color
  - No Material3 color roles used (custom color system only)
- **dependencies**: T01

---

### T03
- **title**: Core data models + NavigationState + InputEvent
- **owner**: data_layer
- **file_paths**:
  - `app/src/main/java/com/wingman/launcher/data/model/NavigationState.kt`
  - `app/src/main/java/com/wingman/launcher/data/model/InputEvent.kt`
  - `app/src/main/java/com/wingman/launcher/data/model/MenuSection.kt`
  - `app/src/main/java/com/wingman/launcher/data/model/ScanFile.kt`
  - `app/src/main/java/com/wingman/launcher/data/model/OrganizerTask.kt`
  - `app/src/main/java/com/wingman/launcher/data/model/TutorialEntry.kt`
  - `app/src/main/java/com/wingman/launcher/data/model/MusicTrack.kt`
  - `app/src/main/java/com/wingman/launcher/data/model/SettingsState.kt`
- **acceptance_criteria**:
  - All sealed classes, data classes, and enums compile cleanly
  - NavigationState covers all 7 states (Boot, Home, 5 sections)
  - InputEvent covers DPAD_UP, DPAD_DOWN, ENTER, BACK, SCROLL_UP, SCROLL_DOWN, ROTARY_SCROLL
  - MenuSection enum has exactly 5 entries matching BRIEF menu items
  - All section models match ARCHITECTURE.md spec
- **dependencies**: T01

---

### T04
- **title**: Dummy data source + repositories
- **owner**: data_layer
- **file_paths**:
  - `app/src/main/java/com/wingman/launcher/data/source/DummyData.kt`
  - `app/src/main/java/com/wingman/launcher/data/repository/ScansRepository.kt`
  - `app/src/main/java/com/wingman/launcher/data/repository/OrganizerRepository.kt`
  - `app/src/main/java/com/wingman/launcher/data/repository/TutorialsRepository.kt`
  - `app/src/main/java/com/wingman/launcher/data/repository/MusicRepository.kt`
  - `app/src/main/java/com/wingman/launcher/data/repository/SettingsRepository.kt`
- **acceptance_criteria**:
  - DummyData provides at least 8 ScanFiles, 6 OrganizerTasks, 3 TutorialEntries, 6 MusicTracks
  - Scan filenames feel like terminal output (e.g. "SYS_LOG_4471.DAT", "NET_PROBE_09.BIN")
  - SettingsRepository reads/writes DataStore correctly (unit test not required, manual verify)
  - All repositories return `Flow<List<T>>` or `StateFlow<T>` as appropriate
- **dependencies**: T03

---

### T05
- **title**: InputMapper + InputHandler
- **owner**: data_layer
- **file_paths**:
  - `app/src/main/java/com/wingman/launcher/input/InputMapper.kt`
  - `app/src/main/java/com/wingman/launcher/input/InputHandler.kt`
- **acceptance_criteria**:
  - InputMapper is a pure function (`fun map(keyCode: Int, event: KeyEvent): InputEvent?`)
  - Returns null for unmapped keys (no crash)
  - InputHandler holds hold-to-accelerate state: initial delay 300ms, repeat every 80ms
  - InputHandler exposes `fun dispatch(event: InputEvent)` used by MainActivity
  - No reference to Compose or ViewModel in InputMapper
- **dependencies**: T03

---

### T06
- **title**: SoundEngine
- **owner**: data_layer
- **file_paths**:
  - `app/src/main/java/com/wingman/launcher/sound/SoundEngine.kt`
  - `app/src/main/res/raw/sfx_click.ogg`
  - `app/src/main/res/raw/sfx_scroll.ogg`
  - `app/src/main/res/raw/sfx_boot.ogg`
  - `app/src/main/res/raw/sfx_back.ogg`
- **acceptance_criteria**:
  - SoundEngine.init(context) loads all 4 sounds into SoundPool
  - SoundEngine.play(SoundId) plays the sound at 0.6f volume
  - SoundEngine.release() cleans up on Activity destroy
  - If SettingsState.soundEnabled = false, play() is a no-op
  - Sounds are valid OGG files (can be placeholder silence for now, replaced later)
- **dependencies**: T03

---

### T07
- **title**: MainViewModel (navigation state machine)
- **owner**: data_layer
- **file_paths**:
  - `app/src/main/java/com/wingman/launcher/viewmodel/MainViewModel.kt`
- **acceptance_criteria**:
  - Exposes `navState: StateFlow<NavigationState>`, initial value = `NavigationState.Boot`
  - `onInputEvent(InputEvent)` transitions state correctly per ARCHITECTURE.md nav rules
  - `onBootComplete()` transitions Boot → Home(0)
  - Back from Home is a no-op (no crash, no navigation)
  - Enter from Home navigates to correct section by selectedIndex
  - Active index wraps: index 0 + UP = index 4; index 4 + DOWN = index 0
- **dependencies**: T03, T05

---

## PHASE 2 — UI Components

### T08
- **title**: Visual effects modifiers (scanlines, glow, grain)
- **owner**: ui
- **file_paths**:
  - `app/src/main/java/com/wingman/launcher/ui/effects/Scanlines.kt`
  - `app/src/main/java/com/wingman/launcher/ui/effects/Glow.kt`
  - `app/src/main/java/com/wingman/launcher/ui/effects/Grain.kt`
- **acceptance_criteria**:
  - `Modifier.scanlines(alpha: Float)` draws horizontal lines at 2px pitch using drawWithContent
  - `Modifier.terminalGlow(color: Color, radius: Dp)` applies blurred shadow halo behind content
  - `Modifier.noiseGrain(intensity: Float)` draws semi-random noise pattern over content
  - All modifiers respect `themeIntensity` parameter — intensity 0.0 = invisible
  - No Bitmap allocation on every frame (use Canvas paths/drawLine)
- **dependencies**: T02

---

### T09
- **title**: PixelIcon composable (Canvas-drawn icons)
- **owner**: ui
- **file_paths**:
  - `app/src/main/java/com/wingman/launcher/ui/components/PixelIcon.kt`
- **acceptance_criteria**:
  - `PixelIcon(section: MenuSection, color: Color, size: Dp)` draws distinct icon per section
  - SCANS: document with corner fold (5 pixel lines)
  - ORGANIZER: checklist lines (3 horizontal bars, left tick on top)
  - TUTORIALS: stack of 3 pages
  - MUSIC: eighth note
  - SETTINGS: wrench silhouette
  - All drawn via Canvas drawRect/drawLine, no vector drawables
  - Renders cleanly at 16dp and 24dp
- **dependencies**: T02, T03

---

### T10
- **title**: TopBar composable
- **owner**: ui
- **file_paths**:
  - `app/src/main/java/com/wingman/launcher/ui/components/TopBar.kt`
- **acceptance_criteria**:
  - Displays username (from SettingsState) left-aligned in WHITE_PIXEL / TerminalSmall
  - Battery icon (pixel style, Canvas) right-aligned with percentage
  - Signal bars (3 bars, pixel) left of battery
  - Thin 1dp GREEN_DIM separator line below the bar
  - Height: 32dp
  - No Material TopAppBar used
- **dependencies**: T02, T08, T09

---

### T11
- **title**: MenuRow + ActiveRowHighlight composables
- **owner**: ui
- **file_paths**:
  - `app/src/main/java/com/wingman/launcher/ui/components/MenuRow.kt`
  - `app/src/main/java/com/wingman/launcher/ui/components/ActiveRowHighlight.kt`
- **acceptance_criteria**:
  - `MenuRow(section, isActive, onClick)` renders icon + label + optional arrow
  - Active state: AMBER_PRIMARY text, full-width amber background bar, ">" arrow right-aligned
  - Inactive state: GREEN_PRIMARY text, transparent background
  - ActiveRowHighlight draws diagonal stripe texture on the amber bar (45° lines, 3px pitch, 10% opacity)
  - Active row has subtle glow using Modifier.terminalGlow
  - Row height: 52dp minimum (Fitts's law — large tap target)
  - Label is always uppercase
- **dependencies**: T02, T08, T09

---

### T12
- **title**: TerminalText composable
- **owner**: ui
- **file_paths**:
  - `app/src/main/java/com/wingman/launcher/ui/components/TerminalText.kt`
- **acceptance_criteria**:
  - `TerminalText(text, style, color, flickerEnabled)` wraps Compose Text with Press Start 2P
  - Optional flicker: random alpha oscillation between 0.88f–1.0f, period ~4s, implemented via `infiniteTransition`
  - No flicker if `flickerEnabled = false`
  - Supports all 4 type tiers via `style` parameter
- **dependencies**: T02

---

## PHASE 3 — Screens

### T13
- **title**: WingmanApp root composable + MainActivity wiring
- **owner**: ui
- **file_paths**:
  - `app/src/main/java/com/wingman/launcher/ui/WingmanApp.kt`
  - `app/src/main/java/com/wingman/launcher/MainActivity.kt`
- **acceptance_criteria**:
  - WingmanApp collects navState from MainViewModel and renders correct screen via `when`
  - MainActivity.onKeyDown routes to InputHandler, InputHandler dispatches to MainViewModel
  - WingmanTheme wraps WingmanApp
  - SoundEngine.init() called in onCreate, SoundEngine.release() in onDestroy
  - No Fragment usage anywhere
- **dependencies**: T02, T07, T05, T06

---

### T14
- **title**: BootScreen
- **owner**: ui
- **file_paths**:
  - `app/src/main/java/com/wingman/launcher/ui/screens/BootScreen.kt`
- **acceptance_criteria**:
  - Follows boot sequence timing from ARCHITECTURE.md (3.5s total)
  - Lines type in one character at a time using coroutine delay per character
  - CRT flash effect on completion (single-frame white overlay, alpha animate 1→0 over 150ms)
  - Calls `viewModel.onBootComplete()` at end of sequence
  - sfx_boot plays at sequence start
  - Background: BG_PRIMARY, text: GREEN_PRIMARY / TerminalBody
  - "OK" line renders in AMBER_PRIMARY
- **dependencies**: T07, T08, T12, T13

---

### T15
- **title**: HomeScreen (main menu)
- **owner**: ui
- **file_paths**:
  - `app/src/main/java/com/wingman/launcher/ui/screens/HomeScreen.kt`
- **acceptance_criteria**:
  - Renders TopBar + vertical list of 5 MenuRows
  - Active index from `NavigationState.Home.selectedIndex`
  - Touch swipe up/down changes selection (gesture via `detectVerticalDragGestures`)
  - Tap on any row triggers Enter action for that row
  - Scanlines + grain overlays applied to full screen
  - Selection change plays sfx_scroll
  - Enter plays sfx_click
  - Active item glow visible but not overwhelming (radius 8dp, alpha 0.4)
- **dependencies**: T07, T08, T10, T11, T12, T13

---

### T16
- **title**: ScansScreen
- **owner**: ui
- **file_paths**:
  - `app/src/main/java/com/wingman/launcher/ui/screens/ScansScreen.kt`
  - `app/src/main/java/com/wingman/launcher/viewmodel/ScansViewModel.kt`
- **acceptance_criteria**:
  - List of ScanFile rows: filename left, status badge right (CLEAN=green, FLAGGED=amber, QUARANTINED=red-amber)
  - Timestamp and size shown in TerminalSmall / GREEN_MUTED below filename
  - Keyboard/DPAD navigates list (selectedIndex in ScansViewModel)
  - Back input returns to Home via MainViewModel
  - Consistent TopBar at top with section title "SCANS" replacing username area
- **dependencies**: T04, T07, T08, T11, T12, T13

---

### T17
- **title**: OrganizerScreen
- **owner**: ui
- **file_paths**:
  - `app/src/main/java/com/wingman/launcher/ui/screens/OrganizerScreen.kt`
  - `app/src/main/java/com/wingman/launcher/viewmodel/OrganizerViewModel.kt`
- **acceptance_criteria**:
  - Task list: title + priority badge + done indicator
  - HIGH priority: amber label prefix "!!"
  - Done tasks: strikethrough style (use Compose TextDecoration.LineThrough)
  - Enter on a task toggles isDone
  - Consistent styling and TopBar
- **dependencies**: T04, T07, T08, T11, T12, T13

---

### T18
- **title**: TutorialsScreen
- **owner**: ui
- **file_paths**:
  - `app/src/main/java/com/wingman/launcher/ui/screens/TutorialsScreen.kt`
  - `app/src/main/java/com/wingman/launcher/viewmodel/TutorialsViewModel.kt`
- **acceptance_criteria**:
  - List of TutorialEntry titles (index screen)
  - Selecting an entry shows detail view (title + scrollable content body)
  - DPAD_UP/DOWN scrolls content when in detail view
  - Back from detail returns to list; Back from list returns to Home
  - Two-level navigation managed entirely in TutorialsViewModel state
- **dependencies**: T04, T07, T08, T11, T12, T13

---

### T19
- **title**: MusicScreen
- **owner**: ui
- **file_paths**:
  - `app/src/main/java/com/wingman/launcher/ui/screens/MusicScreen.kt`
  - `app/src/main/java/com/wingman/launcher/viewmodel/MusicViewModel.kt`
- **acceptance_criteria**:
  - Now-playing area at top: track title, artist, progress bar (pixel style, amber fill)
  - Play/pause state shown with pixel play/pause indicator
  - Track list below; active track highlighted amber
  - DPAD selects track, Enter plays it
  - Progress bar is a mock (no actual audio playback required)
  - Progress animates via `LaunchedEffect` tick when `isPlaying = true`
- **dependencies**: T04, T07, T08, T11, T12, T13

---

### T20
- **title**: SettingsScreen
- **owner**: ui
- **file_paths**:
  - `app/src/main/java/com/wingman/launcher/ui/screens/SettingsScreen.kt`
  - `app/src/main/java/com/wingman/launcher/viewmodel/SettingsViewModel.kt`
- **acceptance_criteria**:
  - Toggle rows: effectsEnabled, glowEnabled, flickerEnabled, soundEnabled
  - Slider row: themeIntensity (rendered as pixel-style segmented bar, not Material Slider)
  - DPAD navigates rows, Enter toggles/adjusts
  - Changes persist via SettingsRepository (DataStore)
  - Disabling effects live-updates the app immediately (Settings are observed globally)
- **dependencies**: T04, T07, T08, T11, T12, T13

---

## PHASE 4 — Polish

### T21
- **title**: Hold-to-accelerate input tuning + scroll snap animation
- **owner**: data_layer
- **file_paths**:
  - `app/src/main/java/com/wingman/launcher/input/InputHandler.kt`
- **acceptance_criteria**:
  - Holding DPAD fires scroll events with 300ms initial delay, then every 80ms
  - After 1s of continuous hold, repeat rate increases to 40ms
  - HomeScreen selection change uses `animateIntAsState` with short tween (80ms) for snap feel
- **dependencies**: T05, T15

---

### T22
- **title**: Flicker animation + grain randomization
- **owner**: ui
- **file_paths**:
  - `app/src/main/java/com/wingman/launcher/ui/effects/Grain.kt`
  - `app/src/main/java/com/wingman/launcher/ui/components/TerminalText.kt`
- **acceptance_criteria**:
  - Grain pattern regenerates every 3 frames (not every frame — performance)
  - TerminalText flicker uses `infiniteTransition` with random-ish easing
  - Both effects pause when `flickerEnabled = false` in SettingsState
- **dependencies**: T08, T12, T20

---

### T23
- **title**: Sound integration across all screens
- **owner**: data_layer
- **file_paths**:
  - `app/src/main/java/com/wingman/launcher/viewmodel/MainViewModel.kt`
  - `app/src/main/java/com/wingman/launcher/viewmodel/ScansViewModel.kt`
  - `app/src/main/java/com/wingman/launcher/viewmodel/OrganizerViewModel.kt`
  - `app/src/main/java/com/wingman/launcher/viewmodel/MusicViewModel.kt`
  - `app/src/main/java/com/wingman/launcher/viewmodel/SettingsViewModel.kt`
- **acceptance_criteria**:
  - sfx_scroll plays on every list navigation step across all screens
  - sfx_click plays on every Enter/confirm action
  - sfx_back plays on every Back action
  - All sound calls gated by `SettingsState.soundEnabled`
- **dependencies**: T06, T16, T17, T18, T19, T20

---

### T24
- **title**: Final integration QA pass
- **owner**: qa
- **file_paths**: all screen files, MainActivity.kt
- **acceptance_criteria**:
  - App survives rotation (though landscape is not primary — must not crash)
  - Back press from Home does not exit the launcher
  - Accessibility: all interactive elements have contentDescription
  - No memory leaks (SoundPool released, no coroutine leaks)
  - Smooth on a Pixel 4a equivalent (no jank on HomeScreen list)
  - Installs as default launcher on clean Android device
- **dependencies**: T14, T15, T16, T17, T18, T19, T20, T21, T22, T23
