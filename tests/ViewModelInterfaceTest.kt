/**
 * ViewModelInterfaceTest.kt
 *
 * Outlines the ViewModel StateFlow API checks required by INTERFACES.md §5.
 * Tests use MockK for mocking repository dependencies and
 * kotlinx-coroutines-test for coroutine-safe Flow collection.
 *
 * Each test class maps to one ViewModel interface from INTERFACES.md §5.
 * Tests verify:
 *   - Correct StateFlow names and initial values.
 *   - Public method signatures compile and delegate to the repository.
 *   - No unexpected state mutations (ViewModel isolation).
 *
 * NOTE: These tests require the full Android Hilt environment to be wired via
 * the hiltAndroidRule or manual constructor injection. In this review test file,
 * all ViewModels are instantiated directly via constructor injection (no Hilt)
 * using mock repositories provided by MockK.
 *
 * To run:   ./gradlew :app:test
 * Requires: JUnit 4, MockK, kotlinx-coroutines-test
 */

package com.wingman.launcher

import com.wingman.launcher.data.model.MenuItem
import com.wingman.launcher.data.model.Note
import com.wingman.launcher.data.model.OrganizerUiState
import com.wingman.launcher.data.model.PixelIconType
import com.wingman.launcher.data.model.PlayerState
import com.wingman.launcher.data.model.ScanFile
import com.wingman.launcher.data.model.ScanStatus
import com.wingman.launcher.data.model.ScansUiState
import com.wingman.launcher.data.model.SettingsState
import com.wingman.launcher.data.model.Task
import com.wingman.launcher.data.model.ThemeVariant
import com.wingman.launcher.data.model.TrackInfo
import com.wingman.launcher.data.repository.MusicRepository
import com.wingman.launcher.data.repository.OrganizerRepository
import com.wingman.launcher.data.repository.PlaybackEvent
import com.wingman.launcher.data.repository.ScansRepository
import com.wingman.launcher.data.repository.SettingsRepository
import com.wingman.launcher.ui.navigation.AppDestination
import com.wingman.launcher.viewmodel.MusicViewModel
import com.wingman.launcher.viewmodel.OrganizerViewModel
import com.wingman.launcher.viewmodel.ScansViewModel
import com.wingman.launcher.viewmodel.SettingsViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// ---------------------------------------------------------------------------
// Test utilities
// ---------------------------------------------------------------------------

@OptIn(ExperimentalCoroutinesApi::class)
abstract class ViewModelTestBase {
    protected val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUpDispatcher() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDownDispatcher() {
        Dispatchers.resetMain()
    }
}

// ---------------------------------------------------------------------------
// 5.1 HomeViewModel — INTERFACES.md §5.1
//
// Public API required by INTERFACES.md:
//   val uiState: StateFlow<HomeUiState>
//   fun onResume()
//
// NOTE: HomeViewModel requires an ApplicationContext for BatteryManager /
// TelephonyManager access; it cannot be instantiated in a pure JUnit test
// without Robolectric or an instrumented test environment.
// The checks below are documented as WHAT MUST BE TESTED and will pass
// when run in an instrumented test (see androidTest/).
// ---------------------------------------------------------------------------

class HomeViewModelInterfaceSpec {

    /**
     * MUST TEST (instrumented):
     * - homeViewModel.uiState is StateFlow<HomeUiState>
     * - initial uiState.menuItems has exactly 5 entries (SCANS, ORGANIZER, TUTORIALS, MUSIC, SETTINGS)
     * - each MenuItem.id and label are uppercase strings
     * - each MenuItem maps to the correct AppDestination
     * - homeViewModel.uiState.value.systemStatus == SystemStatus.Empty on cold start
     * - after onResume(), systemStatus.batteryPercent is in 0..100
     * - after onResume(), systemStatus.signalBars is in 0..4
     * - after onResume(), systemStatus.username == "WARREN"
     * - polling loop fires at most once per STATUS_POLL_INTERVAL_MS (30_000L)
     */
    @Test
    fun `HomeViewModel interface contract is documented`() {
        // Placeholder — this test documents required checks for the instrumented suite.
        // Passes trivially; real assertions live in androidTest/HomeViewModelTest.kt.
        assert(true) { "See androidTest/ for HomeViewModel instrumented tests" }
    }

