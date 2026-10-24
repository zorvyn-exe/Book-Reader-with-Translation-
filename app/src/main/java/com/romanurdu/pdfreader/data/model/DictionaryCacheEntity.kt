package com.romanurdu.pdfreader.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Cached lookup for fast offline access to previously fetched online words
 */
@Entity(
    tableName = "dictionary_cache",
    indices = [Index(value = ["word"], unique = true)]
)
data class DictionaryCacheEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val word: String,
    val romanUrduMeaning: String,
    val englishDefinition: String = "",
    val pos: String = "",
    val source: String = "LOCAL",
    val cachedAt: Long = System.currentTimeMillis()
)
