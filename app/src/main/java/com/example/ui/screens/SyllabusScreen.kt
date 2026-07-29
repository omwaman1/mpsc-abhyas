package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.ApiSyllabusItem
import com.example.data.remote.RetrofitClient
import com.example.ui.theme.TestbookNavy
import com.example.ui.theme.TestbookOrange
import com.example.ui.viewmodel.LanguageMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class SyllabusCategoryGroup(val titleMr: String, val titleEn: String) {
    RAJYASEVA("राज्यसेवा", "Rajyaseva"),
    GROUP_B("MPSC गट ब", "MPSC Group B"),
    GROUP_C("MPSC गट क", "MPSC Group C")
}

private fun categorizeSyllabusItem(item: ApiSyllabusItem): SyllabusCategoryGroup {
    val nameLower = item.examName.lowercase()
    return when {
        nameLower.contains("group b") || nameLower.contains("गट ब") || nameLower.contains("combine b") || nameLower.contains("कंबाइन ब") || nameLower.contains("sti") || nameLower.contains("psi") || nameLower.contains("aso") || nameLower.contains("sub-registrar") || nameLower.contains("निबंधक") || nameLower.contains("मुद्रांक") -> SyllabusCategoryGroup.GROUP_B
        nameLower.contains("group c") || nameLower.contains("गट क") || nameLower.contains("combine c") || nameLower.contains("कंबाइन क") || nameLower.contains("clerk") || nameLower.contains("typist") || nameLower.contains("लिपिक") || nameLower.contains("tax") || nameLower.contains("excise") -> SyllabusCategoryGroup.GROUP_C
        else -> SyllabusCategoryGroup.RAJYASEVA
    }
}

@Composable
fun SyllabusScreen(
    languageMode: LanguageMode,
    onHeaderUpdate: (title: String?, subtitle: String?, backAction: (() -> Unit)?) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var syllabusList by remember { mutableStateOf<List<ApiSyllabusItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedSyllabus by remember { mutableStateOf<ApiSyllabusItem?>(null) }
    var retryCount by remember { mutableIntStateOf(0) }

    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })

    val rajyasevaItems = remember(syllabusList) {
        syllabusList.filter { categorizeSyllabusItem(it) == SyllabusCategoryGroup.RAJYASEVA }
    }
    val groupBItems = remember(syllabusList) {
        syllabusList.filter { categorizeSyllabusItem(it) == SyllabusCategoryGroup.GROUP_B }
    }
    val groupCItems = remember(syllabusList) {
        syllabusList.filter { categorizeSyllabusItem(it) == SyllabusCategoryGroup.GROUP_C }
    }

    LaunchedEffect(retryCount) {
        try {
            isLoading = true
            errorMessage = null
            val resp = withContext(Dispatchers.IO) {
                RetrofitClient.apiService.getSyllabus()
            }
            if (resp.status == "success") {
                syllabusList = resp.data
                errorMessage = null
            } else {
                errorMessage = "Failed to load syllabus from server."
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Connection error. Could not reach database."
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(selectedSyllabus) {
        if (selectedSyllabus != null) {
            onHeaderUpdate(
                selectedSyllabus!!.examName,
                if (languageMode == LanguageMode.MARATHI) "अधिकृत अभ्यासक्रम व परीक्षेचे स्वरूप" else "Official Exam Pattern & Syllabus",
                { selectedSyllabus = null }
            )
        } else {
            onHeaderUpdate(null, null, null)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isLoading) {
            com.example.ui.components.ShimmerCardList(itemCount = 6)
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                    Text("Error: $errorMessage", color = Color(0xFFEF4444), fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { retryCount++ }) {
                        Text("Retry Fetch")
                    }
                }
            }
        } else if (selectedSyllabus != null) {
            // DETAILED SYLLABUS AND EXAM PATTERN VIEW WITH DATA TABLES
            SyllabusDetailView(
                item = selectedSyllabus!!,
                languageMode = languageMode
            )
        } else {
            // EXAM SYLLABUS LIST SELECTION SCREEN WITH 3 CATEGORIES & SWIPE PAGER
            Column(modifier = Modifier.fillMaxSize()) {
                // 1. TOP 3-BUTTON CATEGORY SWITCHER BAR
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val categoryTabs = listOf(
                        SyllabusCategoryGroup.RAJYASEVA,
                        SyllabusCategoryGroup.GROUP_B,
                        SyllabusCategoryGroup.GROUP_C
                    )

                    categoryTabs.forEachIndexed { index, category ->
                        val isSelected = (pagerState.currentPage == index)
                        val itemCount = when (index) {
                            0 -> rajyasevaItems.size
                            1 -> groupBItems.size
                            else -> groupCItems.size
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch { pagerState.animateScrollToPage(index) }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                                    RoundedCornerShape(8.dp)
                                ),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            ),
                            contentPadding = PaddingValues(vertical = 6.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (languageMode == LanguageMode.MARATHI) category.titleMr else category.titleEn,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$itemCount Exams",
                                    fontSize = 9.sp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // 2. HORIZONTAL PAGER WITH SWIPE GESTURES
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { page ->
                    val currentItems = when (page) {
                        0 -> rajyasevaItems
                        1 -> groupBItems
                        else -> groupCItems
                    }

                    SyllabusCategoryGridPage(
                        items = currentItems,
                        languageMode = languageMode,
                        onSelectSyllabus = { selectedSyllabus = it }
                    )
                }
            }
        }
    }
}

@Composable
private fun SyllabusCategoryGridPage(
    items: List<ApiSyllabusItem>,
    languageMode: LanguageMode,
    onSelectSyllabus: (ApiSyllabusItem) -> Unit
) {
    if (items.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = if (languageMode == LanguageMode.MARATHI) "या वर्गवारीसाठी सध्या कोणतीही परीक्षा जोडलेली नाही." else "No exams currently available under this category.",
                    modifier = Modifier.padding(24.dp),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            itemsIndexed(items) { index, item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectSyllabus(item) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = item.examName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp,
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (languageMode == LanguageMode.MARATHI) "स्वरूप व अभ्यासक्रम" else "Syllabus & Pattern",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TestbookOrange
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = TestbookOrange,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SyllabusDetailView(
    item: ApiSyllabusItem,
    languageMode: LanguageMode
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Pattern, 1 = Detailed Syllabus

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Selector Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary,
                    height = 3.dp
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(if (languageMode == LanguageMode.MARATHI) "परीक्षा स्वरूप (Scheme)" else "Exam Scheme", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(if (languageMode == LanguageMode.MARATHI) "विषयवार अभ्यासक्रम (Syllabus)" else "Topic Syllabus", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            )
        }

        // Content Area with Structured Tables
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (selectedTab == 0) {
                // EXAM PATTERN TABLE CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (languageMode == LanguageMode.MARATHI) "गुण विभागणी, वेळ व नियम तक्ता (Exam Scheme Table)" else "Exam Scheme & Pattern Data Table",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val rawPattern = if (languageMode == LanguageMode.MARATHI) item.patternMr else item.patternEn
                        val patternText = if (!rawPattern.isNullOrEmpty()) rawPattern else (item.patternMr ?: "")

                        SyllabusPatternTable(patternText = patternText, languageMode = languageMode)
                    }
                }
            } else {
                // TOPIC WISE SYLLABUS TABLE CARD
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = TestbookOrange,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (languageMode == LanguageMode.MARATHI) "विषयनिहाय अभ्यासक्रम तक्ता (Syllabus Table)" else "Topic-wise Syllabus Breakdown Table",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val rawSyllabus = if (languageMode == LanguageMode.MARATHI) item.syllabusMr else item.syllabusEn
                        val syllabusText = if (!rawSyllabus.isNullOrEmpty()) rawSyllabus else (item.syllabusMr ?: "")

                        SyllabusTopicTable(syllabusText = syllabusText, languageMode = languageMode)
                    }
                }
            }
        }
    }
}

