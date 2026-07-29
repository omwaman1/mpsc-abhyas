package com.example.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MpscNavy

object PlayStoreInstallerChecker {

    private const val PLAY_STORE_PACKAGE = "com.android.vending"
    private const val PLAY_STORE_FEEDBACK = "com.google.android.feedback"

    /**
     * Checks if the app was installed directly via Google Play Store.
     * Returns false if sideloaded from Telegram, Chrome, or unauthorized sources!
     */
    fun isInstalledFromPlayStore(context: Context): Boolean {
        try {
            val packageName = context.packageName
            val installerPackage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.packageManager.getInstallSourceInfo(packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getInstallerPackageName(packageName)
            }

            // In DEBUG mode (local testing in Android Studio), bypass check!
            val isDebuggable = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
            if (isDebuggable) {
                return true
            }

            return installerPackage == PLAY_STORE_PACKAGE || installerPackage == PLAY_STORE_FEEDBACK
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}

@Composable
fun UnauthorizedInstallerDialog(
    context: Context,
    onOpenPlayStore: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Non-dismissable */ },
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Security Alert",
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "अवैध ॲप इन्स्टॉलेशन\n(Unauthorized App Installation)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = "हे ॲप फक्त अधिकृत Google Play Store वरूनच इन्स्टॉल केले जाऊ शकते. सुरक्षेच्या कारणास्तव, कृपया Play Store वरून अधिकृत ॲप डाऊनलोड करा.\n\nThis app can only be used when installed directly from Google Play Store.",
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val appPackageName = context.packageName
                    try {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        )
                    } catch (e: Exception) {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MpscNavy),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Play Store वरून इन्स्टॉल करा",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}
