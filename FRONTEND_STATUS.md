# FRONTEND_STATUS.md — Wingman UI Layer

---

## T02 Color + Typography theme
Status: COMPLETE
Files written:
- app/src/main/java/com/wingman/launcher/ui/theme/Color.kt
- app/src/main/java/com/wingman/launcher/ui/theme/Type.kt
- app/src/main/java/com/wingman/launcher/ui/theme/Theme.kt
Notes: All hex values from ARCHITECTURE.md declared as named constants. WingmanTheme uses no Material. Press Start 2P loaded via R.font.press_start_2p (res/font/press_start_2p.xml font family XML). Font TTF must be placed at res/font/press_start_2p_ttf.ttf at build time.

---

## T08 Visual effects modifiers (scanlines, glow, grain)
Status: COMPLETE
Files written:
- app/src/main/java/com/wingman/launcher/ui/effects/Scanlines.kt
- app/src/main/java/com/wingman/launcher/ui/effects/Glow.kt
- app/src/main/java/com/wingman/launcher/ui/effects/Grain.kt
Notes: scanlines() uses drawWithContent + drawLine (no bitmap). terminalGlow() uses BlurMaskFilter via drawBehind + drawIntoCanvas. noiseGrain() uses Modifier.composed() for state, regenerates seed every 3 frames. All respect themeIntensity — 0.0 = no-op.

---

## T09 PixelIcon composable (Canvas-drawn icons)
Status: COMPLETE
Files written:
- app/src/main/java/com/wingman/launcher/ui/components/PixelIcon.kt
Notes: All 5 icons drawn via Canvas drawRect/drawLine/drawPath/drawOval/drawCircle. No vector drawables. contentDescription set for accessibility. Renders cleanly at 16dp and 24dp.

---

## T10 TopBar composable
Status: COMPLETE
Files written:
- app/src/main/java/com/wingman/launcher/ui/components/TopBar.kt
Notes: Height 32dp. Username left-aligned in WHITE_PIXEL/TerminalSmall. Signal bars (3 ascending pixel bars) and battery icon (pixel outline + fill) right-aligned, drawn via Canvas. 1dp GREEN_DIM separator line below. No Material TopAppBar.

---

