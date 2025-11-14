package com.clipsaver.data.clipboard

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ClipEntryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ClipboardDatabase : RoomDatabase() {
    abstract fun clipEntryDao(): ClipEntryDao
}