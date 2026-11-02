package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.NovaViewModel
import com.example.ui.theme.*

data class VoiceCapability(
    val title: String,
    val endpoint: String,
    val description: String,
    val status: String,
    val icon: ImageVector,
    val isPrimary: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElevenLabsCapabilitiesScreen(
    viewModel: NovaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val capabilities = remember {
        listOf(
            VoiceCapability(
                "Text to Speech (TTS)",
                "POST /v1/text-to-speech/{voice_id}",
                "আল্ট্রা-রিয়েলিস্টিক মানুষের মতো মিষ্টি কণ্ঠস্বরে রূপান্তর।",
                "সক্রিয় (Active)",
                Icons.Default.RecordVoiceOver,
                isPrimary = true
            ),
            VoiceCapability(
                "Voice Library & Catalogue",
                "GET /v1/voices",
                "১০০০+ প্রফেশনাল, অ্যাকসেন্ট ও ন্যাচারাল ভয়েস ব্রাউজিং ও সিলেকশন।",
                "সক্রিয় (Active)",
                Icons.Default.LibraryMusic,
                isPrimary = true
            ),
            VoiceCapability(
                "Instant Voice Cloning",
                "POST /v1/voices/add",
                "১ মিনিটের কণ্ঠস্বরের নমুনা দিয়ে নিজস্ব ডিজিটাল কণ্ঠ প্রস্তুতকরণ।",
                "সক্রিয় (Active)",
                Icons.Default.AutoFixHigh,
                isPrimary = true
            ),
            VoiceCapability(
                "Speech to Text (Transcribe)",
                "POST /v1/speech-to-text",
                "মাল্টিলিঙ্গুয়াল উচ্চমানের ভয়েস ডিক্টেশন ও ট্রানস্ক্রিপশন।",
                "সংযুক্ত (Linked)",
                Icons.Default.GraphicEq
            ),
            VoiceCapability(
                "Voice Design (Generative)",
                "POST /v1/voice-generation/generate-voice",
                "বয়স, জেন্ডার ও অ্যাকসেন্ট নির্ধারণ করে সম্পূর্ণ নতুন ভয়েস ডিজাইন।",
                "রেডি (Ready)",
                Icons.Default.Tune
            ),
            VoiceCapability(
                "Sound Effects (SFX)",
                "POST /v1/sound-generation",
                "প্রাকৃতিক ও সাই-ফাই সাউন্ড ইফেক্ট তৈরি।",
                "রেডি (Ready)",
                Icons.Default.SurroundSound
            ),
            VoiceCapability(
                "Audio Isolation & Denoiser",
                "POST /v1/audio-isolation",
                "ব্যাকগ্রাউন্ড নয়েজ সম্পূর্ণ দূর করে ক্রিস্টাল ক্লিয়ার অডিও।",
                "রেডি (Ready)",
                Icons.Default.MicOff
            ),
            VoiceCapability(
                "AI Dubbing Studio",
                "POST /v1/dubbing",
                "যেকোনো ভিডিও বা অডিও বিভিন্ন ভাষায় স্বয়ংক্রিয় ডাবিং।",
                "রেডি (Ready)",
                Icons.Default.VideoLibrary
            ),
            VoiceCapability(
                "Pronunciation Dictionaries",
                "GET /v1/pronunciation-dictionaries",
                "কাস্টম শব্দ ও নামের সঠিক উচ্চারণ নিশ্চিতকরণ।",
                "সংযুক্ত (Linked)",
                Icons.Default.MenuBook
            )
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NovaBackgroundIce,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "ElevenLabs সক্ষমতা ম্যাট্রিক্স",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = NovaSoftSky
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "অফিসিয়াল ElevenLabs ইঞ্জিন ইন্টিগ্রেশন:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NovaDeepNavy
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "জারভিস এআই ElevenLabs-এর অফিসিয়াল REST API আর্কিটেকচার ব্যবহার করে যাতে বিশ্বের সর্বাধুনিক অডিও সক্ষমতাসমূহ সরাসরি আপনার অ্যান্ড্রয়েড ডিভাইসে ব্যবহৃত হয়।",
                            fontSize = 12.sp,
                            color = NovaTextPrimary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            items(capabilities, key = { it.title }) { cap ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    shadowElevation = 1.dp,
                    border = BorderStroke(1.dp, if (cap.isPrimary) NovaElectricBlue.copy(alpha = 0.5f) else NovaBorderLight)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (cap.isPrimary) NovaElectricBlue else NovaSoftSky),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = cap.icon,
                                contentDescription = null,
                                tint = if (cap.isPrimary) Color.White else NovaElectricBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(cap.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (cap.isPrimary) NovaSuccessGreen.copy(alpha = 0.15f) else NovaSoftSky
                                ) {
                                    Text(
                                        text = cap.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (cap.isPrimary) NovaSuccessGreen else NovaElectricBlue,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(cap.endpoint, fontSize = 11.sp, color = NovaTextMuted)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(cap.description, fontSize = 12.sp, color = NovaTextSecondary)
                        }
                    }
                }
            }
        }
    }
}
