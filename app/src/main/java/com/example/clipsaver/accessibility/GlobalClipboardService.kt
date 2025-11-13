package com.example.clipsaver.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.clipsaver.core.model.ClipEntry
import com.example.clipsaver.data.clipboard.IClipboardRepository
import kotlinx.coroutines.*
import org.koin.android.ext.android.getKoin

class GlobalClipboardService : AccessibilityService() {

    private val repository: IClipboardRepository by lazy { getKoin().get<IClipboardRepository>() }
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var lastText: String? = null
    private var clipboardManager: ClipboardManager? = null

    companion object {
        private const val TAG = "GlobalClipboardService"
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        
        val serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED or
                        AccessibilityEvent.TYPE_VIEW_FOCUSED or
                        AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED or
                        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                    AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE or
                    AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY
        }
        this.serviceInfo = serviceInfo
        
        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        
        clipboardManager?.addPrimaryClipChangedListener(clipboardListener)
        
        Log.i(TAG, "全局剪贴板无障碍服务已启动")
        
        serviceScope.launch {
            val initialText = getClipboardTextSafely()
            if (!initialText.isNullOrBlank()) {
                lastText = initialText
                saveClipboardText(initialText)
                Log.d(TAG, "初始剪贴板内容已记录: ${initialText.take(50)}...")
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 当用户与UI交互时，可以获取剪贴板内容
        event?.let {
            when (it.eventType) {
                AccessibilityEvent.TYPE_VIEW_CLICKED,
                AccessibilityEvent.TYPE_VIEW_FOCUSED,
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED,
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    handleClipboardChanged()
                }
            }
        }
    }

    private val clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
        Log.d(TAG, "剪贴板内容发生变化")
        handleClipboardChanged()
    }

    private fun handleClipboardChanged() {
        serviceScope.launch {
            try {
                val text = getClipboardTextSafely()
                if (!text.isNullOrBlank() && text != lastText) {
                    lastText = text
                    saveClipboardText(text)
                    Log.d(TAG, "剪贴板内容已保存: ${text.take(50)}...")
                } else if (text == null) {
                    Log.w(TAG, "剪贴板内容为空或获取失败")
                } else {
                    Log.d(TAG, "剪贴板内容与上次相同，跳过保存")
                }
            } catch (e: Exception) {
                Log.e(TAG, "处理剪贴板变化失败", e)
            }
        }
    }

    private suspend fun getClipboardTextSafely(): String? {
        return withContext(Dispatchers.Main) {
            try {
                // 作为无障碍服务，我们可以绕过Android 10+的后台限制
                clipboardManager?.let { manager ->
                    val clip = manager.primaryClip
                    if (clip != null && clip.itemCount > 0) {
                        clip.getItemAt(0).coerceToText(this@GlobalClipboardService)?.toString()
                    } else {
                        null
                    }
                }
            } catch (e: SecurityException) {
                Log.w(TAG, "剪贴板访问权限被拒绝", e)
                null
            } catch (e: IllegalStateException) {
                Log.w(TAG, "剪贴板服务暂时不可用", e)
                null
            } catch (e: Exception) {
                Log.e(TAG, "剪贴板访问异常", e)
                null
            }
        }
    }

    private fun saveClipboardText(text: String) {
        serviceScope.launch {
            try {
                repository.insertEntry(ClipEntry(content = text))
                Log.d(TAG, "剪贴板内容已保存到数据库")
            } catch (e: Exception) {
                Log.e(TAG, "保存剪贴板内容失败", e)
            }
        }
    }

    override fun onInterrupt() {
        Log.w(TAG, "无障碍服务被中断")
    }

    override fun onDestroy() {
        super.onDestroy()
        clipboardManager?.removePrimaryClipChangedListener(clipboardListener)
        serviceScope.cancel()
        Log.i(TAG, "全局剪贴板无障碍服务已停止")
    }
}

