package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.NovaViewModel
import com.example.services.JarvisVoiceService
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleAiConfigScreen(
    viewModel: NovaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val savedKey by viewModel.geminiApiKey.collectAsState()
    val testResult by viewModel.apiTestResult.collectAsState()

    var keyInput by remember(savedKey) {
        mutableStateOf(if (savedKey.isNotBlank()) savedKey else viewModel.getEffectiveGeminiKey())
    }
    var isKeyVisible by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NovaBackgroundIce,
        topBar = {
            TopAppBar(
                title = { Text("Google AI Gemini সেটিংস", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NovaTextPrimary) },
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
                    Text(
                        text = "Gemini API কী কনফিগারেশন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = NovaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Google AI Studio থেকে প্রাপ্ত আপনার নিজস্ব Gemini API Key প্রদান করুন। এটি নোভা এআই-এর কথোপকথন বুদ্ধিমত্তা নিয়ন্ত্রণ করে।",
                        fontSize = 12.sp,
                        color = NovaTextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        label = { Text("Gemini API Key") },
                        placeholder = { Text("AIzaSy...") },
                        singleLine = true,
                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                Icon(
                                    imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Visibility"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    viewModel.preferences.saveGeminiApiKey(keyInput)
                                    Toast.makeText(context, "API কী সংরক্ষিত হয়েছে", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NovaElectricBlue),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("সংরক্ষণ করুন")
                        }

                        OutlinedButton(
                            onClick = {
                                if (keyInput.isNotBlank()) {
                                    viewModel.testGeminiApi(keyInput)
                                } else {
                                    Toast.makeText(context, "অনুগ্রহ করে API Key প্রদান করুন", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("সংযোগ পরীক্ষা")
                        }
                    }

                    if (savedKey.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        TextButton(
                            onClick = {
                                scope.launch {
                                    viewModel.preferences.saveGeminiApiKey("")
                                    keyInput = ""
                                    Toast.makeText(context, "API কী মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("কী মুছে ফেলুন (Remove Key)", color = NovaErrorRed, fontSize = 12.sp)
                        }
                    }
                }
            }

            if (testResult != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, NovaElectricBlue)
                ) {
                    Text(
                        text = testResult!!,
                        fontSize = 13.sp,
                        color = NovaElectricBlue,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElevenLabsConfigScreen(
    viewModel: NovaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val savedKey by viewModel.elevenLabsApiKey.collectAsState()
    val testResult by viewModel.apiTestResult.collectAsState()
    val selectedVoiceName by viewModel.selectedVoiceName.collectAsState()

    var keyInput by remember(savedKey) {
        mutableStateOf(if (savedKey.isNotBlank()) savedKey else viewModel.getEffectiveElevenLabsKey())
    }
    var isKeyVisible by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NovaBackgroundIce,
        topBar = {
            TopAppBar(
                title = { Text("ElevenLabs ভয়েস সেটিংস", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NovaTextPrimary) },
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
                    Text(
                        text = "ElevenLabs API কী কনফিগারেশন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = NovaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "ElevenLabs অফিসিয়াল প্ল্যাটফর্মের API কী যুক্ত করে অতি-বাস্তবসম্মত মানুষের মতো ভয়েস সিন্থেসিস এবং ভয়েস ক্লোনিং সক্রিয় করুন।",
                        fontSize = 12.sp,
                        color = NovaTextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        label = { Text("ElevenLabs xi-api-key") },
                        placeholder = { Text("sk_...") },
                        singleLine = true,
                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                Icon(
                                    imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle Visibility"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    viewModel.preferences.saveElevenLabsApiKey(keyInput)
                                    viewModel.fetchElevenLabsVoices()
                                    Toast.makeText(context, "ElevenLabs কী সংরক্ষিত হয়েছে", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NovaElectricBlue),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("সংরক্ষণ করুন")
                        }

                        OutlinedButton(
                            onClick = {
                                if (keyInput.isNotBlank()) {
                                    viewModel.fetchElevenLabsVoices()
                                } else {
                                    Toast.makeText(context, "অনুগ্রহ করে API Key দিন", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("ভয়েস লোড করুন")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.testAssistantVoice() },
                        colors = ButtonDefaults.buttonColors(containerColor = NovaCyanGlow),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = NovaDeepNavy, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("বর্তমান ভয়েস পরীক্ষা ($selectedVoiceName)", color = NovaDeepNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    if (savedKey.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        TextButton(
                            onClick = {
                                scope.launch {
                                    viewModel.preferences.saveElevenLabsApiKey("")
                                    keyInput = ""
                                    Toast.makeText(context, "API কী মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("কী মুছে ফেলুন", color = NovaErrorRed, fontSize = 12.sp)
                        }
                    }
                }
            }

            if (testResult != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, NovaElectricBlue)
                ) {
                    Text(
                        text = testResult!!,
                        fontSize = 13.sp,
                        color = NovaElectricBlue,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalityVoiceSettingsScreen(
    viewModel: NovaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentLang by viewModel.currentLanguage.collectAsState()
    val personality by viewModel.personality.collectAsState()
    val speed by viewModel.speechSpeed.collectAsState()
    val isBgService by viewModel.isBackgroundServiceEnabled.collectAsState()
    val isContinuous by viewModel.isContinuousListening.collectAsState()
    val isFloatingOrb by viewModel.isFloatingOrbEnabled.collectAsState()

    var sliderSpeed by remember(speed) { mutableFloatStateOf(speed) }

    val languages = listOf(
        "bn" to "বাংলা (Bengali)",
        "en" to "English (US)",
        "hi" to "हिन्दी (Hindi)",
        "ar" to "العربية (Arabic)"
    )

    val personalities = listOf(
        "friendly" to "বন্ধুত্বপূর্ণ ও আন্তরিক (Friendly)",
        "intelligent" to "বুদ্ধিমান ও প্রফেশনাল (Intelligent)",
        "companion" to "ব্যক্তিগত সঙ্গী (Personal Companion)"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NovaBackgroundIce,
        topBar = {
            TopAppBar(
                title = { Text("ব্যক্তিত্ব ও ভাষা সেটিংস", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NovaTextPrimary) },
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
            // Language Selection
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("প্রধান কথোপকথন ভাষা:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    languages.forEach { (code, label) ->
                        val isSelected = currentLang == code
                        Surface(
                            onClick = { scope.launch { viewModel.preferences.saveLanguage(code) } },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) NovaSoftSky else Color.Transparent,
                            border = BorderStroke(1.dp, if (isSelected) NovaElectricBlue else NovaBorderLight),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp)
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = NovaElectricBlue)
                                }
                            }
                        }
                    }
                }
            }

            // Personality Selection
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("অ্যাসিস্ট্যান্ট ব্যক্তিত্ব (Tone & Persona):", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    personalities.forEach { (key, label) ->
                        val isSelected = personality == key
                        Surface(
                            onClick = { scope.launch { viewModel.preferences.savePersonality(key) } },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) NovaSoftSky else Color.Transparent,
                            border = BorderStroke(1.dp, if (isSelected) NovaElectricBlue else NovaBorderLight),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp)
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = NovaElectricBlue)
                                }
                            }
                        }
                    }
                }
            }

            // Speech Speed Slider
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("কথা বলার গতি:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(String.format("%.1fx", sliderSpeed), fontWeight = FontWeight.Bold, color = NovaElectricBlue)
                    }

                    Slider(
                        value = sliderSpeed,
                        onValueChange = { sliderSpeed = it },
                        onValueChangeFinished = {
                            scope.launch { viewModel.preferences.saveSpeechSpeed(sliderSpeed) }
                        },
                        valueRange = 0.8f..1.5f,
                        steps = 6,
                        colors = SliderDefaults.colors(thumbColor = NovaElectricBlue, activeTrackColor = NovaElectricBlue)
                    )
                }
            }

            // Background Service Toggle
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ব্যাকগ্রাউন্ড ভয়েস সার্ভিস", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("স্ক্রিন বন্ধ থাকলেও নোটিফিকেশন থেকে কথা শুনবে", fontSize = 12.sp, color = NovaTextSecondary)
                    }

                    Switch(
                        checked = isBgService,
                        onCheckedChange = { enable ->
                            scope.launch {
                                viewModel.preferences.setBackgroundServiceEnabled(enable)
                                if (enable) {
                                    JarvisVoiceService.startService(context)
                                } else {
                                    JarvisVoiceService.stopService(context)
                                }
                            }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = NovaElectricBlue)
                    )
                }
            }

            // Continuous Conversation Toggle
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("নিরবচ্ছিন্ন কথোপকথন (Continuous Mode)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("সহকারী কথা শেষ করলে স্বয়ংক্রিয়ভাবে আবার কথা শুনবে", fontSize = 12.sp, color = NovaTextSecondary)
                    }

                    Switch(
                        checked = isContinuous,
                        onCheckedChange = { enable ->
                            scope.launch {
                                viewModel.preferences.setContinuousListening(enable)
                                viewModel.speechHelper.isContinuousListening = enable
                            }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = NovaElectricBlue)
                    )
                }
            }

            // Floating Animated Orb Overlay Toggle
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ভাসমান ওর্ব ওভারলে (Floating Animated Orb)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("অ্যাপ মিনিমাইজ হলেও স্ক্রিনে ভাসমান অ্যানিমেটেড ওর্ব দৃশ্যমান থাকবে", fontSize = 12.sp, color = NovaTextSecondary)
                    }

                    Switch(
                        checked = isFloatingOrb,
                        onCheckedChange = { enable ->
                            scope.launch {
                                viewModel.preferences.setFloatingOrbEnabled(enable)
                                viewModel.toggleFloatingOverlay(enable)
                            }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = NovaElectricBlue)
                    )
                }
            }
        }
    }
}
