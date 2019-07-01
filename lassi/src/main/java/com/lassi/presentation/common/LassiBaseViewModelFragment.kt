package com.lassi.presentation.common

import android.os.Bundle
import androidx.annotation.CallSuper

abstract class LassiBaseViewModelFragment<T : LassiBaseViewModel> : LassiBaseFragment() {

    protected val viewModel by lazy { buildViewModel() }

    protected abstract fun buildViewModel(): T

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initLiveDataObservers()
        viewModel.loadPage()
    }

    @CallSuper
    protected open fun initLiveDataObservers() {
    }

}
