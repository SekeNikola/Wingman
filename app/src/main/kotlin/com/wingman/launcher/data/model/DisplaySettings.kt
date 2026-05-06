package com.wingman.launcher.data.model

data class DisplaySettings(
    // Top display panel
    val topHeightFrac : Float = 0.460f,
    val topSidePadDp  : Float = 53.1f,
    val topOffsetDp   : Float = 46.0f,
    // Bottom display panel
    val botHeightFrac : Float = 0.21f,
    val botSidePadDp  : Float = 10.0f,
    val botOffsetDp   : Float = 55.0f   // distance up from the bottom of the screen
)
