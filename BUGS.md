# Wingman Launcher — Bug Report

QA + Security Review | Cycle 1 | 2026-05-04

---

### BUG-01 — AudioPlayer.release() is never called — SoundPool resource leak
Status: FIXED
Severity: P2
File: `app/src/main/kotlin/com/wingman/launcher/util/AudioPlayer.kt:70`
`app/src/main/kotlin/com/wingman/launcher/MainActivity.kt`
Description: `AudioPlayer` is a `@Singleton` that wraps a `SoundPool`. It exposes a `release()` method with the KDoc note "Call when the app is destroyed." However, `release()` is never called anywhere in the codebase. `MainActivity` does not override `onDestroy()`, and no ViewModel calls `release()` on its `onCleared()`. This is a native-resource leak that accumulates audio handles across process lifetime.
Suggested Fix: Override `onDestroy()` in `MainActivity` and call `audioPlayer.release()`, or move the `SoundPool` lifecycle into the Hilt `@Singleton` component's cleanup. A `ProcessLifecycleObserver` can also be used if direct Activity access is undesirable.
Verification (Cycle 2): `MainActivity` now has `@Inject lateinit var audioPlayer: AudioPlayer` and overrides `onDestroy()` calling `audioPlayer.release()`. Confirmed fixed.

---

### BUG-02 — AudioPlayer is wired in Hilt but never injected into any screen or ViewModel — sound effects are completely absent
Status: FIXED
Severity: P2
File: `app/src/main/kotlin/com/wingman/launcher/ui/screen/HomeScreen.kt`
`app/src/main/kotlin/com/wingman/launcher/di/AudioModule.kt`
Description: `AudioPlayer` is registered as a singleton in `AudioModule` and the `AudioPlayer` class itself exists with `playScroll()` and `playClick()` methods. However, `AudioPlayer` is injected into zero composables, ViewModels, or screens. TASK-10 acceptance criteria explicitly require "Sound effect triggered on selection change (scroll sound) and on activation (click sound)." These sound effects are never triggered. The `AudioModule.provideAudioPlayer` redundancy (it only wraps `@Inject` AudioPlayer with itself) is also a code smell but not the root cause.
Suggested Fix: Inject `AudioPlayer` into `HomeViewModel` or pass it as a parameter to `HomeScreen`. Call `audioPlayer.playScroll()` inside the `InputEvent.Up` / `InputEvent.Down` branches of the `rememberInputHandler` lambda, and `audioPlayer.playClick()` on `InputEvent.Select`.
Verification (Cycle 2): `HomeViewModel` constructor now injects `AudioPlayer`. `onScrollEvent()` calls `audioPlayer.playScroll()` and `onSelectEvent()` calls `audioPlayer.playClick()`. `HomeScreen` calls `viewModel.onScrollEvent()` on Up/Down and `viewModel.onSelectEvent()` on Select. Confirmed fixed.

---

### BUG-03 — HomeViewModel.onPause() is defined but never called — polling loop runs forever in the background
Status: FIXED
Severity: P2
File: `app/src/main/kotlin/com/wingman/launcher/viewmodel/HomeViewModel.kt:81`
`app/src/main/kotlin/com/wingman/launcher/ui/screen/HomeScreen.kt`
Description: `HomeViewModel.onPause()` cancels the 30-second status polling coroutine. `onResume()` is correctly called via a `LaunchedEffect(Unit)` in `HomeScreen`. However, `onPause()` is never called when the user navigates away from `HomeScreen`. The polling coroutine therefore continues firing every 30 seconds even when the user is on a sub-screen (Scans, Music, etc.), performing unnecessary `BatteryManager` and `TelephonyManager` reads and keeping the CPU awake for a background task that is not needed.
Suggested Fix: Add a `DisposableEffect` to `HomeScreen` (or use `LifecycleEventEffect` from `lifecycle-runtime-compose`) that calls `viewModel.onPause()` in the `onDispose` lambda. Example:
```kotlin
DisposableEffect(Unit) {
    onDispose { viewModel.onPause() }
}
```
Verification (Cycle 2): `HomeScreen` now has `DisposableEffect(Unit) { onDispose { viewModel.onPause() } }` alongside the existing `LaunchedEffect(Unit) { viewModel.onResume() }`. Confirmed fixed.

---

