package com.example.diary.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.diary.data.dao.DiaryDao
import com.example.diary.data.dao.ImageDao
import com.example.diary.data.dao.TagDao
import com.example.diary.data.entity.*

@Database(
    entities = [
        DiaryEntry::class,
        DiaryEntryFts::class,
        DiaryImage::class,
        Tag::class,
        DiaryTagCrossRef::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class DiaryDatabase : RoomDatabase() {

    abstract fun diaryDao(): DiaryDao
    abstract fun imageDao(): ImageDao
    abstract fun tagDao(): TagDao

    companion object {
        private const val DATABASE_NAME = "diary_database"

        @Volatile
        private var INSTANCE: DiaryDatabase? = null

        fun getInstance(context: Context): DiaryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DiaryDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
