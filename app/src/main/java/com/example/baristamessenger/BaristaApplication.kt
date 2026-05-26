package com.example.baristamessenger

import android.app.Application
import com.example.baristamessenger.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BaristaApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@BaristaApplication)
            modules(appModule)
        }
    }
}