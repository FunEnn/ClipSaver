package com.example.clipsaver.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.clipsaver.service.ClipboardMonitorService

/**
 * 开机启动广播接收器
 * 在设备启动完成后自动启动剪贴板监听服务
 */
class BootCompleteReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.i("BootCompleteReceiver", "设备启动完成，开始启动剪贴板监听服务")
                
                // 延迟一段时间启动服务，确保系统完全启动
                android.os.Handler(context.mainLooper).postDelayed({
                    try {
                        ClipboardMonitorService.start(context)
                        Log.i("BootCompleteReceiver", "剪贴板监听服务启动成功")
                    } catch (e: Exception) {
                        Log.e("BootCompleteReceiver", "剪贴板监听服务启动失败", e)
                    }
                }, 5000) // 延迟5秒启动
            }
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                // 应用更新后自动启动服务
                Log.i("BootCompleteReceiver", "应用更新完成，重新启动剪贴板监听服务")
                try {
                    ClipboardMonitorService.start(context)
                } catch (e: Exception) {
                    Log.e("BootCompleteReceiver", "应用更新后服务启动失败", e)
                }
            }
        }
    }
    
    companion object {
        private const val TAG = "BootCompleteReceiver"
    }
}