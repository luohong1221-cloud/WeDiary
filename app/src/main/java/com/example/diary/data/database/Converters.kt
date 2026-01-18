package com.example.diary.data.database

import androidx.room.TypeConverter
import com.example.diary.data.entity.Mood
import com.example.diary.data.entity.Weather

class Converters {

    @TypeConverter
    fun fromMood(mood: Mood): String = mood.name

    @TypeConverter
    fun toMood(value: String): Mood = Mood.valueOf(value)

    @TypeConverter
    fun fromWeather(weather: Weather?): String? = weather?.name

    @TypeConverter
    fun toWeather(value: String?): Weather? = value?.let { Weather.valueOf(it) }
}
