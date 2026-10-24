package com.romanurdu.pdfreader.data.dao

import androidx.room.*
import com.romanurdu.pdfreader.data.model.DictionaryCacheEntity

@Dao
interface DictionaryCacheDao {
    @Query("SELECT * FROM dictionary_cache WHERE LOWER(word) = LOWER(:word) LIMIT 1")
    suspend fun getCachedWord(word: String): DictionaryCacheEntity?

    @Query("SELECT * FROM dictionary_cache WHERE word LIKE '%' || :query || '%' OR romanUrduMeaning LIKE '%' || :query || '%' LIMIT 50")
    suspend fun searchCachedWords(query: String): List<DictionaryCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(entry: DictionaryCacheEntity)
}