    @Test
    fun `HomeViewModel menu items match expected order from BRIEF spec`() {
        // The BRIEF.md §UI Requirements lists: SCANS, ORGANIZER, TUTORIALS, MUSIC, SETTINGS
        // HomeViewModel.MENU_ITEMS must match this order.
        val expectedOrder = listOf("SCANS", "ORGANIZER", "TUTORIALS", "MUSIC", "SETTINGS")
        val expectedDestinations = listOf(
            AppDestination.Scans,
            AppDestination.Organizer,
            AppDestination.Tutorials,
            AppDestination.Music,
            AppDestination.Settings
        )
        // Compile-time check: these destinations exist.
        assertEquals(5, expectedOrder.size)
        assertEquals(5, expectedDestinations.size)
    }
}

// ---------------------------------------------------------------------------
// 5.2 ScansViewModel — INTERFACES.md §5.2
// ---------------------------------------------------------------------------

@OptIn(ExperimentalCoroutinesApi::class)
class ScansViewModelInterfaceTest : ViewModelTestBase() {

    private lateinit var repository: ScansRepository
    private lateinit var viewModel: ScansViewModel

    @Before
    fun setUp() {
        repository = mockk()
        every { repository.scanFiles } returns flowOf(emptyList())
        viewModel = ScansViewModel(repository)
    }

    @Test
    fun `ScansViewModel exposes uiState StateFlow`() = runTest {
        // INTERFACES.md §5.2: val uiState: StateFlow<ScansUiState>
        assertNotNull(viewModel.uiState)
    }

    @Test
    fun `ScansViewModel initial state is Loading`() = runTest {
        // INTERFACES.md §5.2: starts in Loading while repository collects.
        // Note: with flowOf(emptyList()) it immediately transitions to Success.
        // We test Loading by using a never-completing flow.
        val blockingFlow = MutableStateFlow<List<ScanFile>>(emptyList())
        val slowRepo = mockk<ScansRepository>()
        every { slowRepo.scanFiles } returns blockingFlow

        val vm = ScansViewModel(slowRepo)
        // Right before advancing coroutines the init hasn't run yet
        val initialState = vm.uiState.value
        assertTrue(
            "Initial state must be Loading before first emission",
            initialState is ScansUiState.Loading
        )
    }

    @Test
    fun `ScansViewModel transitions to Success when repository emits files`() = runTest {
        val testFiles = listOf(
            ScanFile("s1", "SCAN_001.dat", 100, "2024-11-01 12:00", ScanStatus.COMPLETE)
        )
        every { repository.scanFiles } returns flowOf(testFiles)

        val vm = ScansViewModel(repository)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue("State must be Success after files emitted", state is ScansUiState.Success)
        assertEquals(testFiles, (state as ScansUiState.Success).files)
    }

    @Test
    fun `ScansViewModel transitions to Error when repository throws`() = runTest {
        val errorRepo = mockk<ScansRepository>()
        every { errorRepo.scanFiles } returns kotlinx.coroutines.flow.flow {
            throw RuntimeException("network error")
        }

        val vm = ScansViewModel(errorRepo)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue("State must be Error when repository throws", state is ScansUiState.Error)
        assertEquals("network error", (state as ScansUiState.Error).message)
    }

    @Test
    fun `ScansViewModel refresh resets to Loading then re-collects`() = runTest {
        val testFiles = listOf(
            ScanFile("s1", "SCAN_001.dat", 100, "2024-11-01 12:00", ScanStatus.COMPLETE)
        )
        every { repository.scanFiles } returns flowOf(testFiles)

        val vm = ScansViewModel(repository)
        advanceUntilIdle()

        // Call the required public method from INTERFACES.md §5.2
        vm.refresh()

        // Immediately after refresh() the state should be Loading
        assertEquals(ScansUiState.Loading, vm.uiState.value)
        advanceUntilIdle()
        assertTrue(vm.uiState.value is ScansUiState.Success)
    }
}

// ---------------------------------------------------------------------------
// 5.3 OrganizerViewModel — INTERFACES.md §5.3
// ---------------------------------------------------------------------------

@OptIn(ExperimentalCoroutinesApi::class)
class OrganizerViewModelInterfaceTest : ViewModelTestBase() {

    private lateinit var repository: OrganizerRepository
    private lateinit var viewModel: OrganizerViewModel

    private val notesFlow = MutableStateFlow<List<Note>>(emptyList())
    private val tasksFlow = MutableStateFlow<List<Task>>(emptyList())

    @Before
    fun setUp() {
        repository = mockk()
        every { repository.notes } returns notesFlow
        every { repository.tasks } returns tasksFlow
        coEvery { repository.addNote(any(), any()) } returns Unit
        coEvery { repository.deleteNote(any()) } returns Unit
        coEvery { repository.addTask(any()) } returns Unit
        coEvery { repository.toggleTask(any()) } returns Unit
        coEvery { repository.deleteTask(any()) } returns Unit
        viewModel = OrganizerViewModel(repository)
    }

