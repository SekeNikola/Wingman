# Wingman Launcher — Interfaces & Data Contracts

This document defines all shared types, sealed classes, ViewModel interfaces, and data contracts between the backend (data layer) and frontend (UI layer). The UI layer must only depend on types defined here — never on Room entities or internal data-source types.

---

## 1. Navigation — AppDestination

**File:** `ui/navigation/AppDestination.kt`

```kotlin
sealed class AppDestination {
    object Boot       : AppDestination()
    object Home       : AppDestination()
    object Scans      : AppDestination()
    object Organizer  : AppDestination()
    object Tutorials  : AppDestination()
    object Music      : AppDestination()
    object Settings   : AppDestination()
}
```

**Contract:**
- `WingmanApp` holds a `MutableStateFlow<AppDestination>` initialized to `Boot`.
- All screen composables receive `navigate: (AppDestination) -> Unit` as a parameter.
- No composable directly mutates the navigation state; all changes go through the lambda.
- `Boot` is a terminal state that can only transition to `Home` — no back stack from Boot.

---

## 2. Input Events — InputEvent

**File:** `ui/input/InputHandler.kt`

```kotlin
sealed class InputEvent {
    object Up     : InputEvent()
    object Down   : InputEvent()
    object Select : InputEvent()
    object Back   : InputEvent()
}
```

**Contract:**
- All input hardware (D-pad, keyboard, scroll wheel) is normalized to `InputEvent` before reaching any screen composable.
- Touch events are NOT translated to `InputEvent`; they go through Compose `.clickable` directly.
- Screen composables receive `onInputEvent: (InputEvent) -> Unit` callbacks or use `rememberInputHandler`.

---

## 3. Domain Models

### 3.1 MenuItem

**File:** `data/model/MenuItem.kt`

```kotlin
data class MenuItem(
    val id: String,           // e.g. "SCANS", "ORGANIZER"
    val label: String,        // display label (uppercase)
    val iconType: PixelIconType,
    val destination: AppDestination
)

enum class PixelIconType {
    RADAR, FOLDER, BOOK, MUSIC_NOTE, GEAR
}
```

**Contract:**
- `HomeViewModel` provides the fixed list of 5 `MenuItem` entries.
- The list is never empty and never changes at runtime.
- `PixelIconType` maps to a Canvas-drawn icon in `PixelIcon` composable — no bitmap/vector drawable.

---

### 3.2 SystemStatus

**File:** `data/model/SystemStatus.kt`

```kotlin
data class SystemStatus(
    val username: String,       // always "WARREN" in v1
    val batteryPercent: Int,    // 0–100
    val isCharging: Boolean,
    val signalBars: Int,        // 0–4; 0 = no signal
    val timeLabel: String       // HH:MM formatted string
)

val SystemStatus.Companion.Empty get() = SystemStatus(
    username = "WARREN",
    batteryPercent = 0,
    isCharging = false,
    signalBars = 0,
    timeLabel = "00:00"
)
```

**Contract:**
- `HomeViewModel` emits an updated `SystemStatus` whenever `onResume` is triggered and every 30 seconds while foregrounded.
- UI reads this only from `HomeViewModel.systemStatus: StateFlow<SystemStatus>`.
- `TopBar` composable accepts `SystemStatus` as a plain parameter — no ViewModel reference inside `TopBar`.

---

### 3.3 ScanFile

**File:** `data/model/ScanFile.kt`

```kotlin
data class ScanFile(
    val id: String,
    val filename: String,       // e.g. "SCAN_7741.dat"
    val sizeKb: Int,
    val timestamp: String,      // pre-formatted: "2024-11-03 14:22"
    val status: ScanStatus
)

enum class ScanStatus {
    COMPLETE, CORRUPT, PENDING
}
```

**Contract:**
- Provided by `ScansRepository` as a `Flow<List<ScanFile>>`.
- `ScansViewModel` maps this flow to `ScansUiState`.
- UI must not format or parse `timestamp` — it is already display-ready.

---

### 3.4 Note

**File:** `data/model/Note.kt`

```kotlin
data class Note(
    val id: Long = 0L,
    val title: String,
    val body: String,
    val createdAt: Long         // Unix epoch ms
)
```

---

### 3.5 Task

**File:** `data/model/Task.kt`

```kotlin
data class Task(
    val id: Long = 0L,
    val label: String,
    val isCompleted: Boolean,
    val createdAt: Long
)
```

**Contract (Note + Task):**
- Both are domain models; Room entities (`NoteEntity`, `TaskEntity`) are internal to the data layer and never cross the repository boundary.
- `OrganizerRepository` is the only class that references Room entity types.

