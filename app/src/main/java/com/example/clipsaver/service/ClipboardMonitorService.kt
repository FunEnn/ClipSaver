package com.example.clipsaver.service

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.clipsaver.ClipSaverApplication
import com.example.clipsaver.core.model.ClipEntry
import com.example.clipsaver.data.clipboard.IClipboardRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin

class ClipboardMonitorService : android.app.Service() {
    
    private val repository: IClipboardRepository by lazy {
        getKoin().get<IClipboardRepository>()
    }
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).coerceToText(this@ClipboardMonitorService)?.toString()
            if (!text.isNullOrBlank()) {
                serviceScope.launch {
                    repository.insertEntry(ClipEntry(content = text))
                }
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.addPrimaryClipChangedListener(clipboardListener)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?) = null
    
    override fun onDestroy() {
        super.onDestroy()
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.removePrimaryClipChangedListener(clipboardListener)
    }
    
    companion object {
        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, ClipboardMonitorService::class.java)
            )
        }
    }
}

