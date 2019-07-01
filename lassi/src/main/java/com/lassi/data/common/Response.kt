package com.lassi.data.common

sealed class Response<T> {
    data class Success<T>(val item: T) : Response<T>()
    data class Error<T>(val throwable: Throwable) : Response<T>()
    class Loading<T> : Response<T>()
}