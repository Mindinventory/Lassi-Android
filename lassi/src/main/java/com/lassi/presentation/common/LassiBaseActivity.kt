package com.lassi.presentation.common

import android.os.Bundle
import android.view.LayoutInflater
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.livefront.bridge.Bridge
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class LassiBaseActivity<VB : ViewBinding> : AppCompatActivity() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!

    private val compositeDisposable = CompositeDisposable()

    abstract fun inflateLayout(layoutInflater: LayoutInflater): VB

    protected open fun getBundle() = Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getBundle()
        _binding = inflateLayout(layoutInflater)
        setContentView(binding.root)
        initViews()
        Bridge.restoreInstanceState(this, savedInstanceState)
    }

    @CallSuper
    protected open fun initViews() {
    }

    protected fun Disposable.collect() = compositeDisposable.add(this)

    fun hasExtra(key: String): Boolean {
        return intent.hasExtra(key)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Bridge.saveInstanceState(this, outState)
        outState.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        Bridge.clear(this)
        _binding = null
    }
}