package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TestbookEmerald
import com.example.ui.theme.TestbookGold
import com.example.ui.theme.TestbookNavy
import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(
    onGoogleLogin: (fullName: String, phone: String, email: String, onResult: (Boolean, String?) -> Unit) -> Unit,
    onCheckUserRegistration: (email: String, callback: (com.example.data.remote.ApiUserData?) -> Unit) -> Unit = { _, cb -> cb(null) },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var isRegisterDialogOpen by remember { mutableStateOf(false) }
    var isLoadingUserCheck by remember { mutableStateOf(false) }
    var isSubmittingRegistration by remember { mutableStateOf(false) }
    var registrationErrorMessage by remember { mutableStateOf<String?>(null) }
    var verifiedGoogleId by remember { mutableStateOf("") }

    var inputEmail by remember { mutableStateOf("") }
    var inputFullName by remember { mutableStateOf("") }
    var inputPhone by remember { mutableStateOf("") }

    // Primary SIM Card Phone Number Hint Launcher
    val phoneNumberHintLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        try {
            val phoneNumber = Identity.getSignInClient(context).getPhoneNumberFromIntent(result.data)
            if (!phoneNumber.isNullOrBlank()) {
                val cleanPhone = phoneNumber.replace("+91", "").replace("+", "").replace(" ", "").trim()
                inputPhone = cleanPhone
                registrationErrorMessage = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun requestPrimarySimPhoneNumber() {
        try {
            val request = GetPhoneNumberHintIntentRequest.builder().build()
            Identity.getSignInClient(context)
                .getPhoneNumberHintIntent(request)
                .addOnSuccessListener { result ->
                    try {
                        phoneNumberHintLauncher.launch(
                            IntentSenderRequest.Builder(result.intentSender).build()
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getDeviceProfileName(ctx: Context): String {
        try {
            val cursor = ctx.contentResolver.query(
                android.provider.ContactsContract.Profile.CONTENT_URI,
                arrayOf(android.provider.ContactsContract.Profile.DISPLAY_NAME),
                null,
                null,
                null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIdx = it.getColumnIndex(android.provider.ContactsContract.Profile.DISPLAY_NAME)
                    if (nameIdx >= 0) {
                        val profileName = it.getString(nameIdx)
                        if (!profileName.isNullOrBlank() && !profileName.contains("@")) {
                            return profileName
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    fun autofillFromGoogleAccount(acct: GoogleSignInAccount?) {
        var realName = ""

        if (acct != null) {
            if (!acct.email.isNullOrBlank()) {
                inputEmail = acct.email!!
            }
            if (!acct.displayName.isNullOrBlank()) {
                realName = acct.displayName!!
            } else {
                val gName = acct.givenName.orEmpty()
                val fName = acct.familyName.orEmpty()
                if ((gName + fName).isNotBlank()) {
                    realName = "$gName $fName".trim()
                }
            }
            if (!acct.id.isNullOrBlank()) {
                verifiedGoogleId = acct.id!!
            }
        }

        // Query Android Profile Contact if Google Sign In didn't return a display name
        if (realName.isBlank()) {
            realName = getDeviceProfileName(context)
        }

        // Device Fallback using AccountManager for Google accounts on Android
        if (inputEmail.isBlank()) {
            try {
                val am = android.accounts.AccountManager.get(context)
                val googleAccounts = am.getAccountsByType("com.google")
                if (googleAccounts.isNotEmpty()) {
                    val primaryAccount = googleAccounts[0]
                    inputEmail = primaryAccount.name
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Apply real profile name if found (with numbers stripped), otherwise fallback to formatted email handle
        if (realName.isNotBlank()) {
            inputFullName = realName.replace(Regex("[0-9]"), "").trim()
        } else if (inputEmail.isNotBlank()) {
            val handle = inputEmail.substringBefore("@")
            val cleanedHandle = handle.replace(Regex("[0-9]"), " ").replace(".", " ").replace("_", " ").replace("-", " ")
            val nameWords = cleanedHandle.split(" ").filter { it.isNotBlank() }
            inputFullName = nameWords.joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
        }
    }

    // Trigger auto-fill check on launch
    LaunchedEffect(Unit) {
        val lastAcct = GoogleSignIn.getLastSignedInAccount(context)
        autofillFromGoogleAccount(lastAcct)
    }

    // Official Google Sign-In Launcher for Real Google Account Verification
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
    }
    val googleSignInClient = remember(context) { GoogleSignIn.getClient(context, gso) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        var fetchedAccount: GoogleSignInAccount? = null
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            fetchedAccount = task.getResult(ApiException::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            fetchedAccount = GoogleSignIn.getLastSignedInAccount(context)
        }
        autofillFromGoogleAccount(fetchedAccount)

        val targetEmail = when {
            inputEmail.isNotBlank() -> inputEmail.trim()
            fetchedAccount?.email.orEmpty().isNotBlank() -> fetchedAccount!!.email!!.trim()
            else -> {
                try {
                    val am = android.accounts.AccountManager.get(context)
                    val gAccounts = am.getAccountsByType("com.google")
                    if (gAccounts.isNotEmpty()) gAccounts[0].name else ""
                } catch (e: Exception) { "" }
            }
        }

        if (targetEmail.isNotBlank()) {
            inputEmail = targetEmail
            isLoadingUserCheck = true
            onCheckUserRegistration(targetEmail) { existingUser ->
                isLoadingUserCheck = false
                if (existingUser != null) {
                    // USER ALREADY EXISTS IN DATABASE! BYPASS REGISTRATION DIALOG COMPLETELY!
                    val nameToUse = existingUser.fullName?.ifBlank { null } ?: inputFullName.ifBlank { "MPSC Student" }
                    val phoneToUse = existingUser.phoneNumber?.ifBlank { null } ?: existingUser.mobile?.ifBlank { null } ?: inputPhone
                    onGoogleLogin(nameToUse, phoneToUse, targetEmail) { _, _ -> }
                } else {
                    // FIRST-TIME USER! Trigger SIM Phone Hint & Show Registration Dialog
                    requestPrimarySimPhoneNumber()
                    isRegisterDialogOpen = true
                }
            }
        } else {
            requestPrimarySimPhoneNumber()
            isRegisterDialogOpen = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        TestbookNavy,
                        Color(0xFF0F172A),
                        Color(0xFF0284C7)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Branding Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .border(BorderStroke(1.5.dp, Color(0xFFF59E0B)), shape = RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "MPSC ABHYAS Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "MPSC ABHYAS PRO",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "महाराष्ट्र लोकसेवा आयोग (MPSC) परीक्षा सराव ॲप",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TestbookGold,
                    textAlign = TextAlign.Center
                )
            }

            // Central Banner Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "स्वागत आहे! (Welcome Student)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TestbookNavy
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "तुमचे गूगल खाते सत्यापित करून किंवा माहिती भरून लॉग इन करा.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Real Google Login Trigger Button
                    Button(
                        onClick = {
                            if (isLoadingUserCheck) return@Button

                            if (inputEmail.isNotBlank()) {
                                isLoadingUserCheck = true
                                onCheckUserRegistration(inputEmail) { existingUser ->
                                    isLoadingUserCheck = false
                                    if (existingUser != null) {
                                        // ALREADY REGISTERED USER! DIRECT IMMEDIATE LOGIN!
                                        val nameToUse = existingUser.fullName?.ifBlank { null } ?: inputFullName
                                        val phoneToUse = existingUser.phoneNumber?.ifBlank { null } ?: existingUser.mobile?.ifBlank { null } ?: inputPhone
                                        onGoogleLogin(nameToUse, phoneToUse, inputEmail) { _, _ -> }
                                    } else {
                                        try {
                                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            requestPrimarySimPhoneNumber()
                                            isRegisterDialogOpen = true
                                        }
                                    }
                                }
                            } else {
                                try {
                                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    requestPrimarySimPhoneNumber()
                                    isRegisterDialogOpen = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        if (isLoadingUserCheck) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = TestbookNavy,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEA4335)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "G",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Verify & Sign in with Google",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }
                    }
                }
            }

            // Footer info
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Secure",
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Official MPSC Question Bank Registration",
                        fontSize = 10.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // First Time Registration Dialog (Only shown for NEW unregistered users)
        if (isRegisterDialogOpen) {
            AlertDialog(
                onDismissRequest = {
                    if (!isSubmittingRegistration) isRegisterDialogOpen = false
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "User Registration",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "विद्यार्थी नोंदणी (First Time Registration)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "कृपया तुमची माहिती तपासा व नोंदणी करा (First Time Registration):",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Duplicate Mobile Warning Banner if Scam/Duplicate Phone Error
                        if (!registrationErrorMessage.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF7F1D1D),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Warning",
                                        tint = Color(0xFFF87171),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = registrationErrorMessage!!,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // 1. Email Address Input
                        OutlinedTextField(
                            value = inputEmail,
                            onValueChange = { inputEmail = it },
                            label = { Text("ई-मेल पत्ता (Email Address)") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = "Email", tint = MaterialTheme.colorScheme.primary)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        // 2. Full Name Input
                        OutlinedTextField(
                            value = inputFullName,
                            onValueChange = { inputFullName = it },
                            label = { Text("संपूर्ण नाव (Full Name)") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = "Name", tint = MaterialTheme.colorScheme.primary)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        // 3. Phone Number Input (Read-only, taken as-is from Google SIM Prompt)
                        Column {
                            OutlinedTextField(
                                value = inputPhone,
                                onValueChange = { /* Read only from SIM prompt */ },
                                readOnly = true,
                                enabled = false,
                                label = { Text("मोबाइल नंबर (Verified SIM Phone)") },
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, contentDescription = "Phone", tint = MaterialTheme.colorScheme.primary)
                                },
                                trailingIcon = {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        modifier = Modifier
                                            .padding(end = 4.dp)
                                            .clickable { requestPrimarySimPhoneNumber() }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PhoneAndroid,
                                                contentDescription = "SIM",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = "Select SIM",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    disabledLeadingIconColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (inputEmail.isNotBlank() && inputFullName.isNotBlank() && !isSubmittingRegistration) {
                                isSubmittingRegistration = true
                                registrationErrorMessage = null

                                onGoogleLogin(
                                    inputFullName.trim(),
                                    inputPhone.trim(),
                                    inputEmail.trim()
                                ) { success, errorMsg ->
                                    isSubmittingRegistration = false
                                    if (success) {
                                        isRegisterDialogOpen = false
                                    } else {
                                        registrationErrorMessage = errorMsg ?: "हा मोबाईल नंबर आधीच दुसऱ्या खात्याशी जोडलेला आहे!"
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isSubmittingRegistration
                    ) {
                        if (isSubmittingRegistration) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("नोंदणी पूर्ण करा (Submit)", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { isRegisterDialogOpen = false },
                        enabled = !isSubmittingRegistration
                    ) {
                        Text("रद्द (Cancel)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
