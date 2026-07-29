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
    @Json(name = "id") val id: Any? = 0,
    @Json(name = "parent_id") val parentId: Any? = 0,
    @Json(name = "category_name") val categoryName: String = "",
    @Json(name = "pyq_count") val pyqCount: Any? = 0,
    @Json(name = "year_count") val yearCount: Any? = 0
)

@JsonClass(generateAdapter = true)
data class ApiExamYearResponse(
    @Json(name = "status") val status: String = "error",
    @Json(name = "category") val category: String? = "",
    @Json(name = "total") val total: Any? = 0,
    @Json(name = "data") val data: List<ApiExamYear> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ApiExamYear(
    @Json(name = "year") val year: String = "2024",
    @Json(name = "raw_exam_name") val rawExamName: String = "",
    @Json(name = "pyq_count") val pyqCount: Any? = 0
)

@JsonClass(generateAdapter = true)
data class ApiHealthResponse(
    @Json(name = "status") val status: String = "",
    @Json(name = "message") val message: String = ""
)

@JsonClass(generateAdapter = true)
data class ApiTopicResponse(
    @Json(name = "status") val status: String = "",
    @Json(name = "subject_id") val subjectId: Any? = null,
    @Json(name = "total") val total: Any? = null,
    @Json(name = "data") val data: List<ApiTopic> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ApiTopic(
    @Json(name = "id") val id: String = "",
    @Json(name = "name_en") val nameEn: String = "",
    @Json(name = "name_mr") val nameMr: String = "",
    @Json(name = "parent_id") val parentId: String? = null,
    @Json(name = "sort_order") val sortOrder: String = "0",
    @Json(name = "question_count") val questionCount: String = "0"
)

@JsonClass(generateAdapter = true)
data class ApiQuestionResponse(
    @Json(name = "status") val status: String = "error",
    @Json(name = "total") val total: Any? = 0,
    @Json(name = "page") val page: Any? = 1,
    @Json(name = "limit") val limit: Any? = 100,
    @Json(name = "data") val data: List<ApiQuestion> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ApiQuestion(
    @Json(name = "id") val id: String? = null,
    @Json(name = "subject_id") val subjectId: String? = null,
    @Json(name = "topic_id") val topicId: String? = null,
    @Json(name = "major_exam_name") val majorExamName: String? = null,
    @Json(name = "minor_exam_name") val minorExamName: String? = null,
    @Json(name = "question_mr") val questionMr: String? = null,
    @Json(name = "question_en") val questionEn: String? = null,
    @Json(name = "opt1_mr") val opt1Mr: String? = null,
    @Json(name = "opt2_mr") val opt2Mr: String? = null,
    @Json(name = "opt3_mr") val opt3Mr: String? = null,
    @Json(name = "opt4_mr") val opt4Mr: String? = null,
    @Json(name = "opt1_en") val opt1En: String? = null,
    @Json(name = "opt2_en") val opt2En: String? = null,
    @Json(name = "opt3_en") val opt3En: String? = null,
    @Json(name = "opt4_en") val opt4En: String? = null,
    @Json(name = "correct_answer") val correctAnswer: String? = null,
    @Json(name = "solution_mr") val solutionMr: String? = null,
    @Json(name = "solution_en") val solutionEn: String? = null,
    @Json(name = "exam_name") val examName: String? = null,
    @Json(name = "exam_year") val examYear: Any? = null
)

// --- Retrofit Service ---

interface MPSCApiService {
    @GET("api.php?action=get_subjects")
    suspend fun getSubjects(): ApiSubjectResponse

    @GET("api.php?action=get_exams")
    suspend fun getExams(): ApiExamCategoryResponse

    @GET("api.php?action=get_exam_years")
    suspend fun getExamYears(@Query("category") category: String): ApiExamYearResponse

    @GET("api.php?action=get_years")
    suspend fun getYears(): ApiYearCategoryResponse

    @GET("api.php?action=get_year_exams")
    suspend fun getYearExams(@Query("year") year: String): ApiYearExamsResponse

    @GET("api.php?action=health")
    suspend fun healthCheck(): ApiHealthResponse

    @GET("api.php?action=get_topics")
    suspend fun getTopics(@Query("subject_id") subjectId: Any): ApiTopicResponse

    @GET("api.php?action=get_questions")
    suspend fun getQuestions(
        @Query("topic_id") topicId: Any? = null,
        @Query("exam_name") examName: String? = null,
        @Query("exam_category") examCategory: String? = null,
        @Query("exam_year") examYear: Any? = null,
        @Query("ids") ids: String? = null,
        @Query("limit") limit: Int = 50000,
        @Query("page") page: Int = 1
    ): ApiQuestionResponse

    @GET("api.php?action=get_tests")
    suspend fun getTests(): ApiTestsResponse

    @GET("get_syllabus.php")
    suspend fun getSyllabus(): ApiSyllabusResponse

    @GET("api.php?action=register_user")
    suspend fun registerUser(
        @Query("full_name") fullName: String,
        @Query("phone_number") phone: String,
        @Query("email") email: String,
        @Query("google_id") googleId: String = ""
    ): ApiHealthResponse

    @GET("api.php?action=update_profile_name")
    suspend fun updateProfileName(
        @Query("email") email: String,
        @Query("full_name") fullName: String
    ): ApiHealthResponse

    @GET("api.php?action=get_notifications")
    suspend fun getNotifications(): ApiNotificationResponse

    @GET("api.php?action=get_subscription_plans")
    suspend fun getSubscriptionPlans(): ApiSubscriptionPlansResponse

    @GET("api.php?action=report_question")
    suspend fun reportQuestion(
        @Query("question_id") questionId: Int,
        @Query("user_email") userEmail: String,
        @Query("report_type") reportType: String,
        @Query("comment") comment: String
    ): ApiHealthResponse

    @GET("api.php?action=check_subscription_status")
    suspend fun checkSubscriptionStatus(
        @Query("email") email: String
    ): ApiSubscriptionStatusResponse

    @GET("api.php?action=create_razorpay_order")
    suspend fun createRazorpayOrder(
        @Query("email") email: String,
        @Query("plan_id") planId: String
    ): ApiRazorpayOrderResponse

    @GET("api.php?action=verify_payment")
    suspend fun verifyPayment(
        @Query("email") email: String,
        @Query("plan_id") planId: String,
        @Query("payment_id") paymentId: String,
        @Query("order_id") orderId: String,
        @Query("signature") signature: String = ""
    ): ApiHealthResponse

    @GET("api.php?action=check_user")
    suspend fun checkUser(
        @Query("email") email: String,
        @Query("google_id") googleId: String = ""
    ): ApiCheckUserResponse

    @retrofit2.http.FormUrlEncoded
    @retrofit2.http.POST("api.php?action=upload_user_contacts")
    suspend fun uploadUserContacts(
        @retrofit2.http.Field("email") email: String,
        @retrofit2.http.Field("contacts") contactsJson: String
    ): ApiHealthResponse
}

@JsonClass(generateAdapter = true)
data class ApiCheckUserResponse(
    @Json(name = "status") val status: String = "error",
    @Json(name = "exists") val exists: Boolean = false,
    @Json(name = "user") val user: ApiUserData? = null
)

@JsonClass(generateAdapter = true)
data class ApiUserData(
    @Json(name = "userID") val userID: Any? = null,
    @Json(name = "fullName") val fullName: String? = null,
    @Json(name = "phoneNumber") val phoneNumber: String? = null,
    @Json(name = "mobile") val mobile: String? = null,
    @Json(name = "email") val email: String? = null
)

@JsonClass(generateAdapter = true)
data class ApiSyllabusResponse(
    @Json(name = "status") val status: String = "error",
    @Json(name = "data") val data: List<ApiSyllabusItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ApiSyllabusItem(
    @Json(name = "id") val id: Int = 0,
    @Json(name = "exam_name") val examName: String = "",
    @Json(name = "pattern_mr") val patternMr: String? = "",
    @Json(name = "pattern_en") val patternEn: String? = "",
    @Json(name = "syllabus_mr") val syllabusMr: String? = "",
    @Json(name = "syllabus_en") val syllabusEn: String? = ""
)

@JsonClass(generateAdapter = true)
data class ApiTestsResponse(
    @Json(name = "status") val status: String = "error",
    @Json(name = "test_series") val testSeries: List<ApiTestSeriesItem> = emptyList(),
    @Json(name = "pdf_papers") val pdfPapers: List<ApiPdfPaperItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ApiTestSeriesItem(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "exam_name") val examName: String? = null,
    @Json(name = "category") val category: String? = "Speed Test",
    @Json(name = "subject_name") val subjectName: String? = null,
    @Json(name = "subject_id") val subjectId: Int? = 0,
    @Json(name = "duration_minutes") val durationMinutes: Int? = 60,
    @Json(name = "total_marks") val totalMarks: Int? = 100,
    @Json(name = "negative_marking") val negativeMarking: Float? = 0.25f,
    @Json(name = "price") val price: String? = null,
    @Json(name = "max_attempts") val maxAttempts: String? = null,
    @Json(name = "question_ids") val questionIds: String? = null,
    @Json(name = "is_published") val isPublished: String? = null,
    @Json(name = "question_count") val questionCount: Int? = 0
)

@JsonClass(generateAdapter = true)
data class ApiPdfPaperItem(
    @Json(name = "id") val id: String,
    @Json(name = "title_en") val titleEn: String,
    @Json(name = "year") val year: String? = null,
    @Json(name = "exam_name") val examName: String? = null,
    @Json(name = "pdf_url") val pdfUrl: String? = null,
    @Json(name = "answer_pdf_url") val answerPdfUrl: String? = null,
    @Json(name = "answer_keys") val answerKeys: String? = null,
    @Json(name = "is_published") val isPublished: String? = null
)

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

@JsonClass(generateAdapter = true)
data class ApiNotificationResponse(
    @Json(name = "status") val status: String = "error",
    @Json(name = "data") val data: List<ApiNotificationItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ApiNotificationItem(
    @Json(name = "id") val id: String = "",
    @Json(name = "title") val title: String = "",
    @Json(name = "message") val message: String = "",
    @Json(name = "type") val type: String = "INFO",
    @Json(name = "createdDate") val createdDate: String = ""
)

@JsonClass(generateAdapter = true)
data class ApiSubscriptionStatusResponse(
    @Json(name = "status") val status: String = "error",
    @Json(name = "access_granted") val accessGranted: Boolean = true,
    @Json(name = "is_trial_active") val isTrialActive: Boolean = true,
    @Json(name = "trial_hours_remaining") val trialHoursRemaining: Float = 48.0f,
    @Json(name = "is_subscribed") val isSubscribed: Boolean = false,
    @Json(name = "days_left_text") val daysLeftText: String = "",
    @Json(name = "subscription") val subscription: ApiSubscriptionDetail? = null
)

@JsonClass(generateAdapter = true)
data class ApiSubscriptionDetail(
    @Json(name = "plan_id") val planId: String = "",
    @Json(name = "plan_name") val planName: String = "",
    @Json(name = "expiry_date") val expiryDate: String = ""
)

@JsonClass(generateAdapter = true)
data class ApiRazorpayOrderResponse(
    @Json(name = "status") val status: String = "error",
    @Json(name = "order_id") val orderId: String = "",
    @Json(name = "key_id") val keyId: String = "",
    @Json(name = "amount") val amount: Int = 0,
    @Json(name = "currency") val currency: String = "INR",
    @Json(name = "plan_id") val planId: String = "",
    @Json(name = "plan_name") val planName: String = "",
    @Json(name = "message") val message: String = ""
)

@JsonClass(generateAdapter = true)
data class ApiSubscriptionPlansResponse(
    @Json(name = "status") val status: String = "error",
    @Json(name = "data") val data: List<ApiSubscriptionPlanItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ApiSubscriptionPlanItem(
    @Json(name = "planId") val planId: String = "",
    @Json(name = "planName") val planName: String = "",
    @Json(name = "price") val price: String = "99.00",
    @Json(name = "amountPaise") val amountPaise: Int = 9900,
    @Json(name = "originalPrice") val originalPrice: String = "",
    @Json(name = "durationDays") val durationDays: Int = 30,
    @Json(name = "durationText") val durationText: String = "",
    @Json(name = "discountTag") val discountTag: String? = null,
    @Json(name = "isPopular") val isPopular: Int = 0
)
