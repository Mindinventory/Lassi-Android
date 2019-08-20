package com.lassi

import android.app.Application
import android.os.Bundle
import com.livefront.bridge.Bridge
import com.livefront.bridge.SavedStateHandler
import io.reactivex.annotations.NonNull
import io.reactivex.annotations.Nullable

open class LassiController : Application() {
    override fun onCreate() {
        super.onCreate()
        Bridge.initialize(applicationContext, object : SavedStateHandler {
            override fun saveInstanceState(@NonNull target: Any, @NonNull state: Bundle) {
            }

            override fun restoreInstanceState(@NonNull target: Any, @Nullable state: Bundle?) {
            }
        })
    }
}