package com.example.clipsaver

import android.app.Application
import androidx.room.Room
import com.example.clipsaver.data.clipboard.ClipboardDatabase
import com.example.clipsaver.data.clipboard.ClipboardRepository
import com.example.clipsaver.data.clipboard.IClipboardRepository
import com.example.clipsaver.feature.history.historyModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class ClipSaverApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@ClipSaverApplication)
            modules(appModule, historyModule)
        }
    }
    
    private val appModule = module {
        single {
            Room.databaseBuilder(
                androidContext(),
                ClipboardDatabase::class.java,
                "clipboard_database"
            ).build()
        }
        
        single<IClipboardRepository> {
            ClipboardRepository(get<ClipboardDatabase>().clipEntryDao())
        }
    }
}

