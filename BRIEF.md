Build a fully functional Android launcher inspired by the UI style of the device shown in REPLACED.

Core concept

The launcher must feel like a retro-futuristic handheld terminal, with:

pixel-art inspired UI
dark green / black background
warm orange highlight color
list-based navigation (no icons grid like standard Android)
focus on selection cursor + scrolling, not touch-first interaction
UI Requirements
Main screen (Home)
Fullscreen list menu with items:
SCANS
ORGANIZER
TUTORIALS
MUSIC
SETTINGS
Only one item is “active” at a time (highlighted with orange bar)
Others are dimmed
Add small pixel-style icons to the left of each item
Top bar
Thin header with:
fake username (e.g. “WARREN”)
minimal indicators (battery, signal styled as pixel icons)
Slight scanline / CRT effect overlay (subtle, not heavy)
Visual style
Typography:
pixel / monospaced font (Press Start 2P or similar)
Colors:
background: near black / dark green
primary: amber/orange
secondary: muted green text
Effects:
soft glow on active item
slight noise/grain overlay
optional flicker animation (very subtle)
Interaction Model (IMPORTANT)

The launcher must support:

Scroll input (simulated wheel)
Keyboard navigation
Touch fallback
Navigation behavior
Up/Down:
moves selection
Center/Enter:
opens selected section
Back:
returns to previous menu
Scrolling feel
Smooth but slightly stepped (not iOS smooth)
Optional:
acceleration when holding input
small “snap” effect between items
Sections (basic implementation)

Each menu item opens a simple screen:

SCANS → list of dummy files
ORGANIZER → notes/tasks list
TUTORIALS → static text screen
MUSIC → mock player UI
SETTINGS → toggles (theme intensity, effects)

Keep these minimal but styled consistently.

Technical Requirements
Language: Kotlin
UI: Jetpack Compose
Architecture:
Single Activity
Navigation via state (no fragments)
Must be installable as default launcher (HOME intent filter)
Interaction model (important)
Single tap
tap item → becomes selected
Double tap (or second tap)
activates it

👉 Alternative (simpler):

tap = select + open immediately
but keep visual selection state for feedback
Compose implementation pattern
1. State
var selectedIndex by remember { mutableStateOf(0) }

val items = listOf(
    "Organizer",
    "Tutorials",
    "Music",
    "Settings"
)
2. UI list

Use a Column (not LazyColumn at first — you want tight control):

Column {
    items.forEachIndexed { index, item ->
        val isSelected = index == selectedIndex

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (selectedIndex == index) {
                        openItem(item)
                    } else {
                        selectedIndex = index
                    }
                }
                .background(
                    if (isSelected) Color(0xFFB86B2B) else Color.Transparent
                )
                .padding(12.dp)
        ) {
            Text(
                text = item,
                color = if (isSelected) Color.Black else Color(0xFFB86B2B)
            )
        }
    }
}
Make it feel like that device (this is the difference)

Most people stop at “clickable list” — that’s why it feels wrong.

You want:

1. No ripple effects

Disable Android’s default touch feedback:

.clickable(
    indication = null,
    interactionSource = remember { MutableInteractionSource() }
)
2. Slight delay on selection

Make it feel like hardware:

LaunchedEffect(selectedIndex) {
    // tiny delay or animation trigger
}
3. Subtle animation
val animatedColor by animateColorAsState(
    if (isSelected) Color(0xFFB86B2B) else Color.Transparent
)
4. Glow effect (very important for the vibe)
soft outer glow on selected item
slightly blurred text shadow
faint scanline overlay across screen
Add:
boot screen animation (like device powering on)
subtle sound effects (click, scroll)
Fake system text like:
“INITIALIZING…”
“ACCESSING MODULE…”
Constraints
No Material Design UI components
No standard Android app grid
Everything must feel like a dedicated device OS
Performance must stay smooth on mid-range Android devices
Output

Provide:

Full project structure
MainActivity + composables
Theme + color system
Input handling system
Sample screens for all sections