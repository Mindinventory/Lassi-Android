package com.lassi.common.utils

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.lassi.common.utils.ColorUtils.getColor

object DrawableUtils {
    fun changeIconColor(context: Context, @DrawableRes drawableRes: Int, color: Int): Drawable? {
        val iconDrawable = ContextCompat.getDrawable(context, drawableRes)
        iconDrawable?.setColorFilter(getColor(context, color), PorterDuff.Mode.SRC_ATOP)
        return iconDrawable
    }
}