### BUG-04 — SettingsScreen receives a pre-constructed SettingsViewModel from WingmanApp but also declares its own hiltViewModel() default — parameter mismatch creates two ViewModel instances
Status: FIXED
Severity: P2
File: `app/src/main/kotlin/com/wingman/launcher/ui/WingmanApp.kt:142-145`
`app/src/main/kotlin/com/wingman/launcher/ui/screen/SettingsScreen.kt:44-48`
Description: `WingmanApp` passes the root-level `settingsViewModel` instance to `SettingsScreen(settingsViewModel = settingsViewModel)`. The `SettingsScreen` function signature defaults `settingsViewModel: SettingsViewModel = hiltViewModel()`. When the explicit parameter is passed the correct ViewModel instance is used. However, `SettingsScreen` also calls `hiltViewModel()` for `homeViewModel`, meaning `SettingsScreen` has two ViewModels with different provenance. The deeper issue: `WingmanApp` reads `settingsState` from `settingsViewModel` (line 54) AND passes the same VM to `SettingsScreen` which also reads `settingsState` from it. This is structurally fragile — if anyone removes the explicit parameter the screen silently gets a second ViewModel instance that may not reflect the same DataStore state immediately due to the internal `MutableStateFlow` initialized from `SettingsState.Default`. Furthermore, `SettingsScreen` does NOT accept `effectIntensity` as a parameter (unlike all other screens) but internally reads from `settingsViewModel.settingsState` for the overlay intensities — this breaks the parameter contract defined in INTERFACES.md §7 (all overlay composables should receive `effectIntensity` as a parameter, not read from ViewModel directly). The TASK-12 note says "effectIntensity propagated from WingmanApp root -> all screens via parameter, not direct SettingsViewModel access in leaf composables," but `SettingsScreen` violates this.
Suggested Fix: Add `effectIntensity: Float` to `SettingsScreen`'s parameter list (matching the other screens). `WingmanApp` passes `effectIntensity = effectIntensity` alongside `settingsViewModel`. Remove direct `settingsViewModel.settingsState` access from the overlay calls within `SettingsScreen`.
Verification (Cycle 2): `SettingsScreen` now has `effectIntensity: Float` and `systemStatus: SystemStatus` parameters. The overlays use `ScanlineOverlay(intensity = effectIntensity)` and `NoiseOverlay(intensity = effectIntensity)`. `WingmanApp` passes both `effectIntensity = effectIntensity` and `systemStatus = systemStatus`. Confirmed fixed.

---

### BUG-05 — TASK-03 note contradicts INTERFACES.md §3.7: PlayerState field mismatch
Status: FIXED
Severity: P2
File: `c:\Projects\Wingman\TASKS.md` TASK-03 acceptance criteria vs `app/src/main/kotlin/com/wingman/launcher/data/model/PlayerState.kt`
Description: TASKS.md TASK-03 acceptance criteria states: "`PlayerState` contains `isPlaying: Boolean`, `currentTrack: TrackInfo?`, `progress: Float`." However INTERFACES.md §3.7 defines `progressSeconds: Int` (not `progress: Float`), plus two additional required fields: `queueIndex: Int` and `totalTracks: Int`. The implementation correctly follows INTERFACES.md (uses `progressSeconds: Int`, `queueIndex`, `totalTracks`), which is the authoritative spec, but the TASKS.md document is now incorrect and could mislead future developers or a new team member into thinking `progress: Float` is the correct field name. This is a documentation-level contract inconsistency.
Suggested Fix: Update TASKS.md TASK-03 acceptance criteria to read: "`PlayerState` contains `isPlaying: Boolean`, `currentTrack: TrackInfo?`, `progressSeconds: Int`, `queueIndex: Int`, `totalTracks: Int`." (Owner: PM/Backend)
Verification (Cycle 2): TASKS.md TASK-03 acceptance criteria now reads "`PlayerState` contains `isPlaying: Boolean`, `currentTrack: TrackInfo?`, `progressSeconds: Int`, `queueIndex: Int`, `totalTracks: Int`." Confirmed fixed.

---

### BUG-06 — READ_PHONE_STATE permission is a dangerous permission declared without a runtime request — TelephonyManager call will silently return UNKNOWN on API 29+
Status: FIXED
Severity: P2
File: `app/src/main/AndroidManifest.xml:5`
`app/src/main/kotlin/com/wingman/launcher/viewmodel/HomeViewModel.kt:130-143`
Description: `READ_PHONE_STATE` is a dangerous permission (protection level: dangerous) that must be explicitly granted by the user at runtime on Android 6+ (API 23+). The manifest declares it but there is no runtime permission request anywhere in the codebase. On Android 10+ (API 29), `TelephonyManager.getDataNetworkType()` always returns `NETWORK_TYPE_UNKNOWN` when the permission is not granted, causing `readSignalBars()` to always return 0. The `SecurityException` catch on line 141 will absorb the exception silently but the `when` branch for `null` / unknown type also returns 0, so signal bars always display as empty. The code handles the `SecurityException` but not the case where the permission is simply not granted (returns UNKNOWN without throwing).
Suggested Fix: Either (a) remove the TelephonyManager signal bar logic and hard-code `signalBars = 0` or display a fixed "NO SIGNAL" indicator (acceptable for a demo launcher), or (b) add runtime permission request in `MainActivity.onCreate()` using `ActivityResultContracts.RequestPermission`. For a launcher app, option (a) is safer and simpler.
Verification (Cycle 2): `READ_PHONE_STATE` is absent from `AndroidManifest.xml`. `HomeViewModel.readSystemStatus()` hard-codes `signalBars = 0` with an inline comment referencing BUG-06. No `TelephonyManager` usage anywhere. Confirmed fixed.

---

