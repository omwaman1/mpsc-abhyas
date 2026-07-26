package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TestbookEmerald
import com.example.ui.theme.TestbookNavy
import com.example.ui.viewmodel.AppTab

@Composable
fun MPSCNavigationDrawerContent(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(Color.White)
    ) {
        // Header Section - Dark Navy
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(top = 48.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar Circle
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF59E0B)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AP",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    // Pass Pro Badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = TestbookEmerald
                    ) {
                        Text(
                            text = "PASS PRO ACTIVE",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Aniket Patil",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Email
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "aniket.mpsc2026@gmail.com",
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Phone
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Phone",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "+91 98765 43210",
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Menu Items
        DrawerMenuItem(
            icon = Icons.Default.Home,
            label = "Home",
            isSelected = selectedTab == AppTab.HOME,
            onClick = {
                onTabSelected(AppTab.HOME)
                onCloseDrawer()
            }
        )

        DrawerMenuItem(
            icon = Icons.Default.CreditCard,
            label = "Subscriptions",
            isSelected = false,
            onClick = { onCloseDrawer() }
        )

        DrawerMenuItem(
            icon = Icons.Default.Person,
            label = "Profile",
            isSelected = false,
            onClick = { onCloseDrawer() }
        )

        DrawerMenuItem(
            icon = Icons.Default.Settings,
            label = "App Settings",
            isSelected = false,
            onClick = { onCloseDrawer() }
        )

        DrawerMenuItem(
            icon = Icons.Default.Apps,
            label = "Other Apps",
            isSelected = false,
            onClick = { onCloseDrawer() }
        )

        DrawerMenuItem(
            icon = Icons.Default.Share,
            label = "Share App",
            isSelected = false,
            onClick = { onCloseDrawer() }
        )

        // Spacer to push footer to bottom
        Spacer(modifier = Modifier.weight(1f))

        // Footer - Version Info
        HorizontalDivider(color = Color(0xFFE2E8F0))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Version",
                    tint = Color(0xFF0284C7),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "App Version",
                    fontSize = 13.sp,
                    color = Color(0xFF475569)
                )
            }

            Text(
                text = "v3.2.0 (Pro)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TestbookNavy
            )
        }
    }
}

@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) Color(0xFFEFF6FF) else Color.Transparent
    val textColor = if (isSelected) Color(0xFF0284C7) else Color(0xFF334155)
    val iconColor = if (isSelected) Color(0xFF0284C7) else Color(0xFF64748B)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(bgColor)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}
