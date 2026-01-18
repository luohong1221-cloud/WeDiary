package com.example.diary.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.example.diary.data.dao.DiaryDao
import com.example.diary.data.dao.ImageDao
import com.example.diary.data.dao.TagDao
import com.example.diary.data.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class DiaryRepository(
    private val diaryDao: DiaryDao,
    private val imageDao: ImageDao,
    private val tagDao: TagDao,
    private val context: Context
) {
    // Diary operations
    fun getAllDiariesWithDetails(): Flow<List<DiaryWithDetails>> =
        diaryDao.getAllDiariesWithDetails()

    suspend fun getDiaryWithDetailsById(diaryId: Long): DiaryWithDetails? =
        diaryDao.getDiaryWithDetailsById(diaryId)

    suspend fun getDiariesPaged(limit: Int, offset: Int): List<DiaryWithDetails> =
        diaryDao.getDiariesPaged(limit, offset)

    fun getDiariesBetween(startOfDay: Long, endOfDay: Long): Flow<List<DiaryWithDetails>> =
        diaryDao.getDiariesBetween(startOfDay, endOfDay)

    fun getFavoriteDiaries(): Flow<List<DiaryEntry>> =
        diaryDao.getFavoriteDiaries()

    fun searchDiaries(query: String): Flow<List<DiaryEntry>> {
        val searchQuery = query.trim().split(" ").joinToString(" ") { "$it*" }
        return diaryDao.searchDiaries(searchQuery)
    }

    fun getDatesWithEntries(): Flow<List<String>> =
        diaryDao.getDatesWithEntries()

    fun getDiaryCount(): Flow<Int> =
        diaryDao.getDiaryCount()

    suspend fun saveDiary(
        diary: DiaryEntry,
        imageUris: List<Uri> = emptyList(),
        tagIds: List<Long> = emptyList()
    ): Long {
        val diaryId = if (diary.id == 0L) {
            diaryDao.insertDiary(diary)
        } else {
            diaryDao.updateDiary(diary.copy(updatedAt = System.currentTimeMillis()))
            diary.id
        }

        // Handle images
        if (imageUris.isNotEmpty()) {
            val existingImages = imageDao.getImagesByDiaryIdSync(diaryId)
            val startOrder = existingImages.size

            imageUris.forEachIndexed { index, uri ->
                val savedPath = saveImageToPrivateStorage(uri)
                if (savedPath != null) {
                    val thumbnailPath = createThumbnail(savedPath)
                    val diaryImage = DiaryImage(
                        diaryId = diaryId,
                        imagePath = savedPath,
                        thumbnailPath = thumbnailPath,
                        sortOrder = startOrder + index
                    )
                    imageDao.insertImage(diaryImage)
                }
            }
        }

        // Handle tags
        if (tagIds.isNotEmpty()) {
            tagDao.setTagsForDiary(diaryId, tagIds)
        }

        return diaryId
    }

    suspend fun updateDiary(diary: DiaryEntry) {
        diaryDao.updateDiary(diary.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteDiary(diaryId: Long, permanent: Boolean = false) {
        if (permanent) {
            // Delete associated images from storage
            val images = imageDao.getImagesByDiaryIdSync(diaryId)
            images.forEach { image ->
                deleteImageFile(image.imagePath)
                image.thumbnailPath?.let { deleteImageFile(it) }
            }
            diaryDao.deleteDiaryPermanently(diaryId)
        } else {
            diaryDao.softDeleteDiary(diaryId)
        }
    }

    suspend fun toggleFavorite(diaryId: Long, isFavorite: Boolean) {
        diaryDao.updateFavoriteStatus(diaryId, isFavorite)
    }

    // Image operations
    fun getImagesByDiaryId(diaryId: Long): Flow<List<DiaryImage>> =
        imageDao.getImagesByDiaryId(diaryId)

    suspend fun addImageToDiary(diaryId: Long, uri: Uri): DiaryImage? {
        val savedPath = saveImageToPrivateStorage(uri) ?: return null
        val thumbnailPath = createThumbnail(savedPath)
        val currentCount = imageDao.getImageCountByDiaryId(diaryId)

        val diaryImage = DiaryImage(
            diaryId = diaryId,
            imagePath = savedPath,
            thumbnailPath = thumbnailPath,
            sortOrder = currentCount
        )
        val imageId = imageDao.insertImage(diaryImage)
        return diaryImage.copy(id = imageId)
    }

    suspend fun deleteImage(image: DiaryImage) {
        deleteImageFile(image.imagePath)
        image.thumbnailPath?.let { deleteImageFile(it) }
        imageDao.deleteImage(image)
    }

    suspend fun updateImagesSortOrder(images: List<DiaryImage>) {
        imageDao.updateImagesSortOrder(images)
    }

    // Tag operations
    fun getAllTags(): Flow<List<Tag>> = tagDao.getAllTags()

    suspend fun getAllTagsSync(): List<Tag> = tagDao.getAllTagsSync()

    suspend fun createTag(name: String, color: Long = 0xFF6200EE): Tag {
        val existingTag = tagDao.getTagByName(name)
        if (existingTag != null) return existingTag

        val tag = Tag(name = name, color = color)
        val tagId = tagDao.insertTag(tag)
        return tag.copy(id = tagId)
    }

    suspend fun updateTag(tag: Tag) {
        tagDao.updateTag(tag)
    }

    suspend fun deleteTag(tagId: Long) {
        tagDao.deleteTagById(tagId)
    }

    fun getTagsForDiary(diaryId: Long): Flow<List<Tag>> =
        tagDao.getTagsForDiary(diaryId)

    suspend fun setTagsForDiary(diaryId: Long, tagIds: List<Long>) {
        tagDao.setTagsForDiary(diaryId, tagIds)
    }

    fun getDiaryCountForTag(tagId: Long): Flow<Int> =
        tagDao.getDiaryCountForTag(tagId)

    // Private helper functions
    private suspend fun saveImageToPrivateStorage(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val fileName = "diary_${UUID.randomUUID()}.jpg"
            val imagesDir = File(context.filesDir, "diary_images").apply { mkdirs() }
            val outputFile = File(imagesDir, fileName)

            // Decode and compress the image
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (bitmap != null) {
                FileOutputStream(outputFile).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                }
                bitmap.recycle()
                outputFile.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun createThumbnail(imagePath: String): String? = withContext(Dispatchers.IO) {
        try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(imagePath, options)

            val targetWidth = 300
            val scaleFactor = options.outWidth / targetWidth

            options.apply {
                inJustDecodeBounds = false
                inSampleSize = scaleFactor.coerceAtLeast(1)
            }

            val bitmap = BitmapFactory.decodeFile(imagePath, options) ?: return@withContext null

            val thumbnailDir = File(context.filesDir, "diary_thumbnails").apply { mkdirs() }
            val thumbnailFile = File(thumbnailDir, "thumb_${File(imagePath).name}")

            FileOutputStream(thumbnailFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            }
            bitmap.recycle()

            thumbnailFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun deleteImageFile(path: String) {
        try {
            File(path).delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
