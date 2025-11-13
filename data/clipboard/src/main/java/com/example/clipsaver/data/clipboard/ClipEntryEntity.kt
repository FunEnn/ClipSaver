package com.example.clipsaver.data.clipboard

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.clipsaver.core.model.ClipEntry

@Entity(tableName = "clip_entries")
data class ClipEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val timestamp: Long,
    val isFavorite: Boolean = false,
    val tags: String = "" // 用逗号分隔的标签字符串
)

fun ClipEntryEntity.toDomain(): ClipEntry {
    return ClipEntry(
        id = id,
        content = content,
        timestamp = timestamp,
        isFavorite = isFavorite,
        tags = if (tags.isNotEmpty()) tags.split(",") else emptyList()
    )
}

fun ClipEntry.toEntity(): ClipEntryEntity {
    return ClipEntryEntity(
        id = id,
        content = content,
        timestamp = timestamp,
        isFavorite = isFavorite,
        tags = tags.joinToString(",")
    )
}

