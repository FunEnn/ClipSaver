package com.example.clipsaver

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationManagerCompat
import com.example.clipsaver.feature.history.HistoryScreen
import com.example.clipsaver.service.ClipboardMonitorService
import android.provider.Settings
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.clipsaver.utils.AccessibilityUtils
import com.example.clipsaver.accessibility.GlobalClipboardService

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startClipboardMonitorSafely()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ensureNotificationPermissionAndStartService()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HistoryScreen()
                }
            }
        }
    }

    private fun ensureNotificationPermissionAndStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                startClipboardMonitorSafely()
            } else {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            startClipboardMonitorSafely()
        }
    }

    private fun startClipboardMonitorSafely() {
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            return
        }
        ClipboardMonitorService.start(this)
        ensureAccessibilityEnabled()
    }

    private fun ensureAccessibilityEnabled() {
        val isEnabled = AccessibilityUtils.isServiceEnabled(
            this,
            "com.example.clipsaver.accessibility.GlobalClipboardService"
        )
        
        if (!isEnabled) {
            Log.d("MainActivity", "无障碍服务未启用，引导用户开启")
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } else {
            Log.d("MainActivity", "无障碍服务已启用，确保服务正常运行")
            try {
                val serviceIntent = Intent(this, GlobalClipboardService::class.java)
                startService(serviceIntent)
            } catch (e: Exception) {
                Log.e("MainActivity", "启动无障碍服务失败", e)
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
    }
    
    private fun checkServiceStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasNotificationPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            
            if (!hasNotificationPermission) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            val intent = Intent().apply {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                        action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                        putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                    }
                    else -> {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", packageName, null)
                    }
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            return
        }
        
        try {
            ClipboardMonitorService.start(this)
        } catch (e: Exception) {
            Log.e("MainActivity", "启动剪贴板监听服务失败", e)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
    }
}
