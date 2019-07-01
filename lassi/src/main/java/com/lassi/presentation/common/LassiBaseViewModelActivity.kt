package com.lassi.presentation.common

import android.os.Bundle
import androidx.annotation.CallSuper

abstract class LassiBaseViewModelActivity<T : LassiBaseViewModel> : LassiBaseActivity() {

    protected val viewModel by lazy { buildViewModel() }

    protected abstract fun buildViewModel(): T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initLiveDataObservers()
        viewModel.loadPage()
    }

    @CallSuper
    protected open fun initLiveDataObservers() = Unit
}