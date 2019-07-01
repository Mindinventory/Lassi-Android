package com.lassi.presentation.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class LassiBaseFragment : Fragment() {

    private val compositeDisposable = CompositeDisposable()

    protected abstract fun getContentResource(): Int

    protected open fun getBundle() = Unit

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        getBundle()
        return inflater.inflate(getContentResource(), container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initViews()
    }

    override fun onStart() {
        super.onStart()
        initOnBackPressed()
    }

    @CallSuper
    protected open fun initOnBackPressed() {
    }

    @CallSuper
    protected open fun initViews() {
    }

    protected fun Disposable.collect() = compositeDisposable.add(this)

}