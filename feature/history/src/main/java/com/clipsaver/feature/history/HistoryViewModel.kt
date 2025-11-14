package com.clipsaver.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clipsaver.core.model.ClipEntry
import com.clipsaver.data.clipboard.ClipboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class HistoryViewModel : ViewModel() {
    
    private val repository: ClipboardRepository by inject(ClipboardRepository::class.java)
    
    private val _entries = MutableStateFlow<List<ClipEntry>>(emptyList())
    val entries: StateFlow<List<ClipEntry>> = _entries.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount.asStateFlow()
    
    init {
        loadEntries()
    }
    
    fun loadEntries() {
        viewModelScope.launch {
            repository.getAllEntries().collect { entries ->
                _entries.value = entries
                _totalCount.value = entries.size
            }
        }
    }
    
    fun search(query: String) {
        _searchQuery.value = query
        
        viewModelScope.launch {
            if (query.isBlank()) {
                loadEntries()
            } else {
                repository.searchEntries(query).collect { entries ->
                    _entries.value = entries
                }
            }
        }
    }
    
    fun deleteEntry(entry: ClipEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry.toEntity())
            loadEntries() // 重新加载数据以更新界面
        }
    }
    
    fun toggleFavorite(entry: ClipEntry) {
        viewModelScope.launch {
            val updatedEntry = entry.copy(isFavorite = !entry.isFavorite)
            repository.updateEntry(updatedEntry.toEntity())
            loadEntries() // 重新加载数据以更新界面
        }
    }
    
    fun deleteAllEntries() {
        viewModelScope.launch {
            repository.deleteAllEntries()
            loadEntries()
        }
    }
}