package com.example.diary.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class DiaryWithDetails(
    @Embedded
    val diary: DiaryEntry,

    @Relation(
        parentColumn = "id",
        entityColumn = "diaryId"
    )
    val images: List<DiaryImage>,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = DiaryTagCrossRef::class,
            parentColumn = "diaryId",
            entityColumn = "tagId"
        )
    )
    val tags: List<Tag>
)
