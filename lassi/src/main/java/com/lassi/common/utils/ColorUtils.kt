package com.lassi.common.utils

import android.content.Context
import androidx.core.content.ContextCompat

object ColorUtils {
    fun getColor(context: Context, color: Int): Int {
        return ContextCompat.getColor(context, color)
    }
}