package com.lassi.presentation.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.viewbinding.ViewBinding

abstract class LassiBaseViewModelFragment<T : LassiBaseViewModel, VB: ViewBinding> : LassiBaseFragment<VB>() {
    private var _binding: VB? = null
    protected val binding get() = _binding!!

    protected val viewModel by lazy { buildViewModel() }

    protected abstract fun buildViewModel(): T

    protected open fun getBundle() = Unit

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflateLayout(inflater)
        setHasOptionsMenu(hasOptionMenu())
        getBundle()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initLiveDataObservers()
        viewModel.loadPage()
    }

    @CallSuper
    protected open fun initLiveDataObservers() {
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
