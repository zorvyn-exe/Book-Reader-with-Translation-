package com.romanurdu.pdfreader.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Saved vocabulary word with Roman Urdu meaning and sentence context
 */
@Entity(
    tableName = "vocabulary",
    indices = [Index(value = ["word"])]
)
data class VocabularyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val word: String,
    val romanUrduMeaning: String,
    val englishDefinition: String = "",
    val contextSentence: String = "",
    val bookTitle: String = "",
    val pageNumber: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
