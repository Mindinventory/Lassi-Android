package com.lassi.data.common

sealed class Result<out R> {

    data class Success<out T>(
        val data: T
    ) : Result<T>()

    data class Error(val throwable: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Error -> "Error[exception=$throwable]"
            Loading -> "Loading"
        }
    }
}
