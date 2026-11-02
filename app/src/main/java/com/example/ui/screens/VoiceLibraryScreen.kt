package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.NovaViewModel
import com.example.data.api.VoiceModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceLibraryScreen(
    viewModel: NovaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val voices by viewModel.availableVoices.collectAsState()
    val selectedVoiceName by viewModel.selectedVoiceName.collectAsState()
    val isSpeaking by viewModel.voiceEngine.isSpeaking.collectAsState()
    val voiceSource by viewModel.voiceEngine.currentVoiceSource.collectAsState()
    val lastError by viewModel.lastVoiceError.collectAsState()

    val stability by viewModel.voiceStability.collectAsState()
    val similarity by viewModel.voiceSimilarity.collectAsState()
    val style by viewModel.voiceStyle.collectAsState()
    val speakerBoost by viewModel.voiceSpeakerBoost.collectAsState()
    val selectedVoiceId by viewModel.selectedVoiceId.collectAsState()
    val isLoading by viewModel.isLoadingVoices.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Conversational", "Narration", "Professional")

    val filteredVoices = remember(voices, selectedCategory) {
        if (selectedCategory == "All") voices
        else voices.filter { it.category?.contains(selectedCategory, ignoreCase = true) == true }
    }

    var showTuningSheet by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NovaBackgroundIce,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "ভয়েস লাইব্রেরি (ElevenLabs)",
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
                actions = {
                    IconButton(onClick = { showTuningSheet = !showTuningSheet }) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Tune Parameters",
                            tint = if (showTuningSheet) NovaElectricBlue else NovaTextSecondary
                        )
                    }
                    IconButton(onClick = { viewModel.fetchElevenLabsVoices() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = NovaElectricBlue)
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
        ) {
            // Active Voice Hero Banner
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "সক্রিয় সহকারী ভয়েস (Active Voice)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NovaTextMuted
                            )
                            Text(
                                text = selectedVoiceName,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = NovaTextPrimary
                            )
                            Text(
                                text = "ইঞ্জিন: $voiceSource (Model: eleven_multilingual_v2)",
                                fontSize = 12.sp,
                                color = NovaElectricBlue
                            )
                        }

                        Button(
                            onClick = { viewModel.testAssistantVoice() },
                            enabled = !isSpeaking,
                            colors = ButtonDefaults.buttonColors(containerColor = NovaElectricBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isSpeaking) "বলছি..." else "ভয়েস টেস্ট", fontSize = 12.sp)
                        }
                    }

                    if (lastError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = lastError!!,
                            color = NovaErrorRed,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }

                    if (showTuningSheet) {
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = NovaBorderLight)
                        Spacer(modifier = Modifier.height(10.dp))

                        Text("ভয়েস ফাইন টিউনিং (ElevenLabs Settings)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NovaTextPrimary)

                        // Stability Slider
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Stability (স্থায়িত্ব): ${String.format("%.2f", stability)}", fontSize = 11.sp, color = NovaTextSecondary)
                        }
                        Slider(
                            value = stability,
                            onValueChange = { viewModel.saveCustomVoiceSettings(it, similarity, style, speakerBoost) },
                            valueRange = 0.0f..1.0f
                        )

                        // Similarity Slider
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Similarity (সাদৃশ্য): ${String.format("%.2f", similarity)}", fontSize = 11.sp, color = NovaTextSecondary)
                        }
                        Slider(
                            value = similarity,
                            onValueChange = { viewModel.saveCustomVoiceSettings(stability, it, style, speakerBoost) },
                            valueRange = 0.0f..1.0f
                        )
                    }
                }
            }

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        onClick = { selectedCategory = cat },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) NovaElectricBlue else NovaSoftSky,
                        modifier = Modifier.height(34.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else NovaTextPrimary
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NovaElectricBlue)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredVoices, key = { it.voiceId }) { voice ->
                        val isCurrent = voice.voiceId == selectedVoiceId
                        VoiceCardItem(
                            voice = voice,
                            isSelected = isCurrent,
                            onSelect = { viewModel.selectVoice(voice) },
                            onPreview = { viewModel.previewVoice(voice) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceCardItem(
    voice: VoiceModel,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onPreview: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = if (isSelected) 4.dp else 1.dp,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) NovaElectricBlue else NovaBorderLight
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) NovaElectricBlue else NovaSoftSky),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.RecordVoiceOver,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else NovaElectricBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = voice.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NovaTextPrimary
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NovaElectricBlue
                            ) {
                                Text(
                                    text = "সক্রিয়",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = voice.description ?: voice.category ?: "Natural Voice",
                        fontSize = 12.sp,
                        color = NovaTextSecondary,
                        maxLines = 1
                    )

                    val accent = voice.labels?.get("accent") ?: "Standard"
                    val gender = voice.labels?.get("gender") ?: "Voice"
                    Text(
                        text = "$gender • $accent",
                        fontSize = 11.sp,
                        color = NovaTextMuted
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Play preview
                IconButton(
                    onClick = onPreview,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(NovaSoftSky)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PlayArrow,
                        contentDescription = "Preview",
                        tint = NovaElectricBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Select button
                Button(
                    onClick = onSelect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) NovaSuccessGreen else NovaElectricBlue
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Text(
                        text = if (isSelected) "বাছাইকৃত" else "নির্বাচন",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
