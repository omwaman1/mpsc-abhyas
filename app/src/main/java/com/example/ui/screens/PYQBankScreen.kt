package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.data.remote.ApiSubject
import com.example.data.remote.ApiYearCategory
import com.example.data.remote.RetrofitClient
import com.example.ui.theme.TestbookEmerald
import com.example.ui.theme.TestbookGold
import com.example.ui.theme.TestbookNavy
import com.example.ui.viewmodel.LanguageMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class PYQViewMode {
    SUBJECT_WISE, EXAM_WISE, YEAR_WISE
}

@Composable
fun PYQBankScreen(
    onHeaderUpdate: (title: String?, subtitle: String?, backAction: (() -> Unit)?) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(PYQViewMode.SUBJECT_WISE) }
    var subjects by remember { mutableStateOf<List<ApiSubject>>(emptyList()) }
    var exams by remember { mutableStateOf<List<ApiExamCategory>>(emptyList()) }
    var years by remember { mutableStateOf<List<ApiYearCategory>>(emptyList()) }
    var isConnected by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Navigation state for Subject drill-down
    var selectedSubjectId by remember { mutableStateOf<Int?>(null) }
    var selectedSubjectName by remember { mutableStateOf("") }
    var selectedTopicId by remember { mutableStateOf<Int?>(null) }
    var selectedTopicName by remember { mutableStateOf("") }

    // Navigation state for Exam drill-down
    var selectedExamCategory by remember { mutableStateOf<String?>(null) }
    var selectedExamCategoryPyqs by remember { mutableStateOf(0) }
    var selectedExamYearName by remember { mutableStateOf<String?>(null) }
    var selectedExamYearLabel by remember { mutableStateOf<String?>(null) }
    var isAllYearsSelected by remember { mutableStateOf(false) }

    // Navigation state for Year-wise drill-down
    var selectedYearCategory by remember { mutableStateOf<String?>(null) }
    var selectedYearCategoryPyqs by remember { mutableStateOf(0) }
    var selectedYearExamName by remember { mutableStateOf<String?>(null) }

    // Update parent main top bar dynamically
    LaunchedEffect(
        selectedTopicId, selectedSubjectId, selectedExamYearName,
        isAllYearsSelected, selectedExamCategory, selectedYearExamName, selectedYearCategory
    ) {
        when {
            selectedTopicId != null -> {
                onHeaderUpdate(selectedTopicName, "Question Practice", { selectedTopicId = null })
            }
            selectedSubjectId != null -> {
                onHeaderUpdate(selectedSubjectName, "Select Topic to Practice", { selectedSubjectId = null })
            }
            selectedExamYearName != null -> {
                onHeaderUpdate("$selectedExamCategory - $selectedExamYearLabel", "Question Practice", { selectedExamYearName = null })
            }
            isAllYearsSelected && selectedExamCategory != null -> {
                onHeaderUpdate("$selectedExamCategory (All Years)", "Question Practice", { isAllYearsSelected = false })
            }
            selectedExamCategory != null -> {
                onHeaderUpdate(selectedExamCategory, "Select Year ($selectedExamCategoryPyqs Questions)", { selectedExamCategory = null })
            }
            selectedYearExamName != null -> {
                onHeaderUpdate(selectedYearExamName, "Question Practice", { selectedYearExamName = null })
            }
            selectedYearCategory != null -> {
                onHeaderUpdate("Year $selectedYearCategory Exams", "Select Exam to Practice", { selectedYearCategory = null })
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
                onBack = { selectedTopicId = null }
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
        selectedExamYearName != null -> {
            QuestionAttemptScreen(
                examName = selectedExamYearName,
                title = "$selectedExamCategory - $selectedExamYearLabel",
                onBack = { selectedExamYearName = null }
            )
        }
        isAllYearsSelected && selectedExamCategory != null -> {
            QuestionAttemptScreen(
                examCategory = selectedExamCategory,
                title = "$selectedExamCategory (All Years)",
                onBack = { isAllYearsSelected = false }
            )
        }
        selectedExamCategory != null -> {
            ExamYearListScreen(
                categoryName = selectedExamCategory!!,
                totalPyqs = selectedExamCategoryPyqs,
                onBack = { selectedExamCategory = null },
                onYearSelected = { rawExamName, yearLabel ->
                    selectedExamYearName = rawExamName
                    selectedExamYearLabel = yearLabel
                },
                onAllYearsSelected = { category ->
                    isAllYearsSelected = true
                }
            )
        }
        selectedYearExamName != null -> {
            QuestionAttemptScreen(
                examName = selectedYearExamName,
                title = selectedYearExamName!!,
                onBack = { selectedYearExamName = null }
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

    // Fetch data on first composition
    LaunchedEffect(Unit) {
        try {
            isLoading = true
            withContext(Dispatchers.IO) {
                val healthResp = RetrofitClient.apiService.healthCheck()
                isConnected = healthResp.status == "success"

                val subjectsResp = RetrofitClient.apiService.getSubjects()
                if (subjectsResp.status == "success") {
                    subjects = subjectsResp.data
                }

                val examsResp = RetrofitClient.apiService.getExams()
                if (examsResp.status == "success") {
                    exams = examsResp.data
                }

                val yearsResp = RetrofitClient.apiService.getYears()
                if (yearsResp.status == "success") {
                    years = yearsResp.data
                }
            }
            errorMessage = null
        } catch (e: Exception) {
            errorMessage = e.message
            isConnected = false
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .testTag("pyq_bank_screen")
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Subject-wise / Exam-wise / Year-wise Toggle Tabs (3 Tabs)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Subject-wise tab
            Button(
                onClick = { viewMode = PYQViewMode.SUBJECT_WISE },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewMode == PYQViewMode.SUBJECT_WISE) TestbookNavy else Color.White
                ),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Text(
                    text = "विषयवार",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (viewMode == PYQViewMode.SUBJECT_WISE) Color.White else Color(0xFF475569)
                )
            }

            // Exam-wise tab
            Button(
                onClick = { viewMode = PYQViewMode.EXAM_WISE },
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(0.dp)),
                shape = RoundedCornerShape(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewMode == PYQViewMode.EXAM_WISE) TestbookNavy else Color.White
                ),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Text(
                    text = "परीक्षावार",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (viewMode == PYQViewMode.EXAM_WISE) Color.White else Color(0xFF475569)
                )
            }

            // Year-wise tab
            Button(
                onClick = { viewMode = PYQViewMode.YEAR_WISE },
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)),
                shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewMode == PYQViewMode.YEAR_WISE) TestbookNavy else Color.White
                ),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Text(
                    text = "वर्षवार",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (viewMode == PYQViewMode.YEAR_WISE) Color.White else Color(0xFF475569)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content Area
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = TestbookNavy)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Fetching from TiDB Cloud...",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
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
            when (viewMode) {
                PYQViewMode.SUBJECT_WISE -> {
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

                PYQViewMode.EXAM_WISE -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        itemsIndexed(exams) { index, examCategory ->
                            SubjectExamCard(
                                number = String.format("%02d", index + 1),
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

                PYQViewMode.YEAR_WISE -> {
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Number
            Text(
                text = number,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Vertical divider
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(40.dp)
                    .background(Color(0xFFE2E8F0))
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Title and PYQ count
            Column(modifier = Modifier.weight(1f)) {
                if (titleEn.isNotEmpty()) {
                    Text(
                        text = "$titleMr ($titleEn)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 17.sp
                    )
                } else {
                    Text(
                        text = titleMr,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 17.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "$pyqCount प्रश्न (PYQs)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TestbookEmerald
                )
            }
        }
    }
}

// PYQCardItem - Used by BookmarksScreen to display individual question cards
@Composable
fun PYQCardItem(
    question: QuestionEntity,
    languageMode: LanguageMode,
    isStudyMode: Boolean,
    onToggleBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSolutionExpanded by remember { mutableStateOf(isStudyMode) }
    var selectedOptionByUser by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pyq_card_${question.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    verticalAlignment = Alignment.CenterVertically
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

            // Question Text
            Text(
                text = if (languageMode == LanguageMode.MARATHI) "प्र. ${question.id}. ${question.questionMarathi}" else "Q${question.id}. ${question.questionEnglish}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                lineHeight = 20.sp
            )

            if (languageMode == LanguageMode.MARATHI && question.questionEnglish.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = question.questionEnglish,
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Options 1 to 4
            val optionsList = listOf(
                1 to (if (languageMode == LanguageMode.MARATHI) question.option1Marathi else question.option1English),
                2 to (if (languageMode == LanguageMode.MARATHI) question.option2Marathi else question.option2English),
                3 to (if (languageMode == LanguageMode.MARATHI) question.option3Marathi else question.option3English),
                4 to (if (languageMode == LanguageMode.MARATHI) question.option4Marathi else question.option4English)
            )

            optionsList.forEach { (num, text) ->
                val isCorrect = (num == question.correctOption)
                val isSelected = (num == selectedOptionByUser)

                val optionBg = when {
                    isSolutionExpanded && isCorrect -> Color(0xFFDCFCE7)
                    isSelected && !isCorrect && isSolutionExpanded -> Color(0xFFFEE2E2)
                    isSelected -> TestbookNavy.copy(alpha = 0.1f)
                    else -> Color(0xFFF8FAFC)
                }

                val optionBorder = when {
                    isSolutionExpanded && isCorrect -> TestbookEmerald
                    isSelected && !isCorrect && isSolutionExpanded -> Color(0xFFEF4444)
                    isSelected -> TestbookNavy
                    else -> Color(0xFFE2E8F0)
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
                                    if (isSolutionExpanded && isCorrect) TestbookEmerald else Color.White,
                                    shape = RoundedCornerShape(4.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "($num)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSolutionExpanded && isCorrect) Color.White else Color(0xFF334155)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = text,
                            fontSize = 12.sp,
                            color = Color(0xFF1E293B),
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
                    color = Color(0xFFEFF6FF),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Tip",
                                tint = TestbookNavy,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "योग्य उत्तर: पर्याय (${question.correctOption})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = TestbookNavy
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (languageMode == LanguageMode.MARATHI) question.explanationMarathi else question.explanationEnglish,
                            fontSize = 11.sp,
                            color = Color(0xFF1E293B),
                            lineHeight = 16.sp
                        )
                        if (question.tags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "संदर्भ/टॅग्स: ${question.tags}",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
