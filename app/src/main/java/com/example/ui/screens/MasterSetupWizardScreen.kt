package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.NovaViewModel
import com.example.ui.components.JarvisAnimatedOrb
import com.example.ui.components.OrbState
import com.example.ui.theme.*

data class WizardStepData(
    val stepIndex: Int,
    val titleEn: String,
    val titleBn: String,
    val subtitleEn: String,
    val subtitleBn: String,
    val whyNeededEn: String,
    val whyNeededBn: String,
    val instructionsEn: String,
    val instructionsBn: String,
    val icon: ImageVector,
    val isOptional: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterSetupWizardScreen(
    viewModel: NovaViewModel,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    val status by viewModel.systemStatus.collectAsState()
    var currentStepIndex by remember { mutableIntStateOf(0) }

    // Auto-refresh when returning from Settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshSystemStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val steps = listOf(
        // 1. Default Assistant
        WizardStepData(
            stepIndex = 0,
            titleEn = "Set JARVIS as Default Digital Assistant",
            titleBn = "JARVIS কে ডিফল্ট ডিজিটাল অ্যাসিস্ট্যান্ট হিসেবে সেট করুন",
            subtitleEn = "Make JARVIS your primary system voice assistant",
            subtitleBn = "JARVIS কে ফোনের প্রধান ভয়েস অ্যাসিস্ট্যান্ট বানান",
            whyNeededEn = "Allows you to summon JARVIS instantly by holding the Power button, long-pressing the Home button, or swiping from screen corners.",
            whyNeededBn = "পাওয়ার বাটন চেপে ধরে অথবা হোম বাটনে লং-প্রেস করে যেকোনো জায়গা থেকে সাথে সাথে JARVIS চালু করার জন্য এটি প্রয়োজন।",
            instructionsEn = "Tap 'Set Default Assistant' -> Choose 'JARVIS' -> Confirm.",
            instructionsBn = "'Set Default Assistant' চাপুন -> 'JARVIS' নির্বাচন করুন -> কনফার্ম করুন।",
            icon = Icons.Default.Assistant,
            isOptional = false
        ),
        // 2. Accessibility Service
        WizardStepData(
            stepIndex = 1,
            titleEn = "Enable Accessibility Service",
            titleBn = "অ্যাক্সেসিবিলিটি সার্ভিস চালু করুন",
            subtitleEn = "Screen reading & hands-free button clicks",
            subtitleBn = "স্ক্রিন পড়া এবং স্পর্শ ছাড়াই বাটন ক্লিক করার সুবিধা",
            whyNeededEn = "Enables JARVIS to read what's on your screen and click buttons, scroll, or navigate apps hands-free when you instruct it.",
            whyNeededBn = "আপনার ভয়েস কমান্ড অনুযায়ী স্ক্রিনের তথ্য পড়তে এবং বিভিন্ন অ্যাপে স্বয়ংক্রিয়ভাবে বাটন চাপতে এই অনুমতি প্রয়োজন।",
            instructionsEn = "Settings -> Installed Apps -> JARVIS -> Toggle ON -> Allow.",
            instructionsBn = "সেটিংস -> Installed Apps -> JARVIS -> চালু (ON) করুন -> Allow চাপুন।",
            icon = Icons.Default.AccessibilityNew,
            isOptional = true
        ),
        // 3. Notification Access
        WizardStepData(
            stepIndex = 2,
            titleEn = "Enable Device Notification Access",
            titleBn = "নোটিফিকেশন অ্যাক্সেস চালু করুন",
            subtitleEn = "Read notification alerts & summarize messages",
            subtitleBn = "নোটিফিকেশন পড়া ও মেসেজ সারসংক্ষেপ",
            whyNeededEn = "Lets JARVIS announce incoming messages, read urgent WhatsApp/SMS alerts aloud, and provide daily notification summaries.",
            whyNeededBn = "ইনকামিং মেসেজ ও গুরুত্বপূর্ণ অ্যালার্ট পড়ে শোনানো এবং সারসংক্ষেপ জানানোর জন্য এটি প্রয়োজন।",
            instructionsEn = "Device & App Notifications -> JARVIS -> Allow Notification Access.",
            instructionsBn = "Device & App Notifications -> JARVIS সিলেক্ট করুন -> Allow দিন।",
            icon = Icons.Default.NotificationsActive,
            isOptional = true
        ),
        // 4. Display Over Other Apps
        WizardStepData(
            stepIndex = 3,
            titleEn = "Display Over Other Apps (Floating Orb)",
            titleBn = "অন্যান্য অ্যাপের উপর দেখানোর অনুমতি (ফ্লোটিং অর্ব)",
            subtitleEn = "Always-accessible floating JARVIS orb",
            subtitleBn = "যেকোনো অ্যাপের উপরে ফ্লোটিং JARVIS অর্ব",
            whyNeededEn = "Enables a compact, floating 3D JARVIS orb that stays on screen across all apps for one-tap voice commands.",
            whyNeededBn = "যেকোনো অ্যাপ ব্যবহার করার সময় একটি সুন্দর ফ্লোটিং বল হিসেবে স্ক্রিনে থাকবে যাতে এক ট্যাপেই কথা বলতে পারেন।",
            instructionsEn = "Display Over Other Apps -> Find JARVIS -> Toggle Allow.",
            instructionsBn = "Display Over Other Apps -> JARVIS খুঁজে বের করুন -> Allow চালু করুন।",
            icon = Icons.Default.Layers,
            isOptional = true
        ),
        // 5. Battery Unrestricted
        WizardStepData(
            stepIndex = 4,
            titleEn = "Configure Unrestricted Battery",
            titleBn = "আনরেস্ট্রিক্টেড ব্যাটারি মোড কনফিগার করুন",
            subtitleEn = "Prevent Android OS from killing background listening",
            subtitleBn = "ব্যাকগ্রাউন্ডে 'Hey Jarvis' বন্ধ হওয়া রোধ করুন",
            whyNeededEn = "Android battery savers kill background listening after a few minutes. Unrestricted mode keeps 'Hey Jarvis' wake word active 24/7.",
            whyNeededBn = "অ্যান্ড্রয়েডের ব্যাটারি সেভার ব্যাকগ্রাউন্ড সার্ভিস বন্ধ করে দেয়। আনরেস্ট্রিক্টেড দিলে 'Hey Jarvis' সবসময় শুনবে।",
            instructionsEn = "App Info -> Battery -> Select 'Unrestricted' (Allow Background Activity).",
            instructionsBn = "অ্যাপ ইনফো -> Battery -> 'Unrestricted' নির্বাচন করুন।",
            icon = Icons.Default.BatteryChargingFull,
            isOptional = false
        ),
        // 6. Usage Access
        WizardStepData(
            stepIndex = 5,
            titleEn = "Enable App Usage Access",
            titleBn = "অ্যাপ ইউজেস অ্যাক্সেস দিন",
            subtitleEn = "Detect active foreground applications",
            subtitleBn = "বর্তমানে কোন অ্যাপ চলছে তা সনাক্তকরণ",
            whyNeededEn = "Helps JARVIS provide smart, contextual answers tailored to the exact application you are currently looking at.",
            whyNeededBn = "আপনি বর্তমানে ঠিক কোন অ্যাপে আছেন তা বুঝে সেই অনুযায়ী সঠিক উত্তর দিতে এই অনুমতি সাহায্য করে।",
            instructionsEn = "Usage Access -> Find JARVIS -> Permit Usage Access.",
            instructionsBn = "Usage Access -> JARVIS সিলেক্ট করুন -> Permit চালু করুন।",
            icon = Icons.Default.Insights,
            isOptional = true
        ),
        // 7. Modify System Settings
        WizardStepData(
            stepIndex = 6,
            titleEn = "Modify System Settings",
            titleBn = "সিস্টেম সেটিংস পরিবর্তনের অনুমতি",
            subtitleEn = "Control brightness, volume, and display",
            subtitleBn = "ব্রাইটনেস, সাউন্ড ও ডিসপ্লে নিয়ন্ত্রণ",
            whyNeededEn = "Allows voice commands like 'Turn up brightness', 'Set volume to max', or 'Change screen timeout'.",
            whyNeededBn = "'ব্রাইটনেস বাড়াও' বা 'সাউন্ড ফুল করো' ধরণের ভয়েস কমান্ড সরাসরি কার্যকর করতে এটি প্রয়োজন।",
            instructionsEn = "Modify System Settings -> Find JARVIS -> Allow Modify System Settings.",
            instructionsBn = "Modify System Settings -> JARVIS সিলেক্ট করুন -> Allow দিন।",
            icon = Icons.Default.Tune,
            isOptional = true
        ),
        // 8. Exact Alarm Access
        WizardStepData(
            stepIndex = 7,
            titleEn = "Exact Alarm & Reminders",
            titleBn = "সঠিক সময়ে অ্যালার্ম ও রিমাইন্ডার",
            subtitleEn = "Schedule high-precision voice alarms",
            subtitleBn = "একদম নির্দিষ্ট সেকেন্ডে অ্যালার্ম বাজার সুবিধা",
            whyNeededEn = "Guarantees your timed timers, medicine alerts, and wake-up alarms fire precisely on schedule without Android delays.",
            whyNeededBn = "আপনার সেট করা অ্যালার্ম ও রিমাইন্ডার যেন একদম সঠিক সেকেন্ডে কোনো মিস ছাড়া বাজে।",
            instructionsEn = "Alarms & Reminders -> Allow JARVIS to set alarms.",
            instructionsBn = "Alarms & Reminders -> JARVIS এর জন্য অনুমতি চালু করুন।",
            icon = Icons.Default.Alarm,
            isOptional = true
        ),
        // 9. OEM Auto-Start
        WizardStepData(
            stepIndex = 8,
            titleEn = "OEM Auto-Start & Lock in Recents",
            titleBn = "ডিভাইস অটো-স্টার্ট ও লক ইন রিসেন্ট",
            subtitleEn = "${status.manufacturer.uppercase()} specific background optimization",
            subtitleBn = "${status.manufacturer.uppercase()} ফোনের ব্যাকগ্রাউন্ড অপটিমাইজেশন",
            whyNeededEn = "Brands like Xiaomi, Oppo, Vivo, Samsung, and Realme require manual Auto-Start toggling and locking in the Recent Apps carousel.",
            whyNeededBn = "শাওমি, অপো, ভিভো, স্যামসাং ফোনে ব্যাকগ্রাউন্ডে সার্ভিস চালু রাখতে অটো-স্টার্ট অন এবং রিসেন্ট অ্যাপসে লক করতে হয়।",
            instructionsEn = "Open Phone Manager / Security -> Enable Auto-Start -> Lock JARVIS in Recent Apps.",
            instructionsBn = "Security / Phone Manager -> Auto-Start চালু করুন -> Recent Apps এ JARVIS লক করুন।",
            icon = Icons.Default.PowerSettingsNew,
            isOptional = true
        ),
        // 10. Microphone & Background Voice
        WizardStepData(
            stepIndex = 9,
            titleEn = "Microphone & Background Voice Service",
            titleBn = "মাইক্রোফোন ও ব্যাকগ্রাউন্ড ভয়েস সার্ভিস",
            subtitleEn = "Test hands-free 'Hey Jarvis' listening",
            subtitleBn = "হ্যান্ডস-ফ্রি 'Hey Jarvis' টেস্টিং",
            whyNeededEn = "The core of JARVIS: enables voice recognition, natural conversations, and hands-free background wake word listening.",
            whyNeededBn = "এটি JARVIS-এর মূল ফিচার: যার মাধ্যমে আপনার কথা শুনে প্রাকৃতিক কণ্ঠে উত্তর দেওয়া সম্ভব হয়।",
            instructionsEn = "Grant RECORD_AUDIO permission and start the Background Voice Service.",
            instructionsBn = "মাইক্রোফোন পারমিশন Allow করুন এবং ব্যাকগ্রাউন্ড সার্ভিস চালু করুন।",
            icon = Icons.Default.Mic,
            isOptional = false
        )
    )

    val currentStep = steps[currentStepIndex]

    // Determine if current step is granted/configured
    val isStepConfigured = when (currentStepIndex) {
        0 -> status.isDefaultAssistant
        1 -> status.isAccessibilityEnabled
        2 -> status.isNotificationAccessEnabled
        3 -> status.isOverlayEnabled
        4 -> status.isBatteryUnrestricted
        5 -> status.isUsageAccessEnabled
        6 -> status.isModifySettingsEnabled
        7 -> status.isExactAlarmEnabled
        8 -> true // Manual manufacturer configuration step
        9 -> status.isMicrophoneGranted
        else -> false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Integration Setup Wizard",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = NovaTextPrimary
                        )
                        Text(
                            text = "Step ${currentStepIndex + 1} of ${steps.size}",
                            fontSize = 12.sp,
                            color = NovaElectricBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NovaTextPrimary
                        )
                    }
                },
                actions = {
                    TextButton(onClick = onFinish) {
                        Text("Exit", color = NovaTextSecondary, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStepIndex > 0) {
                        OutlinedButton(
                            onClick = { currentStepIndex-- },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Previous")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (currentStepIndex < steps.size - 1) {
                        Button(
                            onClick = { currentStepIndex++ },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NovaElectricBlue)
                        ) {
                            Text(if (isStepConfigured) "Next Step" else "Skip / Next")
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Button(
                            onClick = onFinish,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Complete Setup")
                        }
                    }
                }
            }
        },
        containerColor = NovaBackgroundIce
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Step Progress Indicator
            LinearProgressIndicator(
                progress = { (currentStepIndex + 1).toFloat() / steps.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = NovaElectricBlue,
                trackColor = Color(0xFFE2E8F0)
            )

            // Step Header Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(if (isStepConfigured) Color(0xFFDCFCE7) else NovaSoftSky),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isStepConfigured) Icons.Default.CheckCircle else currentStep.icon,
                            contentDescription = null,
                            tint = if (isStepConfigured) Color(0xFF16A34A) else NovaElectricBlue,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = currentStep.titleEn,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NovaTextPrimary
                    )

                    Text(
                        text = currentStep.titleBn,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = NovaElectricBlue
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Status Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isStepConfigured) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                    ) {
                        Text(
                            text = if (isStepConfigured) "STATUS: ACTIVE / CONFIGURED" else "STATUS: SETUP REQUIRED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isStepConfigured) Color(0xFF16A34A) else Color(0xFFD97706),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Why Needed Card (Bilingual)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, tint = NovaElectricBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Why This Access Is Needed / কেন এটি প্রয়োজন", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NovaTextPrimary)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = currentStep.whyNeededEn,
                        fontSize = 13.sp,
                        color = NovaTextPrimary,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currentStep.whyNeededBn,
                        fontSize = 13.sp,
                        color = NovaTextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }

            // How To Configure in Android (Bilingual)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TouchApp, contentDescription = null, tint = NovaElectricBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("How To Configure / কীভাবে চালু করবেন", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NovaTextPrimary)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = currentStep.instructionsEn,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NovaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentStep.instructionsBn,
                        fontSize = 13.sp,
                        color = NovaTextSecondary
                    )
                }
            }

            // Action Button
            Button(
                onClick = {
                    when (currentStepIndex) {
                        0 -> viewModel.systemManager.openDefaultAssistantSettings(activity)
                        1 -> viewModel.systemManager.openAccessibilitySettings()
                        2 -> viewModel.systemManager.openNotificationAccessSettings()
                        3 -> viewModel.systemManager.openOverlaySettings()
                        4 -> viewModel.systemManager.requestBatteryUnrestricted()
                        5 -> viewModel.systemManager.openUsageAccessSettings()
                        6 -> viewModel.systemManager.openModifySystemSettings()
                        7 -> viewModel.systemManager.openExactAlarmSettings()
                        8 -> viewModel.systemManager.openOemAutoStartSettings()
                        9 -> viewModel.startBackgroundService()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isStepConfigured) Color(0xFF16A34A) else NovaElectricBlue
                )
            ) {
                Icon(
                    imageVector = if (isStepConfigured) Icons.Default.Check else Icons.Default.Launch,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when {
                        isStepConfigured -> "Configured — Open Settings to Re-check"
                        currentStepIndex == 0 -> "Set JARVIS as Default Assistant"
                        currentStepIndex == 8 -> "Open ${status.manufacturer.uppercase()} Settings"
                        currentStepIndex == 9 -> "Start Voice Service"
                        else -> "Open Android Settings"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            // Quick Recheck
            OutlinedButton(
                onClick = { viewModel.refreshSystemStatus() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Verify Real Status Now", fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
