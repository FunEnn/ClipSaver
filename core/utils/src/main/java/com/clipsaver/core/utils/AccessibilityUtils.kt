package com.clipsaver.core.utils

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.accessibility.AccessibilityManager
import java.util.*

object AccessibilityUtils {
    
    /**
     * 检查指定无障碍服务是否已启用
     */
    fun isServiceEnabled(context: Context, serviceName: String): Boolean {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        
        return enabledServices.any { service ->
            service.id.contains(serviceName)
        }
    }
    
    /**
     * 检查无障碍服务是否启用
     */
    fun isAccessibilityEnabled(context: Context): Boolean {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        return accessibilityManager.isEnabled
    }
    
    /**
     * 获取所有已启用的无障碍服务
     */
    fun getEnabledServices(context: Context): List<String> {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        
        return enabledServices.map { service ->
            service.id
        }
    }
}