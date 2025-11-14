package com.clipsaver.data.clipboard

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipEntryDao {
    
    @Query("SELECT * FROM clip_entries ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ClipEntryEntity>>
    
    @Query("SELECT * FROM clip_entries WHERE content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun search(query: String): Flow<List<ClipEntryEntity>>
    
    @Insert
    suspend fun insert(entry: ClipEntryEntity)
    
    @Update
    suspend fun update(entry: ClipEntryEntity)
    
    @Delete
    suspend fun delete(entry: ClipEntryEntity)
    
    @Query("DELETE FROM clip_entries")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM clip_entries")
    fun getCount(): Flow<Int>
}