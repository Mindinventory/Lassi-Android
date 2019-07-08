package com.lassi.data.common

sealed class VideoRecord<T> {
    data class Start<T>(val item: T) : VideoRecord<T>()

    class Timer<T>(val item: String) : VideoRecord<T>()

    class End<T> : VideoRecord<T>()

    data class Error<T>(val minVideoTime: String) : VideoRecord<T>()
}