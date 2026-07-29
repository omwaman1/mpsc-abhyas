package com.example.ui.screens

import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.data.local.entities.TestPaperEntity
import com.example.ui.theme.TestbookEmerald
import com.example.ui.theme.TestbookEmeraldLight
import com.example.ui.theme.TestbookNavy
import com.example.ui.viewmodel.LanguageMode

enum class TestSeriesGroupTab { PYP, FYP }

fun isPaperPyp(paper: TestPaperEntity): Boolean {
    val cat = paper.category.trim()
    return cat.equals("PYQ", ignoreCase = true) || cat.equals("PYP", ignoreCase = true)
}

fun getEffectiveSubjectName(paper: TestPaperEntity): String {
    val sub = paper.subjectName.trim()
    if (sub.isNotBlank() && !sub.equals("General", ignoreCase = true) && !sub.contains("General") && !sub.contains("सर्व विषय")) {
        return sub
    }
    
    val titleLower = paper.title.lowercase()
    return when {
        titleLower.contains("economy") || titleLower.contains("economic") || titleLower.contains("अर्थशास्त्र") || titleLower.contains("अर्थव्यवस्था") || titleLower.contains("income") || titleLower.contains("gdp") || titleLower.contains("mt 1") || titleLower.contains("mt 2") -> "अर्थव्यवस्था"
        titleLower.contains("history") || titleLower.contains("इतिहास") || titleLower.contains("tt 1") || titleLower.contains("tt 5") || titleLower.contains("tt 10") || titleLower.contains("ct 1") || titleLower.contains("ct 2") || titleLower.contains("ct 3") || titleLower.contains("ct 7") || titleLower.contains("ct 8") -> "इतिहास"
        titleLower.contains("polity") || titleLower.contains("राज्यशास्त्र") || titleLower.contains("राज्यघटना") || titleLower.contains("tt 2") || titleLower.contains("tt 6") || titleLower.contains("tt 11") -> "राज्यशास्त्र"
        titleLower.contains("geography") || titleLower.contains("भूगोल") || titleLower.contains("tt 3") || titleLower.contains("tt 7") || titleLower.contains("tt 12") || titleLower.contains("ct 4") || titleLower.contains("ct 5") || titleLower.contains("ct 6") -> "भूगोल"
        titleLower.contains("science") || titleLower.contains("विज्ञान") || titleLower.contains("tt 4") || titleLower.contains("tt 9") || titleLower.contains("tt 14") -> "सामान्य विज्ञान"
        titleLower.contains("current") || titleLower.contains("घडामोडी") -> "चालू घडामोडी"
        else -> if (sub.isNotBlank()) sub else "General"
    }
}

fun getDifficultyBadge(paper: TestPaperEntity): Triple<String, Color, Color> {
    val titleLower = paper.title.lowercase()
    return when {
        titleLower.contains("easy") || titleLower.contains("basic") || titleLower.contains("सोपे") || titleLower.contains("mt 2") -> {
            Triple("EASY", Color(0xFF34D399).copy(alpha = 0.15f), Color(0xFF34D399))
        }
        titleLower.contains("hard") || titleLower.contains("advanced") || titleLower.contains("कठीण") -> {
            Triple("HARD", Color(0xFFF87171).copy(alpha = 0.15f), Color(0xFFF87171))
        }
        else -> {
            Triple("MODERATE", Color(0xFFFBBF24).copy(alpha = 0.15f), Color(0xFFFBBF24))
        }
    }
}

