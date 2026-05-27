# INTERFACES.md — Wingman Android Launcher

Shared contracts between data layer and UI layer. All Kotlin. No implementation here — signatures and shapes only.

---

## Sealed Classes

### NavigationState

```kotlin
// data/model/NavigationState.kt
sealed class NavigationState {
    object Boot : NavigationState()
    data class Home(val selectedIndex: Int = 0) : NavigationState()
    object Scans : NavigationState()
    object Organizer : NavigationState()
    object Tutorials : NavigationState()
    object Music : NavigationState()
    object Settings : NavigationState()
}
```

---

### InputEvent

```kotlin
// data/model/InputEvent.kt
sealed class InputEvent {
    object DpadUp : InputEvent()
    object DpadDown : InputEvent()
    object Enter : InputEvent()
    object Back : InputEvent()
    object ScrollUp : InputEvent()      // volume-up fallback, swipe
    object ScrollDown : InputEvent()    // volume-down fallback, swipe
    data class RotaryScroll(val delta: Float) : InputEvent()  // future rotary encoder
}
```

---

## Enums

### MenuSection

```kotlin
// data/model/MenuSection.kt
enum class MenuSection(val label: String, val index: Int) {
    SCANS("SCANS", 0),
    ORGANIZER("ORGANIZER", 1),
    TUTORIALS("TUTORIALS", 2),
    MUSIC("MUSIC", 3),
    SETTINGS("SETTINGS", 4)
}
```

---

### ScanStatus

```kotlin
// data/model/ScanFile.kt (same file as ScanFile)
enum class ScanStatus { CLEAN, FLAGGED, QUARANTINED }
```

---

### Priority

```kotlin
// data/model/OrganizerTask.kt (same file as OrganizerTask)
enum class Priority { HIGH, NORMAL, LOW }
```

---

### SoundId

```kotlin
// sound/SoundEngine.kt
enum class SoundId { CLICK, SCROLL, BOOT, BACK }
```

---

## Data Classes

### ScanFile

```kotlin
// data/model/ScanFile.kt
data class ScanFile(
    val id: String,
    val filename: String,
    val sizeKb: Int,
    val timestamp: String,       // formatted: "2089-04-17 03:22"
    val status: ScanStatus
)
```

---

### OrganizerTask

```kotlin
// data/model/OrganizerTask.kt
data class OrganizerTask(
    val id: String,
    val title: String,
    val body: String,
    val priority: Priority,
    val isDone: Boolean
)
```

---

### TutorialEntry

```kotlin
// data/model/TutorialEntry.kt
data class TutorialEntry(
    val id: String,
    val title: String,
    val content: String,         // plain text, may have "\n" line breaks
    val tags: List<String>
)
```

---

### MusicTrack

```kotlin
// data/model/MusicTrack.kt
data class MusicTrack(
    val id: String,
    val title: String,
    val artist: String,
    val durationSeconds: Int
)
```

---

### MusicPlayerState

```kotlin
// data/model/MusicTrack.kt (companion model)
data class MusicPlayerState(
    val currentTrack: MusicTrack?,
    val isPlaying: Boolean,
    val progressSeconds: Int,
    val tracks: List<MusicTrack>
)
```

---

### SettingsState

```kotlin
// data/model/SettingsState.kt
data class SettingsState(
    val effectsEnabled: Boolean = true,
    val glowEnabled: Boolean = true,
    val flickerEnabled: Boolean = false,
    val themeIntensity: Float = 0.7f,      // 0.0–1.0
    val soundEnabled: Boolean = true,
    val username: String = "WARREN"
)
```

---

## ViewModel Interface Contracts

### MainViewModel

```kotlin
// viewmodel/MainViewModel.kt
class MainViewModel : ViewModel() {

    val navState: StateFlow<NavigationState>

    val settings: StateFlow<SettingsState>          // shared, observed by all screens

    fun onInputEvent(event: InputEvent)

    fun onBootComplete()

    fun navigateTo(state: NavigationState)

    fun navigateBack()
}
```

---

### ScansViewModel

```kotlin
// viewmodel/ScansViewModel.kt
class ScansViewModel : ViewModel() {

    val scans: StateFlow<List<ScanFile>>

    val selectedIndex: StateFlow<Int>

    fun onInputEvent(event: InputEvent)
}
```

---

### OrganizerViewModel

```kotlin
// viewmodel/OrganizerViewModel.kt
class OrganizerViewModel : ViewModel() {

    val tasks: StateFlow<List<OrganizerTask>>

    val selectedIndex: StateFlow<Int>

    fun onInputEvent(event: InputEvent)

    fun toggleTask(id: String)
}
```

---

### TutorialsViewModel

```kotlin
// viewmodel/TutorialsViewModel.kt

sealed class TutorialsNavState {
    object List : TutorialsNavState()
    data class Detail(val entry: TutorialEntry, val scrollOffset: Int = 0) : TutorialsNavState()
}

class TutorialsViewModel : ViewModel() {

    val tutorials: StateFlow<List<TutorialEntry>>

    val selectedIndex: StateFlow<Int>

    val tutorialsNavState: StateFlow<TutorialsNavState>

    fun onInputEvent(event: InputEvent)

    fun openEntry(entry: TutorialEntry)

    fun closeEntry()
}
```

