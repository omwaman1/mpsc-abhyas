package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entities.CurrentAffairsEntity
import com.example.data.local.entities.QuestionEntity
import com.example.data.local.entities.TestAttemptEntity
import com.example.data.local.entities.TestPaperEntity
import com.example.data.remote.RetrofitClient
import com.example.data.repository.MPSCRepository
import com.example.util.cleanHtml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.ceil

enum class LanguageMode { MARATHI, ENGLISH }

enum class AppThemeMode { LIGHT, DARK, SYSTEM }

enum class QuestionState { UNATTEMPTED, ANSWERED, MARKED_FOR_REVIEW, ANSWERED_AND_MARKED }

enum class AppTab { HOME, PYQ_BANK, TEST_SERIES, ANALYTICS, BOOKMARKS, SYLLABUS }

data class SubscriptionState(
    val accessGranted: Boolean = true,
    val isTrialActive: Boolean = true,
    val trialHoursRemaining: Float = 48.0f,
    val daysLeftText: String = "2d trial left",
    val isSubscribed: Boolean = false,
    val planName: String? = null,
    val expiryDate: String? = null
)

data class TestActiveState(
    val testPaper: TestPaperEntity,
    val questions: List<QuestionEntity>,
    val currentIndex: Int = 0,
    val userAnswers: Map<Int, Int> = emptyMap(), // questionId -> option (1..4)
    val questionStates: Map<Int, QuestionState> = emptyMap(),
    val timeRemainingSeconds: Int = 0,
    val isTimerRunning: Boolean = false,
    val languageMode: LanguageMode = LanguageMode.MARATHI,
    val isPaletteOpen: Boolean = false,
    val isSubmitDialogOpen: Boolean = false
)

data class TestResultState(
    val attempt: TestAttemptEntity,
    val testPaper: TestPaperEntity,
    val questions: List<QuestionEntity>,
    val languageMode: LanguageMode = LanguageMode.MARATHI
)

data class UserProfile(
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val isLoggedIn: Boolean = false
)

class MPSCViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MPSCRepository
    private var timerJob: Job? = null
    private val prefs = application.getSharedPreferences("mpsc_user_prefs", android.content.Context.MODE_PRIVATE)

    // UI State
    private val _selectedTab = MutableStateFlow(AppTab.HOME)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(
        try {
            LanguageMode.valueOf(prefs.getString("user_language_mode", LanguageMode.ENGLISH.name) ?: LanguageMode.ENGLISH.name)
        } catch (e: Exception) {
            LanguageMode.ENGLISH
        }
    )
    val selectedLanguage: StateFlow<LanguageMode> = _selectedLanguage.asStateFlow()

    fun setSelectedLanguage(mode: LanguageMode) {
        prefs.edit().putString("user_language_mode", mode.name).apply()
        _selectedLanguage.value = mode
    }

    private val _appThemeMode = MutableStateFlow(
        try {
            AppThemeMode.valueOf(prefs.getString("app_theme_mode", AppThemeMode.DARK.name) ?: AppThemeMode.DARK.name)
        } catch (e: Exception) {
            AppThemeMode.DARK
        }
    )
    val appThemeMode: StateFlow<AppThemeMode> = _appThemeMode.asStateFlow()

    private val _isVibrationEnabled = MutableStateFlow(prefs.getBoolean("pref_vibration_enabled", true))
    val isVibrationEnabled: StateFlow<Boolean> = _isVibrationEnabled.asStateFlow()

    private val _isNotificationsEnabled = MutableStateFlow(prefs.getBoolean("pref_notifications_enabled", true))
    val isNotificationsEnabled: StateFlow<Boolean> = _isNotificationsEnabled.asStateFlow()

    // Test Series persistent dropdown states
    private val _selectedTestSeriesExamCategory = MutableStateFlow(prefs.getString("ts_exam_category", "All Exams") ?: "All Exams")
    val selectedTestSeriesExamCategory: StateFlow<String> = _selectedTestSeriesExamCategory.asStateFlow()

    fun setSelectedTestSeriesExamCategory(category: String) {
        prefs.edit().putString("ts_exam_category", category).apply()
        _selectedTestSeriesExamCategory.value = category
    }

    private val _selectedTestSeriesSubject = MutableStateFlow(prefs.getString("ts_subject", "All Subjects") ?: "All Subjects")
    val selectedTestSeriesSubject: StateFlow<String> = _selectedTestSeriesSubject.asStateFlow()

    fun setSelectedTestSeriesSubject(subject: String) {
        prefs.edit().putString("ts_subject", subject).apply()
        _selectedTestSeriesSubject.value = subject
    }

    fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("pref_vibration_enabled", enabled).apply()
        _isVibrationEnabled.value = enabled
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("pref_notifications_enabled", enabled).apply()
        _isNotificationsEnabled.value = enabled
    }

    private val _userProfile = MutableStateFlow(
        UserProfile(
            fullName = prefs.getString("user_name", "") ?: "",
            phone = prefs.getString("user_phone", "") ?: "",
            email = prefs.getString("user_email", "") ?: "",
            isLoggedIn = prefs.getBoolean("is_logged_in", false)
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _subscriptionState = MutableStateFlow(
        SubscriptionState(
            accessGranted = true,
            isSubscribed = true,
            isTrialActive = false,
            trialHoursRemaining = 0.0f,
            daysLeftText = "Free Pro Access"
        )
    )
    val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState.asStateFlow()

    private val _subscriptionPlans = MutableStateFlow<List<com.example.data.remote.ApiSubscriptionPlanItem>>(emptyList())
    val subscriptionPlans: StateFlow<List<com.example.data.remote.ApiSubscriptionPlanItem>> = _subscriptionPlans.asStateFlow()

    init {
        checkSubscriptionStatus()
    }

    fun fetchSubscriptionPlans() {
        // Subscriptions disabled for current v1 release
    }

    fun checkSubscriptionStatus() {
        // ALL USERS GET 100% UNLIMITED FREE PRO ACCESS FOR V1 RELEASE
        _subscriptionState.value = SubscriptionState(
            accessGranted = true,
            isSubscribed = true,
            isTrialActive = false,
            trialHoursRemaining = 0.0f,
            daysLeftText = "Free Pro Access"
        )
    }

    fun verifyRazorpayPayment(
        planId: String,
        paymentId: String,
        orderId: String,
        signature: String = "",
        onResult: (Boolean, String?) -> Unit
    ) {
        val email = _userProfile.value.email
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val resp = RetrofitClient.apiService.verifyPayment(
                    email = email,
                    planId = planId,
                    paymentId = paymentId,
                    orderId = orderId,
                    signature = signature
                )
                if (resp.status == "success") {
                    checkSubscriptionStatus()
                    withContext(Dispatchers.Main) { onResult(true, resp.message) }
                } else {
                    withContext(Dispatchers.Main) { onResult(false, resp.message) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onResult(false, "Verification error") }
            }
        }
    }

    fun setAppThemeMode(mode: AppThemeMode) {
        prefs.edit().putString("app_theme_mode", mode.name).apply()
        _appThemeMode.value = mode
    }

    fun updateProfileName(newName: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentEmail = _userProfile.value.email
                val resp = RetrofitClient.apiService.updateProfileName(currentEmail, newName)
                if (resp.status == "success") {
                    prefs.edit().putString("user_name", newName).apply()
                    _userProfile.value = _userProfile.value.copy(fullName = newName)
                    withContext(Dispatchers.Main) { onResult(true) }
                } else {
                    withContext(Dispatchers.Main) { onResult(false) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onResult(false) }
            }
        }
    }

    fun loginAndRegisterUser(
        fullName: String,
        phone: String,
        email: String,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val response = com.example.data.remote.RetrofitClient.apiService.registerUser(
                    fullName = fullName,
                    phone = phone,
                    email = email
                )
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    if (response.status == "error") {
                        onResult(false, response.message ?: "Registration failed")
                    } else {
                        prefs.edit()
                            .putBoolean("is_logged_in", true)
                            .putString("user_name", fullName)
                            .putString("user_phone", phone)
                            .putString("user_email", email)
                            .apply()

                        _userProfile.value = UserProfile(
                            fullName = fullName,
                            phone = phone,
                            email = email,
                            isLoggedIn = true
                        )
                        // FIRST ACTION ON LOGIN: Check subscription status from server!
                        checkSubscriptionStatus()
                        onResult(true, null)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                prefs.edit()
                    .putBoolean("is_logged_in", true)
                    .putString("user_name", fullName)
                    .putString("user_phone", phone)
                    .putString("user_email", email)
                    .apply()

                _userProfile.value = UserProfile(
                    fullName = fullName,
                    phone = phone,
                    email = email,
                    isLoggedIn = true
                )
                // FIRST ACTION ON LOGIN: Check subscription status from server!
                checkSubscriptionStatus()
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onResult(true, null)
                }
            }
        }
    }

    fun checkUserExisting(email: String, onResult: (com.example.data.remote.ApiUserData?) -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val res = com.example.data.remote.RetrofitClient.apiService.checkUser(email = email)
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    if (res.exists && res.user != null) {
                        onResult(res.user)
                    } else {
                        onResult(null)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    onResult(null)
                }
            }
        }
    }

    fun logoutUser() {
        // 1. Clear SharedPreferences
        prefs.edit().clear().apply()

        // 2. Clear Room Local Database
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllLocalData()
        }

        // 3. Clear In-Memory Session Caches
        com.example.ui.screens.clearPYQBankSessionCache()

        // 4. Reset All ViewModel States
        _userProfile.value = UserProfile()
        _subscriptionState.value = SubscriptionState()
        _selectedTab.value = AppTab.HOME
        _activeTestState.value = null
        _activeResultState.value = null
    }

    // Filters for PYQ Bank
    val selectedExamFilter = MutableStateFlow("All Exams")
    val selectedSubjectFilter = MutableStateFlow("All Subjects")
    val selectedYearFilter = MutableStateFlow("All Years")
    val isStudyMode = MutableStateFlow(true) // Study Mode vs Quiz Mode in PYQ Bank

    // Active Test Engine State
    private val _activeTestState = MutableStateFlow<TestActiveState?>(null)
    val activeTestState: StateFlow<TestActiveState?> = _activeTestState.asStateFlow()

    // Active Result State
    private val _activeResultState = MutableStateFlow<TestResultState?>(null)
    val activeResultState: StateFlow<TestResultState?> = _activeResultState.asStateFlow()

    init {
        val database = AppDatabase.getInstance(application)
        repository = MPSCRepository(database)
        refreshTests()
    }

    fun refreshTests() {
        viewModelScope.launch {
            repository.fetchAndSyncTests()
        }
    }

    val allQuestions: StateFlow<List<QuestionEntity>> = repository.allQuestions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedQuestions: StateFlow<List<QuestionEntity>> = repository.bookmarkedQuestions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTestPapers: StateFlow<List<TestPaperEntity>> = repository.allTestPapers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttempts: StateFlow<List<TestAttemptEntity>> = repository.allAttempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCurrentAffairs: StateFlow<List<CurrentAffairsEntity>> = repository.allCurrentAffairs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered PYQs based on dropdowns
    val filteredPYQs: StateFlow<List<QuestionEntity>> = combine(
        allQuestions,
        selectedExamFilter,
        selectedSubjectFilter,
        selectedYearFilter
    ) { questions, exam, subject, year ->
        questions.filter { q ->
            val matchExam = (exam == "All Exams") || q.examType.contains(exam, ignoreCase = true)
            val matchSubject = (subject == "All Subjects") || q.subject.contains(subject, ignoreCase = true)
            val matchYear = (year == "All Years") || (q.year.toString() == year)
            matchExam && matchSubject && matchYear
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedTab(tab: AppTab) {
        _selectedTab.value = tab
    }

    fun toggleAppLanguage() {
        _selectedLanguage.value = if (_selectedLanguage.value == LanguageMode.MARATHI) LanguageMode.ENGLISH else LanguageMode.MARATHI
    }

    fun toggleBookmark(questionId: Int, isBookmarked: Boolean, note: String = "") {
        viewModelScope.launch {
            repository.toggleBookmark(questionId, !isBookmarked, note)
        }
    }

    // --- TEST ENGINE ACTIONS ---
    fun startTest(testPaper: TestPaperEntity) {
        viewModelScope.launch {
            val qIds = testPaper.questionIdsJson.split(",").mapNotNull { it.trim().toIntOrNull() }
            var matchedQuestions = repository.getQuestionsByIds(qIds).toMutableList()

            // Fetch missing questions from remote API if not available in local DB
            if (qIds.isNotEmpty() && matchedQuestions.size < qIds.size) {
                val missingIds = qIds.filter { id -> matchedQuestions.none { it.id == id } }
                if (missingIds.isNotEmpty()) {
                    repository.fetchAndSyncMissingQuestions(missingIds)
                    matchedQuestions = repository.getQuestionsByIds(qIds).toMutableList()
                }
            }

            // Use exact matched questions without duplicating or padding up to artificial limits
            val finalQuestions = if (matchedQuestions.isNotEmpty()) {
                matchedQuestions
            } else {
                val pool = allQuestions.value.ifEmpty { com.example.data.local.MPSCInitialData.getInitialQuestions() }
                val targetCount = minOf(testPaper.questionCount, pool.size)
                pool.take(targetCount).toMutableList()
            }

            val initialStateMap = finalQuestions.associate { it.id to QuestionState.UNATTEMPTED }

            _activeTestState.value = TestActiveState(
                testPaper = testPaper.copy(questionCount = finalQuestions.size),
                questions = finalQuestions,
                currentIndex = 0,
                userAnswers = emptyMap(),
                questionStates = initialStateMap,
                timeRemainingSeconds = testPaper.durationMinutes * 60,
                isTimerRunning = true,
                languageMode = _selectedLanguage.value
            )

            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val state = _activeTestState.value ?: break
                if (!state.isTimerRunning) continue

                val newTime = state.timeRemainingSeconds - 1
                if (newTime <= 0) {
                    _activeTestState.value = state.copy(timeRemainingSeconds = 0, isTimerRunning = false)
                    submitActiveTest()
                    break
                } else {
                    _activeTestState.value = state.copy(timeRemainingSeconds = newTime)
                }
            }
        }
    }

    fun selectTestOption(optionIndex: Int) {
        val state = _activeTestState.value ?: return
        val currentQ = state.questions.getOrNull(state.currentIndex) ?: return

        if (state.userAnswers[currentQ.id] == optionIndex) {
            clearTestOption()
            return
        }

        val updatedAnswers = state.userAnswers.toMutableMap()
        updatedAnswers[currentQ.id] = optionIndex

        val updatedStates = state.questionStates.toMutableMap()
        val currentState = updatedStates[currentQ.id]
        if (currentState == QuestionState.MARKED_FOR_REVIEW || currentState == QuestionState.ANSWERED_AND_MARKED) {
            updatedStates[currentQ.id] = QuestionState.ANSWERED_AND_MARKED
        } else {
            updatedStates[currentQ.id] = QuestionState.ANSWERED
        }

        _activeTestState.value = state.copy(
            userAnswers = updatedAnswers,
            questionStates = updatedStates
        )
    }

    fun clearTestOption() {
        val state = _activeTestState.value ?: return
        val currentQ = state.questions.getOrNull(state.currentIndex) ?: return
        val updatedAnswers = state.userAnswers.toMutableMap()
        updatedAnswers.remove(currentQ.id)

        val updatedStates = state.questionStates.toMutableMap()
        updatedStates[currentQ.id] = QuestionState.UNATTEMPTED

        _activeTestState.value = state.copy(
            userAnswers = updatedAnswers,
            questionStates = updatedStates
        )
    }

    fun markForReviewAndNext() {
        val state = _activeTestState.value ?: return
        val currentQ = state.questions.getOrNull(state.currentIndex) ?: return
        val updatedStates = state.questionStates.toMutableMap()

        val isAnswered = state.userAnswers.containsKey(currentQ.id)
        updatedStates[currentQ.id] = if (isAnswered) QuestionState.ANSWERED_AND_MARKED else QuestionState.MARKED_FOR_REVIEW

        val nextIndex = if (state.currentIndex + 1 < state.questions.size) state.currentIndex + 1 else state.currentIndex

        _activeTestState.value = state.copy(
            questionStates = updatedStates,
            currentIndex = nextIndex
        )
    }

    fun nextQuestion() {
        val state = _activeTestState.value ?: return
        if (state.currentIndex + 1 < state.questions.size) {
            _activeTestState.value = state.copy(currentIndex = state.currentIndex + 1)
        }
    }

    fun previousQuestion() {
        val state = _activeTestState.value ?: return
        if (state.currentIndex > 0) {
            _activeTestState.value = state.copy(currentIndex = state.currentIndex - 1)
        }
    }

    fun jumpToQuestion(index: Int) {
        val state = _activeTestState.value ?: return
        if (index in 0 until state.questions.size) {
            _activeTestState.value = state.copy(currentIndex = index, isPaletteOpen = false)
        }
    }

    fun togglePalette(open: Boolean) {
        val state = _activeTestState.value ?: return
        _activeTestState.value = state.copy(isPaletteOpen = open)
    }

    fun toggleSubmitDialog(open: Boolean) {
        val state = _activeTestState.value ?: return
        _activeTestState.value = state.copy(isSubmitDialogOpen = open)
    }

    fun toggleTestLanguage() {
        val state = _activeTestState.value ?: return
        val newLang = if (state.languageMode == LanguageMode.MARATHI) LanguageMode.ENGLISH else LanguageMode.MARATHI
        _activeTestState.value = state.copy(languageMode = newLang)
    }

    fun submitActiveTest() {
        timerJob?.cancel()
        val state = _activeTestState.value ?: return

        var correctCount = 0
        var wrongCount = 0
        var unattemptedCount = 0

        val totalMarks = state.testPaper.totalMarks
        val totalQuestions = state.questions.size
        val marksPerQ = if (totalQuestions > 0) totalMarks.toFloat() / totalQuestions else 2.0f

        val userAnswersJsonMap = mutableMapOf<String, Int>()

        state.questions.forEach { q ->
            val ans = state.userAnswers[q.id]
            if (ans != null) {
                userAnswersJsonMap[q.id.toString()] = ans
                if (ans == q.correctOption) {
                    correctCount++
                } else {
                    wrongCount++
                }
            } else {
                unattemptedCount++
            }
        }

        val positiveScore = correctCount * marksPerQ
        // Dynamic negative marking directly from Database (e.g. 0.25 = 1/4th minus mark)
        val negativeRatio = state.testPaper.negativeMarking
        val negativeScore = wrongCount * (marksPerQ * negativeRatio)
        val finalScore = maxOf(0.0f, positiveScore - negativeScore)

        val totalAttempted = correctCount + wrongCount
        val accuracy = if (totalAttempted > 0) (correctCount.toFloat() / totalAttempted) * 100f else 0f
        val timeTaken = (state.testPaper.durationMinutes * 60) - state.timeRemainingSeconds

        val answersJsonStr = userAnswersJsonMap.entries.joinToString(prefix = "{", postfix = "}") {
            "\"${it.key}\": ${it.value}"
        }

        val attemptEntity = TestAttemptEntity(
            testId = state.testPaper.testId,
            testTitle = state.testPaper.title,
            score = finalScore,
            totalMarks = totalMarks,
            correctCount = correctCount,
            wrongCount = wrongCount,
            unattemptedCount = unattemptedCount,
            accuracyPercentage = accuracy,
            timeTakenSeconds = timeTaken,
            userAnswersJson = answersJsonStr
        )

        viewModelScope.launch {
            val attemptId = repository.saveTestAttempt(attemptEntity)
            _activeTestState.value = null

            _activeResultState.value = TestResultState(
                attempt = attemptEntity.copy(attemptId = attemptId),
                testPaper = state.testPaper,
                questions = state.questions,
                languageMode = state.languageMode
            )
        }
    }

    fun closeResultScreen() {
        _activeResultState.value = null
        _selectedTab.value = AppTab.ANALYTICS
    }

    fun exitTestWithoutSubmitting() {
        timerJob?.cancel()
        _activeTestState.value = null
    }

    fun openPastResult(attempt: TestAttemptEntity) {
        viewModelScope.launch {
            val testPaper = repository.getTestPaperById(attempt.testId) ?: return@launch
            val qIds = testPaper.questionIdsJson.split(",").mapNotNull { it.trim().toIntOrNull() }
            val questions = repository.getQuestionsByIds(qIds)

            _activeResultState.value = TestResultState(
                attempt = attempt,
                testPaper = testPaper,
                questions = questions,
                languageMode = _selectedLanguage.value
            )
        }
    }

    fun saveApiQuestionToBookmarks(apiQ: com.example.data.remote.ApiQuestion, isBookmarked: Boolean) {
        viewModelScope.launch {
            val qId = apiQ.id?.toIntOrNull() ?: System.currentTimeMillis().toInt()
            val entity = QuestionEntity(
                id = qId,
                examType = (apiQ.examName ?: "MPSC Exam").cleanHtml(),
                subject = "General Studies",
                year = apiQ.examYear ?: 2024,
                questionMarathi = (apiQ.questionMr ?: apiQ.questionEn ?: "").cleanHtml(),
                questionEnglish = (apiQ.questionEn ?: apiQ.questionMr ?: "").cleanHtml(),
                option1Marathi = (apiQ.opt1Mr ?: apiQ.opt1En ?: "").cleanHtml(),
                option1English = (apiQ.opt1En ?: apiQ.opt1Mr ?: "").cleanHtml(),
                option2Marathi = (apiQ.opt2Mr ?: apiQ.opt2En ?: "").cleanHtml(),
                option2English = (apiQ.opt2En ?: apiQ.opt2Mr ?: "").cleanHtml(),
                option3Marathi = (apiQ.opt3Mr ?: apiQ.opt3En ?: "").cleanHtml(),
                option3English = (apiQ.opt3En ?: apiQ.opt3Mr ?: "").cleanHtml(),
                option4Marathi = (apiQ.opt4Mr ?: apiQ.opt4En ?: "").cleanHtml(),
                option4English = (apiQ.opt4En ?: apiQ.opt4Mr ?: "").cleanHtml(),
                correctOption = apiQ.correctAnswer?.toIntOrNull() ?: 1,
                explanationMarathi = (apiQ.solutionMr ?: "").cleanHtml(),
                explanationEnglish = (apiQ.solutionEn ?: "").cleanHtml(),
                isBookmarked = isBookmarked
            )
            repository.saveQuestion(entity)
        }
    }

    fun reportQuestion(questionId: Int, reportType: String, comment: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val userEmail = _userProfile.value.email
            val success = repository.reportQuestion(questionId, userEmail, reportType, comment)
            onResult(success)
        }
    }
}
