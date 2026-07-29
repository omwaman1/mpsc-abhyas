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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.ConfettiCelebrationOverlay
import com.example.ui.theme.TestbookEmerald
import com.example.ui.theme.TestbookGold
import com.example.ui.theme.TestbookNavy
import com.example.ui.theme.TestbookOrange
import com.example.ui.viewmodel.LanguageMode
import com.example.ui.viewmodel.QuestionState
import com.example.ui.viewmodel.TestActiveState
import com.example.utils.cleanHtml

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

    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { state.questions.size })

    LaunchedEffect(pagerState.currentPage) {
        if (state.questions.isNotEmpty() && pagerState.currentPage in state.questions.indices && pagerState.currentPage != state.currentIndex) {
            onJumpToQuestion(pagerState.currentPage)
        }
    }

    LaunchedEffect(state.currentIndex) {
        if (state.questions.isNotEmpty() && state.currentIndex in state.questions.indices && pagerState.currentPage != state.currentIndex) {
            pagerState.scrollToPage(state.currentIndex)
        }
    }

    val mins = state.timeRemainingSeconds / 60
    val secs = state.timeRemainingSeconds % 60
    val timerStr = String.format("%02d:%02d", mins, secs)
    val isTimeLow = state.timeRemainingSeconds < 120

    var totalDragAmount by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(state.currentIndex, state.questions.size) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount ->
                        totalDragAmount += dragAmount
                    },
                    onDragEnd = {
                        if (totalDragAmount < -70f && state.currentIndex < state.questions.size - 1) {
                            onNextQuestion()
                        } else if (totalDragAmount > 70f && state.currentIndex > 0) {
                            onPreviousQuestion()
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
            // TOP CONTROL HEADER BAR (Matching PYQ Style)
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

                        // Counter Card (e.g., 03 / 100)
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
                                    text = String.format("%02d / %d", state.currentIndex + 1, state.questions.size),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Timer | Language | Square Dots Grid
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Timer Pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isTimeLow) Color(0xFF7F1D1D) else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isTimeLow) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Timer",
                                    tint = if (isTimeLow) Color(0xFFF87171) else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = timerStr,
                                    color = if (isTimeLow) Color(0xFFF87171) else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Lang Toggle
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onToggleLanguage() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = "Lang",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (state.languageMode == LanguageMode.MARATHI) "मराठी" else "ENG",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Square Dots Icon (Question Palette Modal)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFAFAFA),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.size(34.dp).clickable { onTogglePalette(true) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.GridView,
                                    contentDescription = "Question Palette",
                                    tint = TestbookNavy,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Progress Indicator
            LinearProgressIndicator(
                progress = { (state.currentIndex + 1).toFloat() / state.questions.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = TestbookEmerald,
                trackColor = Color(0xFFE2E8F0)
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                val qPage = state.questions.getOrNull(pageIndex) ?: return@HorizontalPager
                val selOption = state.userAnswers[qPage.id]

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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF59E0B).copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = currentQ.subject,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFBBF24),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Text(
                                text = (currentQ.examType).uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEA580C)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val qMrClean = currentQ.questionMarathi.cleanHtml()
                        val qEnClean = currentQ.questionEnglish.cleanHtml()

                        val qText = if (state.languageMode == LanguageMode.MARATHI) {
                            "प्रश्न : ${qMrClean.ifEmpty { qEnClean }}"
                        } else {
                            "Q : ${qEnClean.ifEmpty { qMrClean }}"
                        }

                        Text(
                            text = qText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 23.sp
                        )

                        if (state.languageMode == LanguageMode.MARATHI && qEnClean.isNotEmpty() && !qEnClean.equals(qMrClean, ignoreCase = true)) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = qEnClean,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // OPTIONS LIST (Stripped of HTML tags)
                val opt1 = (if (state.languageMode == LanguageMode.MARATHI) currentQ.option1Marathi.ifEmpty { currentQ.option1English } else currentQ.option1English.ifEmpty { currentQ.option1Marathi }).cleanHtml()
                val opt2 = (if (state.languageMode == LanguageMode.MARATHI) currentQ.option2Marathi.ifEmpty { currentQ.option2English } else currentQ.option2English.ifEmpty { currentQ.option2Marathi }).cleanHtml()
                val opt3 = (if (state.languageMode == LanguageMode.MARATHI) currentQ.option3Marathi.ifEmpty { currentQ.option3English } else currentQ.option3English.ifEmpty { currentQ.option3Marathi }).cleanHtml()
                val opt4 = (if (state.languageMode == LanguageMode.MARATHI) currentQ.option4Marathi.ifEmpty { currentQ.option4English } else currentQ.option4English.ifEmpty { currentQ.option4Marathi }).cleanHtml()

                val optionsList = listOf(
                    1 to opt1,
                    2 to opt2,
                    3 to opt3,
                    4 to opt4
                )

                optionsList.forEach { (num, optionText) ->
                    val isSelected = (selectedOption == num)
                    val isCorrect = (num == currentQ.correctOption)

                    val bg = when {
                        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.surface
                    }
                    val borderColor = when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                onSelectOption(num)
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
                                text = "$num) $optionText",
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp)) // Space for bottom bar
            }
        }

        // STICKY BOTTOM NAVIGATION BAR
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Button (Icon Only: Left Arrow)
                    IconButton(
                        onClick = {
                            onPreviousQuestion()
                            if (state.currentIndex > 0) {
                                coroutineScope.launch { pagerState.animateScrollToPage(state.currentIndex - 1) }
                            }
                        },
                        enabled = (state.currentIndex > 0),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Previous",
                            modifier = Modifier.size(22.dp),
                            tint = if (state.currentIndex > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }

                    // Action Controls: Submit Only
                    Button(
                        onClick = { onToggleSubmitDialog(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Submit Test", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Save & Next Button (Bottom Right Corner)
                    Button(
                        onClick = {
                            onNextQuestion()
                            if (state.currentIndex + 1 < state.questions.size) {
                                coroutineScope.launch { pagerState.animateScrollToPage(state.currentIndex + 1) }
                            }
                        },
                        enabled = (state.currentIndex + 1 < state.questions.size),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (state.languageMode == LanguageMode.MARATHI) "जतन करा व पुढे" else "Save & Next",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(2.dp))
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

        // QUESTION PALETTE MODAL DIALOG (SQUARE DOTS GRID)
        if (state.isPaletteOpen) {
            Dialog(
                onDismissRequest = { onTogglePalette(false) },
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
                                    text = "एकूण ${state.questions.size} प्रश्न (Click square to jump)",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onTogglePalette(false) }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 5-Column Grid
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(5),
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 12.dp)
                        ) {
                            itemsIndexed(state.questions) { idx, q ->
                                val isAnswered = state.userAnswers.containsKey(q.id)
                                val isCurrent = idx == state.currentIndex

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
                                        .clickable { onJumpToQuestion(idx) },
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

        // SUBMIT TEST CONFIRMATION DIALOG
        if (state.isSubmitDialogOpen) {
            AlertDialog(
                onDismissRequest = { onToggleSubmitDialog(false) },
                title = { Text("Submit Test?", fontWeight = FontWeight.Bold) },
                text = {
                    val answeredCount = state.userAnswers.size
                    val remaining = state.questions.size - answeredCount
                    Text("Answered: $answeredCount | Unattempted: $remaining\n\nAre you sure you want to submit your test?")
                },
                confirmButton = {
                    Button(
                        onClick = onSubmitTest,
                        colors = ButtonDefaults.buttonColors(containerColor = TestbookNavy)
                    ) {
                        Text("Yes, Submit")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { onToggleSubmitDialog(false) }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
