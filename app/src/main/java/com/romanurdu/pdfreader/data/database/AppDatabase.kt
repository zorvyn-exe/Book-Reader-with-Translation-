package com.romanurdu.pdfreader.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.romanurdu.pdfreader.data.dao.BookDao
import com.romanurdu.pdfreader.data.dao.BookmarkDao
import com.romanurdu.pdfreader.data.dao.DictionaryCacheDao
import com.romanurdu.pdfreader.data.dao.VocabularyDao
import com.romanurdu.pdfreader.data.model.BookEntity
import com.romanurdu.pdfreader.data.model.BookmarkEntity
import com.romanurdu.pdfreader.data.model.DictionaryCacheEntity
import com.romanurdu.pdfreader.data.model.VocabularyEntity

@Database(
    entities = [
        BookEntity::class,
        BookmarkEntity::class,
        VocabularyEntity::class,
        DictionaryCacheEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun vocabularyDao(): VocabularyDao
    abstract fun dictionaryCacheDao(): DictionaryCacheDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "roman_urdu_pdf_reader.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
