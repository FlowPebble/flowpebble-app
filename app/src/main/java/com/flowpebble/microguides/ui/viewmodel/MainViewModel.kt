package com.flowpebble.microguides.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.flowpebble.microguides.data.local.MicroGuideEntity
import com.flowpebble.microguides.data.preferences.UserPrefs
import com.flowpebble.microguides.data.repository.GuideRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: GuideRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _sharedDraftText = MutableStateFlow<String?>(null)
    val sharedDraftText: StateFlow<String?> = _sharedDraftText.asStateFlow()

    val userPrefs: StateFlow<UserPrefs> = repository.userPrefsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPrefs())

    val standardGuides: StateFlow<List<MicroGuideEntity>> = repository.standardGuidesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bonusGuides: StateFlow<List<MicroGuideEntity>> = repository.bonusGuidesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedGuides: StateFlow<List<MicroGuideEntity>> = repository.savedGuidesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedCount: StateFlow<Int> = repository.completedCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val filteredExploreGuides: StateFlow<List<MicroGuideEntity>> = combine(
        standardGuides,
        bonusGuides,
        userPrefs,
        _searchQuery,
        _selectedCategory
    ) { standard, bonus, prefs, query, category ->
        val pool = if (prefs.bonusVaultUnlocked) standard + bonus else standard
        val trimmedQuery = query.trim().lowercase()
        pool.filter { guide ->
            val matchesCategory = category == "All" || guide.category.equals(category, ignoreCase = true)
            val matchesQuery = trimmedQuery.isEmpty() ||
                    guide.title.lowercase().contains(trimmedQuery) ||
                    guide.summary.lowercase().contains(trimmedQuery) ||
                    guide.category.lowercase().contains(trimmedQuery)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    fun observeGuide(guideId: String): Flow<MicroGuideEntity?> = repository.observeGuideById(guideId)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSharedDraftText(text: String?) {
        _sharedDraftText.value = text
    }

    fun toggleBookmark(guide: MicroGuideEntity) {
        viewModelScope.launch {
            repository.toggleBookmark(guide)
        }
    }

    fun restoreBookmark(guideId: String) {
        viewModelScope.launch {
            repository.setBookmarkExplicit(guideId, true)
        }
    }

    fun toggleStep(guide: MicroGuideEntity, stepIndex: Int) {
        viewModelScope.launch {
            repository.toggleStepChecked(guide, stepIndex)
        }
    }

    fun markCompleted(guide: MicroGuideEntity, onFinished: (newTotalCompleted: Int) -> Unit) {
        viewModelScope.launch {
            repository.markGuideCompleted(guide)
            onFinished(completedCount.value + 1)
        }
    }

    fun createCustomGuide(
        title: String,
        summary: String,
        category: String,
        emoji: String,
        steps: List<String>,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            repository.createCustomGuide(title, summary, category, emoji, steps)
            onSaved()
        }
    }

    fun unlockBonusVault() {
        viewModelScope.launch {
            repository.unlockBonusVault()
        }
    }

    fun setThemeMode(mode: Int) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setHapticsEnabled(enabled)
        }
    }

    fun markOnboardingCompleted() {
        viewModelScope.launch {
            repository.setOnboardingCompleted()
        }
    }

    class Factory(private val repository: GuideRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
