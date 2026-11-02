package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.VolumeUp
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

@Composable
fun VoiceConversationScreen(
    viewModel: NovaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val orbState by viewModel.orbState.collectAsState()
    val statusText by viewModel.currentStatusText.collectAsState()
    val audioRms by viewModel.speechHelper.audioRms.collectAsState()
    val speechWaveform by viewModel.voiceEngine.speakingWaveform.collectAsState()
    val currentVoiceSource by viewModel.voiceEngine.currentVoiceSource.collectAsState()
    val selectedVoiceName by viewModel.selectedVoiceName.collectAsState()
    val transcribedText by viewModel.speechHelper.transcribedText.collectAsState()

    val currentAudioLevel = if (orbState == OrbState.LISTENING) audioRms else speechWaveform

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        NovaBackgroundIce,
                        NovaSoftSky,
                        Color(0xFFE2EDFD)
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onBack,
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Close Voice Session",
                            tint = NovaTextPrimary
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "JARVIS REAL-TIME VOICE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NovaElectricBlue,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = selectedVoiceName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = NovaTextPrimary
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = NovaSoftSky,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Speaker Mode",
                            tint = NovaElectricBlue
                        )
                    }
                }
            }

            // Central Animated Orb
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                com.example.ui.components.JarvisAnimatedOrb(
                    state = orbState,
                    audioLevel = currentAudioLevel,
                    size = 280.dp,
                    onClick = { viewModel.toggleListening() }
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Orb State Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when (orbState) {
                        OrbState.WAKE -> NovaCyanGlow
                        OrbState.LISTENING -> NovaElectricBlue
                        OrbState.SPEAKING -> NovaSecondaryBlue
                        OrbState.THINKING -> NovaDeepNavy
                        OrbState.EXECUTING -> NovaCyanGlow
                        OrbState.SUCCESS -> NovaSuccessGreen
                        OrbState.ERROR -> NovaErrorRed
                        else -> Color.White
                    },
                    shadowElevation = 3.dp
                ) {
                    Text(
                        text = when (orbState) {
                            OrbState.WAKE -> "Wake detected! Ready..."
                            OrbState.LISTENING -> "Listening to your voice..."
                            OrbState.THINKING -> "Thinking with Gemini AI..."
                            OrbState.SPEAKING -> "Speaking ($currentVoiceSource)"
                            OrbState.EXECUTING -> "Executing Android Action..."
                            OrbState.SUCCESS -> "Action Completed"
                            OrbState.ERROR -> "Error encountered"
                            OrbState.INTERRUPTED -> "Interrupted"
                            OrbState.IDLE -> "Say \"Hey Jarvis\" or tap to speak"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (orbState == OrbState.IDLE) NovaTextPrimary else Color.White,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Live Transcription or AI Response Box
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.92f),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 160.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (orbState == OrbState.LISTENING && transcribedText.isNotBlank())
                                "\"$transcribedText\""
                            else statusText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal,
                            color = NovaTextPrimary,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // Bottom Controls (Interrupt, Speak/Stop, Keyboard)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stop Speaking / Interrupt button
                AnimatedVisibility(
                    visible = orbState == OrbState.SPEAKING,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    IconButton(
                        onClick = { viewModel.stopSpeaking() },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(NovaErrorRed.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop Speaking",
                            tint = NovaErrorRed,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Main Push-to-Talk Voice Pill
                Surface(
                    onClick = { viewModel.toggleListening() },
                    shape = CircleShape,
                    color = if (orbState == OrbState.LISTENING) NovaCyanGlow else NovaElectricBlue,
                    shadowElevation = 8.dp,
                    modifier = Modifier.size(76.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (orbState == OrbState.LISTENING) Icons.Default.GraphicEq else Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                // Keyboard / Text fallback button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = "Text Chat",
                        tint = NovaTextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
