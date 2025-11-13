package com.example.clipsaver.service

import android.app.*
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.*
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.clipsaver.MainActivity
import com.example.clipsaver.core.model.ClipEntry
import com.example.clipsaver.data.clipboard.IClipboardRepository
import kotlinx.coroutines.*
import org.koin.android.ext.android.getKoin

class ClipboardMonitorService : Service() {
    
    private val repository: IClipboardRepository by lazy {
        getKoin().get<IClipboardRepository>()
    }
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private var lastClipboardText: String? = null
    private var isMonitoring = false
    
    private val clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
        Log.d(TAG, "剪贴板发生变化，开始处理...")
        handleClipboardChange()
    }
    
    private fun handleClipboardChange() {
        serviceScope.launch {
            try {
                val text = getClipboardTextSafely()
                if (text != null) {
                    saveClipboardText(text)
                } else {
                    Log.d(TAG, "剪贴板内容不可用或为空")
                }
            } catch (e: Exception) {
                Log.e(TAG, "处理剪贴板变化失败", e)
            }
        }
    }
    
    private suspend fun getClipboardTextSafely(): String? {
        return withContext(Dispatchers.Main) {
            try {
                // Android 10+ 后台剪贴板访问限制处理
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Android 10+ 需要特殊处理，尝试使用无障碍服务或主线程
                    tryGetClipboardTextWithWorkaround()
                } else {
                    // Android 9及以下版本可以直接访问
                    getClipboardTextDirectly()
                }
            } catch (e: Exception) {
                Log.e(TAG, "安全获取剪贴板文本失败", e)
                null
            }
        }
    }
    
    private fun tryGetClipboardTextWithWorkaround(): String? {
        return try {
            // 尝试多种获取剪贴板内容的方法
            val methods = listOf(
                { getClipboardTextDirectly() },
                { getClipboardTextViaAccessibility() },
                { getClipboardTextWithSystemInteraction() }
            )
            
            for (method in methods) {
                try {
                    val result = method.invoke()
                    if (result != null) {
                        Log.d(TAG, "成功获取剪贴板内容，方法: ${method.javaClass.simpleName}")
                        return result
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "剪贴板获取方法失败: ${e.message}")
                }
            }
            null
        } catch (e: Exception) {
            Log.w(TAG, "所有剪贴板获取方法都失败")
            null
        }
    }
    
    private fun getClipboardTextViaAccessibility(): String? {
        // 尝试通过无障碍服务机制获取剪贴板内容
        return try {
            // 这里可以调用无障碍服务相关的方法
            // 暂时返回null，待实现
            null
        } catch (e: Exception) {
            Log.d(TAG, "通过无障碍服务获取剪贴板失败: ${e.message}")
            null
        }
    }
    
    private fun getClipboardTextWithSystemInteraction(): String? {
        // 尝试通过系统交互的方式获取剪贴板内容
        return try {
            // 这里可以尝试模拟用户交互或其他系统调用
            // 暂时返回null，待实现
            null
        } catch (e: Exception) {
            Log.d(TAG, "通过系统交互获取剪贴板失败: ${e.message}")
            null
        }
    }
    
    private fun getClipboardTextDirectly(): String? {
        return try {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = clipboard.primaryClip
            if (clip != null && clip.itemCount > 0) {
                clip.getItemAt(0).coerceToText(this)?.toString()
            } else {
                null
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "剪贴板访问权限被拒绝，应用可能处于后台状态")
            null
        } catch (e: IllegalStateException) {
            Log.w(TAG, "剪贴板服务暂时不可用")
            null
        } catch (e: Exception) {
            Log.e(TAG, "剪贴板访问异常", e)
            null
        }
    }
    
    private fun saveClipboardText(text: String?) {
        if (!text.isNullOrBlank() && text != lastClipboardText) {
            lastClipboardText = text
            serviceScope.launch {
                try {
                    repository.insertEntry(ClipEntry(content = text))
                    Log.d(TAG, "剪贴板内容已保存: ${text.take(50)}...")
                } catch (e: Exception) {
                    Log.e(TAG, "保存剪贴板内容失败", e)
                }
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        try {
            startForeground(NOTIFICATION_ID, createNotification())
        } catch (exception: Exception) {
            Log.e(TAG, "无法启动前台服务，停止 ClipboardMonitorService", exception)
            stopSelf()
            return
        }
        
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.addPrimaryClipChangedListener(clipboardListener)
        isMonitoring = true
        
        Log.i(TAG, "剪贴板监听服务已启动")
        
        // 记录当前剪贴板内容作为初始状态
        serviceScope.launch {
            try {
                // 安全地获取初始剪贴板内容
                val initialText = getClipboardTextSafely()
                if (!initialText.isNullOrBlank()) {
                    lastClipboardText = initialText
                }
            } catch (e: Exception) {
                Log.e(TAG, "获取初始剪贴板内容失败", e)
            }
        }
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
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "剪贴板监听服务",
                NotificationManager.IMPORTANCE_HIGH  // 提高优先级
            ).apply {
                description = "用于监听剪贴板内容变化"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(null, null)
                enableVibration(false)
                enableLights(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("剪贴板监听中")
            .setContentText("正在监听剪贴板内容变化")
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    companion object {
        private const val CHANNEL_ID = "clipboard_monitor_channel"
        private const val NOTIFICATION_ID = 1
        private const val TAG = "ClipboardMonitorService"
        
        fun start(context: Context) {
            val intent = Intent(context, ClipboardMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stop(context: Context) {
            val intent = Intent(context, ClipboardMonitorService::class.java)
            context.stopService(intent)
        }
    }
}

