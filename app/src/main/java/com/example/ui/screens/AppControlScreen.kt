package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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

data class AppShortcutItem(
    val name: String,
    val packageName: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppControlScreen(
    viewModel: NovaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val appList = remember {
        listOf(
            AppShortcutItem("YouTube", "com.google.android.youtube", Icons.Default.PlayCircle, Color(0xFFFF0000)),
            AppShortcutItem("WhatsApp", "com.whatsapp", Icons.Default.Chat, Color(0xFF25D366)),
            AppShortcutItem("Chrome", "com.android.chrome", Icons.Default.Public, Color(0xFF4285F4)),
            AppShortcutItem("Maps", "com.google.android.apps.maps", Icons.Default.Map, Color(0xFF34A853)),
            AppShortcutItem("Telegram", "org.telegram.messenger", Icons.Default.Send, Color(0xFF229ED9)),
            AppShortcutItem("Instagram", "com.instagram.android", Icons.Default.CameraAlt, Color(0xFFE1306C)),
            AppShortcutItem("Facebook", "com.facebook.katana", Icons.Default.Share, Color(0xFF1877F2)),
            AppShortcutItem("Camera", "camera", Icons.Default.PhotoCamera, Color(0xFF673AB7)),
            AppShortcutItem("Settings", "settings", Icons.Default.Settings, Color(0xFF607D8B))
        )
    }

    val filteredApps = remember(searchQuery) {
        if (searchQuery.isBlank()) appList
        else appList.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NovaBackgroundIce,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "অ্যাপ কন্ট্রোল হাব",
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
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("অ্যাপ খুঁজুন...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NovaTextMuted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ইনস্টলকৃত ও সমর্থিত অ্যাপ্লিকেশন:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = NovaTextPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    Surface(
                        onClick = {
                            when (app.packageName) {
                                "camera" -> viewModel.actionEngine.openCamera()
                                "settings" -> viewModel.actionEngine.openSystemSettings(android.provider.Settings.ACTION_SETTINGS, "Settings")
                                else -> viewModel.actionEngine.openApp(app.packageName, app.name)
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(app.color.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = app.icon,
                                    contentDescription = app.name,
                                    tint = app.color,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = app.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NovaTextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
