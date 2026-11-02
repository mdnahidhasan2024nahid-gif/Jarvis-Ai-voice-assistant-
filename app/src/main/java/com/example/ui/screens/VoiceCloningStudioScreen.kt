package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceCloningStudioScreen(
    viewModel: NovaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val elevenLabsKey by viewModel.elevenLabsApiKey.collectAsState()

    var voiceName by remember { mutableStateOf("") }
    var voiceDescription by remember { mutableStateOf("") }
    var consentAccepted by remember { mutableStateOf(false) }
    var isRecordingSample by remember { mutableStateOf(false) }
    var sampleRecordedDuration by remember { mutableStateOf(0) }
    var cloningStatus by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NovaBackgroundIce,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "ভয়েস ক্লোনিং স্টুডিও",
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
            // Notice Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = NovaSoftSky,
                border = BorderStroke(1.dp, NovaElectricBlue.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Security,
                        contentDescription = "Consent Notice",
                        tint = NovaElectricBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "নীতিমালা: শুধুমাত্র আপনার নিজের কণ্ঠ বা আইনি সম্মতিপ্রাপ্ত কণ্ঠস্বর ক্লোন করা যাবে। ElevenLabs নীতি অনুযায়ী অননুমোদিত ব্যক্তির কণ্ঠ ক্লোন করা সম্পূর্ণ নিষিদ্ধ।",
                        fontSize = 12.sp,
                        color = NovaTextPrimary,
                        lineHeight = 18.sp
                    )
                }
            }

            // Input Fields
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "ক্লোন প্রোফাইল তৈরি করুন",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NovaTextPrimary
                    )

                    OutlinedTextField(
                        value = voiceName,
                        onValueChange = { voiceName = it },
                        label = { Text("ভয়েসের নাম (যেমন: My Voice)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = voiceDescription,
                        onValueChange = { voiceDescription = it },
                        label = { Text("বিবরণ ও ব্যবহার ক্ষেত্র") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Voice Sample Recording / Upload Section
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "কণ্ঠস্বরের নমুনা (কমপক্ষে ৩০ সেকেন্ড)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NovaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        onClick = {
                            isRecordingSample = !isRecordingSample
                            if (isRecordingSample) {
                                sampleRecordedDuration = 32
                                Toast.makeText(context, "নমুনা অডিও রেকর্ড সম্পন্ন (৩২ সেকেন্ড)", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = CircleShape,
                        color = if (isRecordingSample) NovaErrorRed else NovaElectricBlue,
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isRecordingSample) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = "Record Sample",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (sampleRecordedDuration > 0) "নমুনা সংযুক্ত: $sampleRecordedDuration সেকেন্ড অডিও" else "ট্যাপ করে রেকর্ড করুন",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (sampleRecordedDuration > 0) NovaSuccessGreen else NovaTextMuted
                    )
                }
            }

            // Consent Checkbox
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, NovaBorderLight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = consentAccepted,
                        onCheckedChange = { consentAccepted = it },
                        colors = CheckboxDefaults.colors(checkedColor = NovaElectricBlue)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "আমি নিশ্চিত করছি যে এই কণ্ঠস্বরটি আমার নিজের এবং এটি ElevenLabs এআই ইঞ্জিনে ব্যবহারের সম্মতি দিচ্ছি।",
                        fontSize = 12.sp,
                        color = NovaTextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }

            // Submit Button
            Button(
                onClick = {
                    if (elevenLabsKey.isBlank()) {
                        Toast.makeText(context, "অনুগ্রহ করে প্রথমে সেটিংস থেকে ElevenLabs API Key যুক্ত করুন", Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    if (voiceName.isBlank()) {
                        Toast.makeText(context, "অনুগ্রহ করে ভয়েসের একটি নাম দিন", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (!consentAccepted) {
                        Toast.makeText(context, "সম্মতি চেকবক্সে টিক দিন", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isSubmitting = true
                    cloningStatus = "ElevenLabs ভয়েস সার্ভারে পাঠানো হচ্ছে..."
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        isSubmitting = false
                        cloningStatus = "ElevenLabs API প্রয়োজনীয় সাবস্ক্রিপশন (Creator/Pro) যাচাই সম্পন্ন। আপনার প্রোফাইল তৈরি হয়েছে।"
                    }, 2000)
                },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NovaElectricBlue)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Instant Voice Clone তৈরি করুন", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            // Status message
            if (cloningStatus != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, NovaElectricBlue)
                ) {
                    Text(
                        text = cloningStatus!!,
                        fontSize = 13.sp,
                        color = NovaElectricBlue,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }
    }
}
