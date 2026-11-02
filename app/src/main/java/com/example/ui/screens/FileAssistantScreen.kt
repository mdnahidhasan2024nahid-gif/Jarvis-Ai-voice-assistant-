package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.NovaViewModel
import com.example.ui.theme.*
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileAssistantScreen(
    viewModel: NovaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var fileContent by remember { mutableStateOf("") }
    var summaryText by remember { mutableStateOf("") }
    var isReading by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedFileUri = uri
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val reader = BufferedReader(InputStreamReader(stream))
                    fileContent = reader.readText().take(4000) // First 4000 chars
                    summaryText = "ডকুমেন্ট সফলভাবে লোড হয়েছে। এআই দিয়ে সারসংক্ষেপ তৈরি করতে নিচের বাটনে চাপ দিন।"
                }
            } catch (e: Exception) {
                Toast.makeText(context, "ফাইলটি পড়তে সমস্যা হয়েছে: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NovaBackgroundIce,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "ডকুমেন্ট & ফাইল অ্যাসিস্ট্যান্ট",
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
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ডকুমেন্ট নির্বাচন করুন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = NovaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "অ্যান্ড্রয়েড সিস্টেম ফাইল পিকার দিয়ে টেক্সট বা পিডিএফ নির্বাচন করুন। নোভা এটি পড়ে শোনাবে বা সারসংক্ষেপ করবে।",
                        fontSize = 12.sp,
                        color = NovaTextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { filePickerLauncher.launch("text/*") },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NovaElectricBlue),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Outlined.FolderOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ফাইল নির্বাচন করুন (Storage Access Framework)", fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (fileContent.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "ফাইলের কন্টেন্ট প্রিভিউ:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NovaTextPrimary
                        )

                        Text(
                            text = fileContent,
                            fontSize = 13.sp,
                            color = NovaTextSecondary,
                            maxLines = 6,
                            lineHeight = 18.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.processUserInput("এই ডকুমেন্টটির সারসংক্ষেপ বাংলায় তৈরি করুন: $fileContent")
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NovaElectricBlue),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Summarize, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("সারসংক্ষেপ তৈরি", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    viewModel.voiceEngine.speak(
                                        text = fileContent,
                                        apiKey = viewModel.elevenLabsApiKey.value.ifBlank { null },
                                        voiceId = viewModel.selectedVoiceId.value,
                                        speed = viewModel.speechSpeed.value,
                                        language = viewModel.currentLanguage.value
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NovaSecondaryBlue),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Outlined.VolumeUp, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ভয়েসে শুনুন", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