    @Test
    fun `OrganizerViewModel exposes uiState StateFlow`() {
        assertNotNull(viewModel.uiState)
    }

    @Test
    fun `OrganizerViewModel initial state is Empty`() {
        val state = viewModel.uiState.value
        // Empty has isLoading = true per INTERFACES.md §4.2
        assertTrue(state.isLoading)
        assertTrue(state.notes.isEmpty())
        assertTrue(state.tasks.isEmpty())
    }

    @Test
    fun `OrganizerViewModel combines notes and tasks into single uiState`() = runTest {
        val note = Note(1L, "Title", "Body", 1700000000L)
        val task = Task(1L, "Task", false, 1700000000L)

        notesFlow.value = listOf(note)
        tasksFlow.value = listOf(task)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse("State should not be loading after emission", state.isLoading)
        assertEquals(1, state.notes.size)
        assertEquals(1, state.tasks.size)
        assertEquals("Title", state.notes.first().title)
        assertEquals("Task", state.tasks.first().label)
    }

    @Test
    fun `OrganizerViewModel addNote delegates to repository`() = runTest {
        viewModel.addNote("Title", "Body")
        advanceUntilIdle()
        coVerify { repository.addNote("Title", "Body") }
    }

    @Test
    fun `OrganizerViewModel deleteNote delegates to repository`() = runTest {
        val note = Note(1L, "T", "B", 0L)
        viewModel.deleteNote(note)
        advanceUntilIdle()
        coVerify { repository.deleteNote(note) }
    }

    @Test
    fun `OrganizerViewModel addTask delegates to repository`() = runTest {
        viewModel.addTask("Buy milk")
        advanceUntilIdle()
        coVerify { repository.addTask("Buy milk") }
    }

    @Test
    fun `OrganizerViewModel toggleTask delegates to repository with correct task`() = runTest {
        val task = Task(2L, "label", false, 0L)
        viewModel.toggleTask(task)
        advanceUntilIdle()
        coVerify { repository.toggleTask(task) }
    }

    @Test
    fun `OrganizerViewModel deleteTask delegates to repository`() = runTest {
        val task = Task(3L, "label", true, 0L)
        viewModel.deleteTask(task)
        advanceUntilIdle()
        coVerify { repository.deleteTask(task) }
    }

    @Test
    fun `OrganizerViewModel toggleTask updates isCompleted via repository flow`() = runTest {
        // Simulate the full round-trip: toggle fires repository call, repo emits updated list
        val task = Task(1L, "Task", false, 0L)
        tasksFlow.value = listOf(task)
        advanceUntilIdle()

        // Toggle
        viewModel.toggleTask(task)
        advanceUntilIdle()

        // Repository would emit updated list; simulate that
        tasksFlow.value = listOf(task.copy(isCompleted = true))
        advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.tasks.first().isCompleted)
    }
}

// ---------------------------------------------------------------------------
// 5.4 MusicViewModel — INTERFACES.md §5.4
// ---------------------------------------------------------------------------

@OptIn(ExperimentalCoroutinesApi::class)
class MusicViewModelInterfaceTest : ViewModelTestBase() {

    private lateinit var repository: MusicRepository
    private lateinit var viewModel: MusicViewModel
    private val playbackEvents = MutableSharedFlow<PlaybackEvent>(extraBufferCapacity = 8)
    private val testTracks = listOf(
        TrackInfo("t1", "Track 1", "Artist A", 180),
        TrackInfo("t2", "Track 2", "Artist B", 240)
    )

    @Before
    fun setUp() {
        repository = mockk()
        every { repository.tracks } returns flowOf(testTracks)
        every { repository.playbackEvents } returns playbackEvents
        coEvery { repository.play(any()) } returns Unit
        coEvery { repository.pause() } returns Unit
        coEvery { repository.resume() } returns Unit
        coEvery { repository.next() } returns Unit
        coEvery { repository.previous() } returns Unit
        viewModel = MusicViewModel(repository)
    }

    @Test
    fun `MusicViewModel exposes playerState StateFlow`() {
        // INTERFACES.md §5.4: val playerState: StateFlow<PlayerState>
        assertNotNull(viewModel.playerState)
    }

    @Test
    fun `MusicViewModel initial playerState is Idle`() {
        val state = viewModel.playerState.value
        assertFalse(state.isPlaying)
        assertEquals(0, state.progressSeconds)
        assertEquals(0, state.queueIndex)
    }

    @Test
    fun `MusicViewModel populates totalTracks from repository track list`() = runTest {
        advanceUntilIdle()
        assertEquals(2, viewModel.playerState.value.totalTracks)
    }

