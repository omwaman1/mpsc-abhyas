package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "test_papers")
data class TestPaperEntity(
    @PrimaryKey val testId: String,
    val title: String,
    val category: String, // Full Mock Test, Subject Test, PYQ Paper, Speed Test
    val examType: String, // Rajyaseva, Combine B & C, Talathi & Group C
    val subjectName: String = "",
    val subjectId: Int = 0,
    val questionCount: Int,
    val totalMarks: Int,
    val durationMinutes: Int,
    val negativeMarking: Float = 0.25f, // e.g. 0.25 = 1/4th negative marking
    val attemptsCount: Int = 12450,
    val isFree: Boolean = true,
    val questionIdsJson: String // e.g. "1,2,3,4,5"
)
