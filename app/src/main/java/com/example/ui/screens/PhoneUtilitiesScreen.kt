package com.example.ui.screens

import android.provider.Settings
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.NovaViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneUtilitiesScreen(
    viewModel: NovaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFlashlightOn by remember { mutableStateOf(false) }
    var batteryText by remember { mutableStateOf("চার্জ স্তর চেক করুন") }
    var storageText by remember { mutableStateOf("স্টোরেজ স্পেস চেক করুন") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NovaBackgroundIce,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "ফোন ইউটিলিটি & সেটিংস",
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
            // Flashlight card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isFlashlightOn) NovaWarningAmber else NovaSoftSky),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashlightOn,
                                contentDescription = null,
                                tint = if (isFlashlightOn) Color.White else NovaElectricBlue
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text("টর্চলাইট (Flashlight)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(if (isFlashlightOn) "টর্চ চালু রয়েছে" else "টর্চ বন্ধ রয়েছে", fontSize = 12.sp, color = NovaTextSecondary)
                        }
                    }

                    Switch(
                        checked = isFlashlightOn,
                        onCheckedChange = {
                            isFlashlightOn = it
                            viewModel.actionEngine.toggleFlashlight(it)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = NovaWarningAmber, checkedTrackColor = NovaWarningAmber.copy(alpha = 0.5f))
                    )
                }
            }

            // Quick Device Stats (Battery & Storage)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    onClick = {
                        val res = viewModel.actionEngine.getBatteryStatus()
                        if (res is com.example.actions.ActionResult.Success) {
                            batteryText = res.message
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = NovaElectricBlue)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("ব্যাটারি স্থিতি", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(batteryText, fontSize = 11.sp, color = NovaTextSecondary)
                    }
                }

                Surface(
                    onClick = {
                        val res = viewModel.actionEngine.getStorageStatus()
                        if (res is com.example.actions.ActionResult.Success) {
                            storageText = res.message
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = NovaSecondaryBlue)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("স্টোরেজ মেমরি", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(storageText, fontSize = 11.sp, color = NovaTextSecondary)
                    }
                }
            }

            // Alarms and Timers
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("অ্যালার্ম ও টাইমার", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.actionEngine.setQuickAlarm(7, 0, "সকালের অ্যালার্ম") },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NovaElectricBlue),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Alarm, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("সকাল ৭টায় অ্যালার্ম", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { viewModel.actionEngine.setQuickTimer(300, "৫ মিনিট টাইমার") },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NovaSecondaryBlue),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("৫ মিনিট টাইমার", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Android System Settings Shortcuts (Real official Intents)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("সিস্টেম সেটিংস শর্টকাট", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    SettingsShortcutRow(
                        icon = Icons.Default.Wifi,
                        title = "ওয়াই-ফাই সেটিংস",
                        subtitle = "নেটওয়ার্ক কানেকশন নিয়ন্ত্রণ",
                        onClick = { viewModel.actionEngine.openSystemSettings(Settings.ACTION_WIFI_SETTINGS, "Wi-Fi Settings") }
                    )

                    SettingsShortcutRow(
                        icon = Icons.Default.Bluetooth,
                        title = "ব্লুটুথ সেটিংস",
                        subtitle = "ডিভাইস সংযোগ",
                        onClick = { viewModel.actionEngine.openSystemSettings(Settings.ACTION_BLUETOOTH_SETTINGS, "Bluetooth Settings") }
                    )

                    SettingsShortcutRow(
                        icon = Icons.Default.BrightnessMedium,
                        title = "ডিসপ্লে সেটিংস",
                        subtitle = "উজ্জ্বলতা এবং স্ক্রিন টাইমআউট",
                        onClick = { viewModel.actionEngine.openSystemSettings(Settings.ACTION_DISPLAY_SETTINGS, "Display Settings") }
                    )

                    SettingsShortcutRow(
                        icon = Icons.Default.VolumeUp,
                        title = "শব্দ ও ভলিউম সেটিংস",
                        subtitle = "রিংটোন ও মিডিয়া ভলিউম",
                        onClick = { viewModel.actionEngine.openSystemSettings(Settings.ACTION_SOUND_SETTINGS, "Sound Settings") }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsShortcutRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = NovaSoftSky.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = NovaElectricBlue, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(text = subtitle, fontSize = 11.sp, color = NovaTextSecondary)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = NovaTextMuted)
        }
    }
}
