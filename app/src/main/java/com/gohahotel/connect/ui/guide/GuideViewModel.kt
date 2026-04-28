package com.gohahotel.connect.ui.guide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gohahotel.connect.data.repository.GuideRepository
import com.gohahotel.connect.domain.model.GuideCategory
import com.gohahotel.connect.domain.model.GuideEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GuideUiState(
    val entries: List<GuideEntry> = emptyList(),
    val nearbyEntries: List<GuideEntry> = emptyList(),
    val selectedEntry: GuideEntry?     = null,
    val selectedCategory: GuideCategory? = null,
    val searchQuery: String            = "",
    val isLoading: Boolean             = false
)

@HiltViewModel
class GuideViewModel @Inject constructor(
    private val guideRepository: GuideRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GuideUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadEntries()
        loadNearby()
    }

    private fun loadEntries() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            guideRepository.getAllEntries().collect { entries ->
                _uiState.update { it.copy(entries = entries, isLoading = false) }
            }
        }
    }

    private fun loadNearby() {
        viewModelScope.launch {
            guideRepository.getNearbyEntries().collect { entries ->
                _uiState.update { it.copy(nearbyEntries = entries) }
            }
        }
    }

    fun selectCategory(category: GuideCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
        viewModelScope.launch {
            val flow = if (category == null) guideRepository.getAllEntries()
                       else guideRepository.getEntriesByCategory(category)
            flow.collect { entries -> _uiState.update { it.copy(entries = entries) } }
        }
    }

    fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            loadEntries()
        } else {
            viewModelScope.launch {
                guideRepository.searchEntries(query).collect { entries ->
                    _uiState.update { it.copy(entries = entries) }
                }
            }
        }
    }

    fun loadEntryDetail(entryId: String) {
        viewModelScope.launch {
            val entry = guideRepository.getEntryById(entryId)
            _uiState.update { it.copy(selectedEntry = entry) }
        }
    }
}