---

### MusicViewModel

```kotlin
// viewmodel/MusicViewModel.kt
class MusicViewModel : ViewModel() {

    val playerState: StateFlow<MusicPlayerState>

    val selectedIndex: StateFlow<Int>

    fun onInputEvent(event: InputEvent)

    fun playTrack(track: MusicTrack)

    fun togglePlayPause()
}
```

---

### SettingsViewModel

```kotlin
// viewmodel/SettingsViewModel.kt
class SettingsViewModel : ViewModel() {

    val settings: StateFlow<SettingsState>

    val selectedIndex: StateFlow<Int>

    fun onInputEvent(event: InputEvent)

    fun toggleEffects()

    fun toggleGlow()

    fun toggleFlicker()

    fun toggleSound()

    fun setThemeIntensity(value: Float)
}
```

---

## Input Layer Contracts

### InputMapper

```kotlin
// input/InputMapper.kt
object InputMapper {
    fun map(keyCode: Int, event: KeyEvent): InputEvent?
}
```

---

### InputHandler

```kotlin
// input/InputHandler.kt
class InputHandler(
    private val onEvent: (InputEvent) -> Unit
) {
    fun handleKeyDown(keyCode: Int, event: KeyEvent)
    fun handleKeyUp(keyCode: Int, event: KeyEvent)
    fun release()   // cancel hold-to-accelerate coroutine
}
```

---

### SoundEngine

```kotlin
// sound/SoundEngine.kt
object SoundEngine {
    fun init(context: Context)
    fun play(id: SoundId, settingsState: SettingsState)
    fun release()
}
```

---

## Composable Function Signatures

All composables return Unit. Parameters listed; lambdas use explicit functional types.

### WingmanApp

```kotlin
// ui/WingmanApp.kt
@Composable
fun WingmanApp(
    mainViewModel: MainViewModel
)
```

---

### BootScreen

```kotlin
// ui/screens/BootScreen.kt
@Composable
fun BootScreen(
    onBootComplete: () -> Unit,
    settings: SettingsState
)
```

---

### HomeScreen

```kotlin
// ui/screens/HomeScreen.kt
@Composable
fun HomeScreen(
    selectedIndex: Int,
    settings: SettingsState,
    onSectionSelected: (MenuSection) -> Unit,
    onSelectionChanged: (Int) -> Unit
)
```

---

### ScansScreen

```kotlin
// ui/screens/ScansScreen.kt
@Composable
fun ScansScreen(
    viewModel: ScansViewModel,
    settings: SettingsState,
    onBack: () -> Unit
)
```

---

### OrganizerScreen

```kotlin
// ui/screens/OrganizerScreen.kt
@Composable
fun OrganizerScreen(
    viewModel: OrganizerViewModel,
    settings: SettingsState,
    onBack: () -> Unit
)
```

---

### TutorialsScreen

```kotlin
// ui/screens/TutorialsScreen.kt
@Composable
fun TutorialsScreen(
    viewModel: TutorialsViewModel,
    settings: SettingsState,
    onBack: () -> Unit
)
```

---

### MusicScreen

```kotlin
// ui/screens/MusicScreen.kt
@Composable
fun MusicScreen(
    viewModel: MusicViewModel,
    settings: SettingsState,
    onBack: () -> Unit
)
```

---

### SettingsScreen

```kotlin
// ui/screens/SettingsScreen.kt
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
)
```

---

### TopBar

```kotlin
// ui/components/TopBar.kt
@Composable
fun TopBar(
    username: String,
    modifier: Modifier = Modifier
)
```

---

### MenuRow

```kotlin
// ui/components/MenuRow.kt
@Composable
fun MenuRow(
    section: MenuSection,
    isActive: Boolean,
    settings: SettingsState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
)
```

---

### ActiveRowHighlight

```kotlin
// ui/components/ActiveRowHighlight.kt
@Composable
fun ActiveRowHighlight(
    modifier: Modifier = Modifier
)
```

---

### PixelIcon

```kotlin
// ui/components/PixelIcon.kt
@Composable
fun PixelIcon(
    section: MenuSection,
    color: Color,
    size: Dp = 16.dp,
    modifier: Modifier = Modifier
)
```

---

### TerminalText

```kotlin
// ui/components/TerminalText.kt
@Composable
fun TerminalText(
    text: String,
    style: TextStyle,
    color: Color,
    flickerEnabled: Boolean = false,
    modifier: Modifier = Modifier
)
```

---

## Modifier Extensions (Effects)

```kotlin
// ui/effects/Scanlines.kt
fun Modifier.scanlines(
    alpha: Float = 0.15f,
    lineSpacing: Dp = 2.dp
): Modifier

// ui/effects/Glow.kt
fun Modifier.terminalGlow(
    color: Color,
    radius: Dp = 8.dp,
    glowAlpha: Float = 0.4f
): Modifier

// ui/effects/Grain.kt
fun Modifier.noiseGrain(
    intensity: Float = 0.04f
): Modifier
```
