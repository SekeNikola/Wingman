# Wingman Launcher — Build Tasks

Tasks are ordered from project foundation to polish. Dependencies are listed per task. "Backend" = data layer (DB, repos, ViewModels, models). "Frontend" = UI layer (Compose screens, components, theme, input).

---

## TASK-01 — Project Setup & Launcher Registration

**Owner:** Backend  
**Priority:** P0 — Blocks everything

**Description:**  
Create the Android project skeleton, configure `build.gradle.kts` with all required dependencies, register `MainActivity` as the HOME launcher in `AndroidManifest.xml`, configure Hilt application class.

**File Paths:**
- `app/build.gradle.kts`
- `build.gradle.kts` (root)
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/wingman/launcher/WingmanApplication.kt`
- `app/src/main/java/com/wingman/launcher/MainActivity.kt`
- `app/src/main/java/com/wingman/launcher/di/AppModule.kt`

**Acceptance Criteria:**
- Project builds and runs without errors.
- App appears as a selectable launcher option in Android launcher chooser.
- When set as default, pressing the Home button on the device opens the app.
- Hilt is initialized; `@HiltAndroidApp` on application class, `@AndroidEntryPoint` on `MainActivity`.
- All Compose BOM, Room, Hilt, and Coroutines dependencies resolve at sync time.

**Dependencies:** None

---

## TASK-02 — Theme, Color System & Font

**Owner:** Frontend  
**Priority:** P0 — Blocks all UI tasks

**Description:**  
Define the complete Wingman visual language: color palette, typography using Press Start 2P font, and a `WingmanTheme` composable wrapper. No Material theming — custom `CompositionLocal` for colors and typography only.

**File Paths:**
- `app/src/main/assets/fonts/press_start_2p.ttf`
- `app/src/main/java/com/wingman/launcher/ui/theme/Color.kt`
- `app/src/main/java/com/wingman/launcher/ui/theme/Typography.kt`
- `app/src/main/java/com/wingman/launcher/ui/theme/WingmanTheme.kt`

**Acceptance Criteria:**
- `WingmanTheme` provides `LocalWingmanColors` and `LocalWingmanTypography` composition locals.
- Color constants defined: `ColorBackground` (#0A0F0A), `ColorPrimary` (#D4821A), `ColorSecondary` (#4A7C4A), `ColorDimText` (#2A4A2A), `ColorHighlightBar` (#B86B2B), `ColorBlack` (#000000).
- `Press Start 2P` font loaded from assets; `WingmanTypography` provides `heading`, `body`, `caption`, `label` text styles.
- `WingmanTheme` wrapper sets window background to `ColorBackground` and disables system UI decoration (full screen, hide status bar).
- No `MaterialTheme` is referenced anywhere in theme files.

**Dependencies:** TASK-01

---

## TASK-03 — Domain Models & Shared Data Classes

**Owner:** Backend  
**Priority:** P0 — Blocks data layer and interfaces

**Description:**  
Define all domain model data classes used across the data layer and exposed to the UI via ViewModels. These are the shared contracts (see INTERFACES.md). No Room annotations here — these are pure domain objects.

**File Paths:**
- `app/src/main/java/com/wingman/launcher/data/model/MenuItem.kt`
- `app/src/main/java/com/wingman/launcher/data/model/Note.kt`
- `app/src/main/java/com/wingman/launcher/data/model/Task.kt`
- `app/src/main/java/com/wingman/launcher/data/model/ScanFile.kt`
- `app/src/main/java/com/wingman/launcher/data/model/TrackInfo.kt`
- `app/src/main/java/com/wingman/launcher/data/model/SettingsState.kt`
- `app/src/main/java/com/wingman/launcher/data/model/SystemStatus.kt`
- `app/src/main/java/com/wingman/launcher/data/model/PlayerState.kt`
- `app/src/main/java/com/wingman/launcher/ui/navigation/AppDestination.kt`

**Acceptance Criteria:**
- All data classes are plain Kotlin (`data class`), no Android or Room imports.
- `AppDestination` is a `sealed class` with objects/data classes for every screen: `Boot`, `Home`, `Scans`, `Organizer`, `Tutorials`, `Music`, `Settings`.
- `SettingsState` contains `effectIntensity: Float` (0f–1f) and `themeVariant: ThemeVariant` (enum: `STANDARD`, `HIGH_CONTRAST`).
- `PlayerState` contains `isPlaying: Boolean`, `currentTrack: TrackInfo?`, `progressSeconds: Int`, `queueIndex: Int`, `totalTracks: Int`.
- `SystemStatus` contains `batteryPercent: Int`, `isCharging: Boolean`, `signalBars: Int` (0–4).
- All models compile with no warnings.

**Dependencies:** TASK-01

---

## TASK-04 — Room Database, DAOs & Entities

**Owner:** Backend  
**Priority:** P1

**Description:**  
Implement Room database with entities for `Note` and `Task` (Organizer section). Include DAOs with Flow-returning queries for reactive UI updates. Provide database migration stubs.

**File Paths:**
- `app/src/main/java/com/wingman/launcher/data/db/WingmanDatabase.kt`
- `app/src/main/java/com/wingman/launcher/data/db/entity/NoteEntity.kt`
- `app/src/main/java/com/wingman/launcher/data/db/entity/TaskEntity.kt`
- `app/src/main/java/com/wingman/launcher/data/db/dao/NoteDao.kt`
- `app/src/main/java/com/wingman/launcher/data/db/dao/TaskDao.kt`
- `app/src/main/java/com/wingman/launcher/di/AppModule.kt` (updated with DB provision)

**Acceptance Criteria:**
- `WingmanDatabase` is a `@Database` with `NoteEntity` and `TaskEntity`.
- `NoteDao` provides: `getAllNotes(): Flow<List<NoteEntity>>`, `insert(note: NoteEntity)`, `delete(note: NoteEntity)`.
- `TaskDao` provides: `getAllTasks(): Flow<List<TaskEntity>>`, `insert(task: TaskEntity)`, `updateDone(id: Long, done: Boolean)`, `delete(task: TaskEntity)`.
- `AppModule` provides `WingmanDatabase` and both DAOs as Hilt singletons.
- Room schema exported for migration tracking.

**Dependencies:** TASK-01, TASK-03

---

## TASK-05 — Repositories & Fake Data Sources

**Owner:** Backend  
**Priority:** P1

**Description:**  
Implement all repositories. `OrganizerRepository` wraps Room DAOs and maps entities to domain models. `ScansRepository` wraps `FakeScansDataSource`. `MusicRepository` wraps `FakeMusicDataSource`. `SettingsRepository` wraps `PrefsDataSource`. All repositories expose `Flow`-based APIs.

**File Paths:**
- `app/src/main/java/com/wingman/launcher/data/source/FakeScansDataSource.kt`
- `app/src/main/java/com/wingman/launcher/data/source/FakeMusicDataSource.kt`
- `app/src/main/java/com/wingman/launcher/data/source/PrefsDataSource.kt`
- `app/src/main/java/com/wingman/launcher/data/repository/OrganizerRepository.kt`
- `app/src/main/java/com/wingman/launcher/data/repository/ScansRepository.kt`
- `app/src/main/java/com/wingman/launcher/data/repository/MusicRepository.kt`
- `app/src/main/java/com/wingman/launcher/data/repository/SettingsRepository.kt`

**Acceptance Criteria:**
- `FakeScansDataSource` returns at least 8 dummy `ScanFile` entries with realistic-sounding filenames (e.g. `SCAN_7741.dat`, `ECHO_PROBE_02.bin`).
- `FakeMusicDataSource` returns at least 5 dummy `TrackInfo` entries.
- `OrganizerRepository` maps `NoteEntity`→`Note` and `TaskEntity`→`Task`; exposes insert/delete/update operations as suspend functions.
- `PrefsDataSource` reads/writes `effectIntensity` and `themeVariant` via `DataStore<Preferences>`.
- `SettingsRepository` exposes `settingsFlow: Flow<SettingsState>` and `suspend fun updateEffectIntensity(value: Float)`.
- All repositories are `@Inject`-able singletons via Hilt.

**Dependencies:** TASK-03, TASK-04

---

## TASK-06 — ViewModels

**Owner:** Backend  
**Priority:** P1

**Description:**  
Implement all five ViewModels. Each ViewModel collects from its repository and exposes a single `UiState` sealed class via `StateFlow`. Navigation intents are exposed as events via `SharedFlow`.

**File Paths:**
- `app/src/main/java/com/wingman/launcher/viewmodel/HomeViewModel.kt`
- `app/src/main/java/com/wingman/launcher/viewmodel/ScansViewModel.kt`
- `app/src/main/java/com/wingman/launcher/viewmodel/OrganizerViewModel.kt`
- `app/src/main/java/com/wingman/launcher/viewmodel/MusicViewModel.kt`
- `app/src/main/java/com/wingman/launcher/viewmodel/SettingsViewModel.kt`
- `app/src/main/java/com/wingman/launcher/util/AudioPlayer.kt`

**Acceptance Criteria:**
- Each ViewModel extends `ViewModel()` and is annotated `@HiltViewModel`.
- `HomeViewModel` exposes `systemStatus: StateFlow<SystemStatus>` updated every 30 seconds via `viewModelScope`.
- `ScansViewModel` exposes `ScansUiState` (Loading, Success, Error sealed states) with list of `ScanFile`.
- `OrganizerViewModel` exposes `OrganizerUiState` containing `List<Note>` and `List<Task>`, and methods `addNote`, `deleteNote`, `addTask`, `toggleTask`.
- `MusicViewModel` exposes `PlayerState: StateFlow<PlayerState>` and methods `play`, `pause`, `next`, `previous`.
- `SettingsViewModel` exposes `SettingsState: StateFlow<SettingsState>` and `updateEffectIntensity`, `updateThemeVariant`.
- No direct Android context reference stored in any ViewModel (use Hilt `@ApplicationContext` only where unavoidable).

**Dependencies:** TASK-03, TASK-05

---

## TASK-07 — Root App Composable & Navigation State

**Owner:** Frontend  
**Priority:** P1 — Blocks all screen tasks

**Description:**  
Implement `WingmanApp` composable that owns navigation state (`MutableStateFlow<AppDestination>`), wraps everything in `WingmanTheme`, and renders the correct screen via `AnimatedContent`. Implement boot sequence trigger.

**File Paths:**
- `app/src/main/java/com/wingman/launcher/ui/WingmanApp.kt`
- `app/src/main/java/com/wingman/launcher/MainActivity.kt` (updated: `setContent { WingmanApp() }`)

**Acceptance Criteria:**
- `WingmanApp` starts on `AppDestination.Boot`.
- After boot animation completes (callback from `BootScreen`), state transitions to `AppDestination.Home`.
- Navigation to sub-screens happens by calling `navigate(destination: AppDestination)` lambda passed down from `WingmanApp`.
- Back from any sub-screen returns to `AppDestination.Home`.
- `AnimatedContent` uses a fade + vertical clip enter/exit animation spec; duration 200 ms.
- Android `BackHandler` in `WingmanApp` pops to Home if not already there; does nothing (does not exit app) if already at Home — launcher apps must not exit on Back.

**Dependencies:** TASK-02, TASK-03

---

## TASK-08 — Shared UI Components

**Owner:** Frontend  
**Priority:** P1 — Blocks all screen tasks

**Description:**  
Build the reusable component library: top bar with fake username and status icons, menu row with pixel icon slot, visual effects overlays (scanlines, noise, glow), and the boot screen animation composable.

**File Paths:**
- `app/src/main/java/com/wingman/launcher/ui/components/TopBar.kt`
- `app/src/main/java/com/wingman/launcher/ui/components/MenuRow.kt`
- `app/src/main/java/com/wingman/launcher/ui/components/PixelIcon.kt`
- `app/src/main/java/com/wingman/launcher/ui/components/ScanlineOverlay.kt`
- `app/src/main/java/com/wingman/launcher/ui/components/GlowBox.kt`
- `app/src/main/java/com/wingman/launcher/ui/components/NoiseOverlay.kt`
- `app/src/main/java/com/wingman/launcher/ui/components/BootScreen.kt`
- `app/src/main/java/com/wingman/launcher/ui/components/StatusIndicators.kt`

**Acceptance Criteria:**
- `TopBar` displays username (hardcoded "WARREN"), battery bar indicator, and signal bars, all drawn with `Canvas` — no `Icon` or `Image` composables from Material.
- `MenuRow` accepts `label: String`, `icon: PixelIconType`, `isSelected: Boolean`, `onClick: () -> Unit`. Renders with orange highlight bar when selected, dimmed text when not. No ripple indication.
- `ScanlineOverlay` draws horizontal lines across its full area; alpha derived from `intensity: Float` parameter.
- `GlowBox` wraps content in a `Box` with a blurred orange border shadow effect on the active state.
- `NoiseOverlay` tiles a pre-baked 64x64 noise bitmap across its bounds at low alpha.
- `BootScreen` types out at least 6 lines of fake terminal text character-by-character, then calls `onBootComplete: () -> Unit`. Total duration ~3 seconds.
- All components use only `WingmanTheme` colors and typography — no hardcoded color values in component files.

**Dependencies:** TASK-02, TASK-06

---

## TASK-09 — Input Handling System

**Owner:** Frontend  
**Priority:** P1

**Description:**  
Implement `InputHandler` — a unified Compose keyboard/scroll/touch event processor. Provides a `Modifier` extension and a composable hook that translates all input types into a normalized `InputEvent` (Up, Down, Select, Back).

**File Paths:**
- `app/src/main/java/com/wingman/launcher/ui/input/InputHandler.kt`

**Acceptance Criteria:**
- `InputHandler` provides `fun Modifier.handleWingmanInput(onEvent: (InputEvent) -> Unit): Modifier`.
- `InputEvent` is a sealed class: `Up`, `Down`, `Select`, `Back`.
- D-pad keys (`KEYCODE_DPAD_UP/DOWN/CENTER`), Enter, Backspace/Back are mapped to corresponding `InputEvent`.
- Scroll wheel delta > threshold maps to `Up` or `Down`.
- Key repeat acceleration: after 300 ms of continuous Up/Down key held, events fire every 80 ms instead of 300 ms.
- Touch tap events fall through to Compose `.clickable` — `InputHandler` does not intercept touch events.
- Composable `@Composable fun rememberInputHandler(onEvent: (InputEvent) -> Unit)` sets up the modifier with `remember`.

**Dependencies:** TASK-02

---

## TASK-10 — Home Screen

**Owner:** Frontend  
**Priority:** P2

**Description:**  
Implement the main menu `HomeScreen` composable with the 5-item list (`SCANS`, `ORGANIZER`, `TUTORIALS`, `MUSIC`, `SETTINGS`). Integrate `InputHandler`, selection state, animated highlight, glow effect, scanline overlay, and top bar.

**File Paths:**
- `app/src/main/java/com/wingman/launcher/ui/screen/HomeScreen.kt`

**Acceptance Criteria:**
- Menu renders 5 items in a `Column` (not `LazyColumn`).
- One item is always selected (highlighted orange bar); others are dimmed.
- `animateColorAsState` drives highlight color transition (80 ms tween).
- Glow effect (`GlowBox`) is active on the selected row only.
- `ScanlineOverlay` and `NoiseOverlay` are drawn on top of the full screen.
- `TopBar` shown at the top with live `SystemStatus` from `HomeViewModel`.
- D-pad Up/Down and scroll move selection; Enter/Center or second tap on selected item fires `navigate(destination)`.
- A subtle screen-wide flicker animation runs via `InfiniteTransition` at ~0.97–1.0 alpha.
- No ripple effects on any interactive element.
- Sound effect triggered on selection change (scroll sound) and on activation (click sound).

**Dependencies:** TASK-07, TASK-08, TASK-09, TASK-06

---

## TASK-11 — Section Screens (Scans, Organizer, Tutorials, Music, Settings)

**Owner:** Frontend  
**Priority:** P2

**Description:**  
Implement the five section screens. Each must be styled consistently with the terminal aesthetic. Keep implementations minimal but complete.

**File Paths:**
- `app/src/main/java/com/wingman/launcher/ui/screen/ScansScreen.kt`
- `app/src/main/java/com/wingman/launcher/ui/screen/OrganizerScreen.kt`
- `app/src/main/java/com/wingman/launcher/ui/screen/TutorialsScreen.kt`
- `app/src/main/java/com/wingman/launcher/ui/screen/MusicScreen.kt`
- `app/src/main/java/com/wingman/launcher/ui/screen/SettingsScreen.kt`

**Acceptance Criteria:**

**ScansScreen:**
- `LazyColumn` list of `ScanFile` items showing filename, size, timestamp.
- Items selectable with D-pad; selected item is highlighted.
- "ACCESSING MODULE…" status line shown below the list header.
- Collects state from `ScansViewModel`; shows loading indicator during `Loading` state.

**OrganizerScreen:**
- Two sections: NOTES and TASKS.
- Notes shown as a list; tasks shown with a `[ ]` / `[X]` prefix indicating done state.
- D-pad navigates between items; Enter toggles task done state.
- Back navigates to Home.

**TutorialsScreen:**
- Static scrollable text screen.
- Contains at least 300 words of placeholder terminal-style tutorial content.
- Scroll input scrolls the text.

**MusicScreen:**
- Shows current track name and artist from `PlayerState`.
- ASCII-art style progress bar.
- Play/Pause, Next, Previous controls navigable by D-pad.
- Activated control triggers `MusicViewModel` action.

**SettingsScreen:**
- Two toggle/slider rows: "EFFECT INTENSITY" (slider 0–100) and "THEME VARIANT" (toggle STANDARD / HIGH_CONTRAST).
- Changes call `SettingsViewModel` update methods immediately.
- Values reflect current `SettingsState`.

All screens: `TopBar` present, `ScanlineOverlay` present, `NoiseOverlay` present, Back returns to Home.

**Dependencies:** TASK-07, TASK-08, TASK-09, TASK-06

---

## TASK-12 — Visual Effects Polish & Animations

**Owner:** Frontend  
**Priority:** P3

**Description:**  
Final polish pass on all visual effects: tune scanline density, noise alpha, glow spread, flicker timing. Add the "ACCESSING MODULE…" transition animation between screens. Ensure effect intensity slider in Settings actually adjusts all effects in real time.

**File Paths:**
- `app/src/main/java/com/wingman/launcher/ui/components/ScanlineOverlay.kt` (tuning)
- `app/src/main/java/com/wingman/launcher/ui/components/NoiseOverlay.kt` (tuning)
- `app/src/main/java/com/wingman/launcher/ui/components/GlowBox.kt` (tuning)
- `app/src/main/java/com/wingman/launcher/ui/WingmanApp.kt` (transition text overlay)

**Acceptance Criteria:**
- Scanlines visible but not distracting at intensity 0.5f.
- Noise grain visible but subtle at intensity 0.5f.
- Glow spreads ~8dp around active item; warm orange (#D4821A at 60% alpha).
- Screen-to-screen transitions show "ACCESSING MODULE…" text overlay for ~400 ms.
- `SettingsState.effectIntensity` changes propagate to all overlay composables within 1 frame via `collectAsState`.
- Flicker animation period is 4–8 seconds per cycle (nearly imperceptible but present).

**Dependencies:** TASK-10, TASK-11

---

## TASK-13 — Unit Tests (Data Layer)

**Owner:** Backend  
**Priority:** P3

**Description:**  
Write JUnit unit tests for repositories and ViewModels using MockK for mocking and Kotlin coroutines test library for `Flow` testing.

**File Paths:**
- `app/src/test/java/com/wingman/launcher/repository/OrganizerRepositoryTest.kt`
- `app/src/test/java/com/wingman/launcher/repository/SettingsRepositoryTest.kt`
- `app/src/test/java/com/wingman/launcher/viewmodel/HomeViewModelTest.kt`
- `app/src/test/java/com/wingman/launcher/viewmodel/MusicViewModelTest.kt`
- `app/src/test/java/com/wingman/launcher/viewmodel/OrganizerViewModelTest.kt`

**Acceptance Criteria:**
- `OrganizerRepositoryTest` verifies insert → Flow emission → domain model mapping.
- `SettingsRepositoryTest` verifies read/write round-trip for all settings fields.
- `HomeViewModelTest` verifies `systemStatus` updates on resume.
- `MusicViewModelTest` verifies `PlayerState` transitions on play/pause/next/previous calls.
- `OrganizerViewModelTest` verifies task toggle updates `isCompleted` in emitted state.
- All tests use `TestCoroutineDispatcher` / `UnconfinedTestDispatcher`; no real I/O.
- All tests pass with `./gradlew test`.

**Dependencies:** TASK-05, TASK-06

---

## TASK-14 — Instrumented UI Tests

**Owner:** Frontend  
**Priority:** P3

**Description:**  
Write Compose instrumented UI tests for the Home screen navigation and one section screen to verify selection behavior, keyboard navigation, and that no Material components are present.

**File Paths:**
- `app/src/androidTest/java/com/wingman/launcher/HomeScreenTest.kt`
- `app/src/androidTest/java/com/wingman/launcher/NavigationTest.kt`

**Acceptance Criteria:**
- `HomeScreenTest` verifies all 5 menu items are displayed.
- `HomeScreenTest` simulates Down key press twice; asserts third item is selected (highlighted).
- `HomeScreenTest` simulates Enter on selected item; asserts correct destination screen is shown.
- `NavigationTest` verifies Back from a sub-screen returns to `HomeScreen`.
- `NavigationTest` verifies Back from `HomeScreen` does not close the activity.
- Tests run on an emulator API 26+ with `./gradlew connectedAndroidTest`.

**Dependencies:** TASK-10, TASK-11
