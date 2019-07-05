package com.lassi.common.utils

import java.util.concurrent.TimeUnit

object DurationUtils {
    fun getDuration(videoDuration: Long): String {
        var duration = videoDuration
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
}