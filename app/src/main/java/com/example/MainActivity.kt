package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.services.JarvisFloatingOverlayService
import com.example.ui.components.JarvisAnimatedOrb
import com.example.ui.components.OrbState
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: NovaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize floating overlay if overlay permission is granted
        if (android.provider.Settings.canDrawOverlays(this)) {
            JarvisFloatingOverlayService.start(this)
        }

        handleAssistIntent(intent)

        setContent {
            MyApplicationTheme {
                JarvisApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Hide floating overlay while in foreground to avoid duplicate orb
        JarvisFloatingOverlayService.setAppInForeground(true)
        viewModel.refreshSystemStatus()
    }

    override fun onPause() {
        super.onPause()
        // Reveal floating overlay immediately when leaving foreground
        if (android.provider.Settings.canDrawOverlays(this)) {
            JarvisFloatingOverlayService.start(this)
            JarvisFloatingOverlayService.setAppInForeground(false)
        }
    }

    override fun onStop() {
        super.onStop()
        // Ensure floating overlay remains visible over other applications/launcher
        if (android.provider.Settings.canDrawOverlays(this)) {
            JarvisFloatingOverlayService.setAppInForeground(false)
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAssistIntent(intent)
    }

    private fun handleAssistIntent(intent: android.content.Intent?) {
        if (intent?.getBooleanExtra("EXTRA_START_VOICE_ASSIST", false) == true) {
            viewModel.navigateTo("voice_conversation")
            viewModel.toggleListening()
        }
    }
}

@Composable
fun JarvisApp(viewModel: NovaViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val currentScreen by viewModel.currentScreen.collectAsState()
    val isOnboardingDone by viewModel.preferences.isOnboardingCompleted.collectAsState(initial = true)

    var showSplash by remember { mutableStateOf(true) }

    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {}

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            micLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
        delay(1200)
        showSplash = false
    }

    BackHandler(enabled = currentScreen != "home" || drawerState.isOpen) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else if (currentScreen != "home") {
            viewModel.navigateTo("home")
        }
    }

    if (showSplash) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NovaBackgroundIce),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                JarvisAnimatedOrb(state = OrbState.IDLE, size = 180.dp)
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "JARVIS",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NovaTextPrimary,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Always Listening. Always Understanding. Always Ready.",
                    fontSize = 12.sp,
                    color = NovaElectricBlue,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        return
    }

    if (!isOnboardingDone && currentScreen == "onboarding") {
        OnboardingScreen(
            viewModel = viewModel,
            onComplete = { viewModel.navigateTo("home") }
        )
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentScreen == "home",
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                drawerContentColor = NovaTextPrimary,
                modifier = Modifier.width(310.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Drawer Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(NovaElectricBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("J", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("JARVIS AI", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = NovaTextPrimary)
                            Text("পার্সোনাল ইন্টেলিজেন্ট সহকারী", fontSize = 11.sp, color = NovaTextSecondary)
                        }
                    }

                    HorizontalDivider(color = NovaBorderLight, modifier = Modifier.padding(vertical = 6.dp))

                    Text("প্রধান মডিউল", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NovaTextMuted)

                    DrawerItem(Icons.Default.Home, "হোম (AI Assistant)", currentScreen == "home") {
                        viewModel.navigateTo("home")
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem(Icons.Default.GraphicEq, "রিয়েল-টাইম ভয়েস কনভারসেশন", currentScreen == "voice_conversation") {
                        viewModel.navigateTo("voice_conversation")
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem(Icons.Default.Chat, "চ্যাট হিস্ট্রি (History)", currentScreen == "chat_history") {
                        viewModel.navigateTo("chat_history")
                        scope.launch { drawerState.close() }
                    }

                    HorizontalDivider(color = NovaBorderLight, modifier = Modifier.padding(vertical = 6.dp))
                    Text("ভয়েস ও অডিও সক্ষমতা", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NovaTextMuted)

                    DrawerItem(Icons.Default.RecordVoiceOver, "ElevenLabs ভয়েস লাইব্রেরি", currentScreen == "voice_library") {
                        viewModel.navigateTo("voice_library")
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem(Icons.Default.AutoFixHigh, "ভয়েস ক্লোনিং স্টুডিও (Cloning)", currentScreen == "voice_cloning") {
                        viewModel.navigateTo("voice_cloning")
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem(Icons.Default.Layers, "ElevenLabs সক্ষমতা ম্যাট্রিক্স", currentScreen == "elevenlabs_matrix") {
                        viewModel.navigateTo("elevenlabs_matrix")
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem(Icons.Default.Tune, "ব্যক্তিত্ব ও ভাষা সেটিংস", currentScreen == "personality") {
                        viewModel.navigateTo("personality")
                        scope.launch { drawerState.close() }
                    }

                    HorizontalDivider(color = NovaBorderLight, modifier = Modifier.padding(vertical = 6.dp))
                    Text("ফোন অ্যাকশন ও অটোমেশন", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NovaTextMuted)

                    DrawerItem(Icons.Default.Phone, "কল অ্যাসিস্ট্যান্ট & এআই কল", currentScreen == "call_assistant") {
                        viewModel.navigateTo("call_assistant")
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem(Icons.Default.AccessibilityNew, "অ্যাক্সেসিবিলিটি অটোমেশন", currentScreen == "accessibility") {
                        viewModel.navigateTo("accessibility")
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem(Icons.Default.Message, "মেসেজিং অ্যাসিস্ট্যান্ট", currentScreen == "messaging") {
                        viewModel.navigateTo("messaging")
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem(Icons.Default.Search, "গুগল এআই ওয়েব রিসার্চ", currentScreen == "web_search") {
                        viewModel.navigateTo("web_search")
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem(Icons.Default.Share, "সোশ্যাল মিডিয়া সহকারী", currentScreen == "social_media") {
                        viewModel.navigateTo("social_media")
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem(Icons.Default.Apps, "অ্যাপ কন্ট্রোল হাব", currentScreen == "app_control") {
                        viewModel.navigateTo("app_control")
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem(Icons.Default.Build, "ফোন ইউটিলিটি & সেটিংস", currentScreen == "phone_utilities") {
                        viewModel.navigateTo("phone_utilities")
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem(Icons.Default.Folder, "ডকুমেন্ট & ফাইল সহকারী", currentScreen == "file_assistant") {
                        viewModel.navigateTo("file_assistant")
                        scope.launch { drawerState.close() }
                    }

                    HorizontalDivider(color = NovaBorderLight, modifier = Modifier.padding(vertical = 6.dp))
                    Text("কনফিগারেশন ও গোপনীয়তা", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NovaTextMuted)

                    DrawerItem(Icons.Default.Psychology, "Google AI Gemini API", currentScreen == "google_ai_config") {
                        viewModel.navigateTo("google_ai_config")
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem(Icons.Default.GraphicEq, "ElevenLabs API সেটিংস", currentScreen == "elevenlabs_config") {
                        viewModel.navigateTo("elevenlabs_config")
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem(Icons.Default.Bookmark, "মেমরি ব্যবস্থাপনা", currentScreen == "memory") {
                        viewModel.navigateTo("memory")
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem(Icons.Default.Security, "পারমিশন সেন্টার", currentScreen == "permissions") {
                        viewModel.navigateTo("permissions")
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem(Icons.Default.Lock, "গোপনীয়তা ও ডাটা মুছুন", currentScreen == "privacy") {
                        viewModel.navigateTo("privacy")
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem(Icons.Default.HelpOutline, "সহায়তা ও নির্দেশিকা", currentScreen == "help") {
                        viewModel.navigateTo("help")
                        scope.launch { drawerState.close() }
                    }

                    HorizontalDivider(color = NovaBorderLight, modifier = Modifier.padding(vertical = 6.dp))
                    Text("অ্যান্ড্রয়েড সিস্টেম ইন্টিগ্রেশন", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NovaTextMuted)

                    DrawerItem(Icons.Default.SettingsSuggest, "সিস্টেম ইন্টিগ্রেশন ড্যাশবোর্ড", currentScreen == "system_dashboard") {
                        viewModel.navigateTo("system_dashboard")
                        scope.launch { drawerState.close() }
                    }

                    DrawerItem(Icons.Default.AutoAwesome, "মাস্টার সেটআপ উইজার্ড (10 Steps)", currentScreen == "system_wizard") {
                        viewModel.navigateTo("system_wizard")
                        scope.launch { drawerState.close() }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    ) {
        when (currentScreen) {
            "home" -> HomeScreen(
                viewModel = viewModel,
                onOpenDrawer = { scope.launch { drawerState.open() } },
                onOpenVoiceModal = { viewModel.navigateTo("voice_conversation") },
                onOpenSettings = { viewModel.navigateTo("personality") },
                onOpenCamera = { viewModel.actionEngine.openCamera() },
                onOpenFileAssistant = { viewModel.navigateTo("file_assistant") }
            )
            "voice_conversation" -> VoiceConversationScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo("home") }
            )
            "chat_history" -> ChatHistoryScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo("home") }
            )
            "voice_library" -> VoiceLibraryScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo("home") }
            )
            "voice_cloning" -> VoiceCloningStudioScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo("home") }
            )
            "elevenlabs_matrix" -> ElevenLabsCapabilitiesScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo("home") }
            )
            "accessibility" -> AccessibilityAutomationScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo("home") }
            )
            "call_assistant" -> CallAssistantScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo("home") }
            )
            "messaging" -> MessagingAssistantScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo("home") }
            )
            "web_search" -> WebSearchScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo("home") }
            )
            "social_media" -> SocialMediaScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo("home") }
            )
            "app_control" -> AppControlScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo("home") }
            )
            "phone_utilities" -> PhoneUtilitiesScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo("home") }
            )
            "file_assistant" -> FileAssistantScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo("home") }
            )
            "memory" -> MemoryManagementScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo("home") }
            )
            "permissions" -> PermissionsCenterScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo("home") }
            )
            "google_ai_config" -> GoogleAiConfigScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo("home") }
            )
            "elevenlabs_config" -> ElevenLabsConfigScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo("home") }
            )
            "personality" -> PersonalityVoiceSettingsScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo("home") }
            )
            "privacy" -> PrivacyDataScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo("home") }
            )
            "help" -> HelpSupportScreen(
                onBack = { viewModel.navigateTo("home") }
            )
            "system_dashboard" -> SystemIntegrationDashboardScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo("home") },
                onLaunchWizard = { viewModel.navigateTo("system_wizard") }
            )
            "system_wizard" -> MasterSetupWizardScreen(
                viewModel = viewModel,
                onBack = { viewModel.navigateTo("system_dashboard") },
                onFinish = { viewModel.navigateTo("system_dashboard") }
            )
            else -> HomeScreen(
                viewModel = viewModel,
                onOpenDrawer = { scope.launch { drawerState.open() } },
                onOpenVoiceModal = { viewModel.navigateTo("voice_conversation") },
                onOpenSettings = { viewModel.navigateTo("personality") },
                onOpenCamera = { viewModel.actionEngine.openCamera() },
                onOpenFileAssistant = { viewModel.navigateTo("file_assistant") }
            )
        }
    }
}

@Composable
fun DrawerItem(
    icon: ImageVector,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) NovaSoftSky else Color.Transparent,
        modifier = Modifier.fillMaxWidth().height(46.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) NovaElectricBlue else NovaTextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) NovaElectricBlue else NovaTextPrimary
            )
        }
    }
}
