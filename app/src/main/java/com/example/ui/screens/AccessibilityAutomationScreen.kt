package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.NovaViewModel
import com.example.accessibility.JarvisAccessibilityService
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilityAutomationScreen(
    viewModel: NovaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val status by viewModel.accessibilityStatus.collectAsState()
    val feedback by viewModel.actionFeedback.collectAsState()

    var clickTargetInput by remember { mutableStateOf("") }
    var detectedScreenTexts by remember { mutableStateOf<List<String>>(emptyList()) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NovaBackgroundIce,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "অ্যাক্সেসিবিলিটি অটোমেশন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = NovaTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = NovaTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Service Status Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (status.isConnected) NovaSuccessGreen else NovaBorderLight
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(if (status.isConnected) NovaSuccessGreen else NovaErrorRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (status.isConnected) "সার্ভিস সক্রিয় (Active)" else "সার্ভিস বন্ধ (Disabled)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (status.isConnected) NovaSuccessGreen else NovaErrorRed
                            )
                        }

                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NovaElectricBlue)
                        ) {
                            Text(if (status.isConnected) "সেটিংস দেখুন" else "সক্রিয় করুন", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "সর্বশেষ অ্যাকশন: ${status.lastAction}",
                        fontSize = 12.sp,
                        color = NovaTextSecondary
                    )
                }
            }

            // Emergency Stop Button
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (status.isEmergencyStopped) NovaErrorRed else NovaErrorRed.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, NovaErrorRed)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (status.isEmergencyStopped) "জরুরি বিরতি সক্রিয় (Stopped)" else "জরুরি স্টপ সুইচ (Emergency Stop)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (status.isEmergencyStopped) Color.White else NovaErrorRed
                        )
                        Text(
                            text = "যেকোনো সময়ে সমস্ত অটোমেশন তাত্ক্ষণিকভাবে বন্ধ করতে চাপুন",
                            fontSize = 11.sp,
                            color = if (status.isEmergencyStopped) Color.White.copy(alpha = 0.85f) else NovaTextSecondary
                        )
                    }

                    Button(
                        onClick = {
                            if (status.isEmergencyStopped) {
                                viewModel.resetEmergencyStop()
                            } else {
                                viewModel.triggerEmergencyStop()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (status.isEmergencyStopped) Color.White else NovaErrorRed
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (status.isEmergencyStopped) "রিসেট" else "STOP",
                            color = if (status.isEmergencyStopped) NovaErrorRed else Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Screen Inspection & Reading
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "স্ক্রিন টেক্সট রিডিং ও বিশ্লেষণ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = NovaTextPrimary
                    )

                    Button(
                        onClick = {
                            detectedScreenTexts = JarvisAccessibilityService.readVisibleScreenText()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NovaSecondaryBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("বর্তমান স্ক্রিন রিড করুন")
                    }

                    if (detectedScreenTexts.isNotEmpty()) {
                        Text(
                            text = "শনাক্তকৃত উপাদানসমূহ (${detectedScreenTexts.size}):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NovaTextSecondary
                        )

                        detectedScreenTexts.take(8).forEach { text ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = NovaSoftSky,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = text,
                                    fontSize = 12.sp,
                                    color = NovaTextPrimary,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Click Node By Label
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "বাটন বা লেখায় অটো ক্লিক",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = NovaTextPrimary
                    )

                    OutlinedTextField(
                        value = clickTargetInput,
                        onValueChange = { clickTargetInput = it },
                        label = { Text("বাটনের নাম বা টেক্সট") },
                        placeholder = { Text("যেমন: Submit, Send, Settings") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (clickTargetInput.isNotBlank()) {
                                    viewModel.clickAccessibilityNode(clickTargetInput)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NovaElectricBlue),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.TouchApp, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ক্লিক করুন")
                        }

                        Button(
                            onClick = { viewModel.scrollAccessibility(true) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NovaSecondaryBlue),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("নিচে স্ক্রোল")
                        }
                    }
                }
            }

            // Safety Policy
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = NovaSoftSky
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "নিরাপত্তা নীতিমালা:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = NovaDeepNavy
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "জারভিস অ্যাক্সেসিবিলিটি সার্ভিস শুধুমাত্র আপনার স্পষ্টভাবে অনুমোদিত নির্দেশে দৃশ্যমান স্ক্রিনের সাথে মিথস্ক্রিয়া করে। পাসওয়ার্ড ফিল্ড, পেমেন্ট ট্রানজাকশন বা সংবেদনশীল সিস্টেমে এটি কোনো পদক্ষেপ নেয় না।",
                        fontSize = 12.sp,
                        color = NovaTextPrimary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
