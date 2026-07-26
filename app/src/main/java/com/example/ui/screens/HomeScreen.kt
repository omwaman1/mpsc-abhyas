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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import com.example.ui.components.TestbookPassBanner
import com.example.ui.theme.TestbookEmerald
import com.example.ui.theme.TestbookEmeraldLight
import com.example.ui.theme.TestbookGold
import com.example.ui.theme.TestbookNavy
import com.example.ui.theme.TestbookOrange
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.LanguageMode

@Composable
fun HomeScreen(
    testPapers: List<TestPaperEntity>,
    currentAffairs: List<CurrentAffairsEntity>,
    languageMode: LanguageMode,
    onStartTest: (TestPaperEntity) -> Unit,
    onNavigateTab: (AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val examCategories = listOf("All Exams", "Rajyaseva Prelims", "Combine Group B & C", "CSAT Special", "State Services")
    var selectedCategory by remember { mutableStateOf("All Exams") }

    val filteredTests = testPapers.filter { test ->
        val matchCategory = selectedCategory == "All Exams" || test.examType.contains(selectedCategory, ignoreCase = true)
        val matchSearch = searchQuery.isEmpty() || test.title.contains(searchQuery, ignoreCase = true)
        matchCategory && matchSearch
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .testTag("home_screen_lazy_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search & Pass Banner
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = if (languageMode == LanguageMode.MARATHI) "मराठी प्रश्न, टेस्ट सीरिज किंवा विषय शोधा..." else "Search PYQ tests, subjects or exams...",
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TestbookNavy
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_search_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = TestbookNavy,
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                ),
                singleLine = true
            )
        }

        item {
            TestbookPassBanner()
        }

        // Quick Action Grid (Testbook style icons)
        item {
            Column {
                Text(
                    text = if (languageMode == LanguageMode.MARATHI) "त्वरित प्रवेश (Quick Actions)" else "Quick Actions",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TestbookNavy
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        title = if (languageMode == LanguageMode.MARATHI) "PYQ बँक" else "PYQ Vault",
                        subtitle = "2020-2024",
                        icon = Icons.Default.MenuBook,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f)
                    ) { onNavigateTab(AppTab.PYQ_BANK) }

                    QuickActionCard(
                        title = if (languageMode == LanguageMode.MARATHI) "टेस्ट सिरीज" else "Test Series",
                        subtitle = "Full Mocks",
                        icon = Icons.Default.Assignment,
                        color = TestbookEmerald,
                        modifier = Modifier.weight(1f)
                    ) { onNavigateTab(AppTab.TEST_SERIES) }

                    QuickActionCard(
                        title = if (languageMode == LanguageMode.MARATHI) "स्पीड टेस्ट" else "Speed Quiz",
                        subtitle = "5-10 Mins",
                        icon = Icons.Default.FlashOn,
                        color = TestbookOrange,
                        modifier = Modifier.weight(1f)
                    ) {
                        val speedTest = testPapers.firstOrNull { it.category == "Speed Test" } ?: testPapers.firstOrNull()
                        speedTest?.let { onStartTest(it) }
                    }

                    QuickActionCard(
                        title = if (languageMode == LanguageMode.MARATHI) "सेव्हड प्रश्न" else "Saved Qs",
                        subtitle = "Revision",
                        icon = Icons.Default.Bookmark,
                        color = Color(0xFF8B5CF6),
                        modifier = Modifier.weight(1f)
                    ) { onNavigateTab(AppTab.BOOKMARKS) }
                }
            }
        }

        // Category Selector Chips
        item {
            Column {
                Text(
                    text = if (languageMode == LanguageMode.MARATHI) "परीक्षा वर्गवारी (Exam Categories)" else "Exam Categories",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TestbookNavy
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(examCategories) { category ->
                        FilterChip(
                            selected = (selectedCategory == category),
                            onClick = { selectedCategory = category },
                            label = { Text(text = category, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TestbookNavy,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color(0xFF334155)
                            )
                        )
                    }
                }
            }
        }

        // Featured Mock Tests
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (languageMode == LanguageMode.MARATHI) "प्रमुख मॉक टेस्ट्स (Featured Tests)" else "Featured Test Series",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TestbookNavy
                )
                Text(
                    text = if (languageMode == LanguageMode.MARATHI) "सर्व पहा >" else "View All >",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TestbookNavy,
                    modifier = Modifier.clickable { onNavigateTab(AppTab.TEST_SERIES) }
                )
            }
        }

        items(filteredTests) { testPaper ->
            TestPaperItemCard(
                testPaper = testPaper,
                languageMode = languageMode,
                onStartTest = { onStartTest(testPaper) }
            )
        }

        // Daily Current Affairs Digest (Chalu घडामोडी)
        item {
            Spacer(modifier = Modifier.height(8.dp))
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
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(TestbookNavy.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Newspaper,
                                    contentDescription = "News",
                                    tint = TestbookNavy,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (languageMode == LanguageMode.MARATHI) "चालू घडामोडी (Daily Current Affairs)" else "Daily Current Affairs Digest",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TestbookNavy
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = TestbookEmeraldLight
                        ) {
                            Text(
                                text = "MPSC Specal",
                                color = TestbookEmerald,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    currentAffairs.take(2).forEach { ca ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.MARATHI) ca.titleMarathi else ca.titleEnglish,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (languageMode == LanguageMode.MARATHI) ca.summaryMarathi else ca.summaryEnglish,
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                lineHeight = 15.sp
                            )
                        }
                    }
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TestbookNavy,
                maxLines = 1
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
fun TestPaperItemCard(
    testPaper: TestPaperEntity,
    languageMode: LanguageMode,
    onStartTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("test_item_${testPaper.testId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = TestbookNavy.copy(alpha = 0.08f)
                ) {
                    Text(
                        text = testPaper.examType,
                        color = TestbookNavy,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                if (testPaper.isFree) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = TestbookEmeraldLight
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Free",
                                tint = TestbookEmerald,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "FREE MOCK",
                                color = TestbookEmerald,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = testPaper.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = "Questions",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${testPaper.questionCount} Qs",
                            fontSize = 11.sp,
                            color = Color(0xFF475569)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Duration",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${testPaper.durationMinutes} mins",
                            fontSize = 11.sp,
                            color = Color(0xFF475569)
                        )
                    }

                    Text(
                        text = "${testPaper.totalMarks} Marks",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0284C7)
                    )
                }

                Button(
                    onClick = onStartTest,
                    colors = ButtonDefaults.buttonColors(containerColor = TestbookEmerald),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("attempt_now_btn_${testPaper.testId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Attempt",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (languageMode == LanguageMode.MARATHI) "सोडवा" else "Start",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
