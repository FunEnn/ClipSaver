package com.clipsaver.core.model

import java.util.Date

/**
 * 剪贴板条目数据模型
 */
data class ClipEntry(
    val id: Long = 0,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val tags: List<String> = emptyList()
) {
    val date: Date
        get() = Date(timestamp)
}