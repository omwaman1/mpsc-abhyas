package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.TestbookOrange
import com.example.ui.viewmodel.LanguageMode
import com.example.ui.viewmodel.QuestionState
import com.example.ui.viewmodel.TestActiveState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestRunnerScreen(
    state: TestActiveState,
    onSelectOption: (Int) -> Unit,
    onClearOption: () -> Unit,
    onMarkForReview: () -> Unit,
    onNextQuestion: () -> Unit,
    onPreviousQuestion: () -> Unit,
    onJumpToQuestion: (Int) -> Unit,
    onTogglePalette: (Boolean) -> Unit,
    onToggleSubmitDialog: (Boolean) -> Unit,
    onToggleLanguage: () -> Unit,
    onSubmitTest: () -> Unit,
    onExitTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentQ = state.questions.getOrNull(state.currentIndex) ?: return
    val selectedOption = state.userAnswers[currentQ.id]

    val mins = state.timeRemainingSeconds / 60
    val secs = state.timeRemainingSeconds % 60
    val timerStr = String.format("%02d:%02d", mins, secs)
    val isTimeLow = state.timeRemainingSeconds < 120

    Scaffold(
        topBar = {
            Surface(
                color = TestbookNavy,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onExitTest,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("exit_test_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Exit",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = state.testPaper.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                modifier = Modifier.width(160.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Timer Pill
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isTimeLow) Color(0xFFEF4444) else Color(0xFF1E293B)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "Timer",
                                        tint = if (isTimeLow) Color.White else TestbookGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = timerStr,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Lang Toggle
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = TestbookEmerald,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onToggleLanguage() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = "Lang",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (state.languageMode == LanguageMode.MARATHI) "मराठी" else "ENG",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Question Palette Trigger Icon
                            IconButton(
                                onClick = { onTogglePalette(true) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("open_palette_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GridOn,
                                    contentDescription = "Palette",
                                    tint = TestbookGold
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onClearOption,
                            shape = RoundedCornerShape(8.dp),
                            enabled = (selectedOption != null),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("clear_response_btn")
                        ) {
                            Text(
                                text = if (state.languageMode == LanguageMode.MARATHI) "Clear" else "Clear",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Button(
                            onClick = onMarkForReview,
                            colors = ButtonDefaults.buttonColors(containerColor = TestbookOrange),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("mark_review_btn")
                        ) {
                            Text(
                                text = if (state.languageMode == LanguageMode.MARATHI) "Mark for Review" else "Mark for Review",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                onNextQuestion()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TestbookEmerald),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("save_next_btn")
                        ) {
                            Text(
                                text = if (state.languageMode == LanguageMode.MARATHI) "Save & Next" else "Save & Next",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row {
                            OutlinedButton(
                                onClick = onPreviousQuestion,
                                enabled = (state.currentIndex > 0),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("prev_q_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NavigateBefore,
                                    contentDescription = "Prev",
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("Prev", fontSize = 11.sp)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            OutlinedButton(
                                onClick = onNextQuestion,
                                enabled = (state.currentIndex + 1 < state.questions.size),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("next_q_btn")
                            ) {
                                Text("Next", fontSize = 11.sp)
                                Icon(
                                    imageVector = Icons.Default.NavigateNext,
                                    contentDescription = "Next",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Button(
                            onClick = { onToggleSubmitDialog(true) },
                            colors = ButtonDefaults.buttonColors(containerColor = TestbookNavy),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("submit_test_btn")
                        ) {
                            Text(
                                text = if (state.languageMode == LanguageMode.MARATHI) "जमा करा (Submit)" else "Submit Test",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF1F5F9))
                .padding(16.dp)
        ) {
            // Question Header Line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TestbookNavy
                ) {
                    Text(
                        text = "Q.${state.currentIndex + 1} of ${state.questions.size}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TestbookGold.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = currentQ.subject,
                        color = Color(0xFFB45309),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Question Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (state.languageMode == LanguageMode.MARATHI) currentQ.questionMarathi else currentQ.questionEnglish,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        lineHeight = 22.sp
                    )

                    if (state.languageMode == LanguageMode.MARATHI && currentQ.questionEnglish.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentQ.questionEnglish,
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            lineHeight = 17.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val optionsList = listOf(
                        1 to (if (state.languageMode == LanguageMode.MARATHI) currentQ.option1Marathi else currentQ.option1English),
                        2 to (if (state.languageMode == LanguageMode.MARATHI) currentQ.option2Marathi else currentQ.option2English),
                        3 to (if (state.languageMode == LanguageMode.MARATHI) currentQ.option3Marathi else currentQ.option3English),
                        4 to (if (state.languageMode == LanguageMode.MARATHI) currentQ.option4Marathi else currentQ.option4English)
                    )

                    optionsList.forEach { (num, optionText) ->
                        val isSelected = (selectedOption == num)

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectOption(num) }
                                .testTag("option_${num}_btn"),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) TestbookNavy.copy(alpha = 0.08f) else Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                if (isSelected) TestbookNavy else Color(0xFFE2E8F0)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onSelectOption(num) },
                                    colors = RadioButtonDefaults.colors(selectedColor = TestbookNavy)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "($num) $optionText",
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) TestbookNavy else Color(0xFF1E293B)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Question Palette Bottom Sheet
    if (state.isPaletteOpen) {
        ModalBottomSheet(
            onDismissRequest = { onTogglePalette(false) },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (state.languageMode == LanguageMode.MARATHI) "प्रश्न सूची (Question Palette)" else "Question Palette Grid",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TestbookNavy
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LegendItem("Answered", TestbookEmerald)
                    LegendItem("Marked", TestbookOrange)
                    LegendItem("Ans & Marked", Color(0xFF8B5CF6))
                    LegendItem("Unattempted", Color(0xFFCBD5E1))
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.questions.size) { index ->
                        val q = state.questions[index]
                        val qState = state.questionStates[q.id] ?: QuestionState.UNATTEMPTED

                        val itemBg = when (qState) {
                            QuestionState.ANSWERED -> TestbookEmerald
                            QuestionState.MARKED_FOR_REVIEW -> TestbookOrange
                            QuestionState.ANSWERED_AND_MARKED -> Color(0xFF8B5CF6)
                            QuestionState.UNATTEMPTED -> Color(0xFFE2E8F0)
                        }

                        val textColor = if (qState == QuestionState.UNATTEMPTED) Color(0xFF334155) else Color.White

                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(itemBg)
                                .border(
                                    2.dp,
                                    if (index == state.currentIndex) TestbookNavy else Color.Transparent,
                                    CircleShape
                                )
                                .clickable { onJumpToQuestion(index) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Submit Confirmation Dialog
    if (state.isSubmitDialogOpen) {
        val attemptedCount = state.questionStates.values.count { it == QuestionState.ANSWERED || it == QuestionState.ANSWERED_AND_MARKED }
        val markedCount = state.questionStates.values.count { it == QuestionState.MARKED_FOR_REVIEW || it == QuestionState.ANSWERED_AND_MARKED }
        val unattemptedCount = state.questions.size - attemptedCount

        AlertDialog(
            onDismissRequest = { onToggleSubmitDialog(false) },
            title = {
                Text(
                    text = if (state.languageMode == LanguageMode.MARATHI) "टेस्ट जमा करण्याची खात्री आहे?" else "Submit Test Confirmation",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(text = "एकूण प्रश्न: ${state.questions.size}", fontSize = 13.sp)
                    Text(text = "सोडवलेले प्रश्न (Attempted): $attemptedCount", fontSize = 13.sp, color = TestbookEmerald, fontWeight = FontWeight.Bold)
                    Text(text = "न सोडवलेले प्रश्न (Unattempted): $unattemptedCount", fontSize = 13.sp, color = Color(0xFFEF4444))
                    Text(text = "पुनरावलोकनासाठी ठेवलेले (Marked): $markedCount", fontSize = 13.sp, color = TestbookOrange)
                }
            },
            confirmButton = {
                Button(
                    onClick = onSubmitTest,
                    colors = ButtonDefaults.buttonColors(containerColor = TestbookEmerald),
                    modifier = Modifier.testTag("confirm_submit_btn")
                ) {
                    Text(text = "जमा करा (Confirm Submit)", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { onToggleSubmitDialog(false) }) {
                    Text(text = "रद्द करा (Cancel)")
                }
            }
        )
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 9.sp, color = Color(0xFF475569))
    }
}
