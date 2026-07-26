package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entities.CurrentAffairsEntity
import com.example.data.local.entities.QuestionEntity
import com.example.data.local.entities.TestAttemptEntity
import com.example.data.local.entities.TestPaperEntity
import com.example.data.repository.MPSCRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LanguageMode { MARATHI, ENGLISH }

enum class QuestionState { UNATTEMPTED, ANSWERED, MARKED_FOR_REVIEW, ANSWERED_AND_MARKED }

enum class AppTab { HOME, PYQ_BANK, TEST_SERIES, ANALYTICS, BOOKMARKS }

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

class MPSCViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MPSCRepository
    private var timerJob: Job? = null

    // UI State
    private val _selectedTab = MutableStateFlow(AppTab.HOME)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(LanguageMode.MARATHI)
    val selectedLanguage: StateFlow<LanguageMode> = _selectedLanguage.asStateFlow()

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
        viewModelScope.launch {
            repository.checkAndSeedDatabase()
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
            var questions = repository.getQuestionsByIds(qIds)
            if (questions.isEmpty()) {
                questions = allQuestions.value.take(testPaper.questionCount)
            }

            val initialStateMap = questions.associate { it.id to QuestionState.UNATTEMPTED }

            _activeTestState.value = TestActiveState(
                testPaper = testPaper,
                questions = questions,
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
        // MPSC negative marking is 1/4th (0.25) per wrong question
        val negativeScore = wrongCount * (marksPerQ * 0.25f)
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
}
