package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.TestPaperEntity
import com.example.ui.theme.TestbookNavy
import com.example.ui.viewmodel.LanguageMode

@Composable
fun TestSeriesScreen(
    testPapers: List<TestPaperEntity>,
    languageMode: LanguageMode,
    onStartTest: (TestPaperEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("All Tests", "Full Mock Test", "Subject Test", "PYQ Paper", "Speed Test")
    var selectedCategory by remember { mutableStateOf("All Tests") }

    val filteredList = if (selectedCategory == "All Tests") {
        testPapers
    } else {
        testPapers.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .testTag("test_series_lazy_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Banner Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = TestbookNavy),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (languageMode == LanguageMode.MARATHI) "MPSC टेस्ट सिरीज २०२६ (Live Mock Tests)" else "MPSC Test Series 2026",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (languageMode == LanguageMode.MARATHI) "राज्यसेवा व संयुक्त परीक्षा गट ब आणि क साठी परिपूर्ण सराव" else "Real exam environment with detailed solutions & All-Maharashtra Rank",
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Category Filter Tabs
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = (selectedCategory == cat),
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TestbookNavy,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Test List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredList.size} ${if (languageMode == LanguageMode.MARATHI) "टेस्ट्स उपलब्ध" else "Tests Available"}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
            }
        }

        // Tests
        items(filteredList) { paper ->
            TestPaperItemCard(
                testPaper = paper,
                languageMode = languageMode,
                onStartTest = { onStartTest(paper) }
            )
        }
    }
}
