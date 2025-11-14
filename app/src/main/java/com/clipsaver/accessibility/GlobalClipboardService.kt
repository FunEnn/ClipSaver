package com.clipsaver.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.room.Room
import com.clipsaver.data.clipboard.ClipboardDatabase
import com.clipsaver.data.clipboard.ClipboardRepository
import com.clipsaver.data.clipboard.ClipEntryDao
import com.clipsaver.data.clipboard.ClipEntryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GlobalClipboardService : AccessibilityService() {
    
    private lateinit var clipboardManager: ClipboardManager
    private lateinit var repository: ClipboardRepository
    private val scope = CoroutineScope(Dispatchers.IO)
    
    override fun onCreate() {
        super.onCreate()
        
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        
        // 初始化数据库和仓库
        val database = Room.databaseBuilder(
            applicationContext,
            ClipboardDatabase::class.java,
            "clipboard_db"
        ).build()
        
        repository = ClipboardRepository(database.clipEntryDao())
        
        // 设置剪贴板监听器
        setupClipboardListener()
        
        Log.d("GlobalClipboardService", "无障碍服务已创建并初始化")
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
            notificationTimeout = 100
            packageNames = null // 监听所有应用
        }
        
        this.serviceInfo = info
        
        Log.d("GlobalClipboardService", "无障碍服务已连接，开始监听剪贴板")
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 这里可以处理其他无障碍事件，但主要功能在剪贴板监听器中实现
        event?.let {
            if (it.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
                // 可以记录文本变化事件
                Log.d("GlobalClipboardService", "检测到文本变化事件")
            }
        }
    }
    
    override fun onInterrupt() {
        Log.d("GlobalClipboardService", "无障碍服务被中断")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d("GlobalClipboardService", "无障碍服务已销毁")
    }
    
    private fun setupClipboardListener() {
        clipboardManager.addPrimaryClipChangedListener {
            try {
                val clipData = clipboardManager.primaryClip
                if (clipData != null && clipData.itemCount > 0) {
                    val text = clipData.getItemAt(0).text?.toString()
                    if (text != null && text.isNotBlank()) {
                        Log.d("GlobalClipboardService", "检测到剪贴板内容变化: $text")
                        
                        // 保存到数据库
                        scope.launch {
                            try {
                                repository.insertEntry(
                                    ClipEntryEntity(
                                        content = text,
                                        timestamp = System.currentTimeMillis()
                                    )
                                )
                                Log.d("GlobalClipboardService", "剪贴板内容已保存到数据库")
                            } catch (e: Exception) {
                                Log.e("GlobalClipboardService", "保存剪贴板内容失败", e)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("GlobalClipboardService", "处理剪贴板变化时出错", e)
            }
        }
        
        Log.d("GlobalClipboardService", "剪贴板监听器已设置")
    }
}