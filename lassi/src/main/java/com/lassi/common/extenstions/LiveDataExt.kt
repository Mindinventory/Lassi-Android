package com.lassi.common.extenstions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lassi.common.utils.Logger
import com.lassi.data.common.Response

fun <T> LiveData<T>.safeObserve(owner: LifecycleOwner, observer: (T) -> Unit) {
    observe(owner) { it?.let(observer) ?: Logger.d("TAG", "Live data value is null") }
}

fun <T> MutableLiveData<Response<T>>.setSuccess(data: T) = postValue(Response.Success(data))

fun <T> MutableLiveData<Response<T>>.setLoading() = postValue(Response.Loading())

fun <T> MutableLiveData<Response<T>>.setError(throwable: Throwable) =
    postValue(Response.Error(throwable))

fun <T> MutableLiveData<Response<T>>.isLoading() = value is Response.Loading<T>

fun <T> LiveData<Response<T>>.isLoading() = value is Response.Loading<T>