@Composable
fun TestSeriesScreen(
    testPapers: List<TestPaperEntity>,
    languageMode: LanguageMode,
    onStartTest: (TestPaperEntity) -> Unit,
    onRefresh: () -> Unit,
    selectedExamCategoryParam: String? = null,
    onExamCategoryChangeParam: ((String) -> Unit)? = null,
    selectedSubjectParam: String? = null,
    onSubjectChangeParam: ((String) -> Unit)? = null,
    isLocked: Boolean = false,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })

    // Persisted exam and subject dropdown selections across tab navigation & screen re-renders
    var localSelectedExamCategory by rememberSaveable { mutableStateOf("All Exams") }
    var localSelectedSubject by rememberSaveable { mutableStateOf("All Subjects") }

    val selectedExamCategory = selectedExamCategoryParam ?: localSelectedExamCategory
    val onExamCategoryChange: (String) -> Unit = { newExam ->
        localSelectedExamCategory = newExam
        onExamCategoryChangeParam?.invoke(newExam)
    }

    val selectedSubject = selectedSubjectParam ?: localSelectedSubject
    val onSubjectChange: (String) -> Unit = { newSub ->
        localSelectedSubject = newSub
        onSubjectChangeParam?.invoke(newSub)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("test_series_screen_column")
    ) {
        // 1. TOP FIXED PYP / MOCK TESTS SWITCHER TAB BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // PYP Tab Button
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
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "PYP",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (pagerState.currentPage == 0) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Previous Year Papers",
                        fontSize = 9.sp,
                        color = if (pagerState.currentPage == 0) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // MOCK TESTS Tab Button
            Button(
                onClick = {
                    coroutineScope.launch { pagerState.animateScrollToPage(1) }
                },
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f), RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)),
                shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (pagerState.currentPage == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                ),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "MOCK TESTS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (pagerState.currentPage == 1) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Practice & Full Mocks",
                        fontSize = 9.sp,
                        color = if (pagerState.currentPage == 1) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 2. HORIZONTAL PAGER FOR SWIPE NAVIGATION BETWEEN PYP & MOCK TESTS
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            val groupTab = if (page == 0) TestSeriesGroupTab.PYP else TestSeriesGroupTab.FYP

            TestSeriesGroupPage(
                groupTab = groupTab,
                testPapers = testPapers,
                languageMode = languageMode,
                selectedExamCategory = selectedExamCategory,
                onExamCategoryChange = onExamCategoryChange,
                selectedSubject = selectedSubject,
                onSubjectChange = onSubjectChange,
                isLocked = isLocked,
                onStartTest = onStartTest
            )
        }
    }
}

