package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "test_attempts")
data class TestAttemptEntity(
    @PrimaryKey(autoGenerate = true) val attemptId: Long = 0,
    val testId: String,
    val testTitle: String,
    val score: Float,
    val totalMarks: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val unattemptedCount: Int,
    val accuracyPercentage: Float,
    val timeTakenSeconds: Int,
    val userAnswersJson: String, // JSON like {"1": 2, "2": 4}
    val timestamp: Long = System.currentTimeMillis()
)
