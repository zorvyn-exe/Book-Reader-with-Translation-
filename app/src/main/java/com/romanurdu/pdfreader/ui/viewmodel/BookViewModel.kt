package com.romanurdu.pdfreader.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.romanurdu.pdfreader.data.database.AppDatabase
import com.romanurdu.pdfreader.data.model.BookEntity
import com.romanurdu.pdfreader.data.repository.BookRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BookViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val bookRepository = BookRepository(
        context = application,
        bookDao = database.bookDao(),
        bookmarkDao = database.bookmarkDao()
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val books: StateFlow<List<BookEntity>> = combine(
        bookRepository.allBooks,
        _searchQuery
    ) { allBooks, query ->
        if (query.isBlank()) {
            allBooks
        } else {
            allBooks.filter { it.title.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun importPdf(uri: Uri, onImported: (Long) -> Unit) {
        viewModelScope.launch {
            _isImporting.value = true
            _errorMessage.value = null
            val result = bookRepository.importPdf(uri)
            _isImporting.value = false
            result.onSuccess { book ->
                onImported(book.id)
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Failed to import PDF"
            }
        }
    }

    fun deleteBook(book: BookEntity) {
        viewModelScope.launch {
            bookRepository.deleteBook(book)
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
