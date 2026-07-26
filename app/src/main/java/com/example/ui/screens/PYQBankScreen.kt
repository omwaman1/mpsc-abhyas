package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.QuestionEntity
import com.example.ui.theme.TestbookEmerald
import com.example.ui.theme.TestbookGold
import com.example.ui.theme.TestbookNavy
import com.example.ui.viewmodel.LanguageMode

@Composable
fun PYQBankScreen(
    questions: List<QuestionEntity>,
    languageMode: LanguageMode,
    selectedExam: String,
    selectedSubject: String,
    selectedYear: String,
    onExamFilterChanged: (String) -> Unit,
    onSubjectFilterChanged: (String) -> Unit,
    onYearFilterChanged: (String) -> Unit,
    onToggleBookmark: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val examOptions = listOf("All Exams", "Rajyaseva", "Combine", "Subordinate")
    val subjectOptions = listOf("All Subjects", "इतिहास", "भूगोल", "राज्यशास्त्र", "अर्थशास्त्र", "सामान्य विज्ञान", "CSAT")
    val yearOptions = listOf("All Years", "2024", "2023", "2022", "2021")

    var isStudyMode by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .testTag("pyq_bank_lazy_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Mode Header & Switch
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = TestbookNavy),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (languageMode == LanguageMode.MARATHI) "MPSC मूळ प्रश्नपत्रिका (PYQ Archive)" else "MPSC Question Bank (PYQs)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isStudyMode) "अभ्यास मोड (उत्तर व स्पष्टीकरणासह)" else "सरावासाठी उत्तर लपवा",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (languageMode == LanguageMode.MARATHI) "अभ्यास मोड" else "Study Mode",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = isStudyMode,
                            onCheckedChange = { isStudyMode = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = TestbookEmerald
                            )
                        )
                    }
                }
            }
        }

        // Exam Chips Filter
        item {
            Column {
                Text(
                    text = if (languageMode == LanguageMode.MARATHI) "परीक्षा (Exam):" else "Exam Filter:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569)
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(examOptions) { opt ->
                        FilterChip(
                            selected = (selectedExam == opt),
                            onClick = { onExamFilterChanged(opt) },
                            label = { Text(opt, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TestbookNavy,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Subject Chips Filter
        item {
            Column {
                Text(
                    text = if (languageMode == LanguageMode.MARATHI) "विषय (Subject):" else "Subject Filter:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569)
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(subjectOptions) { opt ->
                        FilterChip(
                            selected = (selectedSubject == opt),
                            onClick = { onSubjectFilterChanged(opt) },
                            label = { Text(opt, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TestbookNavy,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Year Filter
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${questions.size} ${if (languageMode == LanguageMode.MARATHI) "प्रश्न सापडले" else "Questions found"}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TestbookNavy
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(yearOptions) { yr ->
                        FilterChip(
                            selected = (selectedYear == yr),
                            onClick = { onYearFilterChanged(yr) },
                            label = { Text(yr, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TestbookEmerald,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }

        // Questions List
        if (questions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "कोणतेही प्रश्न सापडले नाहीत.",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "कृपया फिल्टर पर्याय बदला.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        } else {
            items(questions) { question ->
                PYQCardItem(
                    question = question,
                    languageMode = languageMode,
                    isStudyMode = isStudyMode,
                    onToggleBookmark = { onToggleBookmark(question.id, question.isBookmarked) }
                )
            }
        }
    }
}

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

            // Question Text (Marathi / English based on setting or dual display)
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
                    border = androidx.compose.foundation.BorderStroke(1.dp, optionBorder)
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
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
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
