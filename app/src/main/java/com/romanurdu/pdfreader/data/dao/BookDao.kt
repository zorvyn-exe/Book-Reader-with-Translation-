package com.romanurdu.pdfreader.data.dao

import androidx.room.*
import com.romanurdu.pdfreader.data.model.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY lastOpenedTimestamp DESC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    suspend fun getBookById(id: Long): BookEntity?

    @Query("SELECT * FROM books WHERE uriString = :uri LIMIT 1")
    suspend fun getBookByUri(uri: String): BookEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    @Update
    suspend fun updateBook(book: BookEntity)

    @Query("UPDATE books SET currentPage = :page, scrollPercent = :scroll, lastOpenedTimestamp = :timestamp WHERE id = :id")
    suspend fun updateReadingProgress(id: Long, page: Int, scroll: Float, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE books SET isOcrNeeded = :ocrNeeded WHERE id = :id")
    suspend fun updateOcrStatus(id: Long, ocrNeeded: Boolean)

    @Delete
    suspend fun deleteBook(book: BookEntity)

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteBookById(id: Long)
}
