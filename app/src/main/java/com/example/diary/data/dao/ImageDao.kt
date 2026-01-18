package com.example.diary.data.dao

import androidx.room.*
import com.example.diary.data.entity.DiaryImage
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: DiaryImage): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImages(images: List<DiaryImage>)

    @Update
    suspend fun updateImage(image: DiaryImage)

    @Delete
    suspend fun deleteImage(image: DiaryImage)

    @Query("DELETE FROM diary_images WHERE id = :imageId")
    suspend fun deleteImageById(imageId: Long)

    @Query("DELETE FROM diary_images WHERE diaryId = :diaryId")
    suspend fun deleteImagesByDiaryId(diaryId: Long)

    @Query("SELECT * FROM diary_images WHERE diaryId = :diaryId ORDER BY sortOrder ASC")
    fun getImagesByDiaryId(diaryId: Long): Flow<List<DiaryImage>>

    @Query("SELECT * FROM diary_images WHERE diaryId = :diaryId ORDER BY sortOrder ASC")
    suspend fun getImagesByDiaryIdSync(diaryId: Long): List<DiaryImage>

    @Query("SELECT * FROM diary_images WHERE id = :imageId")
    suspend fun getImageById(imageId: Long): DiaryImage?

    @Query("UPDATE diary_images SET sortOrder = :sortOrder WHERE id = :imageId")
    suspend fun updateImageSortOrder(imageId: Long, sortOrder: Int)

    @Transaction
    suspend fun updateImagesSortOrder(images: List<DiaryImage>) {
        images.forEachIndexed { index, image ->
            updateImageSortOrder(image.id, index)
        }
    }

    @Query("SELECT COUNT(*) FROM diary_images WHERE diaryId = :diaryId")
    suspend fun getImageCountByDiaryId(diaryId: Long): Int
}
