package com.example.clipsaver.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clipsaver.core.model.ClipEntry
import com.example.clipsaver.data.clipboard.IClipboardRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: IClipboardRepository
) : ViewModel() {
    
    private val _entries = MutableStateFlow<List<ClipEntry>>(emptyList())
    val entries: StateFlow<List<ClipEntry>> = _entries.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount.asStateFlow()
    
    private var currentJob: Job? = null
    
    init {
        loadEntries()
    }
    
    private fun loadEntries() {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            repository.getAllEntries().collect { entries ->
                _entries.value = entries
                _totalCount.value = entries.size
            }
        }
    }
    
    fun search(query: String) {
        _searchQuery.value = query
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            if (query.isBlank()) {
                repository.getAllEntries().collect { entries ->
                    _entries.value = entries
                    _totalCount.value = entries.size
                }
            } else {
                repository.searchEntries(query).collect { entries ->
                    _entries.value = entries
                    // 搜索时总条数保持不变，只显示搜索结果数量
                }
            }
        }
    }
    
    fun deleteEntry(entry: ClipEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
        }
    }
    
    fun toggleFavorite(entry: ClipEntry) {
        viewModelScope.launch {
            repository.updateEntry(entry.copy(isFavorite = !entry.isFavorite))
        }
    }
}