@Composable
private fun TestSeriesGroupPage(
    groupTab: TestSeriesGroupTab,
    testPapers: List<TestPaperEntity>,
    languageMode: LanguageMode,
    selectedExamCategory: String,
    onExamCategoryChange: (String) -> Unit,
    selectedSubject: String,
    onSubjectChange: (String) -> Unit,
    isLocked: Boolean = false,
    onStartTest: (TestPaperEntity) -> Unit
) {
    val groupFilteredPapers = remember(groupTab, testPapers) {
        testPapers.filter { paper ->
            if (groupTab == TestSeriesGroupTab.PYP) {
                isPaperPyp(paper)
            } else {
                !isPaperPyp(paper)
            }
        }
    }

    val examCategoryChips = remember(groupFilteredPapers) {
        val list = mutableListOf("All Exams")
        val distinctExams = groupFilteredPapers.map { it.examType.trim() }.distinct().filter { it.isNotBlank() }
        list.addAll(distinctExams)
        list.distinct()
    }

    val examFilteredPapers = remember(selectedExamCategory, groupFilteredPapers) {
        groupFilteredPapers.filter { paper ->
            selectedExamCategory == "All Exams" || 
            paper.examType.trim().equals(selectedExamCategory.trim(), ignoreCase = true)
        }
    }

    val subjectChips = remember(selectedExamCategory, examFilteredPapers, groupFilteredPapers) {
        val list = mutableListOf("All Subjects")
        val papersToExtract = if (examFilteredPapers.isNotEmpty()) examFilteredPapers else groupFilteredPapers
        val distinctSubjects = papersToExtract
            .map { paper -> getEffectiveSubjectName(paper).trim() }
            .distinct()
            .filter { 
                it.isNotBlank() && 
                !it.equals("General", ignoreCase = true) && 
                !it.contains("General") && 
                !it.contains("सर्व विषय") 
            }
        list.addAll(distinctSubjects)
        list.distinct()
    }

    val finalFilteredList = remember(selectedSubject, selectedExamCategory, groupTab, examFilteredPapers) {
        if (groupTab == TestSeriesGroupTab.PYP || selectedSubject == "All Subjects") {
            examFilteredPapers
        } else {
            examFilteredPapers.filter { paper ->
                getEffectiveSubjectName(paper).trim().equals(selectedSubject.trim(), ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // SELECT EXAM DROPDOWN (FULL WIDTH)
        item {
            var isExamDropdownExpanded by remember { mutableStateOf(false) }

            Box(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    onClick = { isExamDropdownExpanded = true },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Exam: ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = selectedExamCategory,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Exam",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                DropdownMenu(
                    expanded = isExamDropdownExpanded,
                    onDismissRequest = { isExamDropdownExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp)),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    examCategoryChips.forEach { exam ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = exam,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedExamCategory == exam) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedExamCategory == exam) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                onExamCategoryChange(exam)
                                isExamDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // SELECT SUBJECT DROPDOWN (FULL WIDTH BELOW EXAM DROPDOWN)
        if (groupTab == TestSeriesGroupTab.FYP) {
            item {
                var isSubjectDropdownExpanded by remember { mutableStateOf(false) }

                Box(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        onClick = { isSubjectDropdownExpanded = true },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Subject: ",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = selectedSubject,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select Subject",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isSubjectDropdownExpanded,
                        onDismissRequest = { isSubjectDropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp)),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        subjectChips.forEach { subject ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = subject,
                                        fontSize = 13.sp,
                                        fontWeight = if (selectedSubject == subject) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedSubject == subject) TestbookEmerald else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    onSubjectChange(subject)
                                    isSubjectDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // AVAILABLE TESTS LIST WITH SHIMMER SKELETON
        if (testPapers.isEmpty()) {
            item {
                com.example.ui.components.ShimmerCardList(itemCount = 4)
            }
        } else if (finalFilteredList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = if (languageMode == LanguageMode.MARATHI) "या वर्गवारीसाठी सध्या कोणतीही टेस्ट उपलब्ध नाही." else "No test series available in this tab currently.",
                        modifier = Modifier.padding(24.dp),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            items(finalFilteredList) { paper ->
                TestSeriesCard(
                    testPaper = paper,
                    languageMode = languageMode,
                    isLocked = isLocked,
                    onStartTest = { onStartTest(paper) }
                )
            }
        }
    }
}

@Composable
fun TestSeriesCard(
    testPaper: TestPaperEntity,
    languageMode: LanguageMode,
    isLocked: Boolean = false,
    onStartTest: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onStartTest() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Badges Row: PYP/FYP Tag + Exam Type Badge + Free Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isPyp = isPaperPyp(testPaper)
                    // PYP / FYP Type Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isPyp) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = if (isPyp) "PYP PAPER" else "MOCK TEST",
                            color = if (isPyp) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    // Exam Type Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = testPaper.examType,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                val (diffText, diffBgColor, diffTextColor) = getDifficultyBadge(testPaper)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = diffBgColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = diffTextColor,
                            modifier = Modifier.size(6.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = diffText,
                            color = diffTextColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

            }

            Spacer(modifier = Modifier.height(10.dp))

            // Test Title
            Text(
                text = testPaper.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Details Row: Qs | Marks | Duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${testPaper.questionCount} Qs (${testPaper.totalMarks} Marks)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${testPaper.durationMinutes} Mins",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subtle Glass / Outlined Pastel Accent CTA Button (Shows Lock icon when Trial Expired!)
            Button(
                onClick = onStartTest,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLocked) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    contentColor = if (isLocked) Color(0xFFD97706) else MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(1.dp, if (isLocked) Color(0xFFF59E0B) else MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.PlayArrow,
                        contentDescription = if (isLocked) "Locked" else "Start Test",
                        tint = if (isLocked) Color(0xFFD97706) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isLocked) {
                            if (languageMode == LanguageMode.MARATHI) "अनलॉक करा 🔒 (Unlock Test)" else "Unlock Test 🔒"
                        } else {
                            if (languageMode == LanguageMode.MARATHI) "टेस्ट सोडवा (Start Test)" else "Start Test Now"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLocked) Color(0xFFD97706) else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
