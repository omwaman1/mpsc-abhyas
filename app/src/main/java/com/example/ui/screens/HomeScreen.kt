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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.CurrentAffairsEntity
import com.example.data.local.entities.TestPaperEntity
import com.example.ui.components.MpscPassBanner
import com.example.ui.theme.MpscEmerald
import com.example.ui.theme.MpscGold
import com.example.ui.theme.MpscNavy
import com.example.ui.theme.MpscOrange
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.LanguageMode

@Composable
fun HomeScreen(
    testPapers: List<TestPaperEntity>,
    currentAffairs: List<CurrentAffairsEntity>,
    languageMode: LanguageMode,
    onStartTest: (TestPaperEntity) -> Unit,
    onNavigateTab: (AppTab) -> Unit,
    isTrialExpired: Boolean = false,
    onSubscribeClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("home_screen_lazy_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 0. EXPIRED SUBSCRIPTION NOTICE (SIMPLE RED ALERT BANNER ABOVE SEARCH BAR)
        if (isTrialExpired) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSubscribeClick() }
                        .testTag("expired_home_banner"),
                    color = Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, Color(0xFFEF4444))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDC2626))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (languageMode == LanguageMode.MARATHI)
                                    "सबस्क्रिप्शन समाप्त झाले आहे 🔒 अनलॉक करण्यासाठी नूतनीकरण करा."
                                else
                                    "Subscription Expired 🔒 Tap to renew access.",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF991B1B)
                            )
                        }
                        Text(
                            text = if (languageMode == LanguageMode.MARATHI) "नूतनीकरण ⚡" else "Renew ⚡",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFDC2626)
                        )
                    }
                }
            }
        }

        // Search Input Bar & Live Auto-Populate Suggestions
        item {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = if (languageMode == LanguageMode.MARATHI) "मराठी प्रश्न, टेस्ट सीरिज किंवा विषय शोधा..." else "Search PYQ tests, subjects or exams...",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true
                )

                // LIVE SEARCH AUTO-SUGGESTIONS DROPDOWN CARD
                if (searchQuery.trim().length >= 1) {
                    val query = searchQuery.trim()

                    // 1. Matching Test Papers
                    val matchingTests = testPapers.filter {
                        it.title.contains(query, ignoreCase = true) ||
                        it.subjectName.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true) ||
                        it.examType.contains(query, ignoreCase = true)
                    }.take(4)

                    // 2. Matching Shortcuts / Subjects / Exams
                    val shortcuts = listOf(
                        HomeSearchShortcut("राज्यसेवा (State Services PYQs)", "MPSC Rajyaseva Prelims & Mains", AppTab.PYQ_BANK, "🏛️ EXAM"),
                        HomeSearchShortcut("गट ब (Combine Group B)", "Combine Prelims & Sub Inspector", AppTab.PYQ_BANK, "🏛️ EXAM"),
                        HomeSearchShortcut("गट क (Combine Group C)", "Clerk Typist & Excise Inspector", AppTab.PYQ_BANK, "🏛️ EXAM"),
                        HomeSearchShortcut("इतिहास (History)", "इतिहास विषयवार प्रश्नसंच", AppTab.PYQ_BANK, "📚 SUBJECT"),
                        HomeSearchShortcut("राज्यशास्त्र (Polity)", "भारतीय राज्यघटना व राज्यशास्त्र", AppTab.PYQ_BANK, "📚 SUBJECT"),
                        HomeSearchShortcut("भूगोल (Geography)", "महाराष्ट्र व भारताचा भूगोल", AppTab.PYQ_BANK, "📚 SUBJECT"),
                        HomeSearchShortcut("अर्थशास्त्र (Economy)", "भारतीय अर्थव्यवस्था व बजेट", AppTab.PYQ_BANK, "📚 SUBJECT"),
                        HomeSearchShortcut("सामान्य विज्ञान (Science)", "भौतिक शास्त्र, रसायन व जीवशास्त्र", AppTab.PYQ_BANK, "📚 SUBJECT"),
                        HomeSearchShortcut("चाचणी मालिका (Test Series)", "सर्व विषयवार मॉक टेस्ट पेपर", AppTab.TEST_SERIES, "📝 TEST"),
                        HomeSearchShortcut("MPSC Syllabus (अभ्यासक्रम)", "नवीन सुधारित अभ्यासक्रम व पॅटर्न", AppTab.SYLLABUS, "📋 SYLLABUS"),
                        HomeSearchShortcut("सेव्ह केलेले प्रश्न (Saved Vault)", "बुकमार्क केलेले प्रश्न", AppTab.BOOKMARKS, "🔖 SAVED")
                    )

                    val matchingShortcuts = shortcuts.filter {
                        it.title.contains(query, ignoreCase = true) ||
                        it.subtitle.contains(query, ignoreCase = true) ||
                        it.type.contains(query, ignoreCase = true)
                    }.take(4)

                    Spacer(modifier = Modifier.height(6.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (languageMode == LanguageMode.MARATHI) "शोध निकाल (Suggestions)" else "Suggestions",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )

                            if (matchingTests.isEmpty() && matchingShortcuts.isEmpty()) {
                                Text(
                                    text = if (languageMode == LanguageMode.MARATHI) "कोणताही निकाल सापडला नाही. ('राज्यसेवा', 'इतिहास', 'मॉक टेस्ट' टाईप करा)" else "No results found. Try searching 'Rajyaseva', 'History', or 'Mock'.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(6.dp)
                                )
                            } else {
                                // Render Matching Test Papers
                                matchingTests.forEach { test ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                searchQuery = ""
                                                onStartTest(test)
                                            }
                                            .padding(horizontal = 8.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = test.title,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${if (test.subjectName.isNotBlank()) test.subjectName else test.examType} • ${test.questionCount} Qs • ${test.durationMinutes} Mins",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "📝 START TEST",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                // Render Matching Shortcuts / Subjects / Exams
                                matchingShortcuts.forEach { sc ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                searchQuery = ""
                                                onNavigateTab(sc.targetTab)
                                            }
                                            .padding(horizontal = 8.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = sc.title,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = sc.subtitle,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MpscGold.copy(alpha = 0.18f)
                                        ) {
                                            Text(
                                                text = sc.type,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFD97706),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
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

        // MPSC ABHYAS Pass Banner (Always visible!)
        item {
            MpscPassBanner()
        }

        // Quick Action Cards Grid (3 Action Launcher Cards)
        item {
            Column {
                Text(
                    text = if (languageMode == LanguageMode.MARATHI) "त्वरित प्रवेश (Quick Actions)" else "Quick Actions",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        title = "PYQ Bank",
                        subtitle = "2008-2026",
                        icon = Icons.Default.MenuBook,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f)
                    ) { onNavigateTab(AppTab.PYQ_BANK) }

                    QuickActionCard(
                        title = "Mock Tests",
                        subtitle = "Full Mocks",
                        icon = Icons.Default.Assignment,
                        color = MpscEmerald,
                        modifier = Modifier.weight(1f)
                    ) { onNavigateTab(AppTab.TEST_SERIES) }

                    QuickActionCard(
                        title = "Syllabus",
                        subtitle = "MPSC Pattern",
                        icon = Icons.Default.ViewList,
                        color = Color(0xFF8B5CF6),
                        modifier = Modifier.weight(1f)
                    ) { onNavigateTab(AppTab.SYLLABUS) }
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

private data class HomeSearchShortcut(
    val title: String,
    val subtitle: String,
    val targetTab: AppTab,
    val type: String
)
