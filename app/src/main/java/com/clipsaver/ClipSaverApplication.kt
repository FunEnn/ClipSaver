package com.clipsaver

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class ClipSaverApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger()
            androidContext(this@ClipSaverApplication)
            modules(
                com.clipsaver.feature.history.HistoryModule.module
            )
        }
    }
}
