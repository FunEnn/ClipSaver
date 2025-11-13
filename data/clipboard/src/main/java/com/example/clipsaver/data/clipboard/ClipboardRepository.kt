package com.example.clipsaver.data.clipboard

import com.example.clipsaver.core.model.ClipEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface IClipboardRepository {
    fun getAllEntries(): Flow<List<ClipEntry>>
    suspend fun getEntryById(id: Long): ClipEntry?
    fun searchEntries(query: String): Flow<List<ClipEntry>>
    fun getFavoriteEntries(): Flow<List<ClipEntry>>
    suspend fun insertEntry(entry: ClipEntry): Long
    suspend fun updateEntry(entry: ClipEntry)
    suspend fun deleteEntry(entry: ClipEntry)
    suspend fun deleteEntryById(id: Long)
    suspend fun deleteAll()
}

class ClipboardRepository(
    private val dao: ClipEntryDao
) : IClipboardRepository {
    
    override fun getAllEntries(): Flow<List<ClipEntry>> {
        return dao.getAllEntries().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getEntryById(id: Long): ClipEntry? {
        return dao.getEntryById(id)?.toDomain()
    }
    
    override fun searchEntries(query: String): Flow<List<ClipEntry>> {
        return dao.searchEntries(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getFavoriteEntries(): Flow<List<ClipEntry>> {
        return dao.getFavoriteEntries().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun insertEntry(entry: ClipEntry): Long {
        return dao.insertEntry(entry.toEntity())
    }
    
    override suspend fun updateEntry(entry: ClipEntry) {
        dao.updateEntry(entry.toEntity())
    }
    
    override suspend fun deleteEntry(entry: ClipEntry) {
        dao.deleteEntry(entry.toEntity())
    }
    
    override suspend fun deleteEntryById(id: Long) {
        dao.deleteEntryById(id)
    }
    
    override suspend fun deleteAll() {
        dao.deleteAll()
    }
}

