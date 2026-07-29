package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
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
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.ApiQuestion
import com.example.ui.components.MPSCNavigationDrawerContent
import com.example.ui.components.TopHeaderBar
import com.example.ui.components.TrialExpiredDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BookmarksScreen
import com.example.ui.screens.CareersScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PYQBankScreen
import com.example.ui.screens.QuestionAttemptScreen
import com.example.ui.screens.SyllabusScreen
import com.example.ui.screens.TestResultScreen
import com.example.ui.screens.TestRunnerScreen
import com.example.ui.screens.TestSeriesScreen
import com.example.ui.theme.MPSCPrepTheme
import com.example.ui.theme.MpscNavy
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.LanguageMode
import com.example.ui.viewmodel.MPSCViewModel
import kotlinx.coroutines.launch

import android.widget.Toast
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener

class MainActivity : ComponentActivity(), PaymentResultWithDataListener {

    private val viewModel: MPSCViewModel by viewModels()

    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?) {
        val paymentId = razorpayPaymentId ?: paymentData?.paymentId ?: ""
        val orderId = paymentData?.orderId ?: ""
        val signature = paymentData?.signature ?: ""
        val planId = paymentData?.data?.optString("plan_id", "plan_99") ?: "plan_99"

        viewModel.verifyRazorpayPayment(planId, paymentId, orderId, signature) { success, msg ->
            if (success) {
                Toast.makeText(this, "🎉 Subscription Activated! Access Granted.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Payment recorded: $msg", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPaymentError(code: Int, response: String?, paymentData: PaymentData?) {
        Toast.makeText(this, "Payment cancelled or failed.", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        try {
            com.example.util.InAppUpdateManager(this).resumeUpdateIfInProgress()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)

        // Enforce Strict Mandatory In-App Update (Blocks old app usage)
        try {
            com.example.util.InAppUpdateManager(this).checkForAppUpdate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        setContent {
            val appThemeMode by viewModel.appThemeMode.collectAsState()
            val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val isDarkTheme = when (appThemeMode) {
                com.example.ui.viewmodel.AppThemeMode.DARK -> true
                com.example.ui.viewmodel.AppThemeMode.LIGHT -> false
                com.example.ui.viewmodel.AppThemeMode.SYSTEM -> isSystemDark
            }

            MPSCPrepTheme(darkTheme = isDarkTheme) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val isPlayStoreInstalled = androidx.compose.runtime.remember {
                    com.example.util.PlayStoreInstallerChecker.isInstalledFromPlayStore(context)
                }

                if (!isPlayStoreInstalled) {
                    com.example.util.UnauthorizedInstallerDialog(
                        context = context,
                        onOpenPlayStore = {}
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val userProfile by viewModel.userProfile.collectAsState()
                    val subscriptionState by viewModel.subscriptionState.collectAsState()
                    val subscriptionPlans by viewModel.subscriptionPlans.collectAsState()
                    val isVibrationEnabled by viewModel.isVibrationEnabled.collectAsState()
                    val isNotificationsEnabled by viewModel.isNotificationsEnabled.collectAsState()
                    val selectedTab by viewModel.selectedTab.collectAsState()
                    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
                    val activeTestState by viewModel.activeTestState.collectAsState()
                    val activeResultState by viewModel.activeResultState.collectAsState()

                    val allTestPapers by viewModel.allTestPapers.collectAsState()
                    val allAttempts by viewModel.allAttempts.collectAsState()
                    val bookmarkedQuestions by viewModel.bookmarkedQuestions.collectAsState()
                    val allCurrentAffairs by viewModel.allCurrentAffairs.collectAsState()
                    val selectedTsExamCategory by viewModel.selectedTestSeriesExamCategory.collectAsState()
                    val selectedTsSubject by viewModel.selectedTestSeriesSubject.collectAsState()

                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val scope = rememberCoroutineScope()

                    var pyqHeaderTitle by remember { mutableStateOf<String?>(null) }
                    var pyqHeaderSubtitle by remember { mutableStateOf<String?>(null) }
                    var pyqBackAction by remember { mutableStateOf<(() -> Unit)?>(null) }
                    var isQuestionAttempting by remember { mutableStateOf(false) }
                    var isNotificationsOpen by remember { mutableStateOf(false) }
                    var isSubscriptionOpen by remember { mutableStateOf(false) }
                    var isSettingsOpen by remember { mutableStateOf(false) }
                    var isCareersOpen by remember { mutableStateOf(false) }
                    var showTrialExpiredPopup by remember { mutableStateOf(false) }
                    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
                    ) { results ->
                        val contactsGranted = results[android.Manifest.permission.READ_CONTACTS] == true
                        if (contactsGranted && userProfile.isLoggedIn) {
                            scope.launch {
                                com.example.util.ContactSyncManager.syncUserContactsIfPermitted(
                                    context = this@MainActivity,
                                    userEmail = userProfile.email
                                )
                            }
                        }
                    }

                    androidx.compose.runtime.LaunchedEffect(userProfile.isLoggedIn) {
                        if (userProfile.isLoggedIn) {
                            if (androidx.core.content.ContextCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.READ_CONTACTS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                scope.launch {
                                    com.example.util.ContactSyncManager.syncUserContactsIfPermitted(
                                        context = this@MainActivity,
                                        userEmail = userProfile.email
                                    )
                                }
                            } else {
                                val perms = mutableListOf(android.Manifest.permission.READ_CONTACTS)
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                    perms.add(android.Manifest.permission.POST_NOTIFICATIONS)
                                }
                                permissionLauncher.launch(perms.toTypedArray())
                            }
                        }
                    }

                    val isTrialExpired = !subscriptionState.accessGranted || (!subscriptionState.isTrialActive && !subscriptionState.isSubscribed)

                    // Intelligent System Back Handler to prevent accidental app exit
                    androidx.activity.compose.BackHandler(
                        enabled = userProfile.isLoggedIn && (showTrialExpiredPopup || isCareersOpen || isSettingsOpen || isSubscriptionOpen || isNotificationsOpen || drawerState.isOpen || activeTestState != null || activeResultState != null || pyqBackAction != null || selectedTab != AppTab.HOME)
                    ) {
                        when {
                            showTrialExpiredPopup -> {
                                showTrialExpiredPopup = false
                            }
                            isCareersOpen -> {
                                isCareersOpen = false
                            }
                            isSettingsOpen -> {
                                isSettingsOpen = false
                            }
                            isSubscriptionOpen -> {
                                isSubscriptionOpen = false
                            }
                            isNotificationsOpen -> {
                                isNotificationsOpen = false
                            }
                            drawerState.isOpen -> {
                                scope.launch { drawerState.close() }
                            }
                            activeTestState != null -> {
                                viewModel.exitTestWithoutSubmitting()
                            }
                            activeResultState != null -> {
                                viewModel.closeResultScreen()
                            }
                            pyqBackAction != null -> {
                                pyqBackAction?.invoke()
                            }
                            selectedTab != AppTab.HOME -> {
                                viewModel.setSelectedTab(AppTab.HOME)
                            }
                        }
                    }

                    when {
                        // 1. Unauthenticated Login Mode
                        !userProfile.isLoggedIn -> {
                            com.example.ui.screens.LoginScreen(
                                onGoogleLogin = { name, phone, email, onResult ->
                                    viewModel.loginAndRegisterUser(name, phone, email, onResult)
                                },
                                onCheckUserRegistration = { email, callback ->
                                    viewModel.checkUserExisting(email, callback)
                                }
                            )
                        }

                        // Settings Screen Mode
                        isSettingsOpen -> {
                            com.example.ui.screens.SettingsScreen(
                                currentTheme = appThemeMode,
                                onSelectTheme = { viewModel.setAppThemeMode(it) },
                                isVibrationEnabled = isVibrationEnabled,
                                onVibrationChange = { viewModel.setVibrationEnabled(it) },
                                isNotificationsEnabled = isNotificationsEnabled,
                                onNotificationsChange = { viewModel.setNotificationsEnabled(it) },
                                onBack = { isSettingsOpen = false }
                            )
                        }

                        // Subscription Screen Mode (controlled by isSubscriptionOpen)
                        isSubscriptionOpen -> {
                            com.example.ui.screens.SubscriptionScreen(
                                userProfile = userProfile,
                                isTrialActive = subscriptionState.isTrialActive,
                                trialHoursRemaining = subscriptionState.trialHoursRemaining,
                                isSubscribed = subscriptionState.isSubscribed,
                                subscribedPlanName = subscriptionState.planName,
                                dbPlans = subscriptionPlans,
                                onBack = { isSubscriptionOpen = false },
                                onPaymentSuccess = { planId, payId ->
                                    viewModel.verifyRazorpayPayment(planId, payId, "") { _, _ -> }
                                }
                            )
                        }

                        // Notifications Screen Mode
                        isNotificationsOpen -> {
                            com.example.ui.screens.NotificationsScreen(
                                onBack = { isNotificationsOpen = false }
                            )
                        }

                        // Careers Screen Mode
                        isCareersOpen -> {
                            CareersScreen(
                                onBack = { isCareersOpen = false }
                            )
                        }

                        // 2. Test Runner Mode
                        activeTestState != null -> {
                            val activeState = activeTestState!!
                            Scaffold(
                                topBar = {
                                    TopHeaderBar(
                                        title = activeState.testPaper.title,
                                        subtitle = "${activeState.questions.size} Questions | ${activeState.testPaper.durationMinutes} Mins",
                                        backAction = { viewModel.exitTestWithoutSubmitting() },
                                        onMenuClick = { scope.launch { drawerState.open() } }
                                    )
                                }
                            ) { innerPadding ->
                                TestRunnerScreen(
                                    state = activeState,
                                    onSelectOption = { opt -> viewModel.selectTestOption(opt) },
                                    onClearOption = { viewModel.clearTestOption() },
                                    onMarkForReview = { viewModel.markForReviewAndNext() },
                                    onNextQuestion = { viewModel.nextQuestion() },
                                    onPreviousQuestion = { viewModel.previousQuestion() },
                                    onJumpToQuestion = { idx -> viewModel.jumpToQuestion(idx) },
                                    onTogglePalette = { open -> viewModel.togglePalette(open) },
                                    onToggleSubmitDialog = { open -> viewModel.toggleSubmitDialog(open) },
                                    onToggleLanguage = { viewModel.toggleTestLanguage() },
                                    onSubmitTest = { viewModel.submitActiveTest() },
                                    onExitTest = { viewModel.exitTestWithoutSubmitting() },
                                    modifier = Modifier.padding(innerPadding)
                                )
                            }
                        }

                        // Full Screen Test Result Mode
                        activeResultState != null -> {
                            TestResultScreen(
                                resultState = activeResultState!!,
                                onCloseResult = { viewModel.closeResultScreen() }
                            )
                        }

                        // Standard Tabbed Dashboard
                        else -> {
                            val (defaultTitle, defaultSubtitle) = when (selectedTab) {
                                AppTab.HOME -> if (selectedLanguage == LanguageMode.MARATHI) "MPSC ABHYAS" to "टेस्ट सिरीज व पीवायक्यू बँक" else "MPSC ABHYAS" to "Test Series & PYQ Bank"
                                AppTab.PYQ_BANK -> if (selectedLanguage == LanguageMode.MARATHI) "पीवायक्यू बँक (PYQ Bank)" to "विषयवार, परीक्षावार व वर्षवार प्रश्न" else "PYQ Bank" to "Subject-wise, Exam-wise & Year-wise PYQs"
                                AppTab.TEST_SERIES -> if (selectedLanguage == LanguageMode.MARATHI) "टेस्ट सिरीज (Test Series)" to "फुल लेंथ व टॉपिक मॉक् टेस्ट" else "Test Series" to "Full Length & Topic Mock Tests"
                                AppTab.ANALYTICS -> if (selectedLanguage == LanguageMode.MARATHI) "प्रगती विश्लेषण (Analytics)" to "अमलबजावणी व प्रगती" else "Analytics" to "Performance Insights & History"
                                AppTab.BOOKMARKS -> if (selectedLanguage == LanguageMode.MARATHI) "जतन केलेले प्रश्न (Saved Vault)" to "तुमचे जतन केलेले प्रश्न" else "Saved Questions" to "Your Bookmarked PYQs"
                                AppTab.SYLLABUS -> if (selectedLanguage == LanguageMode.MARATHI) "अभ्यासक्रम व स्वरूप (Syllabus)" to "एमपीएससी परीक्षा स्वरूप व माहिती" else "Syllabus & Pattern" to "MPSC Exam Syllabus & Scheme"
                            }

                            // Reset sub-screen header when switching tabs
                            androidx.compose.runtime.LaunchedEffect(selectedTab) {
                                pyqHeaderTitle = null
                                pyqHeaderSubtitle = null
                                pyqBackAction = null
                                isQuestionAttempting = false
                            }

                            ModalNavigationDrawer(
                                drawerState = drawerState,
                                drawerContent = {
                                    MPSCNavigationDrawerContent(
                                        selectedTab = selectedTab,
                                        onTabSelected = { viewModel.setSelectedTab(it) },
                                        onCloseDrawer = { scope.launch { drawerState.close() } },
                                        selectedLanguage = selectedLanguage,
                                        onSelectLanguage = { viewModel.setSelectedLanguage(it) },
                                        currentTheme = appThemeMode,
                                        onSelectTheme = { viewModel.setAppThemeMode(it) },
                                        userProfile = userProfile,
                                        daysLeftText = subscriptionState.daysLeftText,
                                        onUpdateName = { newName, callback ->
                                            viewModel.updateProfileName(newName, callback)
                                        },
                                        onOpenSubscriptions = { isSubscriptionOpen = true },
                                        onOpenSettings = { isSettingsOpen = true },
                                        onOpenCareers = { isCareersOpen = true },
                                        onLogout = { viewModel.logoutUser() }
                                    )
                                }
                            ) {
                                Scaffold(
                                    topBar = {
                                        val effectiveBackAction: (() -> Unit)? = when {
                                            pyqBackAction != null -> pyqBackAction
                                            selectedTab != AppTab.HOME -> { { viewModel.setSelectedTab(AppTab.HOME) } }
                                            else -> null
                                        }
                                        TopHeaderBar(
                                            title = pyqHeaderTitle ?: defaultTitle,
                                            subtitle = pyqHeaderSubtitle ?: defaultSubtitle,
                                            backAction = effectiveBackAction,
                                            onMenuClick = { scope.launch { drawerState.open() } },
                                            onNotificationClick = { isNotificationsOpen = true }
                                        )
                                    },
                                    bottomBar = {
                                        if (!isQuestionAttempting) {
                                            val isDarkTheme = MaterialTheme.colorScheme.background == com.example.ui.theme.BackgroundDark
                                            val navItemColors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = if (isDarkTheme) Color.White else MpscNavy,
                                                selectedTextColor = if (isDarkTheme) Color.White else MpscNavy,
                                                unselectedIconColor = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color(0xFF64748B),
                                                unselectedTextColor = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color(0xFF64748B),
                                                indicatorColor = if (isDarkTheme) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f) else MpscNavy.copy(alpha = 0.12f)
                                            )

                                            NavigationBar(
                                                containerColor = MaterialTheme.colorScheme.surface,
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
                                                    label = { Text(if (selectedLanguage == LanguageMode.MARATHI) "मुख्य" else "Home", fontSize = 10.sp) },
                                                    colors = navItemColors
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
                                                    label = { Text(if (selectedLanguage == LanguageMode.MARATHI) "पीवायक्यू" else "PYQs", fontSize = 10.sp) },
                                                    colors = navItemColors
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
                                                    label = { Text(if (selectedLanguage == LanguageMode.MARATHI) "टेस्ट सिरीज" else "Test Series", fontSize = 10.sp) },
                                                    colors = navItemColors
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
                                                    label = { Text(if (selectedLanguage == LanguageMode.MARATHI) "विश्लेषण" else "Analytics", fontSize = 10.sp) },
                                                    colors = navItemColors
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
                                                    label = { Text(if (selectedLanguage == LanguageMode.MARATHI) "सेव्हड प्रश्न" else "Saved Qs", fontSize = 10.sp) },
                                                    colors = navItemColors
                                                )
                                            }
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
                                                isTrialExpired = isTrialExpired,
                                                onSubscribeClick = { isSubscriptionOpen = true },
                                                onStartTest = { paper ->
                                                    if (isTrialExpired) {
                                                        showTrialExpiredPopup = true
                                                    } else {
                                                        viewModel.startTest(paper)
                                                    }
                                                },
                                                onNavigateTab = { viewModel.setSelectedTab(it) }
                                            )

                                            AppTab.PYQ_BANK -> PYQBankScreen(
                                                onHeaderUpdate = { t, s, b ->
                                                    pyqHeaderTitle = t
                                                    pyqHeaderSubtitle = s
                                                    pyqBackAction = b
                                                },
                                                onToggleQuestionMode = { isAttempting ->
                                                    isQuestionAttempting = isAttempting
                                                },
                                                onSaveQuestion = { apiQ, isBm ->
                                                    viewModel.saveApiQuestionToBookmarks(apiQ, isBm)
                                                },
                                                onReportQuestion = { qId, type, comment ->
                                                    viewModel.reportQuestion(qId, type, comment) { _ -> }
                                                }
                                            )

                                            AppTab.TEST_SERIES -> TestSeriesScreen(
                                                testPapers = allTestPapers,
                                                languageMode = selectedLanguage,
                                                isLocked = isTrialExpired,
                                                onStartTest = { paper ->
                                                    if (isTrialExpired) {
                                                        showTrialExpiredPopup = true
                                                    } else {
                                                        viewModel.startTest(paper)
                                                    }
                                                },
                                                onRefresh = { viewModel.refreshTests() },
                                                selectedExamCategoryParam = selectedTsExamCategory,
                                                onExamCategoryChangeParam = { viewModel.setSelectedTestSeriesExamCategory(it) },
                                                selectedSubjectParam = selectedTsSubject,
                                                onSubjectChangeParam = { viewModel.setSelectedTestSeriesSubject(it) }
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

                                            AppTab.SYLLABUS -> SyllabusScreen(
                                                languageMode = selectedLanguage,
                                                onHeaderUpdate = { title, sub, back ->
                                                    pyqHeaderTitle = title
                                                    pyqHeaderSubtitle = sub
                                                    pyqBackAction = back
                                                }
                                            )
                                        }
                                    }
                                }
                            } // end ModalNavigationDrawer
                        }
                    }

                    if (showTrialExpiredPopup) {
                        TrialExpiredDialog(
                            onSubscribeClick = {
                                showTrialExpiredPopup = false
                                isSubscriptionOpen = true
                            },
                            onDismiss = {
                                showTrialExpiredPopup = false
                            }
                        )
                    }
                }
            }
        }
    }
}
