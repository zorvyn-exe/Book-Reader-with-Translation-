package com.romanurdu.pdfreader.data.repository

import android.content.Context
import android.net.Uri
import com.romanurdu.pdfreader.data.dao.BookDao
import com.romanurdu.pdfreader.data.dao.BookmarkDao
import com.romanurdu.pdfreader.data.model.BookEntity
import com.romanurdu.pdfreader.data.model.BookmarkEntity
import com.romanurdu.pdfreader.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class BookRepository(
    private val context: Context,
    private val bookDao: BookDao,
    private val bookmarkDao: BookmarkDao
) {
    val allBooks: Flow<List<BookEntity>> = bookDao.getAllBooks()

    suspend fun getBookById(id: Long): BookEntity? = withContext(Dispatchers.IO) {
        bookDao.getBookById(id)
    }

    suspend fun importPdf(uri: Uri): Result<BookEntity> = withContext(Dispatchers.IO) {
        try {
            // Check if already imported
            val existing = bookDao.getBookByUri(uri.toString())
            if (existing != null) {
                return@withContext Result.success(existing)
            }

            // Copy to app internal storage for reliable offline access & persistent SAF security
            val importedFile = FileUtils.copyUriToAppStorage(context, uri)
                ?: return@withContext Result.failure(Exception("Could not read PDF from device storage"))

            val title = FileUtils.getFileName(context, uri) ?: importedFile.nameWithoutExtension

            val book = BookEntity(
                title = title,
                uriString = uri.toString(),
                localFilePath = importedFile.absolutePath,
                fileSizeBytes = importedFile.length(),
                pageCount = 1,
                currentPage = 1,
                lastOpenedTimestamp = System.currentTimeMillis()
            )

            val newId = bookDao.insertBook(book)
            Result.success(book.copy(id = newId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProgress(bookId: Long, page: Int, scrollPercent: Float) = withContext(Dispatchers.IO) {
        bookDao.updateReadingProgress(bookId, page, scrollPercent)
    }

    suspend fun markScannedPdf(bookId: Long, isScanned: Boolean) = withContext(Dispatchers.IO) {
        bookDao.updateOcrStatus(bookId, isScanned)
    }

    suspend fun deleteBook(book: BookEntity) = withContext(Dispatchers.IO) {
        // Delete local copy
        try {
            val file = File(book.localFilePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        bookDao.deleteBook(book)
    }

    fun getBookmarks(bookId: Long): Flow<List<BookmarkEntity>> = bookmarkDao.getBookmarksForBook(bookId)

    suspend fun toggleBookmark(bookId: Long, pageNumber: Int, snippet: String = "") = withContext(Dispatchers.IO) {
        val isBookmarked = bookmarkDao.isPageBookmarked(bookId, pageNumber)
        if (isBookmarked) {
            bookmarkDao.deleteBookmarkForPage(bookId, pageNumber)
        } else {
            bookmarkDao.insertBookmark(
                BookmarkEntity(
                    bookId = bookId,
                    pageNumber = pageNumber,
                    title = "Page $pageNumber",
                    snippet = snippet
                )
            )
        }
    }

    suspend fun isPageBookmarked(bookId: Long, pageNumber: Int): Boolean = withContext(Dispatchers.IO) {
        bookmarkDao.isPageBookmarked(bookId, pageNumber)
    }
}
