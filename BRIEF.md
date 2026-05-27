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
Only one item is "active" at a time (highlighted with orange bar)
Others are dimmed
Add small pixel-style icons to the left of each item
Top bar
Thin header with:
fake username (e.g. "WARREN")
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
small "snap" effect between items
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
Input handling
Handle:
KEYCODE_DPAD_UP / DOWN
KEYCODE_ENTER
KEYCODE_BACK
Abstract input so later it can support:
rotary encoder (wheel)
external keyboard
Extra polish (if possible)
Add:
boot screen animation (like device powering on)
subtle sound effects (click, scroll)
Fake system text like:
"INITIALIZING…"
"ACCESSING MODULE…"
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
