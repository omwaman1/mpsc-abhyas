package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.remote.ApiQuestion
import com.example.data.remote.RetrofitClient
import com.example.ui.components.ConfettiCelebrationOverlay
import com.example.ui.theme.TestbookEmerald
import com.example.ui.theme.TestbookGold
import com.example.ui.theme.TestbookNavy
import com.example.ui.viewmodel.LanguageMode
import com.example.utils.cleanHtml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun QuestionAttemptScreen(
    topicId: Int? = null,
    examName: String? = null,
    examCategory: String? = null,
    examYear: Int? = null,
    title: String,
    onBack: () -> Unit,
    onSaveQuestion: ((ApiQuestion, Boolean) -> Unit)? = null,
    onReportQuestion: ((Int, String, String) -> Unit)? = null,
    isVibrationEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var reportingQuestionId by remember { mutableStateOf<Int?>(null) }
    var questions by remember { mutableStateOf<List<ApiQuestion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var currentIndex by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { questions.size })

    LaunchedEffect(pagerState.currentPage) {
        if (questions.isNotEmpty() && pagerState.currentPage in questions.indices) {
            currentIndex = pagerState.currentPage
        }
    }

    LaunchedEffect(currentIndex) {
        if (questions.isNotEmpty() && currentIndex in questions.indices && pagerState.currentPage != currentIndex) {
            pagerState.scrollToPage(currentIndex)
        }
    }
    val userAnswers = remember { mutableStateMapOf<Int, Int>() } // questionIndex -> selectedOption
    val showSolution = remember { mutableStateMapOf<Int, Boolean>() } // questionIndex -> show
    val bookmarkedQuestions = remember { mutableStateMapOf<Int, Boolean>() }
    var triggerCelebration by remember { mutableStateOf(false) }
    var showGridPaletteDialog by remember { mutableStateOf(false) }

    var totalDragAmount by remember { mutableFloatStateOf(0f) }
    var languageMode by remember { mutableStateOf<LanguageMode>(LanguageMode.MARATHI) }

    LaunchedEffect(topicId, examName, examCategory, examYear) {
        try {
            isLoading = true
            val resp = withContext(Dispatchers.IO) {
                RetrofitClient.apiService.getQuestions(
                    topicId = topicId,
                    examName = examName,
                    examCategory = examCategory,
                    examYear = examYear,
                    limit = 50000
                )
            }
            if (resp.status == "success") {
                questions = if (examCategory != null) resp.data.shuffled() else resp.data
            }
            errorMessage = null
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(currentIndex, questions.size) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount ->
                        totalDragAmount += dragAmount
                    },
                    onDragEnd = {
                        if (totalDragAmount < -70f && currentIndex < questions.size - 1) {
                            // Swipe Left -> Go to Next Question
                            currentIndex++
                        } else if (totalDragAmount > 70f && currentIndex > 0) {
                            // Swipe Right -> Go to Previous Question
                            currentIndex--
                        }
                        totalDragAmount = 0f
                    },
                    onDragCancel = { totalDragAmount = 0f }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // TOP CONTROL HEADER BAR
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Question Counter Box
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {

                        // Counter Card (e.g., 03 / 143 with left orange indicator)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(34.dp)
                                        .background(Color(0xFFEA580C), shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                                )
                                Text(
                                    text = if (questions.isNotEmpty()) {
                                        String.format("%02d / %d", currentIndex + 1, questions.size)
                                    } else {
                                        "00 / 00"
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Action Icons Row: Notes | Save | Easy | Moderate | Square Dots (Question Palette)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Pencil Report / Feedback Icon (Replaced Notes Icon)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)),
                            modifier = Modifier.size(36.dp).clickable {
                                if (questions.isNotEmpty() && currentIndex in questions.indices) {
                                    val q = questions[currentIndex]
                                    q.id?.toIntOrNull()?.let { qId ->
                                        reportingQuestionId = qId
                                    }
                                }
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Report Question",
                                    tint = Color(0xFF6366F1),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // 2. Download / Save to Bookmarks Icon
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)),
                            modifier = Modifier.size(36.dp).clickable {
                                if (questions.isNotEmpty() && currentIndex in questions.indices) {
                                    val currentQ = questions[currentIndex]
                                    val isCurrentlySaved = bookmarkedQuestions[currentIndex] == true
                                    val newState = !isCurrentlySaved
                                    bookmarkedQuestions[currentIndex] = newState
                                    onSaveQuestion?.invoke(currentQ, newState)
                                }
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (bookmarkedQuestions[currentIndex] == true) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Save to Bookmarks",
                                    tint = if (bookmarkedQuestions[currentIndex] == true) TestbookGold else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // 3. Easy Face (Happy)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)),
                            modifier = Modifier.size(36.dp).clickable { }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.SentimentSatisfiedAlt,
                                    contentDescription = "Easy",
                                    tint = Color(0xFFF97316),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // 4. Moderate Face (Neutral)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)),
                            modifier = Modifier.size(36.dp).clickable { }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.SentimentNeutral,
                                    contentDescription = "Moderate",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // 5. Square Dots Icon (Question Grid Palette)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)),
                            modifier = Modifier.size(36.dp).clickable {
                                showGridPaletteDialog = true
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.GridView,
                                    contentDescription = "Question Grid Palette",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Progress Bar
            if (questions.isNotEmpty()) {
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / questions.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = TestbookEmerald,
                    trackColor = Color(0xFFE2E8F0)
                )
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = TestbookNavy)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Loading questions...", fontSize = 13.sp, color = Color(0xFF64748B))
                    }
                }
            } else if (errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: $errorMessage", color = Color(0xFFEF4444))
                }
            } else if (questions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No questions found for this selection.", color = Color(0xFF64748B))
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { pageIndex ->
                    val q = questions[pageIndex]
                    val selectedOpt = userAnswers[pageIndex]
                    val answered = selectedOpt != null
                    val correctAns = q.correctAnswer?.toIntOrNull() ?: 0
                    val isShowingSolution = showSolution[pageIndex] == true

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                    // QUESTION CONTAINER CARD
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Top Left Exam & Year Tag
                            val rawExam = (q.examName ?: title).uppercase()
                            val rawYear = q.examYear?.toString()?.trim() ?: ""
                            val examTagText = when {
                                rawYear == "0" || rawYear.equals("MOCK", ignoreCase = true) || rawYear.isEmpty() -> "$rawExam • MOCK"
                                !rawExam.contains(rawYear) -> "$rawExam • $rawYear"
                                else -> rawExam
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = examTagText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Question Text (Strictly in active languageMode)
                            val qMrClean = (q.questionMr ?: "").cleanHtml()
                            val qEnClean = (q.questionEn ?: "").cleanHtml()

                            val questionTitle = if (languageMode == LanguageMode.MARATHI) {
                                if (qMrClean.isNotEmpty()) "प्रश्न : $qMrClean" else "Q : $qEnClean"
                            } else {
                                if (qEnClean.isNotEmpty()) "Q : $qEnClean" else "प्रश्न : $qMrClean"
                            }

                            Text(
                                text = questionTitle,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 23.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // OPTIONS LIST (Filtered by active languageMode)
                    val opt1 = if (languageMode == LanguageMode.MARATHI) (q.opt1Mr.takeIf { !it.isNullOrBlank() } ?: q.opt1En ?: "Option 1").cleanHtml() else (q.opt1En.takeIf { !it.isNullOrBlank() } ?: q.opt1Mr ?: "Option 1").cleanHtml()
                    val opt2 = if (languageMode == LanguageMode.MARATHI) (q.opt2Mr.takeIf { !it.isNullOrBlank() } ?: q.opt2En ?: "Option 2").cleanHtml() else (q.opt2En.takeIf { !it.isNullOrBlank() } ?: q.opt2Mr ?: "Option 2").cleanHtml()
                    val opt3 = if (languageMode == LanguageMode.MARATHI) (q.opt3Mr.takeIf { !it.isNullOrBlank() } ?: q.opt3En ?: "Option 3").cleanHtml() else (q.opt3En.takeIf { !it.isNullOrBlank() } ?: q.opt3Mr ?: "Option 3").cleanHtml()
                    val opt4 = if (languageMode == LanguageMode.MARATHI) (q.opt4Mr.takeIf { !it.isNullOrBlank() } ?: q.opt4En ?: "Option 4").cleanHtml() else (q.opt4En.takeIf { !it.isNullOrBlank() } ?: q.opt4Mr ?: "Option 4").cleanHtml()

                    val options = listOf(
                        1 to opt1,
                        2 to opt2,
                        3 to opt3,
                        4 to opt4
                    )

                    options.forEach { (num, text) ->
                        val isCorrect = (num == correctAns)
                        val isSelected = (num == selectedOpt)

                        // Soft green/red or surface background
                        val bg = when {
                            answered && isCorrect -> Color(0xFF059669).copy(alpha = 0.2f)
                            answered && isSelected && !isCorrect -> Color(0xFFDC2626).copy(alpha = 0.2f)
                            isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surface
                        }
                        val borderColor = when {
                            answered && isCorrect -> Color(0xFF059669)
                            answered && isSelected && !isCorrect -> Color(0xFFDC2626)
                            isSelected -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable(enabled = !answered) {
                                    userAnswers[currentIndex] = num
                                    showSolution[currentIndex] = true
                                    // Trigger Telegram poll celebration effect if option is CORRECT!
                                    if (num == correctAns) {
                                        triggerCelebration = true
                                    } else if (isVibrationEnabled) {
                                        try {
                                            val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? android.os.Vibrator
                                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                vibrator?.vibrate(android.os.VibrationEffect.createOneShot(150, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                                            } else {
                                                @Suppress("DEPRECATION")
                                                vibrator?.vibrate(150)
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = bg,
                            border = BorderStroke(1.dp, borderColor)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$num) $text",
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected || (answered && isCorrect)) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                    lineHeight = 20.sp
                                )

                                if (answered && isCorrect) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Correct",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else if (answered && isSelected && !isCorrect) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Wrong",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Solution/Explanation Section (Filtered by active languageMode)
                    if (isShowingSolution && answered) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (languageMode == LanguageMode.MARATHI) "योग्य उत्तर : पर्याय ($correctAns)" else "Correct Answer : Option ($correctAns)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                val solution = if (languageMode == LanguageMode.MARATHI) {
                                    (q.solutionMr.takeIf { !it.isNullOrBlank() } ?: q.solutionEn)?.cleanHtml()
                                } else {
                                    (q.solutionEn.takeIf { !it.isNullOrBlank() } ?: q.solutionMr)?.cleanHtml()
                                }
                                if (!solution.isNullOrEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = solution,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 19.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp)) // Space for bottom bar
                }
            }

            val isBottomSolutionShowing = showSolution[currentIndex] == true

                // STICKY BOTTOM NAVIGATION BAR
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Button (Left Arrow)
                        IconButton(
                            onClick = {
                                if (currentIndex > 0) {
                                    coroutineScope.launch { pagerState.animateScrollToPage(currentIndex - 1) }
                                }
                            },
                            enabled = currentIndex > 0,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Previous",
                                modifier = Modifier.size(24.dp),
                                tint = if (currentIndex > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }

                        // Show Solution Toggle Button
                        OutlinedButton(
                            onClick = {
                                showSolution[currentIndex] = !(showSolution[currentIndex] ?: false)
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (isBottomSolutionShowing) "स्पष्टीकरण लपवा" else "उत्तर व स्पष्टीकरण",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Save & Next Button (Bottom Right Corner)
                        Button(
                            onClick = {
                                if (currentIndex < questions.size - 1) {
                                    coroutineScope.launch { pagerState.animateScrollToPage(currentIndex + 1) }
                                }
                            },
                            enabled = currentIndex < questions.size - 1,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (languageMode == LanguageMode.MARATHI) "जतन करा व पुढे" else "Save & Next",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.NavigateNext,
                                    contentDescription = "Next",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // TELEGRAM POLL STYLE CELEBRATION EFFECT OVERLAY
        ConfettiCelebrationOverlay(
            trigger = triggerCelebration,
            onAnimationEnd = { triggerCelebration = false }
        )

        // QUESTION PALETTE GRID DIALOG (SQUARE DOTS MODAL)
        if (showGridPaletteDialog) {
            Dialog(
                onDismissRequest = { showGridPaletteDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .fillMaxHeight(0.75f),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 12.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "प्रश्न पॅलेट (Question Palette)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "एकूण ${questions.size} प्रश्न (Click any square to jump)",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { showGridPaletteDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Status Legend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color(0xFF059669).copy(alpha = 0.3f), shape = RoundedCornerShape(3.dp))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("उत्तर दिले (Answered)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(3.dp))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("बाकी (Unattempted)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 5-Column Question Squares Grid
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(5),
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 12.dp)
                        ) {
                            itemsIndexed(questions) { idx, _ ->
                                val isAnswered = userAnswers.containsKey(idx)
                                val isCurrent = idx == currentIndex

                                val boxBg = when {
                                    isCurrent -> Color(0xFF2563EB).copy(alpha = 0.25f)
                                    isAnswered -> Color(0xFF059669).copy(alpha = 0.25f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                                val boxBorder = when {
                                    isCurrent -> Color(0xFF60A5FA)
                                    isAnswered -> Color(0xFF34D399)
                                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                                }

                                Surface(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clickable {
                                            currentIndex = idx
                                            showGridPaletteDialog = false
                                        },
                                    shape = RoundedCornerShape(8.dp),
                                    color = boxBg,
                                    border = BorderStroke(1.5.dp, boxBorder)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = String.format("%02d", idx + 1),
                                            fontSize = 12.sp,
                                            fontWeight = if (isCurrent || isAnswered) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isCurrent || isAnswered) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (reportingQuestionId != null) {
        QuestionReportDialog(
            questionId = reportingQuestionId!!,
            onDismiss = { reportingQuestionId = null },
            onSubmit = { reportType, comment ->
                val qId = reportingQuestionId!!
                reportingQuestionId = null
                onReportQuestion?.invoke(qId, reportType, comment)
                android.widget.Toast.makeText(context, "आपली रिपोर्ट यशस्वीपणे नोंदवली गेली आहे!", android.widget.Toast.LENGTH_SHORT).show()
            }
        )
    }
}
