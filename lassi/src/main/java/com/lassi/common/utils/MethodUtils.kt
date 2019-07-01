package com.lassi.common.utils

import android.content.res.Resources
import android.util.TypedValue
import java.util.concurrent.TimeUnit


/**
 * Convert a dp to px.
 */
fun Int.dpsToPixels(): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics
    ).toInt()
}

fun getDuration(duration: Long): String {
    var duration = duration
    val hours = TimeUnit.MILLISECONDS.toHours(duration)
    duration -= TimeUnit.HOURS.toMillis(hours)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
    duration -= TimeUnit.MINUTES.toMillis(minutes)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(duration)
    val durationBuilder = StringBuilder()
    if (hours > 0) {
        durationBuilder.append(hours)
            .append(":")
    }
    if (minutes < 10)
        durationBuilder.append('0')
    durationBuilder.append(minutes)
        .append(":")
    if (seconds < 10)
        durationBuilder.append('0')
    durationBuilder.append(seconds)
    return durationBuilder.toString()
}