### BUG-07 — SettingsScreen UP/DOWN key input on the EFFECT INTENSITY row adjusts intensity but does NOT move focus between rows — focus is permanently stuck on row 0 when intensity is selected
Status: FIXED
Severity: P2
File: `app/src/main/kotlin/com/wingman/launcher/ui/screen/SettingsScreen.kt:58-92`
Description: The `rememberInputHandler` lambda in `SettingsScreen` checks `if (selectedIndex == 0)` to decide whether Up/Down adjusts intensity vs. moves selection. When `selectedIndex == 0` (EFFECT INTENSITY row is selected), pressing Up increments intensity and pressing Down decrements intensity. There is no key to move from row 0 down to row 1 (THEME VARIANT) via D-pad/keyboard while row 0 is selected — the only way to reach row 1 is via touch. The logic for `InputEvent.Down` when `selectedIndex == 0` decrements intensity instead of moving to the next row. This breaks keyboard-only navigation.
Suggested Fix: Separate the concepts of "focused row" and "intensity adjustment." For example: use Left/Right (or a dedicated key) to adjust intensity value, and Up/Down to move between rows. Alternatively, require the user to press Enter first to "enter edit mode" on the intensity row, and then Up/Down adjust the value. A simpler fix: when Down is pressed on row 0 and the value would go below 0, treat it as "move to next row" instead.
Verification (Cycle 2): `rememberInputHandler` in `SettingsScreen` now unconditionally maps Up/Down to row navigation. Intensity adjustment is handled by a separate `onKeyEvent` modifier intercepting Left/Right keys only when `selectedIndex == 0`. A hint text line "LEFT/RIGHT = adjust | UP/DOWN = navigate | ENTER = toggle" is displayed. Keyboard-only navigation between rows is now functional. Confirmed fixed.

---

### BUG-08 — TutorialsScreen does not wire scroll input to the verticalScroll state — scroll wheel and D-pad do not scroll content
Status: FIXED
Severity: P2
File: `app/src/main/kotlin/com/wingman/launcher/ui/screen/TutorialsScreen.kt:83-87`
Description: `TutorialsScreen` uses `rememberScrollState()` and wraps the content `Column` in `Modifier.verticalScroll(scrollState)`. However, the `rememberInputHandler` lambda only handles `InputEvent.Back` — `InputEvent.Up` and `InputEvent.Down` fall through to the `else -> {}` branch and do nothing. Scroll wheel and D-pad input will therefore not scroll the tutorial text. TASK-11 acceptance criteria for TutorialsScreen state "Scroll input scrolls the text." Touch-drag scrolling still works via the Compose scroll state, but the keyboard/wheel path is broken.
Suggested Fix: Add coroutine-based scroll handling in the `rememberInputHandler` lambda:
```kotlin
is InputEvent.Up   -> coroutineScope.launch { scrollState.animateScrollBy(-80f) }
is InputEvent.Down -> coroutineScope.launch { scrollState.animateScrollBy(80f) }
```
Use `rememberCoroutineScope()` and pass `scrollState` to the handler.
Verification (Cycle 2): `TutorialsScreen` now uses `rememberCoroutineScope()` and the `rememberInputHandler` lambda handles `InputEvent.Up` with `coroutineScope.launch { scrollState.animateScrollBy(-80f) }` and `InputEvent.Down` with `coroutineScope.launch { scrollState.animateScrollBy(80f) }`. Confirmed fixed.

---

### BUG-09 — Folder icon in PixelIcon.kt hardcodes the background color #0A0F0A — breaks HIGH_CONTRAST theme
Status: FIXED
Severity: P2
File: `app/src/main/kotlin/com/wingman/launcher/ui/components/PixelIcon.kt:69`
`app/src/main/kotlin/com/wingman/launcher/ui/components/PixelIcon.kt:89`
`app/src/main/kotlin/com/wingman/launcher/ui/components/PixelIcon.kt:112`
Description: Three `drawRect` calls inside `drawFolder`, `drawBook`, and `drawGear` use hardcoded `Color(0xFF0A0F0A)` to "punch out" background-colored cutouts. This is the STANDARD theme background color. In HIGH_CONTRAST mode the background is `Color(0xFF000000)`, making the cutout rendering visually incorrect (a visible green-tinted punch on a black background). TASK-08 acceptance criteria state "All components use only WingmanTheme colors and typography — no hardcoded color values in component files." These three instances violate that rule.
Suggested Fix: `PixelIcon` already receives `tint: Color` — add a `backgroundColor: Color = Color.Unspecified` parameter (defaulting to the current theme background via `LocalWingmanColors.current.background`), or use `Color.Transparent` for the cutouts instead of the hardcoded background color (transparent drawing over a colored background achieves the same visual effect without coupling to a specific color value).
Verification (Cycle 2): All three cutout drawing calls in `drawFolder` (line 70), `drawBook` (line 90–91), and `drawGear` (line 112) now use `Color.Transparent`. No `Color(0xFF0A0F0A)` hardcode remains in `PixelIcon.kt`. Confirmed fixed.

---

### BUG-10 — GlowBox uses setShadowLayer() which has no effect on hardware-accelerated Canvas in Compose — glow effect is silently invisible
Status: FIXED
Severity: P2
File: `app/src/main/kotlin/com/wingman/launcher/ui/components/GlowBox.kt:46`
Description: `setShadowLayer()` on a `Paint.asFrameworkPaint()` only works when software rendering is enabled. Compose draws with hardware acceleration by default; `setShadowLayer()` is documented as having no effect on hardware-accelerated canvas paths (it is a software-only API). The code comment says "API 26+ compatible (not Modifier.blur())" but the actual glow effect will be silently absent on any hardware-accelerated device running Android 26+. The FRONTEND_STATUS.md note "GlowBox uses setShadowLayer() on Paint.asFrameworkPaint() — API 26+ compatible" is technically correct about API level but misleading about actual rendering behavior.
Suggested Fix: To achieve the glow on hardware-accelerated canvas, use one of: (a) `Modifier.graphicsLayer { renderEffect = BlurEffect(...) }` (API 31+ only, but acceptable given minSdk=26 with a version check), or (b) draw multiple layered semi-transparent `drawRect` calls with decreasing alpha to simulate a glow spread, which works on all API levels and hardware acceleration states, or (c) wrap the content in a `graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }` and use `BlurMaskFilter` with `drawWithContent` to force software rendering on the layer.
Verification (Cycle 2): `GlowBox` now uses `Modifier.drawBehind` with four layered `drawRoundRect` calls at decreasing alpha values (0.15, 0.10, 0.06, 0.03) to simulate a glow bloom. No `setShadowLayer()` or `Paint` usage remains. The approach works on all API levels with hardware acceleration. Confirmed fixed.

