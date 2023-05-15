package com.lassi.domain.common

import androidx.lifecycle.Observer

class SafeObserver<T>(private val notifier: (T) -> Unit) : Observer<T> {
    override fun onChanged(value: T) {
        value?.let { notifier(value) }
    }
}