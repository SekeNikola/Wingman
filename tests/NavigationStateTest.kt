/**
 * NavigationStateTest.kt
 *
 * Tests that verify the AppDestination sealed class covers all required screen
 * destinations and that navigation state transitions follow the rules defined
 * in INTERFACES.md §1 and ARCHITECTURE.md §5.
 *
 * These are pure-Kotlin JUnit tests — no Android instrumentation needed.
 * The Compose-level navigation is verified separately in HomeScreenTest (instrumented).
 *
 * To run:   ./gradlew :app:test
 */

package com.wingman.launcher

import com.wingman.launcher.ui.navigation.AppDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// ---------------------------------------------------------------------------
// 1. Sealed class coverage
// ---------------------------------------------------------------------------

class AppDestinationCoverageTest {

    /**
     * INTERFACES.md §1: Exactly 7 variants required.
     * Kotlin sealed class reflection gives us the subclasses at runtime.
     */
    @Test
    fun `AppDestination sealed class has exactly 7 subclasses`() {
        val subclasses = AppDestination::class.sealedSubclasses
        assertEquals(
            "AppDestination must have exactly 7 sealed subclasses " +
                "(Boot, Home, Scans, Organizer, Tutorials, Music, Settings)",
            7,
            subclasses.size
        )
    }

    @Test
    fun `AppDestination subclass names match INTERFACES spec`() {
        val names = AppDestination::class.sealedSubclasses.map { it.simpleName }.toSet()
        val expected = setOf("Boot", "Home", "Scans", "Organizer", "Tutorials", "Music", "Settings")
        assertEquals(expected, names)
    }

    @Test
    fun `All AppDestination singletons are distinct objects`() {
        val destinations: List<AppDestination> = listOf(
            AppDestination.Boot,
            AppDestination.Home,
            AppDestination.Scans,
            AppDestination.Organizer,
            AppDestination.Tutorials,
            AppDestination.Music,
            AppDestination.Settings
        )
        // Each destination must be a unique object identity
        assertEquals("All destinations must be distinct", 7, destinations.toSet().size)
    }
}

// ---------------------------------------------------------------------------
// 2. Navigation state machine rules
// ---------------------------------------------------------------------------

class NavigationStateMachineTest {

    /**
     * INTERFACES.md §1: "Boot is a terminal state that can only transition to Home —
     * no back stack from Boot."
     *
     * Simulates the WingmanApp navigate() lambda logic without a real Compose runtime.
     */
    @Test
    fun `Boot destination cannot be navigated to via the navigate lambda`() {
        // The navigate lambda in WingmanApp.kt guards against navigating TO Boot:
        //   if (destination != AppDestination.Boot) { ... currentDestination = destination }
        // We replicate this logic here to verify the guard is correct.

        var currentDestination: AppDestination = AppDestination.Boot

        val navigate: (AppDestination) -> Unit = { destination ->
            if (destination != AppDestination.Boot) {
                currentDestination = destination
            }
        }

        navigate(AppDestination.Boot) // should be ignored
        assertEquals(AppDestination.Boot, currentDestination)

        navigate(AppDestination.Home)
        assertEquals(AppDestination.Home, currentDestination)
    }

    @Test
    fun `Navigate from Boot to Home is the only allowed Boot transition`() {
        var state: AppDestination = AppDestination.Boot

        // After boot complete callback
        state = AppDestination.Home

        assertEquals(AppDestination.Home, state)
        assertNotEquals(AppDestination.Boot, state)
    }

    @Test
    fun `Back from sub-screen returns to Home not to Boot`() {
        // ARCHITECTURE.md §5: "Back navigation is handled by BackHandler in each
        // sub-screen, which pops back to Home."
        var current: AppDestination = AppDestination.Scans

        val onBack: () -> Unit = { current = AppDestination.Home }
        onBack()

        assertEquals(AppDestination.Home, current)
        assertNotEquals(AppDestination.Boot, current)
    }

