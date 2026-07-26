package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.TopHeaderBar
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BookmarksScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PYQBankScreen
import com.example.ui.screens.TestResultScreen
import com.example.ui.screens.TestRunnerScreen
import com.example.ui.screens.TestSeriesScreen
import com.example.ui.theme.MPSCPrepTheme
import com.example.ui.theme.TestbookEmerald
import com.example.ui.theme.TestbookNavy
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MPSCViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MPSCViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MPSCPrepTheme {
                val activeTestState by viewModel.activeTestState.collectAsStateWithLifecycle()
                val activeResultState by viewModel.activeResultState.collectAsStateWithLifecycle()

                val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
                val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()

                val allQuestions by viewModel.allQuestions.collectAsStateWithLifecycle()
                val bookmarkedQuestions by viewModel.bookmarkedQuestions.collectAsStateWithLifecycle()
                val filteredPYQs by viewModel.filteredPYQs.collectAsStateWithLifecycle()
                val allTestPapers by viewModel.allTestPapers.collectAsStateWithLifecycle()
                val allAttempts by viewModel.allAttempts.collectAsStateWithLifecycle()
                val allCurrentAffairs by viewModel.allCurrentAffairs.collectAsStateWithLifecycle()

                val selectedExamFilter by viewModel.selectedExamFilter.collectAsStateWithLifecycle()
                val selectedSubjectFilter by viewModel.selectedSubjectFilter.collectAsStateWithLifecycle()
                val selectedYearFilter by viewModel.selectedYearFilter.collectAsStateWithLifecycle()

                when {
                    // Full Screen Active Test Mode
                    activeTestState != null -> {
                        TestRunnerScreen(
                            state = activeTestState!!,
                            onSelectOption = { viewModel.selectTestOption(it) },
                            onClearOption = { viewModel.clearTestOption() },
                            onMarkForReview = { viewModel.markForReviewAndNext() },
                            onNextQuestion = { viewModel.nextQuestion() },
                            onPreviousQuestion = { viewModel.previousQuestion() },
                            onJumpToQuestion = { viewModel.jumpToQuestion(it) },
                            onTogglePalette = { viewModel.togglePalette(it) },
                            onToggleSubmitDialog = { viewModel.toggleSubmitDialog(it) },
                            onToggleLanguage = { viewModel.toggleTestLanguage() },
                            onSubmitTest = { viewModel.submitActiveTest() },
                            onExitTest = { viewModel.exitTestWithoutSubmitting() }
                        )
                    }

                    // Full Screen Active Result & Solution Mode
                    activeResultState != null -> {
                        TestResultScreen(
                            resultState = activeResultState!!,
                            onCloseResult = { viewModel.closeResultScreen() }
                        )
                    }

                    // Main App Navigation
                    else -> {
                        Scaffold(
                            topBar = {
                                TopHeaderBar(
                                    currentLanguage = selectedLanguage,
                                    onLanguageToggle = { viewModel.toggleAppLanguage() }
                                )
                            },
                            bottomBar = {
                                NavigationBar(
                                    containerColor = Color.White,
                                    tonalElevation = 8.dp,
                                    modifier = Modifier.testTag("main_bottom_navigation")
                                ) {
                                    NavigationBarItem(
                                        selected = selectedTab == AppTab.HOME,
                                        onClick = { viewModel.setSelectedTab(AppTab.HOME) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selectedTab == AppTab.HOME) Icons.Default.Home else Icons.Outlined.Home,
                                                contentDescription = "Home"
                                            )
                                        },
                                        label = { Text("Home", fontSize = 10.sp) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = TestbookNavy,
                                            selectedTextColor = TestbookNavy,
                                            indicatorColor = TestbookNavy.copy(alpha = 0.12f)
                                        )
                                    )

                                    NavigationBarItem(
                                        selected = selectedTab == AppTab.PYQ_BANK,
                                        onClick = { viewModel.setSelectedTab(AppTab.PYQ_BANK) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selectedTab == AppTab.PYQ_BANK) Icons.Default.MenuBook else Icons.Outlined.MenuBook,
                                                contentDescription = "PYQ Bank"
                                            )
                                        },
                                        label = { Text("PYQs", fontSize = 10.sp) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = TestbookNavy,
                                            selectedTextColor = TestbookNavy,
                                            indicatorColor = TestbookNavy.copy(alpha = 0.12f)
                                        )
                                    )

                                    NavigationBarItem(
                                        selected = selectedTab == AppTab.TEST_SERIES,
                                        onClick = { viewModel.setSelectedTab(AppTab.TEST_SERIES) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selectedTab == AppTab.TEST_SERIES) Icons.Default.Assignment else Icons.Outlined.Assignment,
                                                contentDescription = "Tests"
                                            )
                                        },
                                        label = { Text("Test Series", fontSize = 10.sp) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = TestbookNavy,
                                            selectedTextColor = TestbookNavy,
                                            indicatorColor = TestbookNavy.copy(alpha = 0.12f)
                                        )
                                    )

                                    NavigationBarItem(
                                        selected = selectedTab == AppTab.ANALYTICS,
                                        onClick = { viewModel.setSelectedTab(AppTab.ANALYTICS) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selectedTab == AppTab.ANALYTICS) Icons.Default.Assessment else Icons.Outlined.Assessment,
                                                contentDescription = "Analytics"
                                            )
                                        },
                                        label = { Text("Analytics", fontSize = 10.sp) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = TestbookNavy,
                                            selectedTextColor = TestbookNavy,
                                            indicatorColor = TestbookNavy.copy(alpha = 0.12f)
                                        )
                                    )

                                    NavigationBarItem(
                                        selected = selectedTab == AppTab.BOOKMARKS,
                                        onClick = { viewModel.setSelectedTab(AppTab.BOOKMARKS) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selectedTab == AppTab.BOOKMARKS) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                                contentDescription = "Saved"
                                            )
                                        },
                                        label = { Text("Saved Qs", fontSize = 10.sp) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = TestbookNavy,
                                            selectedTextColor = TestbookNavy,
                                            indicatorColor = TestbookNavy.copy(alpha = 0.12f)
                                        )
                                    )
                                }
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                when (selectedTab) {
                                    AppTab.HOME -> HomeScreen(
                                        testPapers = allTestPapers,
                                        currentAffairs = allCurrentAffairs,
                                        languageMode = selectedLanguage,
                                        onStartTest = { viewModel.startTest(it) },
                                        onNavigateTab = { viewModel.setSelectedTab(it) }
                                    )

                                    AppTab.PYQ_BANK -> PYQBankScreen(
                                        questions = filteredPYQs,
                                        languageMode = selectedLanguage,
                                        selectedExam = selectedExamFilter,
                                        selectedSubject = selectedSubjectFilter,
                                        selectedYear = selectedYearFilter,
                                        onExamFilterChanged = { viewModel.selectedExamFilter.value = it },
                                        onSubjectFilterChanged = { viewModel.selectedSubjectFilter.value = it },
                                        onYearFilterChanged = { viewModel.selectedYearFilter.value = it },
                                        onToggleBookmark = { id, bm -> viewModel.toggleBookmark(id, bm) }
                                    )

                                    AppTab.TEST_SERIES -> TestSeriesScreen(
                                        testPapers = allTestPapers,
                                        languageMode = selectedLanguage,
                                        onStartTest = { viewModel.startTest(it) }
                                    )

                                    AppTab.ANALYTICS -> AnalyticsScreen(
                                        attempts = allAttempts,
                                        languageMode = selectedLanguage,
                                        onReviewAttempt = { viewModel.openPastResult(it) }
                                    )

                                    AppTab.BOOKMARKS -> BookmarksScreen(
                                        bookmarkedQuestions = bookmarkedQuestions,
                                        languageMode = selectedLanguage,
                                        onToggleBookmark = { id, bm -> viewModel.toggleBookmark(id, bm) }
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
