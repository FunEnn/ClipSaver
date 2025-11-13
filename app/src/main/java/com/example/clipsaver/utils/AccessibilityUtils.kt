package com.example.clipsaver.utils

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import java.util.*

object AccessibilityUtils {
    fun isServiceEnabled(context: Context, serviceClassName: String): Boolean {
        val enabled = try {
            Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (_: Exception) {
            0
        }
        if (enabled != 1) return false

        val settingValue = try {
            Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
        } catch (_: Exception) {
            null
        } ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(settingValue.lowercase(Locale.getDefault()))
        val expected = ComponentName(context.packageName, serviceClassName)
            .flattenToString()
            .lowercase(Locale.getDefault())
        while (colonSplitter.hasNext()) {
            val componentName = colonSplitter.next()
            if (componentName == expected) {
                return true
            }
        }
        return false
    }
}

