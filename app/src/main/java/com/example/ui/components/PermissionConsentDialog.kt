package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.MpscNavy
import com.example.ui.theme.MpscGold
import com.example.ui.viewmodel.LanguageMode

@Composable
fun PermissionConsentDialog(
    languageMode: LanguageMode = LanguageMode.MARATHI,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Launcher for Android runtime permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val permissionsToRequest = mutableListOf<String>()
                    permissionsToRequest.add(Manifest.permission.READ_CONTACTS)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    permissionLauncher.launch(permissionsToRequest.toTypedArray())
                },
                colors = ButtonDefaults.buttonColors(containerColor = MpscNavy),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (languageMode == LanguageMode.MARATHI) "स्वीकार करा व परवानगी द्या" else "Agree & Continue",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (languageMode == LanguageMode.MARATHI) "नंतर करा" else "Skip",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MpscNavy.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MpscNavy,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Text(
                text = if (languageMode == LanguageMode.MARATHI) "ॲप परवानग्या व डेटा गोपनीयता" else "App Permissions & Privacy Disclosure",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (languageMode == LanguageMode.MARATHI)
                        "MPSC ABHYAS ॲपचे सर्व फिचर्स सुरळीत चालण्यासाठी खालील परवानग्या आवश्यक आहेत:"
                    else
                        "MPSC ABHYAS requests the following permissions to enable full study features:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 1. Notification Permission Disclosure
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (languageMode == LanguageMode.MARATHI) "१. सूचना (Notifications)" else "1. Push Notifications",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = if (languageMode == LanguageMode.MARATHI)
                                    "दैनंदिन परीक्षा अपडेट्स, मॉक टेस्ट रिमाइंडर व पास अलर्ट मिळवण्यासाठी."
                                else
                                    "To receive daily MPSC exam updates, mock test reminders & pass notifications.",
                                fontSize = 10.5.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }

                // 2. Contacts Permission Prominent Disclosure (Play Store Mandate)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Contacts,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (languageMode == LanguageMode.MARATHI) "२. संपर्क (Contact Access)" else "2. Contact Access",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = if (languageMode == LanguageMode.MARATHI)
                                    "अभ्यास मित्रांना आमंत्रित करण्यासाठी व कस्टम टेस्ट सिरिज थेट शेअर करण्यासाठी."
                                else
                                    "To invite study partners, share custom test papers & sync practice achievements.",
                                fontSize = 10.5.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }

                Text(
                    text = if (languageMode == LanguageMode.MARATHI)
                        "🔒 तुमचा डेटा सुरक्षित आहे व कधीही तिसऱ्या पक्षाला विकला जात नाही."
                    else
                        "🔒 Your personal data is encrypted & never shared with 3rd parties.",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF059669)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}

fun hasRequiredPermissions(context: Context): Boolean {
    val contactsGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    val notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else true
    return contactsGranted && notifGranted
}
