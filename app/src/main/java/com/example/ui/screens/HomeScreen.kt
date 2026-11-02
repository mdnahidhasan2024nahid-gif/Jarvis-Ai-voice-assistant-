package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.NovaViewModel
import com.example.ui.components.JarvisAnimatedOrb
import com.example.ui.components.OrbState
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: NovaViewModel,
    onOpenDrawer: () -> Unit,
    onOpenVoiceModal: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCamera: () -> Unit,
    onOpenFileAssistant: () -> Unit,
    modifier: Modifier = Modifier
) {
    val orbState by viewModel.orbState.collectAsState()
    val statusText by viewModel.currentStatusText.collectAsState()
    val promptsLeft by viewModel.promptsLeft.collectAsState()
    val audioRms by viewModel.speechHelper.audioRms.collectAsState()
    val speechWaveform by viewModel.voiceEngine.speakingWaveform.collectAsState()
    val isBgListening by viewModel.isBackgroundServiceEnabled.collectAsState()
    val isContinuous by viewModel.isContinuousListening.collectAsState()
    val selectedVoiceName by viewModel.selectedVoiceName.collectAsState()
    val sysStatus by viewModel.systemStatus.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val currentAudioLevel = if (orbState == OrbState.LISTENING) audioRms else speechWaveform

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NovaBackgroundIce,
        topBar = {
            // Top Bar matching reference: "<" Back/Drawer, "Ai Assistant", "≡" Menu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back / Drawer toggle button (<)
                Surface(
                    onClick = onOpenDrawer,
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 2.dp,
                    border = BorderStroke(0.5.dp, NovaBorderLight),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Menu & Modules",
                            tint = NovaTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Center Title: "Ai Assistant" matching reference screenshot
                Text(
                    text = "Ai Assistant",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = NovaTextPrimary,
                    letterSpacing = (-0.2).sp
                )

                // Right Menu Button (≡)
                Surface(
                    onClick = onOpenDrawer,
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 2.dp,
                    border = BorderStroke(0.5.dp, NovaBorderLight),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(3.5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(16.dp)
                                    .height(2.dp)
                                    .clip(CircleShape)
                                    .background(NovaTextPrimary)
                            )
                            Box(
                                modifier = Modifier
                                    .width(12.dp)
                                    .height(2.dp)
                                    .clip(CircleShape)
                                    .background(NovaTextPrimary)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // Top context row in orb area: Overlapping avatars on left, Sliders on right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Overlapping avatars group (3 portraits as in reference)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { viewModel.navigateTo("voice_library") }
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.width(58.dp).height(28.dp)) {
                        // Avatar 1
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(0xFFFF9A9E), Color(0xFFFAD0C4))))
                                .border(1.5.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("A", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        // Avatar 2
                        Box(
                            modifier = Modifier
                                .offset(x = 15.dp)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(0xFFA18CD1), Color(0xFFFBC2EB))))
                                .border(1.5.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("J", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        // Avatar 3
                        Box(
                            modifier = Modifier
                                .offset(x = 30.dp)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(NovaElectricBlue, NovaCyanGlow)))
                                .border(1.5.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✦", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Small status dot
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (sysStatus.isDefaultAssistant || isBgListening) NovaSuccessGreen else NovaElectricBlue)
                    )
                }

                // Right: Sliders / Filter icon (Tune)
                Surface(
                    onClick = onOpenSettings,
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 1.dp,
                    border = BorderStroke(0.5.dp, NovaBorderLight),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = "Assistant Settings",
                            tint = NovaTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Central Hero Area: ANIMATED AI ORB, GREETING CAPSULE, & 3-BUTTON ACTION DOCK
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Generative Wave Moiré Orb with Center Double Sparkle Disc
                    JarvisAnimatedOrb(
                        state = orbState,
                        audioLevel = currentAudioLevel,
                        size = 245.dp,
                        onClick = { viewModel.toggleListening() }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Floating Greeting Capsule: "Hello! How can I assist?"
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        shadowElevation = 3.dp,
                        border = BorderStroke(1.dp, NovaSoftSky),
                        modifier = Modifier
                            .clickable {
                                if (orbState == OrbState.SPEAKING) {
                                    viewModel.stopSpeaking()
                                } else {
                                    viewModel.toggleListening()
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (orbState == OrbState.LISTENING) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(NovaElectricBlue)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = if (orbState == OrbState.IDLE && (statusText == "Ready" || statusText == "Tap to speak" || statusText == "শ্রবণ থামানো হয়েছে")) {
                                    "Hello! How can I assist?"
                                } else {
                                    statusText
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NovaTextPrimary,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }
            }

            // Bottom Section: Prompts left counter, Quick Action Chips, Input Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Prompts Left Banner & Mode Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "✦",
                                color = NovaElectricBlue,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$promptsLeft left",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NovaTextPrimary
                            )
                        }

                        // Continuous Mode Toggle Pill
                        Surface(
                            onClick = { viewModel.toggleContinuousListening() },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isContinuous) NovaElectricBlue.copy(alpha = 0.12f) else NovaSoftSky,
                            border = BorderStroke(0.8.dp, if (isContinuous) NovaElectricBlue else NovaBorderLight)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isContinuous) Icons.Default.AllInclusive else Icons.Default.TouchApp,
                                    contentDescription = "Continuous Mode",
                                    tint = if (isContinuous) NovaElectricBlue else NovaTextSecondary,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isContinuous) "Auto-Listen ON" else "Manual Mic",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isContinuous) NovaElectricBlue else NovaTextSecondary
                                )
                            }
                        }
                    }

                    // Active Voice Clickable Pill
                    Surface(
                        onClick = { viewModel.navigateTo("voice_library") },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(0.8.dp, NovaBorderLight)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = "Voice",
                                tint = NovaElectricBlue,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = selectedVoiceName.substringBefore(" ").take(10),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NovaTextPrimary
                            )
                        }
                    }
                }

                // Quick Prompt Chips Row
                val quickPrompts = listOf(
                    "Week Summary",
                    "Create New Plan",
                    "Analyze Code",
                    "Jarvis, তুমি কেমন আছ?",
                    "Turn on flashlight",
                    "Battery Status",
                    "Call Assistant"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickPrompts.forEachIndexed { index, prompt ->
                        val isFirstActive = index == 0
                        Surface(
                            onClick = {
                                viewModel.processUserInput(prompt)
                            },
                            shape = RoundedCornerShape(18.dp),
                            color = if (isFirstActive) NovaTextPrimary else Color.White,
                            border = BorderStroke(1.dp, if (isFirstActive) NovaTextPrimary else NovaBorderLight),
                            shadowElevation = if (isFirstActive) 2.dp else 1.dp
                        ) {
                            Text(
                                text = prompt,
                                fontSize = 12.sp,
                                fontWeight = if (isFirstActive) FontWeight.Bold else FontWeight.Medium,
                                color = if (isFirstActive) Color.White else NovaTextSecondary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom Search/Chat Input Bar: "Feel free to ask anything"
                Surface(
                    shape = RoundedCornerShape(30.dp),
                    color = Color.White,
                    shadowElevation = 3.dp,
                    border = BorderStroke(1.dp, NovaSoftSky),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onOpenCamera,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PhotoCamera,
                                contentDescription = "Camera Vision",
                                tint = NovaTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onOpenFileAssistant,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AddPhotoAlternate,
                                contentDescription = "File & Image Assistant",
                                tint = NovaTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        TextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = {
                                Text(
                                    text = "Feel free to ask anything",
                                    color = NovaTextMuted,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = {
                                if (textInput.isNotBlank()) {
                                    viewModel.processUserInput(textInput)
                                    textInput = ""
                                    keyboardController?.hide()
                                }
                            }),
                            modifier = Modifier.weight(1f)
                        )

                        // Circular Send Button
                        Surface(
                            onClick = {
                                if (textInput.isNotBlank()) {
                                    viewModel.processUserInput(textInput)
                                    textInput = ""
                                    keyboardController?.hide()
                                }
                            },
                            shape = CircleShape,
                            color = NovaElectricBlue,
                            shadowElevation = 2.dp,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