---

### 3.6 TrackInfo

**File:** `data/model/TrackInfo.kt`

```kotlin
data class TrackInfo(
    val id: String,
    val title: String,
    val artist: String,
    val durationSeconds: Int
)
```

---

### 3.7 PlayerState

**File:** `data/model/PlayerState.kt`

```kotlin
data class PlayerState(
    val isPlaying: Boolean,
    val currentTrack: TrackInfo?,
    val progressSeconds: Int,
    val queueIndex: Int,
    val totalTracks: Int
)

val PlayerState.Companion.Idle get() = PlayerState(
    isPlaying = false,
    currentTrack = null,
    progressSeconds = 0,
    queueIndex = 0,
    totalTracks = 0
)
```

**Contract:**
- `MusicViewModel` is the single source of truth for `PlayerState`.
- UI reads via `musicViewModel.playerState: StateFlow<PlayerState>`.
- Progress is expressed in seconds (Int), not milliseconds. UI formats for display.

---

### 3.8 SettingsState

**File:** `data/model/SettingsState.kt`

```kotlin
enum class ThemeVariant { STANDARD, HIGH_CONTRAST }

data class SettingsState(
    val effectIntensity: Float,     // 0.0f (off) to 1.0f (maximum)
    val themeVariant: ThemeVariant
)

val SettingsState.Companion.Default get() = SettingsState(
    effectIntensity = 0.5f,
    themeVariant = ThemeVariant.STANDARD
)
```

**Contract:**
- All visual effect composables (`ScanlineOverlay`, `NoiseOverlay`, `GlowBox`) receive `effectIntensity` as a parameter — they never read from settings directly.
- `WingmanApp` collects `SettingsViewModel.settingsState` and passes `effectIntensity` down the tree.

---

## 4. UiState Sealed Classes

Each ViewModel exposes a `UiState` sealed class. The UI renders based on the current sealed state.

### 4.1 ScansUiState

```kotlin
sealed class ScansUiState {
    object Loading : ScansUiState()
    data class Success(val files: List<ScanFile>) : ScansUiState()
    data class Error(val message: String) : ScansUiState()
}
```

### 4.2 OrganizerUiState

```kotlin
data class OrganizerUiState(
    val notes: List<Note>,
    val tasks: List<Task>,
    val isLoading: Boolean
)

val OrganizerUiState.Companion.Empty get() = OrganizerUiState(
    notes = emptyList(),
    tasks = emptyList(),
    isLoading = true
)
```

### 4.3 MusicUiState

```kotlin
// MusicViewModel uses PlayerState directly as its UI state.
// No separate UiState sealed class required.
// ViewModel exposes: playerState: StateFlow<PlayerState>
```

### 4.4 SettingsUiState

```kotlin
// SettingsViewModel exposes SettingsState directly.
// No wrapper needed.
// ViewModel exposes: settingsState: StateFlow<SettingsState>
```

### 4.5 HomeUiState

```kotlin
data class HomeUiState(
    val menuItems: List<MenuItem>,
    val systemStatus: SystemStatus
)
```

---

## 5. ViewModel Interfaces

These describe the public API surface each ViewModel must present to the UI. The UI layer must only call methods and collect flows listed here.

### 5.1 HomeViewModel

```kotlin
// Exposed to UI:
val uiState: StateFlow<HomeUiState>

fun onResume()   // call from LaunchedEffect when screen becomes active
```

### 5.2 ScansViewModel

```kotlin
// Exposed to UI:
val uiState: StateFlow<ScansUiState>

fun refresh()    // manual refresh trigger
```

### 5.3 OrganizerViewModel

```kotlin
// Exposed to UI:
val uiState: StateFlow<OrganizerUiState>

fun addNote(title: String, body: String)
fun deleteNote(note: Note)
fun addTask(label: String)
fun toggleTask(task: Task)
fun deleteTask(task: Task)
```

### 5.4 MusicViewModel

```kotlin
// Exposed to UI:
val playerState: StateFlow<PlayerState>

fun play()
fun pause()
fun next()
fun previous()
```

### 5.5 SettingsViewModel

```kotlin
// Exposed to UI:
val settingsState: StateFlow<SettingsState>

fun updateEffectIntensity(value: Float)   // clamp to 0f..1f internally
fun updateThemeVariant(variant: ThemeVariant)
```

---

## 6. Repository Interfaces

These describe what each repository exposes to ViewModels. ViewModels depend on these interfaces (not concrete implementations) to allow mocking in tests.

### 6.1 OrganizerRepository