---

### BUG-11 — MusicScreen and SettingsScreen inject HomeViewModel unnecessarily — doubles the ViewModel creation cost and creates a second polling loop
Status: FIXED
Severity: P2
File: `app/src/main/kotlin/com/wingman/launcher/ui/screen/MusicScreen.kt:46-47`
`app/src/main/kotlin/com/wingman/launcher/ui/screen/ScansScreen.kt:47-48`
`app/src/main/kotlin/com/wingman/launcher/ui/screen/OrganizerScreen.kt:47-48`
`app/src/main/kotlin/com/wingman/launcher/ui/screen/SettingsScreen.kt:47-48`
Description: All four sub-screens inject `HomeViewModel` via `hiltViewModel()` to obtain `systemStatus` for the `TopBar`. While Hilt scopes ViewModels to the Activity by default so the same instance is reused, this still tightly couples every sub-screen to `HomeViewModel`. More critically, `HomeViewModel.init` immediately calls `startPolling()`, which launches a `while(true)` loop in `viewModelScope`. Since the sub-screens get the same scoped instance this is technically benign, but `TopBar` only needs a `SystemStatus` value — not a direct `HomeViewModel` reference. INTERFACES.md §3.2 explicitly states: "`TopBar` composable accepts `SystemStatus` as a plain parameter — no ViewModel reference inside `TopBar`." The sub-screens themselves are also reading from `HomeViewModel` directly rather than passing `SystemStatus` down as a parameter, which is a softer contract violation. Additionally, per TASK-11 AC, the `TopBar` on sub-screens should show "live `SystemStatus`" — but the sub-screens read from `HomeViewModel` rather than having `systemStatus` passed from `WingmanApp`.
Suggested Fix: Pass `systemStatus: SystemStatus` as a parameter to each sub-screen composable from `WingmanApp` (where `HomeViewModel` is already available via the nav root), or at minimum document this as an intentional deviation. The current approach works but violates the layering contract in INTERFACES.md §3.2.
Verification (Cycle 2): All four sub-screens (`ScansScreen`, `OrganizerScreen`, `TutorialsScreen`, `MusicScreen`, `SettingsScreen`) now accept `systemStatus: SystemStatus` as a parameter and pass it directly to `TopBar(status = systemStatus)`. `WingmanApp` collects `homeViewModel.uiState` at the root and passes `systemStatus = systemStatus` to every sub-screen call site. No sub-screen injects `HomeViewModel` for this purpose. Confirmed fixed.

---

### BUG-12 — BootScreen hardcodes `intensity = 0.5f` for ScanlineOverlay — ignores live SettingsState
Status: FIXED
Severity: P3
File: `app/src/main/kotlin/com/wingman/launcher/ui/components/BootScreen.kt:106`
Description: `BootScreen` renders `ScanlineOverlay(intensity = 0.5f)` with a hardcoded intensity. The BootScreen composable signature is `fun BootScreen(onBootComplete: () -> Unit)` per INTERFACES.md §7 — it takes no `effectIntensity` parameter. This means the scanline overlay on the boot screen always runs at 50% regardless of the user's saved `SettingsState`. If the user had previously set intensity to 0.0f (effects off), the boot screen still shows scanlines. This is a minor aesthetic inconsistency.
Suggested Fix: Either (a) add `effectIntensity: Float = 0.5f` to `BootScreen`'s parameter list and pass it from `WingmanApp`, or (b) accept the hardcoded 0.5f as intentional boot-screen behavior and document it. If option (b), add a KDoc note explaining the design decision.
Verification (Cycle 2): `BootScreen` signature is now `fun BootScreen(onBootComplete: () -> Unit, effectIntensity: Float = 0.5f)`. `ScanlineOverlay(intensity = effectIntensity)` is used. `WingmanApp` passes `effectIntensity = effectIntensity` to `BootScreen`. Confirmed fixed. Note: INTERFACES.md §7 still specifies the old `fun BootScreen(onBootComplete: () -> Unit)` signature — see BUG-23 for the resulting contract gap.

---

### BUG-13 — lifecycle-runtime-ktx version in libs.versions.toml reuses `lifecycleViewmodelCompose` — potentially wrong artifact version
Status: FIXED
Severity: P2
File: `gradle/libs.versions.toml:40`
Description: The `lifecycle-runtime-ktx` library entry reuses `version.ref = "lifecycleViewmodelCompose"` (which resolves to `2.8.7`). While these are likely compatible at the same version, they are separate artifacts (`lifecycle-viewmodel-compose` and `lifecycle-runtime-ktx`) with independent release schedules. Using a shared version ref can cause a silent version mismatch if one artifact is updated independently. More importantly, `lifecycle-runtime-ktx` is listed in `libs.versions.toml` but it is NOT imported anywhere in `app/build.gradle.kts` — the library entry is dead weight, but `implementation(libs.androidx.lifecycle.runtime.ktx)` is missing from the dependencies block. If any code needs `lifecycleScope` or `repeatOnLifecycle` from `lifecycle-runtime-ktx`, the dependency would be missing.
Suggested Fix: Add a separate `lifecycleRuntimeKtx` version entry in the `[versions]` section, or verify that Compose BOM already transitively provides the correct `lifecycle-runtime-ktx` version. Add `implementation(libs.androidx.lifecycle.runtime.ktx)` to `app/build.gradle.kts` if `lifecycleScope` usage is ever needed.
Verification (Cycle 2): `libs.versions.toml` now has a dedicated `lifecycleRuntimeKtx = "2.8.7"` entry in `[versions]`, and `androidx-lifecycle-runtime-ktx` uses `version.ref = "lifecycleRuntimeKtx"`. `app/build.gradle.kts` includes `implementation(libs.androidx.lifecycle.runtime.ktx)`. Confirmed fixed.

