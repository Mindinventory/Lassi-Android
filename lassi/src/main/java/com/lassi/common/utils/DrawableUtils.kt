package com.lassi.common.utils

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

object DrawableUtils {
    fun changeIconColor(context: Context, @DrawableRes drawableRes: Int, color: Int): Drawable? {
        val iconDrawable = ContextCompat.getDrawable(context, drawableRes)
        iconDrawable?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        return iconDrawable
    }
}