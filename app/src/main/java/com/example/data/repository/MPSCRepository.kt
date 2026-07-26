package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.MPSCInitialData
import com.example.data.local.entities.CurrentAffairsEntity
import com.example.data.local.entities.QuestionEntity
import com.example.data.local.entities.TestAttemptEntity
import com.example.data.local.entities.TestPaperEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MPSCRepository(private val db: AppDatabase) {

    val allQuestions: Flow<List<QuestionEntity>> = db.questionDao().getAllQuestions()
    val bookmarkedQuestions: Flow<List<QuestionEntity>> = db.questionDao().getBookmarkedQuestions()
    val allTestPapers: Flow<List<TestPaperEntity>> = db.testPaperDao().getAllTestPapers()
    val allAttempts: Flow<List<TestAttemptEntity>> = db.testAttemptDao().getAllAttempts()
    val allCurrentAffairs: Flow<List<CurrentAffairsEntity>> = db.currentAffairsDao().getAllCurrentAffairs()

    suspend fun checkAndSeedDatabase() = withContext(Dispatchers.IO) {
        val qCount = db.questionDao().getQuestionCount()
        if (qCount == 0) {
            db.questionDao().insertQuestions(MPSCInitialData.getInitialQuestions())
        }

        val testCount = db.testPaperDao().getTestPaperCount()
        if (testCount == 0) {
            db.testPaperDao().insertTestPapers(MPSCInitialData.getInitialTestPapers())
        }

        val caCount = db.currentAffairsDao().getCount()
        if (caCount == 0) {
            db.currentAffairsDao().insertCurrentAffairs(MPSCInitialData.getInitialCurrentAffairs())
        }
    }

    fun getQuestionsByExamType(examType: String): Flow<List<QuestionEntity>> {
        return db.questionDao().getQuestionsByExamType(examType)
    }

    fun getQuestionsBySubject(subject: String): Flow<List<QuestionEntity>> {
        return db.questionDao().getQuestionsBySubject(subject)
    }

    suspend fun getQuestionsByIds(ids: List<Int>): List<QuestionEntity> = withContext(Dispatchers.IO) {
        db.questionDao().getQuestionsByIds(ids)
    }

    suspend fun getTestPaperById(testId: String): TestPaperEntity? = withContext(Dispatchers.IO) {
        db.testPaperDao().getTestPaperById(testId)
    }

    suspend fun toggleBookmark(id: Int, isBookmarked: Boolean, note: String = "") = withContext(Dispatchers.IO) {
        db.questionDao().updateBookmarkState(id, isBookmarked, note)
    }

    suspend fun saveTestAttempt(attempt: TestAttemptEntity): Long = withContext(Dispatchers.IO) {
        db.testAttemptDao().insertAttempt(attempt)
    }

    suspend fun getAttemptById(attemptId: Long): TestAttemptEntity? = withContext(Dispatchers.IO) {
        db.testAttemptDao().getAttemptById(attemptId)
    }
}
