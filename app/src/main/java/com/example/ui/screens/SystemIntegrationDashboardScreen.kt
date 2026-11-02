package com.example.ui.screens

import android.app.Activity
import android.os.Build
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
import com.example.system.SystemIntegrationStatus
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemIntegrationDashboardScreen(
    viewModel: NovaViewModel,
    onBack: () -> Unit,
    onLaunchWizard: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    val status by viewModel.systemStatus.collectAsState()
    val isOverlayActive by viewModel.isFloatingOverlayActive.collectAsState()

    var showOemDialog by remember { mutableStateOf(false) }

    // Auto-refresh status when returning from Android System Settings
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "System Integration",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NovaTextPrimary
                        )
                        Text(
                            text = "Android System Settings & Default Assistant",
                            fontSize = 11.sp,
                            color = NovaTextSecondary
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
                    IconButton(onClick = { viewModel.refreshSystemStatus() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = NovaElectricBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
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
            // Readiness Score Banner
            ReadinessScoreCard(
                status = status,
                onLaunchWizard = onLaunchWizard,
                onRecheck = { viewModel.refreshSystemStatus() }
            )

            // Quick Control Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.systemManager.openSystemSettings() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NovaElectricBlue)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Android Settings", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = { viewModel.emergencyStopAllServices() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Icon(Icons.Default.StopCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Emergency Stop", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Text(
                text = "CORE SYSTEM INTEGRATIONS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NovaTextSecondary,
                letterSpacing = 1.sp
            )

            // 1. Default Digital Assistant
            IntegrationSettingCard(
                title = "Default Digital Assistant",
                subtitle = "Active Assistant: ${status.defaultAssistantName}",
                explanation = "Set JARVIS as your primary Android voice assistant. Enables long-press power/home assist gestures.",
                isConfigured = status.isDefaultAssistant,
                statusText = if (status.isDefaultAssistant) "JARVIS Selected" else "Not Selected",
                icon = Icons.Default.Assistant,
                actionLabel = "Change Assistant",
                onAction = { viewModel.systemManager.openDefaultAssistantSettings(activity) }
            )

            // 2. Accessibility Service
            IntegrationSettingCard(
                title = "Accessibility Automation",
                subtitle = "Screen reading & hands-free button clicks",
                explanation = "Allows JARVIS to read visible screen texts and execute authorized touch commands hands-free.",
                isConfigured = status.isAccessibilityEnabled,
                statusText = if (status.isAccessibilityEnabled) "Service Enabled" else "Service Disabled",
                icon = Icons.Default.AccessibilityNew,
                actionLabel = "Open Accessibility",
                onAction = { viewModel.systemManager.openAccessibilitySettings() }
            )

            // 3. Notification Access
            IntegrationSettingCard(
                title = "Device Notification Access",
                subtitle = "Read alerts & summarize messages",
                explanation = "Allows JARVIS to listen to incoming app notifications, summarize alerts, and announce urgent updates.",
                isConfigured = status.isNotificationAccessEnabled,
                statusText = if (status.isNotificationAccessEnabled) "Access Granted" else "Access Disabled",
                icon = Icons.Default.NotificationsActive,
                actionLabel = "Configure Access",
                onAction = { viewModel.systemManager.openNotificationAccessSettings() }
            )

            // 4. Battery Optimization / Unrestricted
            IntegrationSettingCard(
                title = "Battery: Unrestricted Background",
                subtitle = "Prevent Android OS from killing JARVIS",
                explanation = "Bypasses strict Doze mode so 'Hey Jarvis' hands-free wake word engine keeps listening reliably.",
                isConfigured = status.isBatteryUnrestricted,
                statusText = if (status.isBatteryUnrestricted) "Unrestricted" else "Optimized (Restricted)",
                icon = Icons.Default.BatteryChargingFull,
                actionLabel = "Set Unrestricted",
                onAction = { viewModel.systemManager.requestBatteryUnrestricted() }
            )

            // 5. Display Over Other Apps (Overlay)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, if (status.isOverlayEnabled) NovaSoftSky else Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (status.isOverlayEnabled) NovaSoftSky else Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = null,
                                tint = if (status.isOverlayEnabled) NovaElectricBlue else NovaTextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Display Over Other Apps", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NovaTextPrimary)
                            Text(
                                if (status.isOverlayEnabled) "Floating Orb Overlay Allowed" else "Overlay Permission Required",
                                fontSize = 12.sp,
                                color = if (status.isOverlayEnabled) Color(0xFF16A34A) else Color(0xFFDC2626)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Allows displaying a persistent, floating 3D JARVIS orb over any open application for instant one-tap voice interaction.",
                        fontSize = 12.sp,
                        color = NovaTextSecondary,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.systemManager.openOverlaySettings() },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Overlay Settings", fontSize = 12.sp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Show Floating Orb", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = NovaTextPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = isOverlayActive,
                                onCheckedChange = { viewModel.toggleFloatingOverlay(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = NovaElectricBlue
                                )
                            )
                        }
                    }
                }
            }

            // 6. Usage Stats Access
            IntegrationSettingCard(
                title = "App Usage Access",
                subtitle = "Detect foreground apps",
                explanation = "Allows JARVIS to know which application is currently in use to provide context-aware voice assistance.",
                isConfigured = status.isUsageAccessEnabled,
                statusText = if (status.isUsageAccessEnabled) "Allowed" else "Not Allowed",
                icon = Icons.Default.Insights,
                actionLabel = "Usage Access",
                onAction = { viewModel.systemManager.openUsageAccessSettings() }
            )

            // 7. Modify System Settings
            IntegrationSettingCard(
                title = "Modify System Settings",
                subtitle = "System volume, brightness & display",
                explanation = "Grants JARVIS permission to adjust system display brightness, screen timeout, and audio parameters via voice commands.",
                isConfigured = status.isModifySettingsEnabled,
                statusText = if (status.isModifySettingsEnabled) "Allowed" else "Restricted",
                icon = Icons.Default.Tune,
                actionLabel = "Configure Write",
                onAction = { viewModel.systemManager.openModifySystemSettings() }
            )

            // 8. Exact Alarm Access
            IntegrationSettingCard(
                title = "Exact Alarm & Reminders",
                subtitle = "Schedule precision alarms",
                explanation = "Ensures your reminders and timers ring at the exact scheduled second without OS battery delays.",
                isConfigured = status.isExactAlarmEnabled,
                statusText = if (status.isExactAlarmEnabled) "Exact Alarms Allowed" else "Inexact / Restricted",
                icon = Icons.Default.Alarm,
                actionLabel = "Exact Alarms",
                onAction = { viewModel.systemManager.openExactAlarmSettings() }
            )

            // 9. OEM Auto-Start & Background Kill Protection
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(NovaSoftSky),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = NovaElectricBlue, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("OEM Auto-Start & Background Kill", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NovaTextPrimary)
                            Text("Device: ${status.manufacturer.uppercase()}", fontSize = 12.sp, color = NovaElectricBlue, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Manufacturers like Xiaomi, Oppo, Vivo, Realme, and Samsung enforce aggressive task killers. Enable Auto-Start and lock JARVIS in Recent Apps.",
                        fontSize = 12.sp,
                        color = NovaTextSecondary,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showOemDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("OEM Guide", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { viewModel.systemManager.openOemAutoStartSettings() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NovaElectricBlue)
                        ) {
                            Text("Open Auto-Start", fontSize = 12.sp)
                        }
                    }
                }
            }

            // 10. Bluetooth & Connected Devices
            IntegrationSettingCard(
                title = "Bluetooth & Audio Routing",
                subtitle = "Wireless headset & mic status",
                explanation = if (status.isBluetoothHeadsetConnected) "Connected to wireless headset! Hands-free SCO audio active." else "Bluetooth: ${if (status.isBluetoothEnabled) "Enabled" else "Off"}. No headset connected.",
                isConfigured = status.isBluetoothEnabled,
                statusText = if (status.isBluetoothHeadsetConnected) "Headset Connected" else if (status.isBluetoothEnabled) "Bluetooth On" else "Bluetooth Off",
                icon = Icons.Default.Bluetooth,
                actionLabel = "Bluetooth Settings",
                onAction = { viewModel.systemManager.openBluetoothSettings() }
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showOemDialog) {
        OemInstructionsDialog(
            manufacturer = status.manufacturer,
            onDismiss = { showOemDialog = false },
            onOpenSettings = {
                showOemDialog = false
                viewModel.systemManager.openOemAutoStartSettings()
            }
        )
    }
}