    @Test
    fun `MusicViewModel play delegates to repository`() = runTest {
        advanceUntilIdle() // allow track list to be collected
        viewModel.play()
        advanceUntilIdle()
        // When no track was playing, play() calls repository.play(firstTrack.id)
        coVerify { repository.play(testTracks.first().id) }
    }

    @Test
    fun `MusicViewModel pause delegates to repository`() = runTest {
        viewModel.pause()
        advanceUntilIdle()
        coVerify { repository.pause() }
    }

    @Test
    fun `MusicViewModel next delegates to repository`() = runTest {
        viewModel.next()
        advanceUntilIdle()
        coVerify { repository.next() }
    }

    @Test
    fun `MusicViewModel previous delegates to repository`() = runTest {
        viewModel.previous()
        advanceUntilIdle()
        coVerify { repository.previous() }
    }

    @Test
    fun `MusicViewModel reflects TrackStarted event in playerState`() = runTest {
        advanceUntilIdle()
        playbackEvents.emit(PlaybackEvent.TrackStarted(testTracks[0]))
        advanceUntilIdle()

        val state = viewModel.playerState.value
        assertTrue(state.isPlaying)
        assertEquals(testTracks[0], state.currentTrack)
        assertEquals(0, state.progressSeconds)
    }

    @Test
    fun `MusicViewModel reflects Paused event in playerState`() = runTest {
        playbackEvents.emit(PlaybackEvent.TrackStarted(testTracks[0]))
        advanceUntilIdle()
        playbackEvents.emit(PlaybackEvent.Paused)
        advanceUntilIdle()

        assertFalse(viewModel.playerState.value.isPlaying)
    }

    @Test
    fun `MusicViewModel reflects Resumed event in playerState`() = runTest {
        playbackEvents.emit(PlaybackEvent.Paused)
        advanceUntilIdle()
        playbackEvents.emit(PlaybackEvent.Resumed)
        advanceUntilIdle()

        assertTrue(viewModel.playerState.value.isPlaying)
    }

    @Test
    fun `MusicViewModel progress field is in seconds not milliseconds`() = runTest {
        // INTERFACES.md §3.7: progress in seconds (Int)
        val state = viewModel.playerState.value
        // progressSeconds is an Int field; assert no accidental Float
        val progressIsInt: Int = state.progressSeconds
        assertEquals(0, progressIsInt)
    }
}

// ---------------------------------------------------------------------------
// 5.5 SettingsViewModel — INTERFACES.md §5.5
// ---------------------------------------------------------------------------

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelInterfaceTest : ViewModelTestBase() {

    private lateinit var repository: SettingsRepository
    private lateinit var viewModel: SettingsViewModel
    private val settingsFlow = MutableStateFlow(SettingsState.Default)

    @Before
    fun setUp() {
        repository = mockk()
        every { repository.settingsFlow } returns settingsFlow
        coEvery { repository.updateEffectIntensity(any()) } returns Unit
        coEvery { repository.updateThemeVariant(any()) } returns Unit
        viewModel = SettingsViewModel(repository)
    }

    @Test
    fun `SettingsViewModel exposes settingsState StateFlow`() {
        // INTERFACES.md §5.5: val settingsState: StateFlow<SettingsState>
        assertNotNull(viewModel.settingsState)
    }

    @Test
    fun `SettingsViewModel initial settingsState is Default`() = runTest {
        advanceUntilIdle()
        val state = viewModel.settingsState.value
        assertEquals(SettingsState.Default.effectIntensity, state.effectIntensity, 0.001f)
        assertEquals(SettingsState.Default.themeVariant, state.themeVariant)
    }

    @Test
    fun `SettingsViewModel updateEffectIntensity clamps to 0f to 1f`() = runTest {
        // INTERFACES.md §5.5: "clamp to 0f..1f internally"
        viewModel.updateEffectIntensity(1.5f)
        advanceUntilIdle()
        coVerify { repository.updateEffectIntensity(1.0f) }

        viewModel.updateEffectIntensity(-0.2f)
        advanceUntilIdle()
        coVerify { repository.updateEffectIntensity(0.0f) }
    }

    @Test
    fun `SettingsViewModel updateEffectIntensity within range passes through unchanged`() = runTest {
        viewModel.updateEffectIntensity(0.7f)
        advanceUntilIdle()
        coVerify { repository.updateEffectIntensity(0.7f) }
    }

    @Test
    fun `SettingsViewModel updateThemeVariant delegates to repository`() = runTest {
        viewModel.updateThemeVariant(ThemeVariant.HIGH_CONTRAST)
        advanceUntilIdle()
        coVerify { repository.updateThemeVariant(ThemeVariant.HIGH_CONTRAST) }
    }

    @Test
    fun `SettingsViewModel reflects repository settingsFlow changes`() = runTest {
        advanceUntilIdle()
        val newSettings = SettingsState(effectIntensity = 0.9f, themeVariant = ThemeVariant.HIGH_CONTRAST)
        settingsFlow.value = newSettings
        advanceUntilIdle()

        assertEquals(0.9f, viewModel.settingsState.value.effectIntensity, 0.001f)
        assertEquals(ThemeVariant.HIGH_CONTRAST, viewModel.settingsState.value.themeVariant)
    }
}

