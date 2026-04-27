package com.lassi.common.extenstions

import android.app.Activity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

fun Activity.applyEdgeToEdge() {

    val window = this.window
    val decorView = window.decorView

    WindowCompat.setDecorFitsSystemWindows(window, false)

    WindowInsetsControllerCompat(window, decorView).apply {
        isAppearanceLightStatusBars = true
        isAppearanceLightNavigationBars = true
    }
}

