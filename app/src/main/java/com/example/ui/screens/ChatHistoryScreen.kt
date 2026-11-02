package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.NovaViewModel
import com.example.data.ChatMessageEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHistoryScreen(
    viewModel: NovaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsState()
    val context = LocalContext.current
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("কথোপকথন মুছে ফেলবেন?") },
            text = { Text("সব চ্যাট হিস্ট্রি স্থায়ীভাবে ডিলিট করা হবে।") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearChatHistory()
                    showClearDialog = false
                    Toast.makeText(context, "চ্যাট মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                }) {
                    Text("মুছে ফেলুন", color = NovaErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NovaBackgroundIce,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "চ্যাট হিস্ট্রি",
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
                    if (messages.isNotEmpty()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Clear History", tint = NovaErrorRed)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Empty",
                        tint = NovaTextMuted,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "কোনো কথোপকথন রেকর্ড নেই",
                        fontSize = 15.sp,
                        color = NovaTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "মাইক চেপে নোভা এআই-এর সাথে কথা বলুন",
                        fontSize = 13.sp,
                        color = NovaTextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubbleItem(
                        message = msg,
                        onPlayVoice = {
                            viewModel.voiceEngine.speak(
                                text = msg.content,
                                apiKey = viewModel.elevenLabsApiKey.value.ifBlank { null },
                                voiceId = viewModel.selectedVoiceId.value,
                                speed = viewModel.speechSpeed.value,
                                language = viewModel.currentLanguage.value
                            )
                        },
                        onCopyText = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("NOVA AI", msg.content))
                            Toast.makeText(context, "টেক্সট কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubbleItem(
    message: ChatMessageEntity,
    onPlayVoice: () -> Unit,
    onCopyText: () -> Unit
) {
    val isUser = message.role == "user"
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.widthIn(max = 320.dp),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(NovaElectricBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✦", color = Color.White, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isUser) 18.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 18.dp
                ),
                color = if (isUser) NovaElectricBlue else Color.White,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = message.content,
                        fontSize = 14.sp,
                        color = if (isUser) Color.White else NovaTextPrimary,
                        lineHeight = 20.sp
                    )

                    if (!message.actionType.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NovaSoftSky
                        ) {
                            Text(
                                text = "ক্রিয়া: ${message.actionType}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NovaElectricBlue,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formattedTime,
                            fontSize = 10.sp,
                            color = if (isUser) Color.White.copy(alpha = 0.7f) else NovaTextMuted
                        )

                        if (!isUser) {
                            Row {
                                IconButton(
                                    onClick = onPlayVoice,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.VolumeUp,
                                        contentDescription = "Play Voice",
                                        tint = NovaElectricBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = onCopyText,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ContentCopy,
                                        contentDescription = "Copy Text",
                                        tint = NovaTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