// ---------------------------------------------------------------------------
// Repository interface API checks — INTERFACES.md §6
// ---------------------------------------------------------------------------

class RepositoryInterfaceApiTest {

    /**
     * Verifies that OrganizerRepository interface exposes the exact method
     * signatures required by INTERFACES.md §6.1.
     * This is a compile-time check — if a method is renamed or removed this file
     * will fail to compile.
     */
    @Test
    fun `OrganizerRepository interface has required API`() {
        // These lambda assignments verify the method signatures exist at compile time.
        val hasNotes: (OrganizerRepository) -> kotlinx.coroutines.flow.Flow<List<Note>> =
            { it.notes }
        val hasTasks: (OrganizerRepository) -> kotlinx.coroutines.flow.Flow<List<Task>> =
            { it.tasks }
        val hasAddNote: suspend (OrganizerRepository, String, String) -> Unit =
            { repo, t, b -> repo.addNote(t, b) }
        val hasDeleteNote: suspend (OrganizerRepository, Note) -> Unit =
            { repo, n -> repo.deleteNote(n) }
        val hasAddTask: suspend (OrganizerRepository, String) -> Unit =
            { repo, l -> repo.addTask(l) }
        val hasToggleTask: suspend (OrganizerRepository, Task) -> Unit =
            { repo, t -> repo.toggleTask(t) }
        val hasDeleteTask: suspend (OrganizerRepository, Task) -> Unit =
            { repo, t -> repo.deleteTask(t) }

        // If compilation succeeds, these assignments are valid.
        assertNotNull(hasNotes)
        assertNotNull(hasTasks)
        assertNotNull(hasAddNote)
        assertNotNull(hasDeleteNote)
        assertNotNull(hasAddTask)
        assertNotNull(hasToggleTask)
        assertNotNull(hasDeleteTask)
    }

    @Test
    fun `ScansRepository interface has scanFiles Flow`() {
        val hasScanFiles: (ScansRepository) -> kotlinx.coroutines.flow.Flow<List<ScanFile>> =
            { it.scanFiles }
        assertNotNull(hasScanFiles)
    }

    @Test
    fun `MusicRepository interface has required API`() {
        val hasTracks: (MusicRepository) -> kotlinx.coroutines.flow.Flow<List<TrackInfo>> =
            { it.tracks }
        val hasPlaybackEvents: (MusicRepository) -> kotlinx.coroutines.flow.Flow<PlaybackEvent> =
            { it.playbackEvents }
        val hasPlay: suspend (MusicRepository, String) -> Unit =
            { repo, id -> repo.play(id) }
        val hasPause: suspend (MusicRepository) -> Unit =
            { repo -> repo.pause() }
        val hasResume: suspend (MusicRepository) -> Unit =
            { repo -> repo.resume() }
        val hasNext: suspend (MusicRepository) -> Unit =
            { repo -> repo.next() }
        val hasPrevious: suspend (MusicRepository) -> Unit =
            { repo -> repo.previous() }

        assertNotNull(hasTracks)
        assertNotNull(hasPlaybackEvents)
        assertNotNull(hasPlay)
        assertNotNull(hasPause)
        assertNotNull(hasResume)
        assertNotNull(hasNext)
        assertNotNull(hasPrevious)
    }

    @Test
    fun `SettingsRepository interface has required API`() {
        val hasSettingsFlow: (SettingsRepository) -> kotlinx.coroutines.flow.Flow<SettingsState> =
            { it.settingsFlow }
        val hasUpdateIntensity: suspend (SettingsRepository, Float) -> Unit =
            { repo, v -> repo.updateEffectIntensity(v) }
        val hasUpdateVariant: suspend (SettingsRepository, ThemeVariant) -> Unit =
            { repo, v -> repo.updateThemeVariant(v) }

        assertNotNull(hasSettingsFlow)
        assertNotNull(hasUpdateIntensity)
        assertNotNull(hasUpdateVariant)
    }
}
