package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val examType: String,
    val majorExamName: String = "",
    val minorExamName: String = "",
    val subject: String,
    val year: Int,
    val questionMarathi: String,
    val questionEnglish: String,
    val option1Marathi: String,
    val option1English: String,
    val option2Marathi: String,
    val option2English: String,
    val option3Marathi: String,
    val option3English: String,
    val option4Marathi: String,
    val option4English: String,
    val correctOption: Int, // 1, 2, 3, or 4
    val explanationMarathi: String,
    val explanationEnglish: String,
    val difficulty: String = "Medium",
    val tags: String = "",
    val isBookmarked: Boolean = false,
    val userNote: String = ""
)
