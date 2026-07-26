package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

// --- API Response Models ---

@JsonClass(generateAdapter = true)
data class ApiSubjectResponse(
    @Json(name = "status") val status: String,
    @Json(name = "total") val total: Int,
    @Json(name = "data") val data: List<ApiSubject>
)

@JsonClass(generateAdapter = true)
data class ApiSubject(
    @Json(name = "id") val id: String,
    @Json(name = "name_en") val nameEn: String,
    @Json(name = "name_mr") val nameMr: String,
    @Json(name = "pyq_count") val pyqCount: String
)

@JsonClass(generateAdapter = true)
data class ApiExamCategoryResponse(
    @Json(name = "status") val status: String,
    @Json(name = "total") val total: Int,
    @Json(name = "data") val data: List<ApiExamCategory>
)

@JsonClass(generateAdapter = true)
data class ApiExamCategory(
    @Json(name = "category_name") val categoryName: String,
    @Json(name = "pyq_count") val pyqCount: Int,
    @Json(name = "year_count") val yearCount: Int
)

@JsonClass(generateAdapter = true)
data class ApiExamYearResponse(
    @Json(name = "status") val status: String,
    @Json(name = "category") val category: String,
    @Json(name = "total") val total: Int,
    @Json(name = "data") val data: List<ApiExamYear>
)

@JsonClass(generateAdapter = true)
data class ApiExamYear(
    @Json(name = "year") val year: String,
    @Json(name = "raw_exam_name") val rawExamName: String,
    @Json(name = "pyq_count") val pyqCount: Int
)

@JsonClass(generateAdapter = true)
data class ApiHealthResponse(
    @Json(name = "status") val status: String,
    @Json(name = "message") val message: String
)

@JsonClass(generateAdapter = true)
data class ApiTopicResponse(
    @Json(name = "status") val status: String,
    @Json(name = "subject_id") val subjectId: Int,
    @Json(name = "total") val total: Int,
    @Json(name = "data") val data: List<ApiTopic>
)

@JsonClass(generateAdapter = true)
data class ApiTopic(
    @Json(name = "id") val id: String,
    @Json(name = "name_en") val nameEn: String,
    @Json(name = "name_mr") val nameMr: String,
    @Json(name = "parent_id") val parentId: String?,
    @Json(name = "sort_order") val sortOrder: String,
    @Json(name = "question_count") val questionCount: String
)

@JsonClass(generateAdapter = true)
data class ApiQuestionResponse(
    @Json(name = "status") val status: String,
    @Json(name = "total") val total: Int,
    @Json(name = "page") val page: Int,
    @Json(name = "limit") val limit: Int,
    @Json(name = "data") val data: List<ApiQuestion>
)

@JsonClass(generateAdapter = true)
data class ApiQuestion(
    @Json(name = "id") val id: String,
    @Json(name = "subject_id") val subjectId: String,
    @Json(name = "topic_id") val topicId: String,
    @Json(name = "question_mr") val questionMr: String?,
    @Json(name = "question_en") val questionEn: String?,
    @Json(name = "opt1_mr") val opt1Mr: String?,
    @Json(name = "opt2_mr") val opt2Mr: String?,
    @Json(name = "opt3_mr") val opt3Mr: String?,
    @Json(name = "opt4_mr") val opt4Mr: String?,
    @Json(name = "opt1_en") val opt1En: String?,
    @Json(name = "opt2_en") val opt2En: String?,
    @Json(name = "opt3_en") val opt3En: String?,
    @Json(name = "opt4_en") val opt4En: String?,
    @Json(name = "correct_answer") val correctAnswer: String,
    @Json(name = "solution_mr") val solutionMr: String?,
    @Json(name = "solution_en") val solutionEn: String?,
    @Json(name = "exam_name") val examName: String?
)

// --- Retrofit Service ---

interface MPSCApiService {
    @GET("get_subjects.php")
    suspend fun getSubjects(): ApiSubjectResponse

    @GET("get_exams.php")
    suspend fun getExams(): ApiExamCategoryResponse

    @GET("get_exam_years.php")
    suspend fun getExamYears(@Query("category") category: String): ApiExamYearResponse

    @GET("get_years.php")
    suspend fun getYears(): ApiYearCategoryResponse

    @GET("get_year_exams.php")
    suspend fun getYearExams(@Query("year") year: String): ApiYearExamsResponse

    @GET("health.php")
    suspend fun healthCheck(): ApiHealthResponse

    @GET("get_topics.php")
    suspend fun getTopics(@Query("subject_id") subjectId: Int): ApiTopicResponse

    @GET("get_questions.php")
    suspend fun getQuestions(
        @Query("topic_id") topicId: Int? = null,
        @Query("exam_name") examName: String? = null,
        @Query("exam_category") examCategory: String? = null,
        @Query("limit") limit: Int = 100,
        @Query("page") page: Int = 1
    ): ApiQuestionResponse
}

@JsonClass(generateAdapter = true)
data class ApiYearCategoryResponse(
    @Json(name = "status") val status: String,
    @Json(name = "total") val total: Int,
    @Json(name = "data") val data: List<ApiYearCategory>
)

@JsonClass(generateAdapter = true)
data class ApiYearCategory(
    @Json(name = "year") val year: String,
    @Json(name = "pyq_count") val pyqCount: Int,
    @Json(name = "exam_count") val examCount: Int
)

@JsonClass(generateAdapter = true)
data class ApiYearExamsResponse(
    @Json(name = "status") val status: String,
    @Json(name = "year") val year: String,
    @Json(name = "total") val total: Int,
    @Json(name = "data") val data: List<ApiYearExam>
)

@JsonClass(generateAdapter = true)
data class ApiYearExam(
    @Json(name = "raw_exam_name") val rawExamName: String,
    @Json(name = "category_name") val categoryName: String,
    @Json(name = "year") val year: String,
    @Json(name = "pyq_count") val pyqCount: Int
)
