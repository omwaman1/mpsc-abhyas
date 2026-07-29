package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.MpscNavy
import com.example.ui.theme.MpscGold
import com.example.ui.theme.MpscEmerald
import com.example.ui.theme.MpscOrange
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Speed
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import kotlinx.coroutines.delay

@Composable
fun TopHeaderBar(
    title: String = "MPSC ABHYAS",
    subtitle: String = "MPSC ABHYAS Test Series & PYQ Bank",
    backAction: (() -> Unit)? = null,
    onMenuClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isAppDark = MaterialTheme.colorScheme.background == BackgroundDark

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (isAppDark) MaterialTheme.colorScheme.surface else MpscNavy,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Icon (Back Arrow or User Avatar) & App/Screen Title
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (backAction != null) {
                        IconButton(onClick = backAction) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    } else {
                        // MPSC ABHYAS App Logo - Curved Square Icon
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF0F172A),
                                            Color(0xFF1E3A8A),
                                            Color(0xFF2563EB)
                                        )
                                    )
                                )
                                .border(
                                    BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.6f)),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onMenuClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.app_logo),
                                contentDescription = "MPSC ABHYAS Logo",
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subtitle,
                            color = Color(0xFFCBD5E1),
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Actions: Notification Icon on Top Right (Home Screen Only)
                if (backAction == null) {
                    IconButton(
                        onClick = onNotificationClick,
                        modifier = Modifier.testTag("notification_icon_button")
                    ) {
                        Box {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            // Red notification badge indicator dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444))
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }
                }
            }
        }
    }
}

data class AppBannerItem(
    val badgeText: String,
    val badgeColor: Color,
    val title: String,
    val subtitle: String,
    val gradientColors: List<Color>,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun MpscPassBanner(
    modifier: Modifier = Modifier
) {
    val banners = listOf(
        AppBannerItem(
            badgeText = "MPSC ABHYAS PRO",
            badgeColor = Color(0xFFFFD700),
            title = "५०,०००+ हून अधिक प्रश्न उपलब्ध",
            subtitle = "राज्यसेवा, गट ब व गट क परीक्षेसाठी अमर्याद सराव संच!",
            gradientColors = listOf(Color(0xFF0B1938), Color(0xFF0F3D7B), Color(0xFF0284C7)),
            icon = Icons.Default.Star
        ),
        AppBannerItem(
            badgeText = "PYQ BANK 2008-2026",
            badgeColor = Color(0xFF34D399),
            title = "२००८ ते २०२६ अद्ययावत प्रश्नसंच",
            subtitle = "विषयानुसार व परीक्षावार सर्व MPSC जुने प्रश्न!",
            gradientColors = listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF0D9488)),
            icon = Icons.Default.MenuBook
        ),
        AppBannerItem(
            badgeText = "DAILY MOCK TESTS",
            badgeColor = Color(0xFFF472B6),
            title = "दररोज नवीन सराव चाचण्यांचा समावेश",
            subtitle = "अभ्यासाची तयारी तपासा - ऑल महाराष्ट्र रँकिंगसह!",
            gradientColors = listOf(Color(0xFF4C1D95), Color(0xFF6D28D9), Color(0xFF7C3AED)),
            icon = Icons.Default.Speed
        )
    )

    val pagerState = rememberPagerState(pageCount = { banners.size })

    // Auto-scroll loop every 3.5 seconds
    LaunchedEffect(pagerState) {
        while (true) {
            delay(3500)
            val nextPage = (pagerState.currentPage + 1) % banners.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val banner = banners[page]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp)
                    .testTag("app_banner_$page"),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brush = Brush.horizontalGradient(colors = banner.gradientColors))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = banner.badgeColor
                            ) {
                                Text(
                                    text = banner.badgeText,
                                    color = Color.Black,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = banner.title,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 19.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = banner.subtitle,
                                color = Color(0xFFE2E8F0),
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = banner.icon,
                                contentDescription = banner.badgeText,
                                tint = banner.badgeColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Animated Carousel Indicator Dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(banners.size) { iteration ->
                val isSelected = pagerState.currentPage == iteration
                val dotWidth by animateDpAsState(
                    targetValue = if (isSelected) 22.dp else 6.dp,
                    label = "dotWidth"
                )
                val dotColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)

                Box(
                    modifier = Modifier
                        .height(6.dp)
                        .width(dotWidth)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
        }
    }
}
