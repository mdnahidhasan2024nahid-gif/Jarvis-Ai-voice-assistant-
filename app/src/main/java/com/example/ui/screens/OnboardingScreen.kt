package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.NovaViewModel
import com.example.ui.components.JarvisAnimatedOrb
import com.example.ui.components.OrbState
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    viewModel: NovaViewModel,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var currentStep by remember { mutableIntStateOf(1) }

    val totalSteps = 8

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        NovaBackgroundIce,
                        NovaSoftSky,
                        Color.White
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "JARVIS",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = NovaElectricBlue
                )

                Text(
                    text = "$currentStep / $totalSteps",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NovaTextSecondary
                )
            }

            LinearProgressIndicator(
                progress = { currentStep.toFloat() / totalSteps },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = NovaElectricBlue,
                trackColor = NovaBorderLight
            )

            // Step Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (currentStep) {
                    1 -> OnboardingStep1Logo()
                    2 -> OnboardingStep2Welcome()
                    3 -> OnboardingStep3GoogleAi(viewModel)
                    4 -> OnboardingStep4ElevenLabs(viewModel)
                    5 -> OnboardingStep5Permissions(viewModel)
                    6 -> OnboardingStep6Voice(viewModel)
                    7 -> OnboardingStep7AssistantSetup(viewModel)
                    8 -> OnboardingStep8Completion()
                }
            }

            // Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 1) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("পূর্ববর্তী")
                    }
                } else {
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Button(
                    onClick = {
                        if (currentStep < totalSteps) {
                            currentStep++
                        } else {
                            scope.launch {
                                viewModel.preferences.setOnboardingCompleted(true)
                                onComplete()
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NovaElectricBlue),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = if (currentStep == totalSteps) "যাত্রা শুরু করুন" else "পরবর্তী",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun OnboardingStep1Logo() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        com.example.ui.components.JarvisAnimatedOrb(state = OrbState.IDLE, size = 200.dp)
        Spacer(modifier = Modifier.height(24.dp))
        Text("JARVIS", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = NovaTextPrimary, letterSpacing = 2.sp)
        Text("Always Listening. Always Understanding. Always Ready.", fontSize = 12.sp, color = NovaElectricBlue, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun OnboardingStep2Welcome() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(NovaSoftSky),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = NovaElectricBlue, modifier = Modifier.size(36.dp))
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text("স্বাগতম JARVIS-এ", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = NovaTextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "আপনার ব্যক্তিগত বুদ্ধিমান সহকারী। এটি সার্বক্ষণিক জেগে থাকে (\"Hey Jarvis\"), প্রাকৃতিক ভাষায় কথা বলে এবং অ্যান্ড্রয়েড সিস্টেমের যেকোনো অনুমোদিত কাজ সম্পন্ন করে।",
            fontSize = 14.sp,
            color = NovaTextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
fun OnboardingStep3GoogleAi(viewModel: NovaViewModel) {
    val key by viewModel.geminiApiKey.collectAsState()
    var input by remember(key) { mutableStateOf(key) }
    val scope = rememberCoroutineScope()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(Icons.Outlined.Psychology, contentDescription = null, tint = NovaElectricBlue, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(14.dp))
        Text("Google AI Gemini কনফিগারেশন", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("বুদ্ধিমত্তা ও উত্তরের জন্য আপনার Gemini API কী প্রদান করতে পারেন (ঐচ্ছিক):", fontSize = 12.sp, color = NovaTextSecondary, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedTextField(
            value = input,
            onValueChange = {
                input = it
                scope.launch { viewModel.preferences.saveGeminiApiKey(it) }
            },
            label = { Text("Gemini API Key") },
            placeholder = { Text("AIzaSy... (সেটিংস থেকেও দেওয়া যাবে)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun OnboardingStep4ElevenLabs(viewModel: NovaViewModel) {
    val key by viewModel.elevenLabsApiKey.collectAsState()
    var input by remember(key) { mutableStateOf(key) }
    val scope = rememberCoroutineScope()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(Icons.Outlined.RecordVoiceOver, contentDescription = null, tint = NovaElectricBlue, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(14.dp))
        Text("ElevenLabs ভয়েস ইঞ্জিন", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("আল্ট্রা-রিয়েলিস্টিক মানুষের মতো মিষ্টি কণ্ঠস্বরের জন্য আপনার ElevenLabs API কী দিন:", fontSize = 12.sp, color = NovaTextSecondary, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedTextField(
            value = input,
            onValueChange = {
                input = it
                scope.launch { viewModel.preferences.saveElevenLabsApiKey(it) }
            },
            label = { Text("ElevenLabs Key (ঐচ্ছিক)") },
            placeholder = { Text("sk_... না থাকলে সিস্টেম TTS ব্যবহার হবে") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun OnboardingStep5Permissions(viewModel: NovaViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
        Icon(Icons.Outlined.Security, contentDescription = null, tint = NovaSuccessGreen, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(14.dp))
        Text("নিরাপত্তা ও প্রয়োজনীয় পারমিশন", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "মাইক্রোফোন দিয়ে কথা শোনা, কল করা ও ক্যামেরা টর্চ ব্যবহারের জন্য পারমিশন প্রয়োজন। অ্যাপের ভেতরে প্রয়োজন অনুযায়ী অনুমতি চাওয়া হবে।",
            fontSize = 13.sp,
            color = NovaTextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun OnboardingStep6Voice(viewModel: NovaViewModel) {
    val selectedVoiceName by viewModel.selectedVoiceName.collectAsState()

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
        Icon(Icons.Outlined.Hearing, contentDescription = null, tint = NovaElectricBlue, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(14.dp))
        Text("ডিফল্ট ভয়েস নির্বাচন", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Text("বর্তমান কণ্ঠস্বর: $selectedVoiceName", fontSize = 14.sp, color = NovaElectricBlue, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(14.dp))
        Text("অ্যাপের লাইব্রেরি থেকে পরবর্তীতে যেকোনো সময় কণ্ঠস্বর পরিবর্তন করতে পারবেন।", fontSize = 12.sp, color = NovaTextSecondary, textAlign = TextAlign.Center)
    }
}

@Composable
fun OnboardingStep7AssistantSetup(viewModel: NovaViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
        Icon(Icons.Outlined.SettingsVoice, contentDescription = null, tint = NovaElectricBlue, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(14.dp))
        Text("ব্যক্তিগত সহকারী প্রস্তুত", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("বাংলা, ইংরেজি, হিন্দি এবং আরবি ভাষার স্বয়ংক্রিয় সমর্থন সক্রিয় করা হয়েছে।", fontSize = 13.sp, color = NovaTextSecondary, textAlign = TextAlign.Center)
    }
}

@Composable
fun OnboardingStep8Completion() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(NovaSuccessGreen.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NovaSuccessGreen, modifier = Modifier.size(48.dp))
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text("সবকিছু প্রস্তুত!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = NovaTextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "নোভা এআই এখন আপনার ব্যক্তিগত সহকারী হিসেবে সক্রিয়। চলুন কথা শুরু করি!",
            fontSize = 14.sp,
            color = NovaTextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}