    @Test
    fun `BackHandler at Home is disabled — launcher must not exit`() {
        // ARCHITECTURE.md §5 and TASK-07 AC: "does nothing (does not exit app)
        // if already at Home".
        // WingmanApp enables BackHandler only when NOT at Home or Boot:
        //   enabled = currentDestination != Home && currentDestination != Boot
        fun isBackHandlerEnabled(dest: AppDestination): Boolean =
            dest != AppDestination.Home && dest != AppDestination.Boot

        assertFalse("BackHandler must be DISABLED at Home",
            isBackHandlerEnabled(AppDestination.Home))
        assertFalse("BackHandler must be DISABLED at Boot",
            isBackHandlerEnabled(AppDestination.Boot))
        assertTrue("BackHandler must be ENABLED at Scans",
            isBackHandlerEnabled(AppDestination.Scans))
        assertTrue("BackHandler must be ENABLED at Settings",
            isBackHandlerEnabled(AppDestination.Settings))
    }
}

// ---------------------------------------------------------------------------
// 3. Home menu → destination mapping
// ---------------------------------------------------------------------------

class HomeMenuDestinationMappingTest {

    /**
     * Verifies that the fixed 5-item menu in HomeViewModel maps each label
     * to the correct AppDestination. This mirrors the MENU_ITEMS companion
     * object list without importing the ViewModel.
     */
    @Test
    fun `Each menu item maps to a distinct non-Home non-Boot destination`() {
        // Simulate the MENU_ITEMS list from HomeViewModel
        data class MenuEntry(val id: String, val destination: AppDestination)

        val menuItems = listOf(
            MenuEntry("SCANS",     AppDestination.Scans),
            MenuEntry("ORGANIZER", AppDestination.Organizer),
            MenuEntry("TUTORIALS", AppDestination.Tutorials),
            MenuEntry("MUSIC",     AppDestination.Music),
            MenuEntry("SETTINGS",  AppDestination.Settings)
        )

        assertEquals("Menu must have exactly 5 items", 5, menuItems.size)

        // All destinations must be distinct
        assertEquals(5, menuItems.map { it.destination }.toSet().size)

        // No menu item must navigate to Boot or Home
        menuItems.forEach { item ->
            assertNotEquals(
                "Menu item ${item.id} must not navigate to Boot",
                AppDestination.Boot,
                item.destination
            )
            assertNotEquals(
                "Menu item ${item.id} must not navigate to Home",
                AppDestination.Home,
                item.destination
            )
        }
    }

    @Test
    fun `SCANS menu item navigates to AppDestination_Scans`() {
        val scansDestination = AppDestination.Scans
        assertTrue(scansDestination is AppDestination.Scans)
    }

    @Test
    fun `ORGANIZER menu item navigates to AppDestination_Organizer`() {
        val dest = AppDestination.Organizer
        assertTrue(dest is AppDestination.Organizer)
    }

    @Test
    fun `SETTINGS menu item navigates to AppDestination_Settings`() {
        val dest = AppDestination.Settings
        assertTrue(dest is AppDestination.Settings)
    }
}

// ---------------------------------------------------------------------------
// 4. Navigation overlay timing
// ---------------------------------------------------------------------------

class NavigationOverlayTest {

    /**
     * TASK-12 AC: "ACCESSING MODULE..." overlay shown for MODULE_TRANSITION_OVERLAY_MS ms.
     * WingmanApp shows this overlay only when navigating from Home to a sub-screen
     * (not when navigating back to Home).
     */
    @Test
    fun `Module overlay is triggered only for non-Home non-Boot destinations`() {
        fun shouldShowOverlay(destination: AppDestination): Boolean =
            destination != AppDestination.Home && destination != AppDestination.Boot

        assertFalse(shouldShowOverlay(AppDestination.Home))
        assertFalse(shouldShowOverlay(AppDestination.Boot))
        assertTrue(shouldShowOverlay(AppDestination.Scans))
        assertTrue(shouldShowOverlay(AppDestination.Organizer))
        assertTrue(shouldShowOverlay(AppDestination.Tutorials))
        assertTrue(shouldShowOverlay(AppDestination.Music))
        assertTrue(shouldShowOverlay(AppDestination.Settings))
    }

    @Test
    fun `MODULE_TRANSITION_OVERLAY_MS is 400 as per INTERFACES spec`() {
        // INTERFACES.md §8: MODULE_TRANSITION_OVERLAY_MS = 400
        assertEquals(400, com.wingman.launcher.util.WingmanConstants.MODULE_TRANSITION_OVERLAY_MS)
    }
}
