package com.example.clipsaver.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

object ClipboardHelper {
    
    /**
     * 获取当前剪贴板文本内容
     */
    fun getClipboardText(context: Context): String? {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = clipboard.primaryClip
            if (clip != null && clip.itemCount > 0) {
                clip.getItemAt(0).coerceToText(context)?.toString()
            } else {
                null
            }
        } catch (_: SecurityException) {
            null
        } catch (_: IllegalStateException) {
            null
        }
    }
    
    /**
     * 设置剪贴板文本内容
     */
    fun setClipboardText(context: Context, text: String, label: String = "ClipSaver") {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
        } catch (_: SecurityException) {
            // ignore
        } catch (_: IllegalStateException) {
            // ignore
        }
    }
    
    /**
     * 检查剪贴板是否有内容
     */
    fun hasClipboardContent(context: Context): Boolean {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.hasPrimaryClip() && clipboard.primaryClip != null
        } catch (_: SecurityException) {
            false
        } catch (_: IllegalStateException) {
            false
        }
    }
}

