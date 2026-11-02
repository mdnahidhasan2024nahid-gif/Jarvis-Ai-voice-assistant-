package com.example.ui.screens

import android.app.SearchManager
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.OpenInBrowser
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebSearchScreen(
    viewModel: NovaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf<String?>(null) }
    var isSearching by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NovaBackgroundIce,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "ওয়েব তথ্য অনুসন্ধান",
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
                        text = "গুগল এআই ওয়েব রিসার্চ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = NovaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("কী খুঁজতে চান? যেমন: কৃত্রিম বুদ্ধিমত্তার ভবিষ্যৎ") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            IconButton(onClick = {
                                if (searchQuery.isNotBlank()) {
                                    isSearching = true
                                    viewModel.processUserInput("অনুসন্ধান করুন: $searchQuery")
                                    searchResult = "অনুসন্ধানের ফলাফল প্রস্তুত করা হচ্ছে..."
                                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                        isSearching = false
                                        searchResult = "অনুসন্ধান সমাপ্ত: $searchQuery সম্পর্কিত বিস্তারিত তথ্য এআই ভয়েস অ্যাসিস্ট্যান্ট দ্বারা তৈরি করা হয়েছে।"
                                    }, 1500)
                                }
                            }) {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = NovaElectricBlue)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                                    putExtra(SearchManager.QUERY, searchQuery)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NovaElectricBlue),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.OpenInBrowser, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ব্রাউজারে খুলুন", fontSize = 12.sp)
                        }
                    }
                }
            }

            if (searchResult != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "এআই ফলাফল সারসংক্ষেপ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = NovaElectricBlue
                            )
                            IconButton(onClick = {
                                searchResult?.let {
                                    viewModel.voiceEngine.speak(
                                        text = it,
                                        apiKey = viewModel.elevenLabsApiKey.value.ifBlank { null },
                                        voiceId = viewModel.selectedVoiceId.value,
                                        speed = viewModel.speechSpeed.value,
                                        language = viewModel.currentLanguage.value
                                    )
                                }
                            }) {
                                Icon(Icons.Outlined.VolumeUp, contentDescription = "Read Aloud", tint = NovaElectricBlue)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = searchResult!!,
                            fontSize = 14.sp,
                            color = NovaTextPrimary,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }
    }
}