```kotlin
interface OrganizerRepository {
    val notes: Flow<List<Note>>
    val tasks: Flow<List<Task>>
    suspend fun addNote(title: String, body: String)
    suspend fun deleteNote(note: Note)
    suspend fun addTask(label: String)
    suspend fun toggleTask(task: Task)
    suspend fun deleteTask(task: Task)
}
```

### 6.2 ScansRepository

```kotlin
interface ScansRepository {
    val scanFiles: Flow<List<ScanFile>>
}
```

### 6.3 MusicRepository

```kotlin
interface MusicRepository {
    val tracks: Flow<List<TrackInfo>>
    suspend fun play(trackId: String)
    suspend fun pause()
    suspend fun resume()
    suspend fun next()
    suspend fun previous()
    val playbackEvents: Flow<PlaybackEvent>
}

sealed class PlaybackEvent {
    data class TrackStarted(val track: TrackInfo) : PlaybackEvent()
    object Paused : PlaybackEvent()
    object Resumed : PlaybackEvent()
    object TrackEnded : PlaybackEvent()
    data class Error(val reason: String) : PlaybackEvent()
}
```

### 6.4 SettingsRepository

```kotlin
interface SettingsRepository {
    val settingsFlow: Flow<SettingsState>
    suspend fun updateEffectIntensity(value: Float)
    suspend fun updateThemeVariant(variant: ThemeVariant)
}
```

---

## 7. Component Parameter Contracts

These define the exact public parameter signatures for shared UI components, so frontend can build screens while backend builds the data layer in parallel.

### TopBar

```kotlin
@Composable
fun TopBar(
    status: SystemStatus,
    modifier: Modifier = Modifier
)
```

### MenuRow

```kotlin
@Composable
fun MenuRow(
    label: String,
    iconType: PixelIconType,
    isSelected: Boolean,
    effectIntensity: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

### ScanlineOverlay

```kotlin
@Composable
fun ScanlineOverlay(
    intensity: Float,           // 0f = invisible, 1f = full intensity
    lineSpacingDp: Dp = 4.dp,
    modifier: Modifier = Modifier
)
```

### GlowBox

```kotlin
@Composable
fun GlowBox(
    isActive: Boolean,
    intensity: Float,
    content: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier
)
```

### NoiseOverlay

```kotlin
@Composable
fun NoiseOverlay(
    intensity: Float,
    modifier: Modifier = Modifier
)
```

### BootScreen

```kotlin
@Composable
fun BootScreen(
    onBootComplete: () -> Unit
)
```

### PixelIcon

```kotlin
@Composable
fun PixelIcon(
    type: PixelIconType,
    tint: Color,
    sizeDp: Dp = 16.dp,
    modifier: Modifier = Modifier
)
```

---

## 8. Key Constant Definitions

These values must be consistent across the entire codebase. Define in a single `Constants.kt` file.

```kotlin
object WingmanConstants {
    const val USERNAME = "WARREN"
    const val BOOT_DURATION_MS = 3_000L
    const val SELECTION_ANIMATION_MS = 80
    const val SCREEN_TRANSITION_MS = 200
    const val MODULE_TRANSITION_OVERLAY_MS = 400
    const val KEY_REPEAT_INITIAL_DELAY_MS = 300L
    const val KEY_REPEAT_FAST_INTERVAL_MS = 80L
    const val FLICKER_ALPHA_MIN = 0.97f
    const val FLICKER_ALPHA_MAX = 1.0f
    const val FLICKER_PERIOD_MIN_MS = 4_000
    const val FLICKER_PERIOD_MAX_MS = 8_000
    const val GLOW_SPREAD_DP = 8
    const val SCANLINE_SPACING_DP = 4
    const val NOISE_BITMAP_SIZE_PX = 64
    const val STATUS_POLL_INTERVAL_MS = 30_000L
}
```

---

## 9. Mapping Rules — Entity to Domain Model

These mappings are internal to the data layer but documented here so both sides understand the shape of data at the boundary.

| Room Entity field | Domain Model field | Notes |
|---|---|---|
| `NoteEntity.id` | `Note.id` | Long, auto-generated |
| `NoteEntity.title` | `Note.title` | Direct |
| `NoteEntity.body` | `Note.body` | Direct |
| `NoteEntity.createdAt` | `Note.createdAt` | Unix ms |
| `TaskEntity.id` | `Task.id` | Long, auto-generated |
| `TaskEntity.label` | `Task.label` | Direct |
| `TaskEntity.isDone` | `Task.isCompleted` | Renamed for clarity |
| `TaskEntity.createdAt` | `Task.createdAt` | Unix ms |

Room entities must **never** be imported in ViewModel or UI files. `OrganizerRepository` is the exclusive mapping boundary.
