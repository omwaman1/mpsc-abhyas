package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.ApiSubscriptionPlanItem
import com.example.data.remote.RetrofitClient
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.TestbookEmerald
import com.example.ui.theme.TestbookGold
import com.example.ui.theme.TestbookNavy
import com.example.ui.viewmodel.UserProfile
import com.razorpay.Checkout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    userProfile: UserProfile,
    isTrialActive: Boolean,
    trialHoursRemaining: Float,
    isSubscribed: Boolean,
    subscribedPlanName: String?,
    dbPlans: List<ApiSubscriptionPlanItem> = emptyList(),
    onBack: () -> Unit,
    onPaymentSuccess: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedPlanId by remember { mutableStateOf("plan_150") }
    var isProcessing by remember { mutableStateOf(false) }

    // Fallback plans if database plans not yet loaded
    val fallbackPlans = listOf(
        ApiSubscriptionPlanItem(
            planId = "plan_99",
            planName = "1 Month MPSC Pro Pass",
            price = "99.00",
            amountPaise = 9900,
            originalPrice = "₹199",
            durationDays = 30,
            durationText = "1 Month Access (30 Days)",
            discountTag = "50% OFF",
            isPopular = 0
        ),
        ApiSubscriptionPlanItem(
            planId = "plan_150",
            planName = "2 Months MPSC Pro Pass",
            price = "150.00",
            amountPaise = 15000,
            originalPrice = "₹399",
            durationDays = 60,
            durationText = "2 Months Access (60 Days)",
            discountTag = "MOST POPULAR - SAVE 60%",
            isPopular = 1
        ),
        ApiSubscriptionPlanItem(
            planId = "plan_199",
            planName = "3 Months MPSC Pro Pass",
            price = "199.00",
            amountPaise = 19900,
            originalPrice = "₹599",
            durationDays = 90,
            durationText = "3 Months Access (90 Days)",
            discountTag = "BEST VALUE - SAVE 66%",
            isPopular = 0
        )
    )

    val activePlans = if (dbPlans.isNotEmpty()) dbPlans else fallbackPlans

    fun startRazorpayCheckout(planId: String) {
        val activity = context as? Activity ?: return
        isProcessing = true

        scope.launch(Dispatchers.IO) {
            try {
                val resp = RetrofitClient.apiService.createRazorpayOrder(
                    email = userProfile.email.ifBlank { "user@mpsc.com" },
                    planId = planId
                )

                withContext(Dispatchers.Main) {
                    isProcessing = false
                    if (resp.status == "success" && resp.orderId.isNotBlank()) {
                        val checkout = Checkout()
                        checkout.setKeyID(resp.keyId.ifBlank { "rzp_live_RrqH1rKPqejvOQ" })

                        val options = JSONObject()
                        options.put("name", "MPSC ABHYAS PRO PASS")
                        options.put("description", resp.planName)
                        options.put("order_id", resp.orderId)
                        options.put("currency", "INR")
                        options.put("amount", resp.amount)

                        val prefill = JSONObject()
                        prefill.put("email", userProfile.email)
                        prefill.put("contact", userProfile.phone)
                        options.put("prefill", prefill)

                        val theme = JSONObject()
                        theme.put("color", "#0F2C59")
                        options.put("theme", theme)

                        checkout.open(activity, options)
                    } else {
                        Toast.makeText(context, resp.message.ifBlank { "Order creation failed" }, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    isProcessing = false
                    Toast.makeText(context, "Error connecting to server", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            val isAppDark = MaterialTheme.colorScheme.background == BackgroundDark
            TopAppBar(
                title = {
                    Text(
                        text = "MPSC PRO Subscriptions",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = if (isAppDark) MaterialTheme.colorScheme.surface else TestbookNavy),
                modifier = Modifier.statusBarsPadding()
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Trial Status Banner
            item {
                when {
                    isSubscribed -> {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFD1FAE5),
                            border = BorderStroke(1.dp, TestbookEmerald),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TestbookEmerald, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("🎉 Active Subscription Plan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF065F46))
                                    Text(subscribedPlanName ?: "MPSC Pro Pass Active", fontSize = 12.sp, color = Color(0xFF047857))
                                }
                            }
                        }
                    }
                    isTrialActive -> {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEF3C7),
                            border = BorderStroke(1.dp, TestbookGold),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = TestbookGold, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("⏳ 2-Day Free Trial Active!", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFB45309))
                                    Text("$trialHoursRemaining Hours Remaining for full free access", fontSize = 12.sp, color = Color(0xFF78350F))
                                }
                            }
                        }
                    }
                    else -> {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEE2E2),
                            border = BorderStroke(1.dp, Color(0xFFEF4444)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("⚠️ Free 2-Day Trial Expired", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF991B1B))
                                    Text("Please select a pass below to continue full access to 50,000+ PYQs & Mock Tests", fontSize = 12.sp, color = Color(0xFFB91C1C))
                                }
                            }
                        }
                    }
                }
            }

            // Subscription Plan Selection Header
            item {
                Text(
                    text = "Select MPSC Pass Plan (प्लॅन निवडा):",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Dynamic Subscription Plan Cards (No Hardcoding, Clean Borders)
            items(activePlans) { plan ->
                val isSelected = (selectedPlanId == plan.planId)
                val cardBorderColor = if (isSelected) Color(0xFF2563EB) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                val cardBgColor = if (isSelected) Color(0xFF2563EB).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { selectedPlanId = plan.planId },
                    colors = CardDefaults.cardColors(containerColor = cardBgColor),
                    border = BorderStroke(width = if (isSelected) 2.dp else 1.dp, color = cardBorderColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                if (!plan.discountTag.isNull_orEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (plan.isPopular == 1) TestbookGold else Color(0xFF0284C7)
                                    ) {
                                        Text(
                                            text = plan.discountTag!!,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                                Text(
                                    text = plan.durationText.ifBlank { plan.planName },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "₹${plan.price.split(".")[0]}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) Color(0xFF2563EB) else MaterialTheme.colorScheme.onSurface
                                )
                                if (plan.originalPrice.isNotBlank()) {
                                    Text(
                                        text = plan.originalPrice,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Subscribe Now CTA Button
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { startRazorpayCheckout(selectedPlanId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB), contentColor = Color.White),
                    enabled = !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TestbookGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Subscribe Now",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Plan Benefits Checklist
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "⚡ What's Included in MPSC PRO:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        BenefitRow("Unlimited 50,000+ MPSC PYQs (2008 - 2026)")
                        BenefitRow("Full-Length & Speed Mock Test Series")
                        BenefitRow("Detailed Explanations in Marathi & English")
                        BenefitRow("Topic-wise & Exam-wise Filter Banking")
                        BenefitRow("Saved Questions Vault & Analytics")
                    }
                }
            }
        }
    }
}

private fun String?.isNull_orEmpty(): Boolean = this == null || this.trim().isEmpty()

@Composable
fun BenefitRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(Color(0xFFD1FAE5)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(14.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
        )
    }
}
