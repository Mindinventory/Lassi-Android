package com.lassi.common.extenstions

import android.view.View
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