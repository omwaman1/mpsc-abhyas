package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.QuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions ORDER BY year DESC, id ASC")
    fun getAllQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE examType = :examType ORDER BY year DESC")
    fun getQuestionsByExamType(examType: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE subject = :subject ORDER BY year DESC")
    fun getQuestionsBySubject(subject: String): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE isBookmarked = 1 ORDER BY id DESC")
    fun getBookmarkedQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE id IN (:ids)")
    suspend fun getQuestionsByIds(ids: List<Int>): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE id = :id")
    suspend fun getQuestionById(id: Int): QuestionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Update
    suspend fun updateQuestion(question: QuestionEntity)

    @Query("UPDATE questions SET isBookmarked = :isBookmarked, userNote = :userNote WHERE id = :id")
    suspend fun updateBookmarkState(id: Int, isBookmarked: Boolean, userNote: String)

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getQuestionCount(): Int
}
