package com.example.clipsaver.data.clipboard

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.clipsaver.core.model.ClipEntry

@Database(
    entities = [ClipEntryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ClipboardDatabase : RoomDatabase() {
    abstract fun clipEntryDao(): ClipEntryDao
}

