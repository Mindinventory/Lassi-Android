package com.lassi.common.utils

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat

object DrawableUtils {
    fun changeIconColor(
        context: Context,
        @DrawableRes drawableRes: Int,
        color: Int
    ) = ContextCompat.getDrawable(context, drawableRes)?.let { drawable ->

        val wrappedDrawable = DrawableCompat.wrap(drawable).mutate()
        DrawableCompat.setTint(wrappedDrawable, color)

        wrappedDrawable
    }
}