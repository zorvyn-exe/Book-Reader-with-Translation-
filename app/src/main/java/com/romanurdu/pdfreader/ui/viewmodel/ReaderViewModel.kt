package com.romanurdu.pdfreader.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.romanurdu.pdfreader.data.database.AppDatabase
import com.romanurdu.pdfreader.data.model.BookEntity
import com.romanurdu.pdfreader.data.model.WordLookupResult
import com.romanurdu.pdfreader.data.repository.BookRepository
import com.romanurdu.pdfreader.data.repository.DictionaryRepository
import com.romanurdu.pdfreader.dictionary.DictionaryEngine
import com.romanurdu.pdfreader.tts.TtsManager
import com.romanurdu.pdfreader.ui.theme.ReadingTheme
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TapCoordinates(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val windowWidth: Float,
    val windowHeight: Float,
    val dpr: Float,
    val pageNum: Int
)

class ReaderViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val dictionaryEngine = DictionaryEngine(application)
    private val bookRepository = BookRepository(application, database.bookDao(), database.bookmarkDao())
    private val dictionaryRepository = DictionaryRepository(dictionaryEngine, database.vocabularyDao())
    private val ttsManager = TtsManager(application)

    private val _currentBook = MutableStateFlow<BookEntity?>(null)
    val currentBook: StateFlow<BookEntity?> = _currentBook.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _totalPages = MutableStateFlow(1)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()

    private val _isBookmarked = MutableStateFlow(false)
    val isBookmarked: StateFlow<Boolean> = _isBookmarked.asStateFlow()

    private val _selectedWord = MutableStateFlow<String?>(null)
    val selectedWord: StateFlow<String?> = _selectedWord.asStateFlow()

    private val _surroundingSentence = MutableStateFlow<String?>(null)
    val surroundingSentence: StateFlow<String?> = _surroundingSentence.asStateFlow()

    private val _tapCoordinates = MutableStateFlow<TapCoordinates?>(null)
    val tapCoordinates: StateFlow<TapCoordinates?> = _tapCoordinates.asStateFlow()

    private val _lookupResult = MutableStateFlow<WordLookupResult?>(null)
    val lookupResult: StateFlow<WordLookupResult?> = _lookupResult.asStateFlow()

    private val _isLoadingMeaning = MutableStateFlow(false)
    val isLoadingMeaning: StateFlow<Boolean> = _isLoadingMeaning.asStateFlow()

    private val _isSavedToVocab = MutableStateFlow(false)
    val isSavedToVocab: StateFlow<Boolean> = _isSavedToVocab.asStateFlow()

    private val _isScannedPdf = MutableStateFlow(false)
    val isScannedPdf: StateFlow<Boolean> = _isScannedPdf.asStateFlow()

    private val _readingTheme = MutableStateFlow(ReadingTheme.LIGHT)
    val readingTheme: StateFlow<ReadingTheme> = _readingTheme.asStateFlow()

    private val _zoomScale = MutableStateFlow(1.25f)
    val zoomScale: StateFlow<Float> = _zoomScale.asStateFlow()

    init {
        viewModelScope.launch {
            dictionaryEngine.initialize()
        }
    }

    fun loadBook(bookId: Long) {
        viewModelScope.launch {
            val book = bookRepository.getBookById(bookId)
            _currentBook.value = book
            if (book != null) {
                _currentPage.value = book.currentPage
                _isScannedPdf.value = book.isOcrNeeded
                checkBookmarkStatus(book.id, book.currentPage)
            }
        }
    }

    fun onDocumentLoaded(total: Int) {
        _totalPages.value = total
        _currentBook.value?.let { book ->
            viewModelScope.launch {
                bookRepository.updateProgress(book.id, _currentPage.value, 0f)
            }
        }
    }

    fun onPageChanged(page: Int, total: Int, scrollPercent: Float) {
        _currentPage.value = page
        if (total > 0) _totalPages.value = total

        _currentBook.value?.let { book ->
            viewModelScope.launch {
                bookRepository.updateProgress(book.id, page, scrollPercent)
                checkBookmarkStatus(book.id, page)
            }
        }
    }

    fun onScannedPdfDetected(pageNum: Int) {
        _isScannedPdf.value = true
        _currentBook.value?.let { book ->
            viewModelScope.launch {
                bookRepository.markScannedPdf(book.id, true)
            }
        }
    }

    fun dismissScannedWarning() {
        _isScannedPdf.value = false
    }

    /**
     * Tapped word detected from WebView PDF.js text layer
     */
    fun onWordTapped(
        word: String,
        sentence: String,
        coords: TapCoordinates
    ) {
        val cleanWord = word.trim().replace(Regex("^[^a-zA-Z0-9]+|[^a-zA-Z0-9]+$"), "")
        if (cleanWord.isBlank()) return

        _selectedWord.value = cleanWord
        _surroundingSentence.value = sentence
        _tapCoordinates.value = coords
        _isLoadingMeaning.value = true
        _isSavedToVocab.value = false

        viewModelScope.launch {
            // Check if already in saved vocabulary
            _isSavedToVocab.value = dictionaryRepository.isWordSaved(cleanWord)

            // Perform context-aware lookup
            val result = dictionaryRepository.lookupWord(cleanWord, sentence)
            _lookupResult.value = result
            _isLoadingMeaning.value = false
        }
    }

    /**
     * Triggers online lookup fallback
     */
    fun performOnlineLookup() {
        val word = _selectedWord.value ?: return
        _isLoadingMeaning.value = true

        viewModelScope.launch {
            val onlineResult = dictionaryRepository.lookupOnline(word)
            _lookupResult.value = onlineResult
            _isLoadingMeaning.value = false
        }
    }

    /**
     * Save the currently selected word to user's persistent vocabulary
     */
    fun saveCurrentWordToVocab() {
        val word = _selectedWord.value ?: return
        val result = _lookupResult.value ?: return

        viewModelScope.launch {
            dictionaryRepository.saveWord(
                word = word,
                meaning = result.matchedMeaning,
                sentence = _surroundingSentence.value ?: "",
                bookTitle = _currentBook.value?.title ?: "Document",
                pageNumber = _currentPage.value
            )
            _isSavedToVocab.value = true
        }
    }

    fun speakCurrentWord() {
        val word = _selectedWord.value ?: return
        ttsManager.speak(word)
    }

    fun clearSelectedWord() {
        _selectedWord.value = null
        _surroundingSentence.value = null
        _tapCoordinates.value = null
        _lookupResult.value = null
        _isLoadingMeaning.value = false
    }

    fun toggleBookmark() {
        val book = _currentBook.value ?: return
        viewModelScope.launch {
            bookRepository.toggleBookmark(book.id, _currentPage.value)
            checkBookmarkStatus(book.id, _currentPage.value)
        }
    }

    private suspend fun checkBookmarkStatus(bookId: Long, page: Int) {
        _isBookmarked.value = bookRepository.isPageBookmarked(bookId, page)
    }

    fun setReadingTheme(theme: ReadingTheme) {
        _readingTheme.value = theme
    }

    fun zoomIn() {
        val newScale = (_zoomScale.value + 0.25f).coerceAtMost(3.0f)
        _zoomScale.value = newScale
    }

    fun zoomOut() {
        val newScale = (_zoomScale.value - 0.25f).coerceAtLeast(0.75f)
        _zoomScale.value = newScale
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
