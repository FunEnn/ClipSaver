package com.example.clipsaver.data.clipboard

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipEntryDao {
    @Query("SELECT * FROM clip_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<ClipEntryEntity>>
    
    @Query("SELECT * FROM clip_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): ClipEntryEntity?
    
    @Query("SELECT * FROM clip_entries WHERE content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchEntries(query: String): Flow<List<ClipEntryEntity>>
    
    @Query("SELECT * FROM clip_entries WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteEntries(): Flow<List<ClipEntryEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: ClipEntryEntity): Long
    
    @Update
    suspend fun updateEntry(entry: ClipEntryEntity)
    
    @Delete
    suspend fun deleteEntry(entry: ClipEntryEntity)
    
    @Query("DELETE FROM clip_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)
    
    @Query("DELETE FROM clip_entries")
    suspend fun deleteAll()
}

