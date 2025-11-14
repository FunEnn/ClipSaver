package com.clipsaver.data.clipboard

import kotlinx.coroutines.flow.Flow

interface ClipboardRepository {
    suspend fun insertEntry(entry: ClipEntryEntity)
    suspend fun updateEntry(entry: ClipEntryEntity)
    suspend fun deleteEntry(entry: ClipEntryEntity)
    suspend fun deleteAllEntries()
    fun getAllEntries(): Flow<List<ClipEntry>>
    fun searchEntries(query: String): Flow<List<ClipEntry>>
}

class ClipboardRepositoryImpl(private val dao: ClipEntryDao) : ClipboardRepository {
    
    override suspend fun insertEntry(entry: ClipEntryEntity) {
        dao.insert(entry)
    }
    
    override suspend fun updateEntry(entry: ClipEntryEntity) {
        dao.update(entry)
    }
    
    override suspend fun deleteEntry(entry: ClipEntryEntity) {
        dao.delete(entry)
    }
    
    override suspend fun deleteAllEntries() {
        dao.deleteAll()
    }
    
    override fun getAllEntries(): Flow<List<ClipEntry>> {
        return dao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun searchEntries(query: String): Flow<List<ClipEntry>> {
        return dao.search(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}