@Composable
fun SyllabusPatternTable(
    patternText: String,
    languageMode: LanguageMode
) {
    val lines = patternText.lines().filter { it.isNotBlank() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
    ) {
        // Table Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (languageMode == LanguageMode.MARATHI) "अ.क्र." else "Sr.",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.width(30.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (languageMode == LanguageMode.MARATHI) "परीक्षेचे स्वरूप, गुण विभागणी व नियम" else "Exam Component, Marks & Rules",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
        }

        // Table Data Rows
        lines.forEachIndexed { index, line ->
            val cleanLine = line.replace("•", "").trim()
            val bg = if (index % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bg)
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = TestbookOrange.copy(alpha = 0.15f),
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${index + 1}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TestbookOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = cleanLine,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            if (index < lines.size - 1) {
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), thickness = 0.8.dp)
            }
        }
    }
}

@Composable
fun SyllabusTopicTable(
    syllabusText: String,
    languageMode: LanguageMode
) {
    val lines = syllabusText.lines().filter { it.isNotBlank() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
    ) {
        // Table Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (languageMode == LanguageMode.MARATHI) "अ.क्र." else "Sr.",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.width(30.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (languageMode == LanguageMode.MARATHI) "विषय / घटकाचा सविस्तर अभ्यासक्रम" else "Subject & Topic-wise Breakdown",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
        }

        // Table Data Rows
        lines.forEachIndexed { index, line ->
            val bg = if (index % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant

            val parts = line.split(":", limit = 2)
            val titlePart = parts.getOrNull(0)?.trim() ?: ""
            val detailPart = parts.getOrNull(1)?.trim() ?: ""

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bg)
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.width(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            text = "${index + 1}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    if (detailPart.isNotBlank()) {
                        Text(
                            text = titlePart,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = detailPart,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 20.sp
                        )
                    } else {
                        Text(
                            text = line,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            if (index < lines.size - 1) {
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), thickness = 0.8.dp)
            }
        }
    }
}
