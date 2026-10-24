package com.romanurdu.pdfreader.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents an imported PDF book in user library
 */
@Entity(
    tableName = "books",
    indices = [Index(value = ["uriString"], unique = true)]
)
data class BookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val uriString: String,
    val localFilePath: String,
    val fileSizeBytes: Long = 0L,
    val pageCount: Int = 0,
    val currentPage: Int = 1,
    val scrollPercent: Float = 0f,
    val lastOpenedTimestamp: Long = System.currentTimeMillis(),
    val isOcrNeeded: Boolean = false,
    val coverPath: String? = null
)