---

### BUG-14 — TASK-03 acceptance criteria conflict with INTERFACES.md on `PlayerState.progress` field
Status: FIXED
Severity: P3
File: `c:\Projects\Wingman\TASKS.md:82`
Description: TASKS.md TASK-03 acceptance criteria states "`PlayerState` contains... `progress: Float`." INTERFACES.md §3.7 defines the field as `progressSeconds: Int`. The implementation correctly uses `progressSeconds: Int`. This is already tracked in BUG-05 — listed here again as a separate P3 documentation issue for clarity and because TASKS.md is used as a working reference by the backend developer.
Suggested Fix: Update TASKS.md TASK-03 to match INTERFACES.md §3.7. (See BUG-05.)
Verification (Cycle 2): Fixed as part of BUG-05. TASKS.md TASK-03 acceptance criteria now lists the correct fields. Confirmed fixed.

---

### BUG-15 — `enableEdgeToEdge()` in MainActivity conflicts with fullscreen window flags — system bar insets not handled
Status: FIXED
Severity: P2
File: `app/src/main/kotlin/com/wingman/launcher/MainActivity.kt:25`
`app/src/main/res/values/themes.xml:4`
Description: `MainActivity.onCreate()` calls `enableEdgeToEdge()`, which sets the window to draw behind system bars and expects the app to handle insets via `WindowInsetsCompat`. Simultaneously, `themes.xml` uses `android:Theme.Material.NoTitleBar.Fullscreen` with `android:windowFullscreen="true"` and `android:statusBarColor`. These two approaches conflict. `enableEdgeToEdge()` removes status bar and navigation bar colors set by the theme and expects the app to handle padding — but there is no `WindowInsets` handling anywhere in WingmanApp.kt or HomeScreen.kt. On API 29+, the status bar area may render system decorations over the `TopBar` composable, or conversely, the app content may be clipped or padded incorrectly. Since BRIEF.md requires a fullscreen launcher, `enableEdgeToEdge()` is likely unnecessary — the theme already achieves fullscreen behavior.
Suggested Fix: Remove `enableEdgeToEdge()` from `MainActivity.onCreate()` if the intent is truly fullscreen (relying on the XML theme), OR add `Modifier.systemBarsPadding()` / `Modifier.windowInsetsPadding(WindowInsets.systemBars)` to the root Box in `WingmanApp` to handle insets correctly when edge-to-edge is active.
Verification (Cycle 2): `enableEdgeToEdge()` is absent from `MainActivity`. Instead, `MainActivity.onCreate()` uses `WindowCompat.setDecorFitsSystemWindows(window, false)` followed by `WindowInsetsControllerCompat(window, window.decorView)` with `controller.hide(WindowInsetsCompat.Type.systemBars())` and `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`. This is the correct programmatic fullscreen approach. Confirmed fixed.

---

### BUG-16 — `android:Theme.Material.NoTitleBar.Fullscreen` is a deprecated theme parent — use WindowManager API or AppCompat equivalent
Status: FIXED
Severity: P3
File: `app/src/main/res/values/themes.xml:4`
Description: The base theme `android:Theme.Material.NoTitleBar.Fullscreen` is deprecated in modern Android development. For a Compose-based app, a windowless `Theme.AppCompat.NoActionBar` base (or no XML theme at all with programmatic window control) is preferred. The deprecated theme may also interfere with the Compose window insets behavior introduced in API 30+.
Suggested Fix: Change the parent to `Theme.AppCompat.DayNight.NoActionBar` (or `android:Theme.Material.NoTitleBar` without `Fullscreen`) and handle fullscreen programmatically via `WindowInsetsController.hide(WindowInsetsCompat.Type.systemBars())` in `MainActivity.onCreate()`.
Verification (Cycle 2): `themes.xml` parent is now `Theme.AppCompat.DayNight.NoActionBar`. The fullscreen flags (`windowFullscreen`, `windowNoTitle`, etc.) that relied on the deprecated theme are no longer needed; the programmatic `WindowInsetsControllerCompat` approach in `MainActivity` handles fullscreen (BUG-15). Confirmed fixed.

---

