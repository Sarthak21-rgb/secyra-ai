package com.secyra.ai.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.secyra.ai.data.repository.Contact
import com.secyra.ai.data.repository.ContactRepository
import com.secyra.ai.core.security.CallerIdentificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchHistoryItem(
    val id: String,
    val query: String,
    val timestamp: Long,
    val type: SearchType
)

enum class SearchType {
    CONTACT,
    NUMBER,
    PROFILE
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val callerIdentificationService: CallerIdentificationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        loadSearchHistory()
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        
        if (query.length >= 2) {
            searchGlobally(query)
        } else {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
        }
    }

    fun onSearchSubmitted(query: String) {
        if (query.isNotEmpty()) {
            // Add to search history
            val historyItem = SearchHistoryItem(
                id = System.currentTimeMillis().toString(),
                query = query,
                timestamp = System.currentTimeMillis(),
                type = if (query.matches(Regex("^[+]?[0-9]+$"))) SearchType.NUMBER else SearchType.CONTACT
            )
            
            val updatedHistory = listOf(historyItem) + _uiState.value.searchHistory
                .filter { it.query != query }
                .take(50)
            
            _uiState.value = _uiState.value.copy(searchHistory = updatedHistory)
        }
    }

    fun onContactClicked(contact: Contact) {
        // Add to search history when a contact is clicked
        val historyItem = SearchHistoryItem(
            id = System.currentTimeMillis().toString(),
            query = contact.name,
            timestamp = System.currentTimeMillis(),
            type = SearchType.CONTACT
        )
        
        val updatedHistory = listOf(historyItem) + _uiState.value.searchHistory
            .filter { it.query != contact.name }
            .take(50)
        
        _uiState.value = _uiState.value.copy(searchHistory = updatedHistory)
    }

    fun clearSearchHistory() {
        _uiState.value = _uiState.value.copy(searchHistory = emptyList())
    }

    fun removeFromHistory(item: SearchHistoryItem) {
        _uiState.value = _uiState.value.copy(
            searchHistory = _uiState.value.searchHistory.filter { it.id != item.id }
        )
    }

    private fun searchGlobally(query: String) {
        viewModelScope.launch {
            try {
                // Search local contacts first
                val localContacts = contactRepository.searchContacts(query, limit = 20)
                
                // For global search, use caller ID service to identify the number
                // In production, this would integrate with a global caller ID API
                val globalResults = if (query.matches(Regex("^[+]?[0-9]+$"))) {
                    // It's a phone number - use caller ID service
                    val callerInfo = callerIdentificationService.identifyNumber(query)
                    if (callerInfo.name != null) {
                        listOf(Contact(
                            id = query,
                            name = callerInfo.name,
                            phoneNumber = query,
                            photoUri = null
                        ))
                    } else {
                        emptyList()
                    }
                } else {
                    emptyList()
                }
                
                // Combine results, prioritizing local contacts
                val allResults = (localContacts + globalResults).distinctBy { it.phoneNumber }
                _uiState.value = _uiState.value.copy(searchResults = allResults)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(searchResults = emptyList())
            }
        }
    }

    private fun loadSearchHistory() {
        // In a real app, this would load from persistent storage
        // For now, we'll use an empty list
        _uiState.value = _uiState.value.copy(searchHistory = emptyList())
    }
}

data class SearchUiState(
    val searchQuery: String = "",
    val searchResults: List<Contact> = emptyList(),
    val searchHistory: List<SearchHistoryItem> = emptyList()
)
