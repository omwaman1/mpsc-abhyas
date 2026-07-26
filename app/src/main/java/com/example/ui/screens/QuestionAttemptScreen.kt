package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.ApiQuestion
import com.example.data.remote.RetrofitClient
import com.example.ui.components.TopHeaderBar
import com.example.ui.theme.TestbookEmerald
import com.example.ui.theme.TestbookNavy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun QuestionAttemptScreen(
    topicId: Int? = null,
    examName: String? = null,
    examCategory: String? = null,
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var questions by remember { mutableStateOf<List<ApiQuestion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentIndex by remember { mutableIntStateOf(0) }
    val userAnswers = remember { mutableStateMapOf<Int, Int>() } // questionIndex -> selectedOption
    val showSolution = remember { mutableStateMapOf<Int, Boolean>() } // questionIndex -> show

    LaunchedEffect(topicId, examName, examCategory) {
        try {
            isLoading = true
            val resp = withContext(Dispatchers.IO) {
                RetrofitClient.apiService.getQuestions(
                    topicId = topicId,
                    examName = examName,
                    examCategory = examCategory,
                    limit = 100
                )
            }
            if (resp.status == "success") {
                questions = resp.data
            }
            errorMessage = null
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
    ) {
        // Progress Bar
        if (questions.isNotEmpty()) {
            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / questions.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
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
                Text("No questions found for this topic.", color = Color(0xFF64748B))
            }
        } else {
            val q = questions[currentIndex]
            val selectedOpt = userAnswers[currentIndex]
            val answered = selectedOpt != null
            val correctAns = q.correctAnswer.toIntOrNull() ?: 0
            val isShowingSolution = showSolution[currentIndex] == true

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Question Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Exam badge
                        if (!q.examName.isNullOrEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = TestbookNavy.copy(alpha = 0.08f)
                            ) {
                                Text(
                                    text = q.examName,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TestbookNavy
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        Text(
                            text = "Q${currentIndex + 1}. ${q.questionMr ?: q.questionEn ?: ""}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            lineHeight = 22.sp
                        )

                        if (!q.questionEn.isNullOrEmpty() && !q.questionMr.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = q.questionEn,
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Options
                val options = listOf(
                    1 to (q.opt1Mr ?: q.opt1En ?: "Option 1"),
                    2 to (q.opt2Mr ?: q.opt2En ?: "Option 2"),
                    3 to (q.opt3Mr ?: q.opt3En ?: "Option 3"),
                    4 to (q.opt4Mr ?: q.opt4En ?: "Option 4")
                )

                options.forEach { (num, text) ->
                    val isCorrect = (num == correctAns)
                    val isSelected = (num == selectedOpt)

                    val bg = when {
                        answered && isCorrect -> Color(0xFFDCFCE7)
                        answered && isSelected && !isCorrect -> Color(0xFFFEE2E2)
                        isSelected -> TestbookNavy.copy(alpha = 0.08f)
                        else -> Color.White
                    }
                    val borderColor = when {
                        answered && isCorrect -> TestbookEmerald
                        answered && isSelected && !isCorrect -> Color(0xFFEF4444)
                        isSelected -> TestbookNavy
                        else -> Color(0xFFE2E8F0)
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable(enabled = !answered) {
                                userAnswers[currentIndex] = num
                                showSolution[currentIndex] = true
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = bg,
                        border = BorderStroke(1.5.dp, borderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Option number circle
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when {
                                            answered && isCorrect -> TestbookEmerald
                                            answered && isSelected && !isCorrect -> Color(0xFFEF4444)
                                            else -> Color(0xFFF1F5F9)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$num",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (answered && (isCorrect || isSelected)) Color.White else Color(0xFF475569)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = text,
                                fontSize = 13.sp,
                                color = Color(0xFF1E293B),
                                modifier = Modifier.weight(1f),
                                lineHeight = 18.sp
                            )

                            if (answered && isCorrect) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Correct",
                                    tint = TestbookEmerald,
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

                // Solution/Explanation
                if (isShowingSolution && answered) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = TestbookNavy,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Correct Answer: Option $correctAns",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = TestbookNavy
                                )
                            }
                            val solution = q.solutionMr ?: q.solutionEn
                            if (!solution.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = solution,
                                    fontSize = 12.sp,
                                    color = Color(0xFF334155),
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp)) // space for nav buttons
            }

            // Bottom Navigation Buttons
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (currentIndex > 0) currentIndex--
                        },
                        enabled = currentIndex > 0,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            Icons.Default.NavigateBefore,
                            contentDescription = "Previous",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Previous", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Question counter pill
                    Text(
                        text = "${currentIndex + 1} / ${questions.size}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TestbookNavy
                    )

                    Button(
                        onClick = {
                            if (currentIndex < questions.size - 1) currentIndex++
                        },
                        enabled = currentIndex < questions.size - 1,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TestbookNavy),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text("Next", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.NavigateNext,
                            contentDescription = "Next",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