### BUG-17 — Key-repeat acceleration never resets for non-direction keys — held Enter/Back incorrectly maintains UP/DOWN held state
Status: FIXED
Severity: P3
File: `app/src/main/kotlin/com/wingman/launcher/ui/input/InputHandler.kt:113-122`
Description: In `rememberInputHandler`, the `heldKey` state is set to `HeldKey.UP` or `HeldKey.DOWN` on Up/Down events, and to `HeldKey.NONE` on `Select` or `Back`. However, the `handleWingmanInput` modifier does not emit `Select` or `Back` events for held keys (only on `KeyDown`), and the `LaunchedEffect(heldKey)` loop for acceleration continues running until `heldKey` changes. If a user presses Down (acceleration starts) and then presses Enter without releasing Down, the `heldKey` transitions to `NONE`, which is correct. However there is no mechanism for the acceleration coroutine to know the physical key was released — Compose `onKeyEvent` only fires `KeyDown` (not `KeyUp`) for the acceleration use case. If the user holds Down then releases without pressing another key, `heldKey` remains `HeldKey.DOWN` and the acceleration loop continues firing `InputEvent.Down` indefinitely after key release. There is no `KeyUp` handler that resets `heldKey` to `HeldKey.NONE`.
Suggested Fix: Add a `KeyEventType.KeyUp` branch in `handleWingmanInput`:
```kotlin
keyEvent.type == KeyEventType.KeyUp &&
    (keyEvent.key == Key.DirectionUp || keyEvent.key == Key.W ||
     keyEvent.key == Key.DirectionDown || keyEvent.key == Key.S) -> {
    onEvent(InputEvent.Back) // or emit a new InputEvent.Released
    true
}
```
Or add a dedicated `InputEvent.Released` variant. The `rememberInputHandler` should set `heldKey = HeldKey.NONE` on any `KeyUp` event.
Verification (Cycle 2): `InputHandler.kt` now defines `InputHandlerState` data class holding `onEvent` and `onDirectionKeyUp` callbacks. `handleWingmanInput` has a `KeyEventType.KeyUp` branch that calls `onDirectionKeyUp()` when a direction key is released (does not consume the event). `rememberInputHandler` returns an `InputHandlerState` with `onDirectionKeyUp = { heldKey = HeldKey.NONE }`. A convenience `Modifier.handleWingmanInput(InputHandlerState)` overload keeps call sites unchanged. The acceleration coroutine correctly stops on key release. Confirmed fixed.

---

### BUG-18 — OrganizerScreen selection index uses a flat combined index across notes and tasks — when list sizes change, selected index may point to wrong item
Status: FIXED
Severity: P2
File: `app/src/main/kotlin/com/wingman/launcher/ui/screen/OrganizerScreen.kt:55-56`
`app/src/main/kotlin/com/wingman/launcher/ui/screen/OrganizerScreen.kt:61-62`
Description: `OrganizerScreen` maintains `selectedIndex` as a flat index across `[notes... tasks...]`. The `totalItems` calculation is `uiState.notes.size + uiState.tasks.size`. If a note is added or deleted while the screen is open, `selectedIndex` may point to a different item than the one the user had visually selected (a note index may now point to a task after insertion). Additionally, `coerceAtMost((totalItems - 1).coerceAtLeast(0))` handles empty lists correctly, but when `totalItems` shrinks (deletion) `selectedIndex` is only clamped on the next Up/Down press — a stale `selectedIndex` persists until navigation. This is the same issue in `ScansScreen` on lines 66.
Suggested Fix: Use `LaunchedEffect(uiState.notes.size, uiState.tasks.size)` to clamp `selectedIndex` whenever the list sizes change:
```kotlin
LaunchedEffect(uiState.notes.size, uiState.tasks.size) {
    val total = uiState.notes.size + uiState.tasks.size
    if (total > 0) selectedIndex = selectedIndex.coerceAtMost(total - 1)
    else selectedIndex = 0
}
```
Verification (Cycle 2): `OrganizerScreen` has `LaunchedEffect(uiState.notes.size, uiState.tasks.size)` that clamps `selectedIndex` immediately when list sizes change. `ScansScreen` has `LaunchedEffect(scans.size)` that clamps `selectedIndex` when the scan list changes. Both match the suggested fix pattern. Confirmed fixed.

---

### BUG-19 — MusicRepositoryImpl.isPlaying local flag is not thread-safe — concurrent suspend calls may produce inconsistent state
Status: FIXED
Severity: P3
File: `app/src/main/kotlin/com/wingman/launcher/data/repository/MusicRepository.kt:54-55`
Description: `MusicRepositoryImpl` uses a plain `var isPlaying: Boolean = false` and `var currentIndex: Int = 0` as mutable state that is read and written from `suspend fun` calls dispatched on `viewModelScope`. If `play()` and `next()` are called concurrently (unlikely in practice with a single-user UI, but possible), the `currentIndex` and `isPlaying` mutations are not synchronized. Kotlin `var` on a JVM is not atomic. Since these are called from `viewModelScope.launch` without a `Mutex`, a race condition is theoretically possible.
Suggested Fix: Wrap the mutable state in `@GuardedBy` or use `Mutex`:
```kotlin
private val mutex = Mutex()
override suspend fun play(trackId: String) = mutex.withLock { /* ... */ }
```
Or use `StateFlow` / `AtomicInteger` for `currentIndex`.
Verification (Cycle 2): `MusicRepositoryImpl` now imports `kotlinx.coroutines.sync.Mutex` and `withLock`. A `private val mutex = Mutex()` is declared. All mutations of `isPlaying` and `currentIndex` in `play()`, `pause()`, `resume()`, `next()`, and `previous()` are wrapped in `mutex.withLock { }`. `_playbackEvents.emit()` calls are correctly placed outside the lock to avoid suspending while holding it. Confirmed fixed.

---

