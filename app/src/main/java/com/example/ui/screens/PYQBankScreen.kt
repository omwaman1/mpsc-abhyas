package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.QuestionEntity
import com.example.data.remote.ApiExamCategory
import com.example.data.remote.ApiQuestion
import com.example.data.remote.ApiSubject
import com.example.data.remote.ApiYearCategory
import com.example.data.remote.RetrofitClient
import com.example.ui.theme.TestbookEmerald
import com.example.ui.theme.TestbookGold
import com.example.ui.theme.TestbookNavy
import com.example.ui.viewmodel.LanguageMode
import com.example.util.cleanHtml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// In-memory session cache: Only fetch categories once per app session!
private var pyqSubjectsCache: List<ApiSubject>? = null
private var pyqExamsCache: List<ApiExamCategory>? = null
private var pyqYearsCache: List<ApiYearCategory>? = null

fun clearPYQBankSessionCache() {
    pyqSubjectsCache = null
    pyqExamsCache = null
    pyqYearsCache = null
}

enum class PYQViewMode {
    SUBJECT_WISE, EXAM_WISE, YEAR_WISE
}

@Composable
fun PYQBankScreen(
    onHeaderUpdate: (title: String?, subtitle: String?, backAction: (() -> Unit)?) -> Unit = { _, _, _ -> },
    onToggleQuestionMode: (Boolean) -> Unit = {},
    onSaveQuestion: ((ApiQuestion, Boolean) -> Unit)? = null,
    onReportQuestion: ((Int, String, String) -> Unit)? = null,
    isVibrationEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var reportingQuestionId by remember { mutableStateOf<Int?>(null) }
    
    // Pager state for smooth Left/Right horizontal swiping
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    var subjects by remember { mutableStateOf<List<ApiSubject>>(pyqSubjectsCache ?: emptyList()) }
    var exams by remember { mutableStateOf<List<ApiExamCategory>>(pyqExamsCache ?: emptyList()) }
    var years by remember { mutableStateOf<List<ApiYearCategory>>(pyqYearsCache ?: emptyList()) }
    var isConnected by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(pyqSubjectsCache == null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }

    // Navigation state for Subject drill-down
    var selectedSubjectId by remember { mutableStateOf<Int?>(null) }
    var selectedSubjectName by remember { mutableStateOf("") }
    var selectedTopicId by remember { mutableStateOf<Int?>(null) }
    var selectedTopicName by remember { mutableStateOf("") }

    // Navigation state for Exam drill-down
    var selectedExamCategory by remember { mutableStateOf<String?>(null) }
    var selectedExamCategoryPyqs by remember { mutableStateOf(0) }

    // Navigation state for Year-wise drill-down
    var selectedYearCategory by remember { mutableStateOf<String?>(null) }
    var selectedYearCategoryPyqs by remember { mutableStateOf(0) }
    var selectedYearExamName by remember { mutableStateOf<String?>(null) }

    // Update parent main top bar dynamically
    LaunchedEffect(
        selectedTopicId, selectedSubjectId, selectedExamCategory,
        selectedYearExamName, selectedYearCategory
    ) {
        val isAttemptingQuestion = (selectedTopicId != null || selectedExamCategory != null || selectedYearExamName != null)
        onToggleQuestionMode(isAttemptingQuestion)
        when {
            selectedTopicId != null -> {
                onHeaderUpdate(selectedTopicName, "Attempt Questions", { selectedTopicId = null })
            }
            selectedSubjectId != null -> {
                onHeaderUpdate(selectedSubjectName, "Select Topic", { selectedSubjectId = null })
            }
            selectedExamCategory != null -> {
                onHeaderUpdate(selectedExamCategory, "Exam PYQs", { selectedExamCategory = null })
            }
            selectedYearExamName != null -> {
                onHeaderUpdate(selectedYearExamName, "Year $selectedYearCategory PYQs", { selectedYearExamName = null })
            }
            selectedYearCategory != null -> {
                onHeaderUpdate("$selectedYearCategory Exams", "Select Exam Paper", { selectedYearCategory = null })
            }
            else -> {
                onHeaderUpdate(null, null, null)
            }
        }
    }

    when {
        selectedTopicId != null -> {
            QuestionAttemptScreen(
                topicId = selectedTopicId!!,
                title = selectedTopicName,
                onBack = { selectedTopicId = null },
                onSaveQuestion = onSaveQuestion,
                isVibrationEnabled = isVibrationEnabled
            )
        }
        selectedSubjectId != null -> {
            TopicListScreen(
                subjectId = selectedSubjectId!!,
                subjectName = selectedSubjectName,
                onBack = { selectedSubjectId = null },
                onTopicSelected = { tId, tName ->
                    selectedTopicId = tId
                    selectedTopicName = tName
                }
            )
        }
        selectedExamCategory != null -> {
            QuestionAttemptScreen(
                examCategory = selectedExamCategory,
                title = selectedExamCategory!!,
                onBack = { selectedExamCategory = null },
                onSaveQuestion = onSaveQuestion,
                isVibrationEnabled = isVibrationEnabled
            )
        }
        selectedYearExamName != null -> {
            QuestionAttemptScreen(
                examName = selectedYearExamName,
                examYear = selectedYearCategory?.toIntOrNull(),
                title = "$selectedYearExamName - $selectedYearCategory",
                onBack = { selectedYearExamName = null },
                onSaveQuestion = onSaveQuestion,
                isVibrationEnabled = isVibrationEnabled
            )
        }
        selectedYearCategory != null -> {
            YearExamListScreen(
                yearLabel = selectedYearCategory!!,
                totalPyqs = selectedYearCategoryPyqs,
                onBack = { selectedYearCategory = null },
                onExamSelected = { rawExamName, _ ->
                    selectedYearExamName = rawExamName
                }
            )
        }
        else -> {

    // Fetch data on first visit in session, and reuse cache for subsequent visits
    LaunchedEffect(refreshKey) {
        if (refreshKey == 0 && pyqSubjectsCache != null && pyqExamsCache != null && pyqYearsCache != null) {
            subjects = pyqSubjectsCache!!
            exams = pyqExamsCache!!
            years = pyqYearsCache!!
            isLoading = false
            return@LaunchedEffect
        }

        try {
            isLoading = (pyqSubjectsCache == null)
            withContext(Dispatchers.IO) {
                val healthResp = RetrofitClient.apiService.healthCheck()
                isConnected = healthResp.status == "success"

                val subjectsResp = RetrofitClient.apiService.getSubjects()
                if (subjectsResp.status == "success") {
                    subjects = subjectsResp.data
                    pyqSubjectsCache = subjectsResp.data
                }

                val examsResp = RetrofitClient.apiService.getExams()
                if (examsResp.status == "success") {
                    exams = examsResp.data
                    pyqExamsCache = examsResp.data
                }

                val yearsResp = RetrofitClient.apiService.getYears()
                if (yearsResp.status == "success") {
                    years = yearsResp.data
                    pyqYearsCache = yearsResp.data
                }
            }
            errorMessage = null
        } catch (e: Exception) {
            if (pyqSubjectsCache == null) errorMessage = e.message
            isConnected = false
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("pyq_bank_screen")
    ) {
        // Subject-wise / Exam-wise / Year-wise Toggle Tabs (3 Tabs synced with HorizontalPager)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Subject-wise tab (Page 0)
            Button(
                onClick = {
                    coroutineScope.launch { pagerState.animateScrollToPage(0) }
                },
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f), RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)),
                shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (pagerState.currentPage == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                ),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Text(
                    text = "विषयवार",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (pagerState.currentPage == 0) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }

            // Exam-wise tab (Page 1)
            Button(
                onClick = {
                    coroutineScope.launch { pagerState.animateScrollToPage(1) }
                },
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f), RoundedCornerShape(0.dp)),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (pagerState.currentPage == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                ),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Text(
                    text = "परीक्षावार",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (pagerState.currentPage == 1) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }

            // Year-wise tab (Page 2)
            Button(
                onClick = {
                    coroutineScope.launch { pagerState.animateScrollToPage(2) }
                },
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f), RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)),
                shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (pagerState.currentPage == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                ),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Text(
                    text = "वर्षवार",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (pagerState.currentPage == 2) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content Area with Shimmer Skeleton Loading & Swipeable HorizontalPager
        if (isLoading) {
            ShimmerLoadingGrid()
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Connection Error",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorMessage ?: "Unknown error",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> { // Subject-wise View
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            itemsIndexed(subjects) { index, subject ->
                                SubjectExamCard(
                                    number = String.format("%02d", index + 1),
                                    titleMr = subject.nameMr,
                                    titleEn = subject.nameEn,
                                    pyqCount = subject.pyqCount,
                                    onClick = {
                                        selectedSubjectId = subject.id.toIntOrNull() ?: 0
                                        selectedSubjectName = subject.nameMr.ifEmpty { subject.nameEn }
                                    }
                                )
                            }
                        }
                    }

                    1 -> { // Exam-wise View
                        val majorSections = remember(exams) {
                            val majors = exams.filter { it.parentId == 0 || it.id in 1..3 }
                            val minorsMap = exams.filter { it.parentId > 0 }.groupBy { it.parentId }
                            
                            majors.map { major ->
                                val minors = minorsMap[major.id] ?: emptyList()
                                Pair(major, minors)
                            }
                        }

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            var majorCounter = 1
                            var subCounter = 1

                            majorSections.forEach { (majorExam, subExams) ->
                                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                                    GrayStripHeader(title = majorExam.categoryName)
                                }

                                val allSectionCards = listOf(majorExam) + subExams
                                itemsIndexed(allSectionCards) { _, examCategory ->
                                    val isMajor = examCategory.parentId == 0
                                    SubjectExamCard(
                                        number = String.format("%02d", subCounter++),
                                        titleMr = examCategory.categoryName,
                                        titleEn = "${examCategory.yearCount} Years",
                                        pyqCount = "${examCategory.pyqCount}",
                                        onClick = {
                                            selectedExamCategory = examCategory.categoryName
                                            selectedExamCategoryPyqs = examCategory.pyqCount
                                        }
                                    )
                                }
                            }
                        }
                    }

                    2 -> { // Year-wise View
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            itemsIndexed(years) { index, yearCategory ->
                                SubjectExamCard(
                                    number = String.format("%02d", index + 1),
                                    titleMr = "वर्ष ${yearCategory.year}",
                                    titleEn = "${yearCategory.examCount} Exams",
                                    pyqCount = "${yearCategory.pyqCount}",
                                    onClick = {
                                        selectedYearCategory = yearCategory.year
                                        selectedYearCategoryPyqs = yearCategory.pyqCount
                                    }
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
}

@Composable
fun GrayStripHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 2.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SubjectExamCard(
    number: String,
    titleMr: String,
    titleEn: String,
    pyqCount: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Number Badge (01, 02...)
            Text(
                text = number,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Thin Vertical Divider Line
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(32.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Title and Question count
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titleMr,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )

                if (pyqCount.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (pyqCount.endsWith("Qs") || pyqCount.contains("प्रश्न")) pyqCount else "$pyqCount Qs",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

// PYQCardItem - Used by BookmarksScreen & PYQBankScreen to display individual question cards
@Composable
fun PYQCardItem(
    question: QuestionEntity,
    languageMode: LanguageMode,
    isStudyMode: Boolean,
    onToggleBookmark: () -> Unit,
    showSubjectTag: Boolean = true,
    onReportClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isSolutionExpanded by remember { mutableStateOf(isStudyMode) }
    var selectedOptionByUser by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pyq_card_${question.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Tags & Bookmark
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = TestbookNavy.copy(alpha = 0.08f)
                    ) {
                        Text(
                            text = "${question.examType} ${question.year}",
                            color = TestbookNavy,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (showSubjectTag) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = TestbookGold.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = question.subject,
                                color = Color(0xFFB45309),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("bookmark_btn_${question.id}")
                ) {
                    Icon(
                        imageVector = if (question.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (question.isBookmarked) TestbookGold else Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Question Text (Strictly in active languageMode)
            val qMrClean = question.questionMarathi.cleanHtml()
            val qEnClean = question.questionEnglish.cleanHtml()

            val questionText = if (languageMode == LanguageMode.MARATHI) {
                qMrClean.ifEmpty { qEnClean }
            } else {
                qEnClean.ifEmpty { qMrClean }
            }

            Text(
                text = if (languageMode == LanguageMode.MARATHI) "प्र. ${question.id}. $questionText" else "Q${question.id}. $questionText",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Options 1 to 4
            val optionsList = listOf(
                1 to (if (languageMode == LanguageMode.MARATHI) question.option1Marathi else question.option1English).cleanHtml(),
                2 to (if (languageMode == LanguageMode.MARATHI) question.option2Marathi else question.option2English).cleanHtml(),
                3 to (if (languageMode == LanguageMode.MARATHI) question.option3Marathi else question.option3English).cleanHtml(),
                4 to (if (languageMode == LanguageMode.MARATHI) question.option4Marathi else question.option4English).cleanHtml()
            )

            optionsList.forEach { optionPair ->
                val num = optionPair.first
                val text = optionPair.second
                val isCorrect = (num == question.correctOption)
                val isSelected = (num == selectedOptionByUser)

                val optionBg = when {
                    isSolutionExpanded && isCorrect -> Color(0xFF059669).copy(alpha = 0.2f)
                    isSelected && !isCorrect && isSolutionExpanded -> Color(0xFFDC2626).copy(alpha = 0.2f)
                    isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }

                val optionBorder = when {
                    isSolutionExpanded && isCorrect -> TestbookEmerald
                    isSelected && !isCorrect && isSolutionExpanded -> Color(0xFFEF4444)
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                }

                val tickBoxBg = when {
                    isSolutionExpanded && isCorrect -> TestbookEmerald
                    isSelected && !isCorrect && isSolutionExpanded -> Color(0xFFDC2626)
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.surface
                }

                val tickBoxTextColor = when {
                    isSolutionExpanded && isCorrect || isSelected -> Color.White
                    else -> MaterialTheme.colorScheme.onSurface
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clickable {
                            selectedOptionByUser = num
                            if (!isSolutionExpanded) isSolutionExpanded = true
                        },
                    shape = RoundedCornerShape(8.dp),
                    color = optionBg,
                    border = BorderStroke(1.dp, optionBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .background(
                                    tickBoxBg,
                                    shape = RoundedCornerShape(4.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "($num)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = tickBoxTextColor
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = text,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )

                        if (isSolutionExpanded && isCorrect) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Correct Answer",
                                tint = TestbookEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Expand Explanation Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { isSolutionExpanded = !isSolutionExpanded },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("solution_toggle_${question.id}")
                ) {
                    Icon(
                        imageVector = if (isSolutionExpanded) Icons.Default.Visibility else Icons.Default.Lightbulb,
                        contentDescription = "Solution",
                        tint = TestbookNavy,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSolutionExpanded) "स्पष्टीकरण लपवा" else "उत्तर व स्पष्टीकरण पहा",
                        fontSize = 11.sp,
                        color = TestbookNavy,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Explanation Box
            if (isSolutionExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Tip",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "योग्य उत्तर: पर्याय (${question.correctOption})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (languageMode == LanguageMode.MARATHI) question.explanationMarathi else question.explanationEnglish,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                        if (question.tags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "संदर्भ/टॅग्स: ${question.tags}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionReportDialog(
    questionId: Int,
    onDismiss: () -> Unit,
    onSubmit: (reportType: String, comment: String) -> Unit
) {
    val reportTypes = listOf(
        "Question is Wrong (प्रश्न किंवा उत्तर चुकीचे आहे)",
        "Options Error (पर्यायांमध्ये चूक आहे)",
        "Spelling / Language Error (भाषांतर / स्पेलिंग चूक)",
        "General Feedback (अभिप्राय / फीडबॅक)"
    )
    var selectedType by remember { mutableStateOf(reportTypes[0]) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var commentText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "प्रश्न रिपोर्ट करा (Report Q#$questionId)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "समस्या प्रकार निवडा (Choose Issue Type):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        onClick = { isDropdownExpanded = true },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedType,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown"
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        reportTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(text = type, fontSize = 12.sp) },
                                onClick = {
                                    selectedType = type
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "स्पष्टीकरण किंवा मत लिहा (Write Details):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("उदा. पर्याय २ बरोबर आहे किंवा स्पेलिंग चूक आहे...", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(8.dp),
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (commentText.isNotBlank()) {
                        onSubmit(selectedType, commentText)
                    }
                },
                enabled = commentText.isNotBlank()
            ) {
                Text("सबमिट करा (Submit)")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("रद्द करा (Cancel)")
            }
        }
    )
}

@Composable
fun ShimmerLoadingGrid() {
    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val alphaAnim by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(8) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(105.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = alphaAnim * 0.10f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                Box(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                    Column {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alphaAnim * 0.25f))
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alphaAnim * 0.35f))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .height(12.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alphaAnim * 0.20f))
                        )
                    }
                }
            }
        }
    }
}
