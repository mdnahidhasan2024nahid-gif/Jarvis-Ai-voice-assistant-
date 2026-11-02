package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.NovaViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyDataScreen(
    viewModel: NovaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("সকল ডাটা মুছে ফেলতে চান?") },
            text = { Text("এটি আপনার সংরক্ষিত চ্যাট হিস্ট্রি, মেমরি, ভয়েস স্ক্রিপ্ট এবং API কীসমূহ লোকাল স্টোরেজ থেকে চিরতরে ডিলিট করবে।") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        viewModel.clearChatHistory()
                        viewModel.clearAllMemories()
                        viewModel.preferences.clearAllData()
                        showDeleteConfirm = false
                        Toast.makeText(context, "সকল ডাটা মুছে ফেলা হয়েছে", Toast.LENGTH_LONG).show()
                    }
                }) {
                    Text("সব মুছুন", color = NovaErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("বাতিল") }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NovaBackgroundIce,
        topBar = {
            TopAppBar(
                title = { Text("গোপনীয়তা ও ডাটা নিরাপত্তা", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NovaTextPrimary) },
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
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("নিরাপত্তা প্রতিশ্রুতি", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NovaTextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "১. ব্যক্তিগত কী এনক্রিপশন: আপনার Google AI Gemini এবং ElevenLabs API কীসমূহ আপনার ডিভাইসে সুরক্ষিতভাবে সংরক্ষিত থাকে।\n\n২. লোকাল ডাটাবেস: আপনার চ্যাট হিস্ট্রি এবং মেমরি তথ্য ডিভাইসের Room SQLite ডাটাবেসে থাকে। কোনো অননুমোদিত রিমোট সার্ভারে পাঠানো হয় না।\n\n৩. অডিও গোপনীয়তা: মাইক্রোফোন শুধুমাত্র আপনার কমান্ড গ্রহণের সময় সক্রিয় হয়। কোনো ব্যাকগ্রাউন্ড স্পাই রেকর্ড হয় না।",
                        fontSize = 13.sp,
                        color = NovaTextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ডাটা মুছে ফেলা (Data Wipe)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NovaErrorRed)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "এক ক্লিকে আপনার সমস্ত ব্যক্তিগত তথ্য, চ্যাট এবং কী রিসেট করুন।",
                        fontSize = 12.sp,
                        color = NovaTextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NovaErrorRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Outlined.DeleteForever, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("সকল ডাটা মুছুন (Erase All Data)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val commands = listOf(
        "Open YouTube" to "ইউটিউব অ্যাপ ওপেন করবে",
        "Open WhatsApp" to "হোয়াটসঅ্যাপ ওপেন করবে",
        "Turn on flashlight" to "টর্চলাইট চালু করবে",
        "Flashlight off" to "টর্চলাইট বন্ধ করবে",
        "Battery status" to "ব্যাটারি চার্জ শতাংশ জানাবে",
        "Storage info" to "ফ্রি মেমরি স্ট্যাটাস দেখাবে",
        "Set alarm at 7 AM" to "সকাল ৭টায় অ্যালার্ম সেট করবে",
        "Call brother" to "ফোন ডায়ালারে কল প্রস্তুত করবে",
        "Search for AI news" to "ওয়েব অনুসন্ধান করবে"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NovaBackgroundIce,
        topBar = {
            TopAppBar(
                title = { Text("সহায়তা ও ভয়েস নির্দেশিকা", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NovaTextPrimary) },
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = NovaSoftSky
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("কীভাবে ব্যবহার করবেন?", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NovaDeepNavy)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "হোম স্ক্রিনে থাকা নীল এনার্জি অর্ব (Orb) অথবা মাইক বাটনে ট্যাপ করুন। এরপর আপনার স্বাভাবিক ভাষায় কথা বলুন। নোভা আপনার কথা শুনে উত্তর দেবে এবং অনুমোদিত অ্যান্ড্রয়েড অ্যাকশন সম্পন্ন করবে।",
                        fontSize = 12.sp,
                        color = NovaTextPrimary,
                        lineHeight = 18.sp
                    )
                }
            }

            Text("জনপ্রিয় ভয়েস কমান্ডসমূহ:", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NovaTextPrimary)

            commands.forEach { (cmd, desc) ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, NovaBorderLight)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = NovaElectricBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "\"$cmd\"", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NovaElectricBlue)
                            Text(text = desc, fontSize = 12.sp, color = NovaTextSecondary)
                        }
                    }
                }
            }
        }
    }
}
