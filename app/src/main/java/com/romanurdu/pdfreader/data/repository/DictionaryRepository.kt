package com.romanurdu.pdfreader.data.repository

import com.romanurdu.pdfreader.data.dao.VocabularyDao
import com.romanurdu.pdfreader.data.model.VocabularyEntity
import com.romanurdu.pdfreader.data.model.WordLookupResult
import com.romanurdu.pdfreader.dictionary.DictionaryEngine
import kotlinx.coroutines.flow.Flow

class DictionaryRepository(
    private val dictionaryEngine: DictionaryEngine,
    private val vocabularyDao: VocabularyDao
) {
    val allVocabulary: Flow<List<VocabularyEntity>> = vocabularyDao.getAllVocabulary()
    val favoriteVocabulary: Flow<List<VocabularyEntity>> = vocabularyDao.getFavoriteVocabulary()

    fun searchVocabulary(query: String): Flow<List<VocabularyEntity>> = vocabularyDao.searchVocabulary(query)

    suspend fun lookupWord(word: String, sentence: String?): WordLookupResult {
        return dictionaryEngine.lookupWord(word, sentence)
    }

    suspend fun lookupOnline(word: String): WordLookupResult {
        return dictionaryEngine.lookupOnline(word)
    }

    suspend fun searchLocalDictionary(query: String): List<Pair<String, String>> {
        return dictionaryEngine.searchLocal(query)
    }

    suspend fun saveWord(
        word: String,
        meaning: String,
        sentence: String = "",
        bookTitle: String = "",
        pageNumber: Int = 1
    ): Long {
        return vocabularyDao.insertVocabulary(
            VocabularyEntity(
                word = word,
                romanUrduMeaning = meaning,
                contextSentence = sentence,
                bookTitle = bookTitle,
                pageNumber = pageNumber
            )
        )
    }

    suspend fun toggleFavorite(vocab: VocabularyEntity) {
        vocabularyDao.updateVocabulary(vocab.copy(isFavorite = !vocab.isFavorite))
    }

    suspend fun deleteVocabulary(vocab: VocabularyEntity) {
        vocabularyDao.deleteVocabulary(vocab)
    }

    suspend fun isWordSaved(word: String): Boolean {
        return vocabularyDao.isWordSaved(word)
    }
}
