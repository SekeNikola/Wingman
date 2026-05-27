# BACKEND_STATUS.md — Wingman Data Layer Progress

---

## T01 — Project Scaffold + Manifest
Status: COMPLETE
Files written:
- `build.gradle.kts` (root)
- `settings.gradle.kts`
- `gradle/libs.versions.toml`
- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/proguard-rules.pro`
- `app/src/main/res/font/press_start_2p.xml` (placeholder — see note)
- `app/src/main/res/mipmap-mdpi/ic_launcher.xml` (adaptive icon placeholder)
- `generate_sfx_placeholders.sh` (run with ffmpeg to generate OGG stubs)

Notes:
- AndroidManifest.xml has full HOME + DEFAULT + LAUNCHER intent-filter. App will appear in launcher picker.
- Font: `press_start_2p.xml` is a placeholder. Download the .ttf from Google Fonts and place it at `app/src/main/res/font/press_start_2p.ttf` (same directory, same stem). Delete the .xml once the .ttf is in place. Gradle auto-generates `R.font.press_start_2p` from the .ttf.
- OGG sound files: Run `generate_sfx_placeholders.sh` (requires ffmpeg) to create silent placeholder files. Replace with real audio before release.
- Compose BOM 2024.09.02, Kotlin 2.0.0, AGP 8.5.2, DataStore 1.1.1, Coroutines 1.8.1.
- No Hilt. Manual DI via ViewModelFactory pattern.

---

## T03 — Core Data Models + NavigationState + InputEvent
Status: COMPLETE
Files written:
- `app/src/main/java/com/wingman/launcher/data/model/NavigationState.kt`
- `app/src/main/java/com/wingman/launcher/data/model/InputEvent.kt`
- `app/src/main/java/com/wingman/launcher/data/model/MenuSection.kt`
- `app/src/main/java/com/wingman/launcher/data/model/ScanFile.kt`
- `app/src/main/java/com/wingman/launcher/data/model/OrganizerTask.kt`
- `app/src/main/java/com/wingman/launcher/data/model/TutorialEntry.kt`
- `app/src/main/java/com/wingman/launcher/data/model/MusicTrack.kt`
- `app/src/main/java/com/wingman/launcher/data/model/SettingsState.kt`

Notes:
- All 7 NavigationState variants present: Boot, Home(selectedIndex), Scans, Organizer, Tutorials, Music, Settings.
- All 7 InputEvent variants: DpadUp, DpadDown, Enter, Back, ScrollUp, ScrollDown, RotaryScroll(delta).
- MenuSection has exactly 5 entries with `fromIndex()` helper on companion object.
- MusicPlayerState is co-located in MusicTrack.kt per INTERFACES.md spec.
- ScanStatus and Priority enums co-located in their model files per INTERFACES.md.

---

## T04 — Dummy Data Source + Repositories
Status: COMPLETE
Files written:
- `app/src/main/java/com/wingman/launcher/data/source/DummyData.kt`
- `app/src/main/java/com/wingman/launcher/data/repository/ScansRepository.kt`
- `app/src/main/java/com/wingman/launcher/data/repository/OrganizerRepository.kt`
- `app/src/main/java/com/wingman/launcher/data/repository/TutorialsRepository.kt`
- `app/src/main/java/com/wingman/launcher/data/repository/MusicRepository.kt`
- `app/src/main/java/com/wingman/launcher/data/repository/SettingsRepository.kt`

Notes:
- DummyData: 8 ScanFiles (terminal-style names), 6 OrganizerTasks (2 HIGH, 2 NORMAL, 1 LOW, 1 done), 3 TutorialEntries, 6 MusicTracks.
- Scan filenames: SYS_LOG_4471.DAT, NET_PROBE_09.BIN, BOOT_TRACE_001.LOG, KERNEL_PATCH_X7.HEX, COMMS_BUFFER_44.TMP, SEC_AUDIT_R22.RPT, MEM_DUMP_0xF800.BIN, PROC_TABLE_77.DAT.
- All repositories expose `Flow<List<T>>` via `MutableStateFlow`.
- OrganizerRepository supports `toggleTask(id)` mutation in-memory.
- SettingsRepository wraps DataStore Preferences — reads/writes persist across sessions.
- DataStore singleton scoped to applicationContext via `by preferencesDataStore("wingman_settings")`.

---

## T05 — InputMapper + InputHandler
Status: COMPLETE
Files written:
- `app/src/main/java/com/wingman/launcher/input/InputMapper.kt`
- `app/src/main/java/com/wingman/launcher/input/InputHandler.kt`

Notes:
- InputMapper is a pure `object` with a single `fun map(keyCode: Int, event: KeyEvent): InputEvent?` — returns null for unmapped keys. No state, no Compose refs.
- Mapped keys: DPAD_UP, DPAD_DOWN, ENTER, DPAD_CENTER, BUTTON_A, BACK, VOLUME_UP, VOLUME_DOWN.
- InputHandler uses its own `CoroutineScope(SupervisorJob() + Dispatchers.Main)` — not tied to ViewModel lifecycle.
- Hold-to-accelerate: 300ms initial delay → 80ms repeat → 40ms after 1s of continuous hold.
- Only directional events repeat; Enter and Back fire once per key-down.
- System key repeat suppressed via `event.repeatCount > 0` guard.
- `release()` cancels the hold coroutine — called from `MainActivity.onDestroy()`.

---

## T06 — SoundEngine
Status: COMPLETE
Files written:
- `app/src/main/java/com/wingman/launcher/sound/SoundEngine.kt`
- `generate_sfx_placeholders.sh` (run to create OGG stubs in `app/src/main/res/raw/`)

Notes:
- SoundEngine is a `singleton object`. SoundPool max 4 streams, AudioAttributes USAGE_GAME.
- SoundId enum: CLICK, SCROLL, BOOT, BACK — co-located in SoundEngine.kt per INTERFACES.md.
- `init(context)` idempotent — safe to call multiple times.
- `play(id, settingsState)` is a no-op if `settingsState.soundEnabled = false` or soundId = 0.
- Volume fixed at 0.6f per spec.
- `release()` cleans up SoundPool and resets initialized flag.
- OGG files at `app/src/main/res/raw/sfx_*.ogg` — must be created via the generator script before building. SoundPool load failure is non-fatal (soundId stays 0, play() is silently skipped).

---

## T07 — MainViewModel (Navigation State Machine)
Status: COMPLETE
Files written:
- `app/src/main/java/com/wingman/launcher/viewmodel/MainViewModel.kt`
- `app/src/main/java/com/wingman/launcher/viewmodel/MainViewModelFactory.kt`
- `app/src/main/java/com/wingman/launcher/viewmodel/SettingsViewModelFactory.kt`

Notes:
- `navState: StateFlow<NavigationState>` — initial value `NavigationState.Boot`.
- `settings: StateFlow<SettingsState>` — shared global state, observed by all screens.
- `onBootComplete()` → `Home(0)`.
- DPAD_UP/DOWN wraps: index 0 + UP = index 4; index 4 + DOWN = index 0.
- Enter from Home navigates to correct section by `MenuSection.fromIndex(selectedIndex)`.
- Back from Home is a no-op (launcher root — never exits).
- Back from any section → `Home(lastHomeIndex)` and plays sfx_back.
- `onSectionSelected(section)` and `onSelectionChanged(index)` for touch gesture integration.
- `sectionInputDispatcher: ((InputEvent) -> Boolean)?` — UI layer sets this via DisposableEffect to forward hardware DPAD events to the active section ViewModel. Returns true if consumed (Tutorials detail-Back intercept).
- No Android KeyEvent references anywhere in ViewModel.
- ManualViewModelFactory provided for both MainViewModel and SettingsViewModel (no Hilt).

---

## T21 — Hold-to-Accelerate Input Tuning
Status: COMPLETE
Files written:
- `app/src/main/java/com/wingman/launcher/input/InputHandler.kt` (covered in T05)

Notes:
- 300ms initial delay, 80ms normal repeat, 40ms fast repeat after 1s hold.
- All timing driven by coroutine delays — no Handler/postDelayed.
- Fully implemented in T05 delivery; T21 adds no new files.

---

## T23 — Sound Integration Across All Screens
Status: COMPLETE
Files written:
- `app/src/main/java/com/wingman/launcher/viewmodel/MainViewModel.kt`
- `app/src/main/java/com/wingman/launcher/viewmodel/ScansViewModel.kt`
- `app/src/main/java/com/wingman/launcher/viewmodel/OrganizerViewModel.kt`
- `app/src/main/java/com/wingman/launcher/viewmodel/TutorialsViewModel.kt`
- `app/src/main/java/com/wingman/launcher/viewmodel/MusicViewModel.kt`
- `app/src/main/java/com/wingman/launcher/viewmodel/SettingsViewModel.kt`

Notes:
- sfx_scroll: plays on every DPAD_UP/DOWN/ScrollUp/ScrollDown across all screens.
- sfx_click: plays on every Enter/confirm action.
- sfx_back: plays on every Back action (both MainViewModel.navigateBack() and TutorialsViewModel detail→list).
- All calls gated by `settingsState.soundEnabled` inside SoundEngine.play().
- Section ViewModels accept `settings: SettingsState` parameter on `onInputEvent()` — MainViewModel passes its own `settings.value` for self-contained calls.

---

## Additional Files (Supporting Data Layer)

### MainActivity.kt
Status: COMPLETE (data layer wiring — UI composable call deferred to UI developer)
- Wires InputHandler → MainViewModel.onInputEvent()
- Calls SoundEngine.init(applicationContext) in onCreate()
- Calls SoundEngine.release() and inputHandler.release() in onDestroy()
- Creates SettingsRepository once, shares it between MainViewModel and SettingsViewModel

### Section ViewModels (T16–T20 data layer portion)
Status: COMPLETE
Files written:
- `app/src/main/java/com/wingman/launcher/viewmodel/ScansViewModel.kt`
- `app/src/main/java/com/wingman/launcher/viewmodel/OrganizerViewModel.kt`
- `app/src/main/java/com/wingman/launcher/viewmodel/TutorialsViewModel.kt` (includes TutorialsNavState sealed class)
- `app/src/main/java/com/wingman/launcher/viewmodel/MusicViewModel.kt`
- `app/src/main/java/com/wingman/launcher/viewmodel/SettingsViewModel.kt`

Notes:
- All implement `onInputEvent(event, settings)` — no Compose/KeyEvent references.
- TutorialsViewModel implements two-level navigation (List/Detail) entirely in state.
- TutorialsViewModel.onInputEvent() returns Boolean: true = consumed, false = bubble to MainViewModel.
- MusicViewModel mock progress tick: 1s coroutine delay, auto-stops when track duration reached.
- SettingsViewModel cycles themeIntensity in 0.1 steps (0.0→1.0→0.0) on Enter.
- All repositories use MutableStateFlow — zero Room, zero database.

---

## Pending — UI Developer Actions Required

1. **Font**: Place `press_start_2p.ttf` in `app/src/main/res/font/press_start_2p.ttf`. Delete the placeholder `.xml`.
2. **Sound files**: Run `generate_sfx_placeholders.sh` (needs ffmpeg) OR place real OGG files at `app/src/main/res/raw/sfx_click.ogg`, `sfx_scroll.ogg`, `sfx_boot.ogg`, `sfx_back.ogg`.
3. **Screen composables**: T13–T20 screen files needed (BootScreen, HomeScreen, ScansScreen, OrganizerScreen, TutorialsScreen, MusicScreen, SettingsScreen).
4. **WingmanApp.kt**: Already written by UI developer — verify `onSectionSelected` and `onSelectionChanged` signatures match MainViewModel (they do).
5. **Launcher icon**: Replace `mipmap-mdpi/ic_launcher.xml` with real pixel art icon.
