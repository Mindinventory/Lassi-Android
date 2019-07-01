package com.lassi.presentation.common

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class LassiBaseActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()

    protected abstract fun getContentResource(): Int

    protected open fun getBundle() = Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getBundle()
        setContentView(getContentResource())
        initViews()
    }

    @CallSuper
    protected open fun initViews() {
    }

    protected fun Disposable.collect() = compositeDisposable.add(this)

    fun hasExtra(key: String): Boolean {
        return intent.hasExtra(key)
    }
}