package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MpscEmerald
import com.example.ui.theme.MpscGold
import com.example.ui.theme.MpscNavy
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.AppThemeMode
import com.example.ui.viewmodel.LanguageMode
import com.example.ui.viewmodel.UserProfile

@Composable
fun MPSCNavigationDrawerContent(
    selectedTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    onCloseDrawer: () -> Unit,
    selectedLanguage: LanguageMode = LanguageMode.ENGLISH,
    onSelectLanguage: (LanguageMode) -> Unit = {},
    currentTheme: AppThemeMode = AppThemeMode.LIGHT,
    onSelectTheme: (AppThemeMode) -> Unit = {},
    userProfile: UserProfile = UserProfile(),
    daysLeftText: String = "",
    onUpdateName: (String, (Boolean) -> Unit) -> Unit = { _, _ -> },
    onOpenSubscriptions: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenCareers: () -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isProfileEditOpen by remember { mutableStateOf(false) }

    if (isProfileEditOpen) {
        ProfileEditDialog(
            userProfile = userProfile,
            onSaveName = onUpdateName,
            onDismiss = { isProfileEditOpen = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
    ) {
        // Header Section - Dark Navy with User Details & Edit Pencil Icon
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(top = 44.dp, start = 16.dp, end = 16.dp, bottom = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    val displayName = if (userProfile.fullName.isNotBlank()) userProfile.fullName else "Student"
                    val initials = displayName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase().ifEmpty { "MP" }

                    // Avatar Circle with App Logo
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "User Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = displayName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                            if (daysLeftText.isNotBlank()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF0284C7)
                                ) {
                                    Text(
                                        text = daysLeftText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (userProfile.phone.isNotBlank()) userProfile.phone else if (userProfile.email.isNotBlank()) userProfile.email else "MPSC Aspirant",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 1
                        )
                    }
                }

                // Edit Profile Icon Pencil Button
                IconButton(
                    onClick = {
                        onCloseDrawer()
                        isProfileEditOpen = true
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Language Switcher Toggle Control (Marathi vs English)
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Language",
                    tint = MpscNavy,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (selectedLanguage == LanguageMode.MARATHI) "भाषा निवडा (Select Language):" else "Select Language:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            val isAppDark = MaterialTheme.colorScheme.background == com.example.ui.theme.BackgroundDark
            val activePillBg = if (isAppDark) MaterialTheme.colorScheme.primary else MpscNavy

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val isMarathi = (selectedLanguage == LanguageMode.MARATHI)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectLanguage(LanguageMode.MARATHI) },
                    color = if (isMarathi) activePillBg else Color.Transparent
                ) {
                    Text(
                        text = "मराठी",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isMarathi) Color.White else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelectLanguage(LanguageMode.ENGLISH) },
                    color = if (!isMarathi) activePillBg else Color.Transparent
                ) {
                    Text(
                        text = "English",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!isMarathi) Color.White else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Main Drawer Menu List
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
            icon = Icons.Default.Share,
            label = "Share App (ॲप शेअर करा)",
            isSelected = false,
            onClick = {
                onCloseDrawer()
                val packageName = context.packageName
                val playStoreUrl = "https://play.google.com/store/apps/details?id=$packageName"
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "MPSC ABHYAS - Exam Preparation App")
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "MPSC परीक्षा तयारीसाठी सर्वोत्तम ॲप MPSC ABHYAS! PYQ बँक, मॉक् टेस्ट आणि नोट्स डाऊनलोड करा:\n\n📲 Google Play Store Link:\n$playStoreUrl\n\n🌐 वेबसाईट: https://mpscabhyas.in/"
                    )
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share MPSC ABHYAS App"))
            }
        )

        DrawerMenuItem(
            icon = Icons.Default.Star,
            label = "Rate the App (ॲपला रेटिंग द्या)",
            isSelected = false,
            onClick = {
                onCloseDrawer()
                val packageName = context.packageName
                try {
                    val rateIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                    rateIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                    context.startActivity(rateIntent)
                } catch (e: Exception) {
                    val webRateIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
                    context.startActivity(webRateIntent)
                }
            }
        )

        DrawerMenuItem(
            icon = Icons.Default.Apps,
            label = "Other Apps (आमचे इतर ॲप्स)",
            isSelected = false,
            onClick = {
                onCloseDrawer()
                val devUrl = "https://play.google.com/store/apps/developer?id=Softweb_Technologies"
                val devIntent = Intent(Intent.ACTION_VIEW, Uri.parse(devUrl))
                context.startActivity(devIntent)
            }
        )

        DrawerMenuItem(
            icon = Icons.Default.Info,
            label = "Privacy Policy (गोपनीयता धोरण)",
            isSelected = false,
            onClick = {
                onCloseDrawer()
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://mpscabhyas.in/privacy-policy.html"))
                context.startActivity(intent)
            }
        )

        DrawerMenuItem(
            icon = Icons.Default.Settings,
            label = "App Settings (ॲप सेटिंग्ज)",
            isSelected = false,
            onClick = {
                onCloseDrawer()
                onOpenSettings()
            }
        )

        DrawerMenuItem(
            icon = Icons.Default.GroupAdd,
            label = "Join Us (सामील व्हा)",
            isSelected = false,
            onClick = {
                onCloseDrawer()
                onOpenCareers()
            }
        )

        DrawerMenuItem(
            icon = Icons.Default.ExitToApp,
            label = "Logout (लॉग आउट)",
            isSelected = false,
            onClick = {
                onCloseDrawer()
                onLogout()
            }
        )

        // Spacer for clean layout separation
        Spacer(modifier = Modifier.height(20.dp))

        // Social Groups Section (Telegram & WhatsApp Links)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Join Study Groups (अभ्यास गट):",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // WhatsApp Button
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://chat.whatsapp.com/JjuUUBlcszmEsaqHPrffsI?s=sw&p=a&ilr=4&amv=0"))
                            context.startActivity(intent)
                        },
                    color = Color(0xFF25D366)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "WhatsApp",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                // Telegram Button
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/+k6sD39Fi2B03Yzc9"))
                            context.startActivity(intent)
                        },
                    color = Color(0xFF0088CC)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Telegram",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Telegram", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Footer - Version Info with Navigation Bar Inset Safety Padding
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
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
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Text(
                text = "v2.5.0 (Pro)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.navigationBarsPadding())
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isAppDark = MaterialTheme.colorScheme.background == com.example.ui.theme.BackgroundDark

    val backgroundColor = when {
        isSelected && isAppDark -> MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
        isSelected -> MpscNavy.copy(alpha = 0.12f)
        else -> Color.Transparent
    }
    val textColor = when {
        isSelected && isAppDark -> Color.White
        isSelected -> MpscNavy
        else -> MaterialTheme.colorScheme.onSurface
    }
    val iconColor = when {
        isSelected && isAppDark -> MaterialTheme.colorScheme.primary
        isSelected -> MpscNavy
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
fun AppSettingsDialog(
    currentTheme: AppThemeMode,
    onSelectTheme: (AppThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MpscNavy,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("App Theme Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(
                    text = "App Theme / थीम निवडा:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                ThemeOptionRow(
                    title = "☀️ Light Mode (प्रकाश थीम)",
                    selected = currentTheme == AppThemeMode.LIGHT,
                    onSelect = { onSelectTheme(AppThemeMode.LIGHT) }
                )
                ThemeOptionRow(
                    title = "🌙 Dark Mode (डार्क थीम)",
                    selected = currentTheme == AppThemeMode.DARK,
                    onSelect = { onSelectTheme(AppThemeMode.DARK) }
                )
                ThemeOptionRow(
                    title = "📱 System Default (सिस्टम डिफॉल्ट)",
                    selected = currentTheme == AppThemeMode.SYSTEM,
                    onSelect = { onSelectTheme(AppThemeMode.SYSTEM) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", fontWeight = FontWeight.Bold, color = MpscNavy)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp)
    )
}

@Composable
fun SubscriptionDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Premium Pass",
                    tint = MpscGold,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("MPSC ABHYAS Subscriptions", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, MpscGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "🎉 MPSC FREE PRO PASS ACTIVE",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "You have full free access to all 50,000+ PYQs, Test Series, and Mock Papers!",
                            fontSize = 11.sp,
                            color = Color(0xFF78350F)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MpscNavy)
            ) {
                Text("Awesome!", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp)
    )
}

@Composable
fun ThemeOptionRow(
    title: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(selectedColor = MpscNavy)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MpscNavy else MaterialTheme.colorScheme.onSurface
        )
    }
}
