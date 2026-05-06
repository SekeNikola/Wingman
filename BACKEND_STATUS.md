# Backend Development Status

## TASK-01 — Project Setup & Launcher Registration
Status: DONE
Files written:
- `settings.gradle.kts`
- `build.gradle.kts` (root)
- `gradle/libs.versions.toml`
- `gradle/wrapper/gradle-wrapper.properties`
- `gradlew` / `gradlew.bat`
- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/res/values/strings.xml`
- `app/src/main/res/values/themes.xml`
- `app/src/main/res/mipmap-hdpi/ic_launcher.xml`
- `app/src/main/res/mipmap-hdpi/ic_launcher_round.xml`
- `app/src/main/res/raw/sfx_click.ogg` (placeholder)
- `app/src/main/res/raw/sfx_scroll.ogg` (placeholder)
- `app/proguard-rules.pro`
- `app/src/main/kotlin/com/wingman/launcher/WingmanApplication.kt`
- `app/src/main/kotlin/com/wingman/launcher/MainActivity.kt`
Notes:
- AndroidManifest includes HOME + DEFAULT + LAUNCHER intent filter on MainActivity with launchMode="singleTask" and taskAffinity="".
- @HiltAndroidApp on WingmanApplication; @AndroidEntryPoint on MainActivity.
- compileSdk=35, minSdk=26, targetSdk=35 per ARCHITECTURE.md spec.
- sfx_*.ogg files are stub placeholders; real sound files must be added separately.
- gradle-wrapper.jar is not included (binary file, generated/downloaded by Gradle on first sync).
- Press Start 2P font file (press_start_2p.ttf) must be manually downloaded and placed at app/src/main/res/font/press_start_2p.ttf (see https://fonts.google.com/specimen/Press+Start+2P).

---

## TASK-03 — Domain Models & Shared Data Classes
Status: DONE
Files written:
- `app/src/main/kotlin/com/wingman/launcher/data/model/MenuItem.kt`
- `app/src/main/kotlin/com/wingman/launcher/data/model/Note.kt`
- `app/src/main/kotlin/com/wingman/launcher/data/model/Task.kt`
- `app/src/main/kotlin/com/wingman/launcher/data/model/ScanFile.kt`
- `app/src/main/kotlin/com/wingman/launcher/data/model/TrackInfo.kt`
- `app/src/main/kotlin/com/wingman/launcher/data/model/PlayerState.kt`
- `app/src/main/kotlin/com/wingman/launcher/data/model/SettingsState.kt`
- `app/src/main/kotlin/com/wingman/launcher/data/model/SystemStatus.kt`
- `app/src/main/kotlin/com/wingman/launcher/data/model/UiStates.kt` (ScansUiState, OrganizerUiState, HomeUiState)
- `app/src/main/kotlin/com/wingman/launcher/ui/navigation/AppDestination.kt`
- `app/src/main/kotlin/com/wingman/launcher/util/WingmanConstants.kt`
Notes:
- All models are pure Kotlin data classes with no Android or Room imports.
- Companion object extension properties follow INTERFACES.md: SystemStatus.Empty, PlayerState.Idle, SettingsState.Default, OrganizerUiState.Empty.
- UiStates.kt consolidates ScansUiState, OrganizerUiState, HomeUiState to minimize file count.
- PixelIconType enum lives in MenuItem.kt per INTERFACES.md.
- ThemeVariant enum lives in SettingsState.kt per INTERFACES.md.

---

## TASK-04 — Room Database, DAOs & Entities
Status: DONE
Files written:
- `app/src/main/kotlin/com/wingman/launcher/data/db/entity/NoteEntity.kt`
- `app/src/main/kotlin/com/wingman/launcher/data/db/entity/TaskEntity.kt`
- `app/src/main/kotlin/com/wingman/launcher/data/db/dao/NoteDao.kt`
- `app/src/main/kotlin/com/wingman/launcher/data/db/dao/TaskDao.kt`
- `app/src/main/kotlin/com/wingman/launcher/data/db/WingmanDatabase.kt`
- `app/src/main/kotlin/com/wingman/launcher/di/AppModule.kt` (provides DB + DAOs)
Notes:
- WingmanDatabase version=1, exportSchema=true; schema exported to app/schemas/.
- NoteDao: getAllNotes() Flow, insert(), delete().
- TaskDao: getAllTasks() Flow, insert(), updateDone(id, done), delete().
- Room KSP processor wired in app/build.gradle.kts; schema export path configured via ksp arg.
- fallbackToDestructiveMigration() used for v1; proper migrations to be added before production.
- AppModule split into AppProvides (object, @Provides) and AppBinds (abstract class, @Binds) per Hilt best practices.

---

## TASK-05 — Repositories & Fake Data Sources
Status: DONE
Files written:
- `app/src/main/kotlin/com/wingman/launcher/data/source/FakeScansDataSource.kt`
- `app/src/main/kotlin/com/wingman/launcher/data/source/FakeMusicDataSource.kt`
- `app/src/main/kotlin/com/wingman/launcher/data/source/PrefsDataSource.kt`
- `app/src/main/kotlin/com/wingman/launcher/data/repository/OrganizerRepository.kt` (interface)
- `app/src/main/kotlin/com/wingman/launcher/data/repository/OrganizerRepositoryImpl.kt`
- `app/src/main/kotlin/com/wingman/launcher/data/repository/ScansRepository.kt` (interface + impl)
- `app/src/main/kotlin/com/wingman/launcher/data/repository/MusicRepository.kt` (interface + impl + PlaybackEvent)
- `app/src/main/kotlin/com/wingman/launcher/data/repository/SettingsRepository.kt` (interface + impl)
Notes:
- FakeScansDataSource provides 10 ScanFile entries with realistic filenames.
- FakeMusicDataSource provides 7 TrackInfo entries.
- PrefsDataSource uses DataStore<Preferences>; reads/writes effectIntensity (Float) and themeVariant (String enum name).
- MusicRepositoryImpl simulates playback via PlaybackEvent SharedFlow; no real MediaPlayer needed.
- All repositories bound as singletons via @Binds in AppModule.AppBinds.
- OrganizerRepository interface matches INTERFACES.md §6.1 exactly.
- ScansRepository, MusicRepository, SettingsRepository interfaces match INTERFACES.md §6.2–6.4 exactly.

---

## TASK-06 — ViewModels
Status: DONE
Files written:
- `app/src/main/kotlin/com/wingman/launcher/viewmodel/HomeViewModel.kt`
- `app/src/main/kotlin/com/wingman/launcher/viewmodel/ScansViewModel.kt`
- `app/src/main/kotlin/com/wingman/launcher/viewmodel/OrganizerViewModel.kt`
- `app/src/main/kotlin/com/wingman/launcher/viewmodel/MusicViewModel.kt`
- `app/src/main/kotlin/com/wingman/launcher/viewmodel/SettingsViewModel.kt`
- `app/src/main/kotlin/com/wingman/launcher/util/AudioPlayer.kt`
- `app/src/main/kotlin/com/wingman/launcher/util/Extensions.kt`
- `app/src/main/kotlin/com/wingman/launcher/di/AudioModule.kt`
Notes:
- All ViewModels are @HiltViewModel with @Inject constructor; no Activity context stored.
- HomeViewModel: injects @ApplicationContext; polls BatteryManager + TelephonyManager every 30s via coroutine loop; exposes HomeUiState StateFlow; onResume()/onPause() lifecycle hooks.
- ScansViewModel: injects ScansRepository; exposes ScansUiState (Loading/Success/Error).
- OrganizerViewModel: injects OrganizerRepository; combines notes+tasks Flow via combine(); all mutations are suspend delegations.
- MusicViewModel: injects MusicRepository; builds PlayerState from PlaybackEvent stream; play/pause/next/previous delegate to repo.
- SettingsViewModel: injects SettingsRepository; collects DataStore flow for persistence; updateEffectIntensity clamps to 0..1.
- AudioPlayer wraps SoundPool; gracefully handles missing res/raw files.
- ViewModel interfaces match INTERFACES.md §5.1–5.5 exactly.

---

## Final Summary

All six backend tasks (TASK-01, TASK-03, TASK-04, TASK-05, TASK-06) are complete.

### Project Structure Created
A full Android Gradle project at `c:\Projects\Wingman\` with:
- KTS Gradle build files (settings, root, app) using version catalog (libs.versions.toml)
- All required dependencies: Compose BOM, Room (KSP), Hilt, DataStore, Coroutines
- AndroidManifest with HOME intent filter for launcher registration
- Complete data layer: entities, DAOs, Room database, repositories, fake data sources
- All 5 ViewModels with repository injection and proper StateFlow exposure
- DI modules: AppModule (DB/repos) and AudioModule (audio player)

### Interface Compliance
All public APIs match INTERFACES.md contracts exactly:
- Domain models: MenuItem, Note, Task, ScanFile, TrackInfo, PlayerState, SettingsState, SystemStatus
- UiState sealed classes: ScansUiState, OrganizerUiState, HomeUiState
- ViewModel interfaces: HomeViewModel.uiState, ScansViewModel.uiState, OrganizerViewModel.uiState, MusicViewModel.playerState, SettingsViewModel.settingsState
- Repository interfaces: OrganizerRepository, ScansRepository, MusicRepository, SettingsRepository

### Deviations / Notes
1. `press_start_2p.ttf` font must be manually downloaded from Google Fonts and placed at `app/src/main/res/font/press_start_2p.ttf` before the project builds.
2. `gradle/wrapper/gradle-wrapper.jar` must be generated by running `gradle wrapper` or opening the project in Android Studio.
3. `sfx_click.ogg` and `sfx_scroll.ogg` in `res/raw/` are stub placeholders; real audio files must be added for sound effects to work.
4. `local.properties` contains a placeholder SDK path; developers must update it to their local Android SDK location.
5. OrganizerRepository interface is in its own file (not co-located with impl) for clean separation.
6. MusicRepository.kt contains both the interface AND the implementation AND PlaybackEvent in one file for cohesion; could be split if preferred.

## P2 Fix Round 1
Status: DONE
Bugs fixed: BUG-05, BUG-06, BUG-13, BUG-19, BUG-21
Files changed:
- `gradle/libs.versions.toml` (BUG-13: added dedicated `lifecycleRuntimeKtx = "2.8.7"` version entry; updated `androidx-lifecycle-runtime-ktx` library entry to use `version.ref = "lifecycleRuntimeKtx"`)
- `TASKS.md` (BUG-05: corrected TASK-03 acceptance criteria — replaced `progress: Float` with `progressSeconds: Int`, `queueIndex: Int`, `totalTracks: Int`)
- `app/src/main/kotlin/com/wingman/launcher/data/repository/MusicRepository.kt` (BUG-19: added `Mutex`, wrapped `isPlaying`/`currentIndex` mutations in `mutex.withLock` in `play()`, `pause()`, `resume()`, `next()`, `previous()`)
Notes:
- BUG-21 (remove LAUNCHER category): AndroidManifest.xml already contained only MAIN, HOME, and DEFAULT categories — no change required.
- BUG-06 (remove TelephonyManager / READ_PHONE_STATE): HomeViewModel already hard-codes `signalBars = 0` and the manifest already omits READ_PHONE_STATE — no change required. Both were fixed in a prior pass.
- BUG-13 (app/build.gradle.kts): `implementation(libs.androidx.lifecycle.runtime.ktx)` was already present — only the version catalog needed updating.
- BUG-19 mutex pattern: `_playbackEvents.emit()` calls are intentionally placed outside `mutex.withLock` to avoid suspending while holding the lock, preventing potential deadlocks.
