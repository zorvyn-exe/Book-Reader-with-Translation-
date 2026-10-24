package com.romanurdu.pdfreader.data.dao

import androidx.room.*
import com.romanurdu.pdfreader.data.model.VocabularyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyDao {
    @Query("SELECT * FROM vocabulary ORDER BY createdAt DESC")
    fun getAllVocabulary(): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabulary WHERE word LIKE '%' || :query || '%' OR romanUrduMeaning LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchVocabulary(query: String): Flow<List<VocabularyEntity>>

    @Query("SELECT * FROM vocabulary WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteVocabulary(): Flow<List<VocabularyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVocabulary(vocab: VocabularyEntity): Long

    @Update
    suspend fun updateVocabulary(vocab: VocabularyEntity)

    @Delete
    suspend fun deleteVocabulary(vocab: VocabularyEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM vocabulary WHERE LOWER(word) = LOWER(:word))")
    suspend fun isWordSaved(word: String): Boolean
}
