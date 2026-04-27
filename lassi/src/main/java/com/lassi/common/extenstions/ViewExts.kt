package com.lassi.common.extenstions

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.hide() {
    this.isVisible = false
}

fun View.show() {
    this.isVisible = true
}

fun View.applyTopInset() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
        view.setPadding(view.paddingLeft, top, view.paddingRight, view.paddingBottom)
        insets
    }
}

fun View.applyBottomInset() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
        view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, bottom)
        insets
    }
}

fun View.applySystemBarsInsets(
    top: Boolean = false,
    bottom: Boolean = false
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->

        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

        view.setPadding(
            view.paddingLeft,
            if (top) systemBars.top else 0,
            view.paddingRight,
            if (bottom) systemBars.bottom else 0
        )

        insets
    }

    ViewCompat.requestApplyInsets(this)
}