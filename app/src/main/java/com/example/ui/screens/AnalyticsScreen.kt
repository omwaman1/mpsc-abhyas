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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.TestAttemptEntity
import com.example.ui.theme.TestbookEmerald
import com.example.ui.theme.TestbookGold
import com.example.ui.theme.TestbookNavy
import com.example.ui.theme.TestbookOrange
import com.example.ui.viewmodel.LanguageMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnalyticsScreen(
    attempts: List<TestAttemptEntity>,
    languageMode: LanguageMode,
    onReviewAttempt: (TestAttemptEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalTests = attempts.size
    val avgAccuracy = if (attempts.isNotEmpty()) attempts.map { it.accuracyPercentage }.average().toFloat() else 0f
    val avgScore = if (attempts.isNotEmpty()) attempts.map { it.score }.average().toFloat() else 0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .testTag("analytics_lazy_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Overall Performance Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TestbookNavy),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = "Analytics",
                                tint = TestbookGold,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (languageMode == LanguageMode.MARATHI) "माझी कामगिरी (Performance Scorecard)" else "My Performance Analytics",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E293B)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = "Rank",
                                    tint = TestbookGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "State Rank #124",
                                    color = TestbookGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatBox(
                            title = if (languageMode == LanguageMode.MARATHI) "एकूण चाचण्या" else "Tests Taken",
                            value = "$totalTests",
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            title = if (languageMode == LanguageMode.MARATHI) "सरासरी अचूकता" else "Avg Accuracy",
                            value = "${String.format(Locale.getDefault(), "%.1f", avgAccuracy)}%",
                            color = TestbookEmerald,
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            title = if (languageMode == LanguageMode.MARATHI) "सरासरी गुण" else "Avg Marks",
                            value = String.format(Locale.getDefault(), "%.1f", avgScore),
                            color = TestbookGold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Subject Proficiency Radar/Bar Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "Subjects",
                                tint = TestbookNavy,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (languageMode == LanguageMode.MARATHI) "विषयनिहाय पकड (Subject Strengths)" else "Subject Mastery",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TestbookNavy
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    SubjectProgressBar("राज्यशास्त्र (Polity)", 0.85f, TestbookEmerald)
                    Spacer(modifier = Modifier.height(10.dp))
                    SubjectProgressBar("इतिहास (History)", 0.65f, TestbookOrange)
                    Spacer(modifier = Modifier.height(10.dp))
                    SubjectProgressBar("भूगोल (Geography)", 0.78f, Color(0xFF0284C7))
                    Spacer(modifier = Modifier.height(10.dp))
                    SubjectProgressBar("अर्थशास्त्र (Economics)", 0.70f, TestbookGold)
                    Spacer(modifier = Modifier.height(10.dp))
                    SubjectProgressBar("अंकगणित व CSAT", 0.92f, TestbookEmerald)
                }
            }
        }

        // Recent Attempts History
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (languageMode == LanguageMode.MARATHI) "चाचण्यांचा इतिहास (Test Attempts History)" else "Attempt History",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TestbookNavy
                )
            }
        }

        if (attempts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "No History",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (languageMode == LanguageMode.MARATHI) "तुम्ही अद्याप कोणतीही टेस्ट दिलेली नाही." else "You haven't attempted any mock tests yet.",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = if (languageMode == LanguageMode.MARATHI) "सराव सुरू करण्यासाठी 'टेस्ट सिरीज' वर जा." else "Go to Test Series tab to begin practicing.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        } else {
            items(attempts) { attempt ->
                AttemptItemCard(
                    attempt = attempt,
                    languageMode = languageMode,
                    onReviewAttempt = { onReviewAttempt(attempt) }
                )
            }
        }
    }
}

@Composable
fun StatBox(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1E293B),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                color = Color(0xFF94A3B8),
                maxLines = 1
            )
        }
    }
}

@Composable
fun SubjectProgressBar(
    subjectName: String,
    progress: Float,
    barColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = subjectName,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B)
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = barColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = Color(0xFFE2E8F0)
        )
    }
}

@Composable
fun AttemptItemCard(
    attempt: TestAttemptEntity,
    languageMode: LanguageMode,
    onReviewAttempt: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val dateStr = dateFormat.format(Date(attempt.timestamp))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("attempt_item_${attempt.attemptId}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = attempt.testTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = dateStr,
                    fontSize = 10.sp,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Text(
                            text = if (languageMode == LanguageMode.MARATHI) "गुण" else "Score",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "${attempt.score}/${attempt.totalMarks}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = TestbookNavy
                        )
                    }

                    Column {
                        Text(
                            text = if (languageMode == LanguageMode.MARATHI) "अचूकता" else "Accuracy",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "${String.format(Locale.getDefault(), "%.1f", attempt.accuracyPercentage)}%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TestbookEmerald
                        )
                    }

                    Column {
                        Text(
                            text = if (languageMode == LanguageMode.MARATHI) "बरोबर/चूक" else "Correct/Wrong",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "✅${attempt.correctCount} ❌${attempt.wrongCount}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF334155)
                        )
                    }
                }

                Button(
                    onClick = onReviewAttempt,
                    colors = ButtonDefaults.buttonColors(containerColor = TestbookNavy),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("review_solutions_${attempt.attemptId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Review",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (languageMode == LanguageMode.MARATHI) "स्पष्टीकरण" else "Review",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