@Composable
private fun ReadinessScoreCard(
    status: SystemIntegrationStatus,
    onLaunchWizard: () -> Unit,
    onRecheck: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "INTEGRATION READINESS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NovaTextSecondary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${status.totalConfiguredCount} / ${status.totalMonitoredCount} Configured",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NovaTextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(NovaSoftSky),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${status.readinessPercent}%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NovaElectricBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { status.readinessPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = if (status.readinessPercent >= 80) Color(0xFF16A34A) else NovaElectricBlue,
                trackColor = Color(0xFFE2E8F0),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onLaunchWizard,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NovaElectricBlue)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Launch Master Setup Wizard", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun IntegrationSettingCard(
    title: String,
    subtitle: String,
    explanation: String,
    isConfigured: Boolean,
    statusText: String,
    icon: ImageVector,
    actionLabel: String,
    onAction: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (isConfigured) NovaSoftSky else Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isConfigured) NovaSoftSky else Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isConfigured) NovaElectricBlue else NovaTextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NovaTextPrimary)
                    Text(
                        subtitle,
                        fontSize = 12.sp,
                        color = NovaTextSecondary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isConfigured) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isConfigured) Color(0xFF16A34A) else Color(0xFFDC2626),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = explanation,
                fontSize = 12.sp,
                color = NovaTextSecondary,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isConfigured) NovaSoftSky else NovaElectricBlue,
                        contentColor = if (isConfigured) NovaElectricBlue else Color.White
                    )
                ) {
                    Text(actionLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun OemInstructionsDialog(
    manufacturer: String,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val mfg = manufacturer.lowercase()
    val steps = when {
        mfg.contains("xiaomi") || mfg.contains("redmi") || mfg.contains("poco") -> listOf(
            "1. Open Security App -> Manage Apps -> Permissions -> Autostart -> Enable JARVIS.",
            "2. Under Battery Saver -> Set JARVIS to 'No Restrictions'.",
            "3. Open Recent Apps, long-press JARVIS card and tap the Padlock icon to prevent killing."
        )
        mfg.contains("oppo") || mfg.contains("realme") -> listOf(
            "1. Open Phone Manager -> Privacy Permissions -> Startup Manager -> Enable JARVIS.",
            "2. App Info -> Battery -> Allow Foreground & Background Activity.",
            "3. Lock JARVIS in Recent Apps screen."
        )
        mfg.contains("vivo") || mfg.contains("iqoo") -> listOf(
            "1. Open iManager -> App Management -> Autostart Manager -> Allow JARVIS.",
            "2. Battery -> High Background Power Consumption -> Enable for JARVIS."
        )
        mfg.contains("samsung") -> listOf(
            "1. Settings -> Battery and device care -> Battery -> Background usage limits.",
            "2. Add JARVIS to 'Never sleeping apps'.",
            "3. Settings -> Apps -> JARVIS -> Battery -> Unrestricted."
        )
        else -> listOf(
            "1. Settings -> Apps -> JARVIS -> Battery -> Choose 'Unrestricted'.",
            "2. Disable any third-party RAM cleaner or battery saving apps for JARVIS.",
            "3. Pin or Lock JARVIS in your device's Recent Apps carousel."
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Auto-Start Guide: ${manufacturer.uppercase()}",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = NovaTextPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "To guarantee 24/7 hands-free 'Hey Jarvis' listening without the OS terminating the service:",
                    fontSize = 12.sp,
                    color = NovaTextSecondary
                )
                steps.forEach { step ->
                    Text(
                        text = step,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = NovaTextPrimary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onOpenSettings,
                colors = ButtonDefaults.buttonColors(containerColor = NovaElectricBlue)
            ) {
                Text("Open OEM Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
