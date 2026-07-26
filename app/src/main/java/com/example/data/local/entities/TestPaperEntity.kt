package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "test_papers")
data class TestPaperEntity(
    @PrimaryKey val testId: String,
    val title: String,
    val category: String, // Full Mock Test, Subject Test, PYQ Paper, Speed Test
    val examType: String, // Rajyaseva, Combine B & C, Subordinate
    val questionCount: Int,
    val totalMarks: Int,
    val durationMinutes: Int,
    val attemptsCount: Int = 12450,
    val isFree: Boolean = true,
    val questionIdsJson: String // e.g. "1,2,3,4,5"
)