### BUG-20 — ScanlineOverlay does not use `modifier` parameter — modifier is always replaced by `fillMaxSize()`
Status: OPEN
Severity: P3
File: `app/src/main/kotlin/com\wingman\launcher\ui\components\ScanlineOverlay.kt:28`
Description: `ScanlineOverlay` accepts `modifier: Modifier = Modifier` as a parameter but then uses `Canvas(modifier = modifier.fillMaxSize())`. This means any modifier passed by the caller is honored (because `fillMaxSize()` is chained after it), so the bug is minor. However, the `modifier` parameter is not applied first — callers who intend to constrain the overlay size via a passed modifier will find it overridden by `fillMaxSize()`. The same issue exists in `NoiseOverlay.kt`. This is a P3 since in practice all call sites pass `Modifier` (default), but it is a usability trap for future usage.
Suggested Fix: Either change to `modifier.fillMaxSize()` (which already chains correctly) or document that `ScanlineOverlay` always fills its parent and the modifier is for positioning only.
Verification (Cycle 2): No fix applied in P2 Fix Round 1 (this was a P3 not included in the fix batch). Status remains OPEN. No regression introduced.

---

### BUG-21 — LAUNCHER intent-filter category is present alongside HOME and DEFAULT — may cause app to appear in the app drawer
Status: FIXED
Severity: P2
File: `app/src/main/AndroidManifest.xml:29`
Description: The `AndroidManifest.xml` includes `android.intent.category.LAUNCHER` alongside `HOME` and `DEFAULT`. Including `LAUNCHER` causes the launcher app itself to appear as an icon in whatever is currently set as the home screen app drawer. For a dedicated launcher app this is typically undesirable — the launcher app should only appear in the HOME chooser, not in the app grid. ARCHITECTURE.md §3 specifies only `HOME` and `DEFAULT` categories. The `LAUNCHER` category was added extra.
Suggested Fix: Remove the `android.intent.category.LAUNCHER` category from the intent-filter. Keep only `MAIN`, `HOME`, and `DEFAULT` as specified in ARCHITECTURE.md §3.
Verification (Cycle 2): `AndroidManifest.xml` intent-filter contains only `android.intent.action.MAIN`, `android.intent.category.HOME`, and `android.intent.category.DEFAULT`. The `LAUNCHER` category is absent. Confirmed fixed. (Backend notes state this was already correct before the fix round — no change was required.)

---

### BUG-22 — `WingmanApp` navigation lambda allows navigating to `Boot` only at startup — but the guard only blocks explicit Boot navigation; boot screen back is also blocked by BackHandler being disabled at Boot
Status: FIXED
Severity: P3
File: `app/src/main/kotlin/com/wingman/launcher/ui/WingmanApp.kt:80-87`
`app/src/main/kotlin/com/wingman/launcher/ui/WingmanApp.kt:73-78`
Description: The `navigate` lambda correctly blocks `AppDestination.Boot` as a target. The `BackHandler` is also disabled at `Boot` (enabled only when NOT Home and NOT Boot). This correctly prevents leaving the boot screen via Back. However, if `BootScreen.onBootComplete` is never called (e.g., due to a coroutine cancellation or exception in the `LaunchedEffect`), the app is permanently stuck on the boot screen with no user escape. There is no timeout or error recovery in `BootScreen`.
Suggested Fix: Add a timeout to `BootScreen.LaunchedEffect`:
```kotlin
withTimeout(WingmanConstants.BOOT_DURATION_MS + 2000L) {
    // ... typing effect ...
}
onBootComplete()
```
Or add a tap-to-skip gesture on the boot screen.
Verification (Cycle 2): `WingmanApp` now has a `LaunchedEffect(Unit)` that wraps a spin-wait loop in `withTimeout(WingmanConstants.BOOT_DURATION_MS + 2000L)`. A `catch (_: kotlinx.coroutines.TimeoutCancellationException)` block force-advances `currentDestination` to `AppDestination.Home` if boot has not completed within the timeout window. This prevents the app from being permanently stuck on the boot screen. Confirmed fixed.

---

### BUG-23 — INTERFACES.md §7 BootScreen contract is stale — fix for BUG-12 added `effectIntensity` parameter but INTERFACES.md was not updated
Status: OPEN
Severity: P3
File: `c:\Projects\Wingman\INTERFACES.md` §7
`app/src/main/kotlin/com/wingman/launcher/ui/components/BootScreen.kt:46-49`
Description: The fix for BUG-12 correctly added `effectIntensity: Float = 0.5f` to `BootScreen`'s parameter list and updated `WingmanApp` to pass the live value. However, INTERFACES.md §7 still specifies:
```kotlin
@Composable
fun BootScreen(
    onBootComplete: () -> Unit
)
```
The actual implemented signature is:
```kotlin
fun BootScreen(
    onBootComplete: () -> Unit,
    effectIntensity: Float = 0.5f
)
```
Any developer consulting INTERFACES.md as the authoritative contract will see a signature that no longer matches the implementation. While the default value means callers not passing `effectIntensity` will still compile, the contract document is out of date. This is the same category of documentation drift that was BUG-05/BUG-14 in Cycle 1.
Suggested Fix: Update INTERFACES.md §7 BootScreen entry to:
```kotlin
@Composable
fun BootScreen(
    onBootComplete: () -> Unit,
    effectIntensity: Float = 0.5f
)
```
(Owner: whoever maintains INTERFACES.md — typically the tech lead or PM.)

---

## REVIEW LOG

