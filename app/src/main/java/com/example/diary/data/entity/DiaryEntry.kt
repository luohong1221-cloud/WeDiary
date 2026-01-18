package com.example.diary.data.entity

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val mood: Mood = Mood.NEUTRAL,
    val weather: Weather? = null,
    val location: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val isDeleted: Boolean = false
)

@Entity(tableName = "diary_entries_fts")
@Fts4(contentEntity = DiaryEntry::class)
data class DiaryEntryFts(
    val title: String,
    val content: String
)

enum class Mood(val emoji: String, val label: String) {
    VERY_HAPPY("\uD83D\uDE04", "Very Happy"),
    HAPPY("\uD83D\uDE0A", "Happy"),
    NEUTRAL("\uD83D\uDE10", "Neutral"),
    SAD("\uD83D\uDE1E", "Sad"),
    VERY_SAD("\uD83D\uDE2D", "Very Sad"),
    ANGRY("\uD83D\uDE20", "Angry"),
    ANXIOUS("\uD83D\uDE30", "Anxious"),
    TIRED("\uD83D\uDE2B", "Tired"),
    EXCITED("\uD83E\uDD29", "Excited"),
    PEACEFUL("\uD83D\uDE0C", "Peaceful")
}

enum class Weather(val icon: String, val label: String) {
    SUNNY("\u2600\uFE0F", "Sunny"),
    CLOUDY("\u2601\uFE0F", "Cloudy"),
    RAINY("\uD83C\uDF27\uFE0F", "Rainy"),
    SNOWY("\u2744\uFE0F", "Snowy"),
    WINDY("\uD83C\uDF2C\uFE0F", "Windy"),
    STORMY("\u26C8\uFE0F", "Stormy"),
    FOGGY("\uD83C\uDF2B\uFE0F", "Foggy")
}
