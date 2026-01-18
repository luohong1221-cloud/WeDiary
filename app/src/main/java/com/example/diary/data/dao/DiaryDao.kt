package com.example.diary.data.dao

import androidx.room.*
import com.example.diary.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {

    // DiaryEntry operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiary(diary: DiaryEntry): Long

    @Update
    suspend fun updateDiary(diary: DiaryEntry)

    @Query("UPDATE diary_entries SET isDeleted = 1, updatedAt = :updatedAt WHERE id = :diaryId")
    suspend fun softDeleteDiary(diaryId: Long, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM diary_entries WHERE id = :diaryId")
    suspend fun deleteDiaryPermanently(diaryId: Long)

    @Query("SELECT * FROM diary_entries WHERE id = :diaryId AND isDeleted = 0")
    suspend fun getDiaryById(diaryId: Long): DiaryEntry?

    @Transaction
    @Query("SELECT * FROM diary_entries WHERE id = :diaryId AND isDeleted = 0")
    suspend fun getDiaryWithDetailsById(diaryId: Long): DiaryWithDetails?

    @Transaction
    @Query("SELECT * FROM diary_entries WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllDiariesWithDetails(): Flow<List<DiaryWithDetails>>

    @Transaction
    @Query("SELECT * FROM diary_entries WHERE isDeleted = 0 ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getDiariesPaged(limit: Int, offset: Int): List<DiaryWithDetails>

    @Query("SELECT * FROM diary_entries WHERE isDeleted = 0 AND date(createdAt/1000, 'unixepoch', 'localtime') = date(:timestamp/1000, 'unixepoch', 'localtime') ORDER BY createdAt DESC")
    fun getDiariesByDate(timestamp: Long): Flow<List<DiaryEntry>>

    @Transaction
    @Query("SELECT * FROM diary_entries WHERE isDeleted = 0 AND createdAt BETWEEN :startOfDay AND :endOfDay ORDER BY createdAt DESC")
    fun getDiariesBetween(startOfDay: Long, endOfDay: Long): Flow<List<DiaryWithDetails>>

    @Query("SELECT * FROM diary_entries WHERE isDeleted = 0 AND isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteDiaries(): Flow<List<DiaryEntry>>

    @Query("UPDATE diary_entries SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :diaryId")
    suspend fun updateFavoriteStatus(diaryId: Long, isFavorite: Boolean, updatedAt: Long = System.currentTimeMillis())

    // Full-text search
    @Query("""
        SELECT diary_entries.* FROM diary_entries
        JOIN diary_entries_fts ON diary_entries.id = diary_entries_fts.rowid
        WHERE diary_entries_fts MATCH :query AND diary_entries.isDeleted = 0
        ORDER BY diary_entries.createdAt DESC
    """)
    fun searchDiaries(query: String): Flow<List<DiaryEntry>>

    // Get dates that have diary entries (for calendar view)
    @Query("SELECT DISTINCT date(createdAt/1000, 'unixepoch', 'localtime') as diaryDate FROM diary_entries WHERE isDeleted = 0")
    fun getDatesWithEntries(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM diary_entries WHERE isDeleted = 0")
    fun getDiaryCount(): Flow<Int>
}
