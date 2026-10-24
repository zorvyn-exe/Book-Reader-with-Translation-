package com.romanurdu.pdfreader.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.romanurdu.pdfreader.data.database.AppDatabase
import com.romanurdu.pdfreader.data.model.VocabularyEntity
import com.romanurdu.pdfreader.data.repository.DictionaryRepository
import com.romanurdu.pdfreader.dictionary.DictionaryEngine
import com.romanurdu.pdfreader.tts.TtsManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class VocabularyViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val dictionaryEngine = DictionaryEngine(application)
    private val repository = DictionaryRepository(dictionaryEngine, database.vocabularyDao())
    private val ttsManager = TtsManager(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterFavoritesOnly = MutableStateFlow(false)
    val filterFavoritesOnly: StateFlow<Boolean> = _filterFavoritesOnly.asStateFlow()

    val vocabularyList: StateFlow<List<VocabularyEntity>> = combine(
        repository.allVocabulary,
        _searchQuery,
        _filterFavoritesOnly
    ) { all, query, favOnly ->
        all.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.word.contains(query, ignoreCase = true) ||
                    item.romanUrduMeaning.contains(query, ignoreCase = true)
            val matchesFav = !favOnly || item.isFavorite
            matchesQuery && matchesFav
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleFilterFavorites() {
        _filterFavoritesOnly.value = !_filterFavoritesOnly.value
    }

    fun toggleFavorite(item: VocabularyEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(item)
        }
    }

    fun deleteVocabulary(item: VocabularyEntity) {
        viewModelScope.launch {
            repository.deleteVocabulary(item)
        }
    }

    fun pronounceWord(word: String) {
        ttsManager.speak(word)
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
