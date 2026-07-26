package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TestbookEmerald
import com.example.ui.theme.TestbookGold
import com.example.ui.theme.TestbookNavy
import com.example.ui.viewmodel.LanguageMode
import com.example.ui.viewmodel.TestResultState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestResultScreen(
    resultState: TestResultState,
    onCloseResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val attempt = resultState.attempt
    val userAnswers = remember(attempt.userAnswersJson) {
        try {
            val clean = attempt.userAnswersJson.trim('{', '}')
            if (clean.isEmpty()) emptyMap<Int, Int>()
            else {
                clean.split(",").associate {
                    val parts = it.split(":")
                    parts[0].trim('"', ' ').toInt() to parts[1].trim(' ', '"').toInt()
                }
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = TestbookNavy,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onCloseResult,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("close_result_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "निकाल व स्पष्टीकरण (Test Analysis)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF1F5F9))
        ) {
            // Tab Switcher
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = TestbookNavy,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = TestbookNavy
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("गुण व पृथक्करण (Overview)", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("सविस्तर उत्तरे (Solutions)", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }

            if (selectedTab == 0) {
                // Score Analysis Overview
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = TestbookNavy),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = resultState.testPaper.title,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = String.format(Locale.getDefault(), "%.1f", attempt.score),
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TestbookGold
                                    )
                                    Text(
                                        text = " / ${attempt.totalMarks}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    ResultPill("अचूकता (Accuracy)", "${String.format(Locale.getDefault(), "%.1f", attempt.accuracyPercentage)}%", TestbookEmerald)
                                    ResultPill("वेळ (Time)", "${attempt.timeTakenSeconds / 60} min", Color(0xFF38BDF8))
                                    ResultPill("अंदाजित रँक", "#124 / 15k", TestbookGold)
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "प्रश्नांचे विश्लेषण (Attempt Breakdown)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TestbookNavy
                                )
                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    BreakdownItem("बरोबर (Correct)", "${attempt.correctCount}", TestbookEmerald)
                                    BreakdownItem("चुकीचे (Wrong)", "${attempt.wrongCount}", Color(0xFFEF4444))
                                    BreakdownItem("न सोडवलेले", "${attempt.unattemptedCount}", Color(0xFF64748B))
                                }
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = { selectedTab = 1 },
                            colors = ButtonDefaults.buttonColors(containerColor = TestbookNavy),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("view_detailed_solutions_btn")
                        ) {
                            Text(
                                text = "सविस्तर उत्तरे व स्पष्टीकरण पहा >",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            } else {
                // Detailed Solution Review
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    itemsIndexed(resultState.questions) { index, q ->
                        val userPick = userAnswers[q.id]
                        val isCorrect = (userPick == q.correctOption)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Q.${index + 1}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = TestbookNavy
                                    )

                                    val statusTag = when {
                                        userPick == null -> "Skipped" to Color(0xFF64748B)
                                        isCorrect -> "Correct (+2)" to TestbookEmerald
                                        else -> "Wrong (-0.5)" to Color(0xFFEF4444)
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = statusTag.second.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = statusTag.first,
                                            color = statusTag.second,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = if (resultState.languageMode == LanguageMode.MARATHI) q.questionMarathi else q.questionEnglish,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF0F172A)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                val opts = listOf(
                                    1 to (if (resultState.languageMode == LanguageMode.MARATHI) q.option1Marathi else q.option1English),
                                    2 to (if (resultState.languageMode == LanguageMode.MARATHI) q.option2Marathi else q.option2English),
                                    3 to (if (resultState.languageMode == LanguageMode.MARATHI) q.option3Marathi else q.option3English),
                                    4 to (if (resultState.languageMode == LanguageMode.MARATHI) q.option4Marathi else q.option4English)
                                )

                                opts.forEach { (num, text) ->
                                    val isCorrectOpt = (num == q.correctOption)
                                    val isUserPick = (num == userPick)

                                    val bg = when {
                                        isCorrectOpt -> Color(0xFFDCFCE7)
                                        isUserPick && !isCorrectOpt -> Color(0xFFFEE2E2)
                                        else -> Color(0xFFF8FAFC)
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        color = bg,
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isCorrectOpt) TestbookEmerald else if (isUserPick) Color(0xFFEF4444) else Color(0xFFE2E8F0)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "($num) $text",
                                                fontSize = 12.sp,
                                                color = Color(0xFF1E293B),
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (isCorrectOpt) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Correct",
                                                    tint = TestbookEmerald,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            } else if (isUserPick) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Wrong Pick",
                                                    tint = Color(0xFFEF4444),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFEFF6FF)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Lightbulb,
                                                contentDescription = "Explanation",
                                                tint = TestbookNavy,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "स्पष्टीकरण (Explanation):",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = TestbookNavy
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (resultState.languageMode == LanguageMode.MARATHI) q.explanationMarathi else q.explanationEnglish,
                                            fontSize = 11.sp,
                                            color = Color(0xFF334155),
                                            lineHeight = 15.sp
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
fun ResultPill(title: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = color)
        Text(text = title, fontSize = 10.sp, color = Color(0xFF94A3B8))
    }
}

@Composable
fun BreakdownItem(label: String, count: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 11.sp, color = Color(0xFF64748B))
    }
}