## T11 MenuRow + ActiveRowHighlight composables
Status: COMPLETE
Files written:
- app/src/main/java/com/wingman/launcher/ui/components/MenuRow.kt
- app/src/main/java/com/wingman/launcher/ui/components/ActiveRowHighlight.kt
Notes: Row height 52dp (Fitts's law). Active: amber highlight bar with diagonal stripe texture (45° lines, 6px pitch, 10% white alpha), AMBER_PRIMARY text, ">" arrow right. Inactive: transparent bg, GREEN_PRIMARY text. Glow applied via terminalGlow modifier when glowEnabled. contentDescription on row for accessibility.

---

## T12 TerminalText composable
Status: COMPLETE
Files written:
- app/src/main/java/com/wingman/launcher/ui/components/TerminalText.kt
Notes: Uses BasicText (not Material Text). All text uppercased via text.uppercase(). Flicker: infiniteTransition animates alpha 0.88–1.0 over ~3.8s with Reverse repeat. No flicker when flickerEnabled=false.

---

## T13 WingmanApp root composable + MainActivity wiring
Status: COMPLETE
Files written:
- app/src/main/java/com/wingman/launcher/ui/WingmanApp.kt
- app/src/main/java/com/wingman/launcher/MainActivity.kt (updated from data layer stub)
Notes: WingmanApp uses when(navState) to render correct screen. sectionInputDispatcher pattern routes hardware key events from MainViewModel to active section ViewModel via DisposableEffect. Settings always read from mainViewModel.settings.value (not stale captured value). SettingsViewModel factory added to MainActivity. No Fragments.

---

## T14 BootScreen
Status: COMPLETE
Files written:
- app/src/main/java/com/wingman/launcher/ui/screens/BootScreen.kt
Notes: 3.5s total sequence. Lines typed character-by-character (40ms/char) via coroutine + mutableStateListOf. CRT flash uses Animatable(0f) → animateTo(0f, tween(150)). sfx_boot plays at sequence start. "OK" line in AMBER_PRIMARY. Scanlines + grain overlays applied.

---

## T15 HomeScreen (main menu)
Status: COMPLETE
Files written:
- app/src/main/java/com/wingman/launcher/ui/screens/HomeScreen.kt
Notes: TopBar + 5 MenuRows. animateIntAsState(80ms tween) for stepped snap feel. Touch swipe up/down via detectVerticalDragGestures with 80px threshold. Tap on row enters section. Scanlines + grain overlays applied. Sound played via MainViewModel (which owns scroll/click SFX).

---

## T16 ScansScreen
Status: COMPLETE
Files written:
- app/src/main/java/com/wingman/launcher/ui/screens/ScansScreen.kt
Notes: LazyColumn auto-scrolls to selected index. Filename left, status badge right (GREEN/AMBER/RED-AMBER). Timestamp + size in TerminalSmall/GREEN_MUTED below filename. "SCANS" in TopBar username position. Active row has amber tint bg.

---

## T17 OrganizerScreen
Status: COMPLETE
Files written:
- app/src/main/java/com/wingman/launcher/ui/screens/OrganizerScreen.kt
Notes: HIGH priority tasks show "!!" amber prefix. Done tasks have TextDecoration.LineThrough + GREEN_MUTED color. [X]/[ ] done indicator right-aligned. LazyColumn with auto-scroll. Enter toggles via OrganizerViewModel.

---

## T18 TutorialsScreen
Status: COMPLETE
Files written:
- app/src/main/java/com/wingman/launcher/ui/screens/TutorialsScreen.kt
Notes: Two-level: list → detail. List shows title + tags. Detail shows title (AMBER/TerminalLarge) + content lines. Scroll in detail view driven by TutorialsNavState.Detail.scrollOffset via Modifier.offset. Back from detail → list (consumed by TutorialsViewModel); Back from list → Home (not consumed → MainViewModel.navigateBack).

---

## T19 MusicScreen
Status: COMPLETE
Files written:
- app/src/main/java/com/wingman/launcher/ui/screens/MusicScreen.kt
Notes: Now-playing panel at top with track title, artist, pixel segmented progress bar (20 segments), play/pause pixel indicator. Track list below with active/selected highlighting. Progress animates via MusicViewModel tick (LaunchedEffect in ViewModel). formatTime helper for MM:SS display.

---

## T20 SettingsScreen
Status: COMPLETE
Files written:
- app/src/main/java/com/wingman/launcher/ui/screens/SettingsScreen.kt
Notes: 5 rows: Effects, Glow, Flicker, Sound, Intensity. Intensity row has pixel segmented bar (10 segments) + cycle value. DPAD navigates, Enter toggles/cycles. Changes persist via SettingsViewModel → SettingsRepository (DataStore). No Material Slider.

---

## T22 Flicker animation + grain randomization
Status: COMPLETE (implemented in T08/T12)
Files written:
- app/src/main/java/com/wingman/launcher/ui/effects/Grain.kt (grain regenerates every 3 frames)
- app/src/main/java/com/wingman/launcher/ui/components/TerminalText.kt (infiniteTransition flicker)
Notes: Grain uses frame counter mod 3 for seed regeneration — not every frame. TerminalText flicker uses infiniteTransition which pauses when flickerEnabled=false (conditional branch avoids creating the transition).

---

## DEPENDENCY NOTE
The following data-layer files were needed for UI compilation and were provided by the data layer agent (T03, T04, T05, T06, T07):
- All data/model/*.kt files
- All data/repository/*.kt files  
- data/source/DummyData.kt
- input/InputMapper.kt + InputHandler.kt
- sound/SoundEngine.kt
- viewmodel/MainViewModel.kt + all section ViewModels
- viewmodel/MainViewModelFactory.kt

MainViewModel was extended (not replaced) with: onSelectionChanged(), onSectionSelected(), sectionInputDispatcher field.

## PENDING (owner:data_layer)
- T21: Hold-to-accelerate input tuning — data layer task
- T23: Sound integration across all screens — data layer task (VMs already call SoundEngine)
- T24: Final QA pass — qa task

## FONT ASSET REQUIRED
Press Start 2P TTF must be placed at:
- app/src/main/res/font/press_start_2p_ttf.ttf  (for R.font access)
- app/src/main/assets/fonts/press_start_2p.ttf  (per ARCHITECTURE.md)
Download from: https://fonts.google.com/specimen/Press+Start+2P
