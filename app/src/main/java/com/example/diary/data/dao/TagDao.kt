package com.example.diary.data.dao

import androidx.room.*
import com.example.diary.data.entity.DiaryTagCrossRef
import com.example.diary.data.entity.Tag
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: Tag): Long

    @Update
    suspend fun updateTag(tag: Tag)

    @Delete
    suspend fun deleteTag(tag: Tag)

    @Query("DELETE FROM tags WHERE id = :tagId")
    suspend fun deleteTagById(tagId: Long)

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<Tag>>

    @Query("SELECT * FROM tags ORDER BY name ASC")
    suspend fun getAllTagsSync(): List<Tag>

    @Query("SELECT * FROM tags WHERE id = :tagId")
    suspend fun getTagById(tagId: Long): Tag?

    @Query("SELECT * FROM tags WHERE name = :name")
    suspend fun getTagByName(name: String): Tag?

    // Diary-Tag relationship operations
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDiaryTagCrossRef(crossRef: DiaryTagCrossRef)

    @Delete
    suspend fun deleteDiaryTagCrossRef(crossRef: DiaryTagCrossRef)

    @Query("DELETE FROM diary_tag_cross_ref WHERE diaryId = :diaryId")
    suspend fun deleteAllTagsForDiary(diaryId: Long)

    @Query("DELETE FROM diary_tag_cross_ref WHERE diaryId = :diaryId AND tagId = :tagId")
    suspend fun removeTagFromDiary(diaryId: Long, tagId: Long)

    @Transaction
    suspend fun setTagsForDiary(diaryId: Long, tagIds: List<Long>) {
        deleteAllTagsForDiary(diaryId)
        tagIds.forEach { tagId ->
            insertDiaryTagCrossRef(DiaryTagCrossRef(diaryId, tagId))
        }
    }

    @Query("""
        SELECT tags.* FROM tags
        INNER JOIN diary_tag_cross_ref ON tags.id = diary_tag_cross_ref.tagId
        WHERE diary_tag_cross_ref.diaryId = :diaryId
        ORDER BY tags.name ASC
    """)
    fun getTagsForDiary(diaryId: Long): Flow<List<Tag>>

    @Query("""
        SELECT tags.* FROM tags
        INNER JOIN diary_tag_cross_ref ON tags.id = diary_tag_cross_ref.tagId
        WHERE diary_tag_cross_ref.diaryId = :diaryId
        ORDER BY tags.name ASC
    """)
    suspend fun getTagsForDiarySync(diaryId: Long): List<Tag>

    @Query("""
        SELECT COUNT(*) FROM diary_tag_cross_ref WHERE tagId = :tagId
    """)
    fun getDiaryCountForTag(tagId: Long): Flow<Int>
}
