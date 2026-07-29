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

import com.example.data.remote.RetrofitClient

class MPSCRepository(private val db: AppDatabase) {

    val allQuestions: Flow<List<QuestionEntity>> = db.questionDao().getAllQuestions()
    val bookmarkedQuestions: Flow<List<QuestionEntity>> = db.questionDao().getBookmarkedQuestions()
    val allTestPapers: Flow<List<TestPaperEntity>> = db.testPaperDao().getAllTestPapers()
    val allAttempts: Flow<List<TestAttemptEntity>> = db.testAttemptDao().getAllAttempts()
    val allCurrentAffairs: Flow<List<CurrentAffairsEntity>> = db.currentAffairsDao().getAllCurrentAffairs()

    suspend fun checkAndSeedDatabase() = withContext(Dispatchers.IO) {
        // Fast, lightweight startup: Only fetch test paper metadata.
        // Questions are fetched lazily on-demand when user opens a specific paper!
        fetchAndSyncTests()
    }

    suspend fun fetchAndSyncQuestions() = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiService.getQuestions(limit = 100)
            if (response.status == "success" && response.data.isNotEmpty()) {
                val questionEntities = response.data.mapIndexed { index, apiQ ->
                    val qId = apiQ.id?.toIntOrNull() ?: (index + 100)
                    QuestionEntity(
                        id = qId,
                        examType = apiQ.examName.takeIf { !it.isNullOrBlank() } ?: "Rajyaseva Prelims",
                        majorExamName = apiQ.majorExamName ?: "",
                        minorExamName = apiQ.minorExamName ?: "",
                        subject = "General Studies",
                        year = apiQ.examYear ?: 2024,
                        questionMarathi = apiQ.questionMr.takeIf { !it.isNullOrBlank() } ?: apiQ.questionEn ?: "प्रश्न $qId",
                        questionEnglish = apiQ.questionEn.takeIf { !it.isNullOrBlank() } ?: apiQ.questionMr ?: "Question $qId",
                        option1Marathi = apiQ.opt1Mr.takeIf { !it.isNullOrBlank() } ?: apiQ.opt1En ?: "पर्याय १",
                        option1English = apiQ.opt1En.takeIf { !it.isNullOrBlank() } ?: apiQ.opt1Mr ?: "Option 1",
                        option2Marathi = apiQ.opt2Mr.takeIf { !it.isNullOrBlank() } ?: apiQ.opt2En ?: "पर्याय २",
                        option2English = apiQ.opt2En.takeIf { !it.isNullOrBlank() } ?: apiQ.opt2Mr ?: "Option 2",
                        option3Marathi = apiQ.opt3Mr.takeIf { !it.isNullOrBlank() } ?: apiQ.opt3En ?: "पर्याय ३",
                        option3English = apiQ.opt3En.takeIf { !it.isNullOrBlank() } ?: apiQ.opt3Mr ?: "Option 3",
                        option4Marathi = apiQ.opt4Mr.takeIf { !it.isNullOrBlank() } ?: apiQ.opt4En ?: "पर्याय ४",
                        option4English = apiQ.opt4En.takeIf { !it.isNullOrBlank() } ?: apiQ.opt4Mr ?: "Option 4",
                        correctOption = apiQ.correctAnswer?.toIntOrNull() ?: 1,
                        explanationMarathi = apiQ.solutionMr.takeIf { !it.isNullOrBlank() } ?: "स्पष्टीकरण उपलब्ध आहे.",
                        explanationEnglish = apiQ.solutionEn.takeIf { !it.isNullOrBlank() } ?: "Explanation available.",
                        difficulty = "Medium",
                        tags = "MPSC, Test Series"
                    )
                }
                db.questionDao().insertQuestions(questionEntities)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun fetchAndSyncMissingQuestions(ids: List<Int>) = withContext(Dispatchers.IO) {
        if (ids.isEmpty()) return@withContext
        try {
            val idsStr = ids.take(200).joinToString(",")
            val response = RetrofitClient.apiService.getQuestions(ids = idsStr, limit = 200)
            if (response.status == "success" && response.data.isNotEmpty()) {
                val questionEntities = response.data.mapNotNull { apiQ ->
                    val qId = apiQ.id?.toIntOrNull() ?: return@mapNotNull null
                    QuestionEntity(
                        id = qId,
                        examType = apiQ.examName.takeIf { !it.isNullOrBlank() } ?: "Combine Group B & C",
                        majorExamName = apiQ.majorExamName ?: "",
                        minorExamName = apiQ.minorExamName ?: "",
                        subject = "General Studies",
                        year = apiQ.examYear ?: 2022,
                        questionMarathi = apiQ.questionMr.takeIf { !it.isNullOrBlank() } ?: apiQ.questionEn ?: "प्रश्न $qId",
                        questionEnglish = apiQ.questionEn.takeIf { !it.isNullOrBlank() } ?: apiQ.questionMr ?: "Question $qId",
                        option1Marathi = apiQ.opt1Mr.takeIf { !it.isNullOrBlank() } ?: apiQ.opt1En ?: "पर्याय १",
                        option1English = apiQ.opt1En.takeIf { !it.isNullOrBlank() } ?: apiQ.opt1Mr ?: "Option 1",
                        option2Marathi = apiQ.opt2Mr.takeIf { !it.isNullOrBlank() } ?: apiQ.opt2En ?: "पर्याय २",
                        option2English = apiQ.opt2En.takeIf { !it.isNullOrBlank() } ?: apiQ.opt2Mr ?: "Option 2",
                        option3Marathi = apiQ.opt3Mr.takeIf { !it.isNullOrBlank() } ?: apiQ.opt3En ?: "पर्याय ३",
                        option3English = apiQ.opt3En.takeIf { !it.isNullOrBlank() } ?: apiQ.opt3Mr ?: "Option 3",
                        option4Marathi = apiQ.opt4Mr.takeIf { !it.isNullOrBlank() } ?: apiQ.opt4En ?: "पर्याय ४",
                        option4English = apiQ.opt4En.takeIf { !it.isNullOrBlank() } ?: apiQ.opt4Mr ?: "Option 4",
                        correctOption = apiQ.correctAnswer?.toIntOrNull() ?: 1,
                        explanationMarathi = apiQ.solutionMr.takeIf { !it.isNullOrBlank() } ?: "स्पष्टीकरण उपलब्ध आहे.",
                        explanationEnglish = apiQ.solutionEn.takeIf { !it.isNullOrBlank() } ?: "Explanation available.",
                        difficulty = "Medium",
                        tags = "MPSC, Test Series"
                    )
                }
                if (questionEntities.isNotEmpty()) {
                    db.questionDao().insertQuestions(questionEntities)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun fetchAndSyncTests() = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiService.getTests()
            if (response.status == "success" && response.testSeries.isNotEmpty()) {
                val remoteTests = mutableListOf<TestPaperEntity>()

                response.testSeries.forEach { ts ->
                    val qIds = ts.questionIds ?: ""
                    val parsedQCount = ts.questionCount ?: 0
                    val qCount = if (parsedQCount > 0) parsedQCount else qIds.split(",").filter { it.isNotBlank() }.size
                    val finalCount = if (qCount > 0) qCount else 100
                    val duration = ts.durationMinutes ?: (if (finalCount >= 100) 60 else 30)
                    val marks = ts.totalMarks ?: finalCount
                    val negative = ts.negativeMarking ?: 0.25f

                    remoteTests.add(
                        TestPaperEntity(
                            testId = "TS_${ts.id}",
                            title = ts.title,
                            category = if (!ts.category.isNullOrBlank()) ts.category else "PYQ",
                            examType = if (!ts.examName.isNullOrBlank()) ts.examName else "MPSC Group C",
                            subjectName = ts.subjectName ?: "",
                            subjectId = ts.subjectId ?: 0,
                            questionCount = finalCount,
                            totalMarks = marks,
                            durationMinutes = duration,
                            negativeMarking = negative,
                            attemptsCount = 5000 + (ts.id.hashCode() % 5000).let { if (it < 0) -it else it },
                            isFree = true,
                            questionIdsJson = qIds
                        )
                    )
                }

                if (remoteTests.isNotEmpty()) {
                    db.testPaperDao().deleteAllTestPapers()
                    db.testPaperDao().insertTestPapers(remoteTests)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
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

    suspend fun saveQuestion(question: QuestionEntity) = withContext(Dispatchers.IO) {
        db.questionDao().insertQuestions(listOf(question))
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

    suspend fun reportQuestion(questionId: Int, userEmail: String, reportType: String, comment: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.apiService.reportQuestion(
                questionId = questionId,
                userEmail = userEmail,
                reportType = reportType,
                comment = comment
            )
            response.status == "success"
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun clearAllLocalData() = withContext(Dispatchers.IO) {
        try {
            db.clearAllTables()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