### Review Cycle 1 — 2026-05-04

Tasks reviewed: TASK-01, TASK-02, TASK-03, TASK-04, TASK-05, TASK-06, TASK-07, TASK-08, TASK-09, TASK-10, TASK-11, TASK-12

**Files reviewed:**
- AndroidManifest.xml
- app/build.gradle.kts, gradle/libs.versions.toml
- All domain models (MenuItem, Note, Task, ScanFile, TrackInfo, PlayerState, SettingsState, SystemStatus, UiStates)
- AppDestination.kt, WingmanConstants.kt
- All 4 repository interfaces + implementations (OrganizerRepository, ScansRepository, MusicRepository, SettingsRepository)
- All DAOs and Room entities (NoteDao, TaskDao, NoteEntity, TaskEntity, WingmanDatabase)
- FakeScansDataSource.kt, FakeMusicDataSource.kt, PrefsDataSource.kt
- All 5 ViewModels (HomeViewModel, ScansViewModel, OrganizerViewModel, MusicViewModel, SettingsViewModel)
- WingmanApplication.kt, MainActivity.kt, WingmanApp.kt
- All UI components (TopBar, MenuRow, PixelIcon, ScanlineOverlay, GlowBox, NoiseOverlay, BootScreen, StatusIndicators, WingmanText)
- All 6 screens (HomeScreen, ScansScreen, OrganizerScreen, TutorialsScreen, MusicScreen, SettingsScreen)
- InputHandler.kt, AudioPlayer.kt, AudioModule.kt, AppModule.kt, Extensions.kt
- WingmanTheme.kt, Color.kt, Typography.kt

**What PASSED:**
- No Material Design components found anywhere in the codebase (WingmanText uses BasicText; no MaterialTheme, Scaffold, TopAppBar, etc.)
- No hardcoded secrets, API keys, or credentials
- No forced non-null assertions (!!) or unsafe casts
- AppDestination sealed class has all 7 required variants per INTERFACES.md §1
- All ViewModel StateFlow names match INTERFACES.md §5 exactly (uiState, playerState, settingsState)
- All domain model field names match INTERFACES.md §3 (including isCompleted vs isDone mapping)
- All repository interfaces match INTERFACES.md §6 exactly
- WingmanConstants has all 15 values from INTERFACES.md §8 with correct values
- Room entities never imported in ViewModel or UI files
- Hilt setup is complete (@HiltAndroidApp, @AndroidEntryPoint, @HiltViewModel, @Inject, @Singleton)
- KSP is correctly configured; KAPT not used (correct for current Kotlin/Hilt versions)
- BackHandler in WingmanApp correctly prevents exiting the launcher on Back from Home
- Boot → Home is the only Boot transition; navigate() lambda blocks navigating TO Boot
- AnimatedContent covers all 7 AppDestination branches (exhaustive when expression)
- No ripple effects on any interactive element (indication = null on all clickable calls)
- Press Start 2P font is loaded from R.font (not hardcoded)
- Flicker animation uses correct range (FLICKER_ALPHA_MIN=0.97f, FLICKER_ALPHA_MAX=1.0f, 4000ms period)
- BootScreen types 8 lines (>6 per TASK-08 AC), calls onBootComplete, ~3s total
- ScanlineOverlay max alpha capped at 0.35 (non-distracting per TASK-12 AC)
- ACCESSING MODULE... overlay shown for 400ms (MODULE_TRANSITION_OVERLAY_MS) per TASK-12 AC
- effectIntensity propagated from WingmanApp root to HomeScreen, ScansScreen, OrganizerScreen, TutorialsScreen, MusicScreen (SettingsScreen violation noted in BUG-04)
- FakeScansDataSource provides 10 entries (>8 required), FakeMusicDataSource provides 7 (>5 required)
- All 5 main menu items present: SCANS, ORGANIZER, TUTORIALS, MUSIC, SETTINGS
- Column (not LazyColumn) used for main 5-item menu in HomeScreen (per ARCHITECTURE.md §11)
- LazyColumn used for ScansScreen and OrganizerScreen (correct for longer lists)
- TutorialsScreen has >300 words of terminal-style content
- D-pad / keyboard / scroll wheel input handling is implemented via unified InputHandler
- Key-repeat acceleration implemented (300ms initial, 80ms fast interval) — key release not handled (BUG-17)
- Two-tap pattern on HomeScreen: first tap selects, second tap navigates

P1 count: 0
P2 count: 15 (BUG-01, BUG-02, BUG-03, BUG-04, BUG-05, BUG-06, BUG-07, BUG-08, BUG-09, BUG-10, BUG-11, BUG-13, BUG-15, BUG-18, BUG-21)
P3 count: 7 (BUG-12, BUG-14, BUG-16, BUG-17, BUG-19, BUG-20, BUG-22)

Overall status: PASS (no P1 crashes, build failures, or security vulnerabilities; P2s are behavioral and contract issues that must be fixed before production release)

---

### Review Cycle 2 — 2026-05-04
Tasks reviewed: all P2/P3 fixes from Round 1
Bugs re-verified: 22
Bugs confirmed fixed: 21
Bugs still open: 1 (BUG-20, P3 — not in fix scope for Round 1)
New bugs found: 1 (BUG-23, P3 — INTERFACES.md BootScreen contract stale after BUG-12 fix)
P1 count: 0
P2 count: 0
P3 count: 2 (BUG-20, BUG-23)
Overall status: PASS
