package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.NovaViewModel
import com.example.ui.theme.*

data class PermissionItem(
    val permission: String,
    val title: String,
    val description: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsCenterScreen(
    viewModel: NovaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val permissionsList = listOf(
        PermissionItem(
            Manifest.permission.RECORD_AUDIO,
            "মাইক্রোফোন (Microphone)",
            "ভয়েস কমান্ড গ্রহণ ও রিয়েল-টাইম কথাবার্তার জন্য অত্যাবশ্যক।",
            Icons.Default.Mic
        ),
        PermissionItem(
            Manifest.permission.POST_NOTIFICATIONS,
            "নোটিফিকেশন (Notifications)",
            "ব্যাকগ্রাউন্ড ভয়েস লিসেনার এবং অ্যালার্ট বার্তা প্রদর্শনের জন্য।",
            Icons.Default.Notifications
        ),
        PermissionItem(
            Manifest.permission.READ_CONTACTS,
            "কন্টাক্টস (Contacts)",
            "ভয়েস নির্দেশে পরিচিত ব্যক্তিদের খুঁজে কল বা মেসেজ প্রস্তুত করতে।",
            Icons.Default.Contacts
        ),
        PermissionItem(
            Manifest.permission.CALL_PHONE,
            "ফোন কল (Phone Calls)",
            "সরাসরি আপনার অনুমতিক্রমে ডায়ালারে কল সূচনা করতে।",
            Icons.Default.Call
        ),
        PermissionItem(
            Manifest.permission.READ_CALENDAR,
            "ক্যালেন্ডার (Calendar)",
            "আপনার শিডিউল দেখতে এবং নতুন মিটিং ও ইভেন্ট যুক্ত করতে।",
            Icons.Default.CalendarMonth
        ),
        PermissionItem(
            Manifest.permission.ACCESS_FINE_LOCATION,
            "লোকেশন (Location)",
            "লাইভ স্থানীয় আবহাওয়া এবং ভৌগোলিক তথ্য নির্ধারণের জন্য।",
            Icons.Default.LocationOn
        ),
        PermissionItem(
            Manifest.permission.CAMERA,
            "ক্যামেরা (Camera)",
            "টর্চলাইট এবং ভিজ্যুয়াল অ্যাসিস্ট্যান্ট ফিচার চালুর জন্য।",
            Icons.Default.PhotoCamera
        )
    )

    var permissionTrigger by remember { mutableStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        permissionTrigger++
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NovaBackgroundIce,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "পারমিশন সেন্টার",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = NovaSoftSky
            ) {
                Row(modifier = Modifier.padding(14.dp)) {
                    Icon(Icons.Outlined.Shield, contentDescription = null, tint = NovaElectricBlue)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "গোপনীয়তার নিশ্চয়তা: JARVIS শুধুমাত্র অনুমোদিত ফিচারের জন্য পারমিশন ব্যবহার করে। আপনার কোনো অডিও বা ব্যক্তিগত তথ্য বিনা অনুমতিতে সংরক্ষণ বা শেয়ার করা হয় না।",
                        fontSize = 12.sp,
                        color = NovaTextPrimary,
                        lineHeight = 18.sp
                    )
                }
            }

            // Special System Settings Banner
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, NovaElectricBlue)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "অ্যান্ড্রয়েড স্পেশাল সিস্টেম অ্যাক্সেস",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NovaTextPrimary
                        )
                        Text(
                            text = "ডিফল্ট অ্যাসিস্ট্যান্ট, ব্যাটারি আনরেস্ট্রিক্টেড, ওভারলে ও অ্যাক্সেসিবিলিটি",
                            fontSize = 11.sp,
                            color = NovaTextSecondary
                        )
                    }
                    Button(
                        onClick = { viewModel.navigateTo("system_dashboard") },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NovaElectricBlue)
                    ) {
                        Text("ওপেন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            permissionsList.forEach { item ->
                key(item.permission, permissionTrigger) {
                    val isGranted = ContextCompat.checkSelfPermission(
                        context,
                        item.permission
                    ) == PackageManager.PERMISSION_GRANTED

                    PermissionCard(
                        item = item,
                        isGranted = isGranted,
                        onRequest = {
                            if (!isGranted) {
                                permissionLauncher.launch(item.permission)
                            } else {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PermissionCard(
    item: PermissionItem,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, if (isGranted) NovaSuccessGreen.copy(alpha = 0.5f) else NovaBorderLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isGranted) NovaSuccessGreen.copy(alpha = 0.15f) else NovaSoftSky),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = if (isGranted) NovaSuccessGreen else NovaElectricBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = item.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NovaTextPrimary
                        )
                        Text(
                            text = if (isGranted) "অনুমোদিত (Granted)" else "প্রয়োজনীয় (Required)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isGranted) NovaSuccessGreen else NovaErrorRed
                        )
                    }
                }

                Button(
                    onClick = onRequest,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isGranted) NovaSuccessGreen.copy(alpha = 0.15f) else NovaElectricBlue
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = if (isGranted) "ব্যবস্থাপনা" else "অনুমতি দিন",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isGranted) NovaSuccessGreen else Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.description,
                fontSize = 12.sp,
                color = NovaTextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}
