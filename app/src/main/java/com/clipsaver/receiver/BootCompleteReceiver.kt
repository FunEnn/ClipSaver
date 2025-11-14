package com.clipsaver.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.clipsaver.service.ClipboardMonitorService

class BootCompleteReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.d("BootCompleteReceiver", "设备启动完成，启动剪贴板监听服务")
                ClipboardMonitorService.start(context)
            }
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                Log.d("BootCompleteReceiver", "应用更新完成，重启剪贴板监听服务")
                ClipboardMonitorService.start(context)
            }
        }
    }
}