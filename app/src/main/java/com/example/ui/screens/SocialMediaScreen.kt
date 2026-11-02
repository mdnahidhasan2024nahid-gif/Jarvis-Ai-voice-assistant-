package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Share
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
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialMediaScreen(
    viewModel: NovaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var topicInput by remember { mutableStateOf("") }
    var captionResult by remember { mutableStateOf("") }
    var selectedPlatform by remember { mutableStateOf("Facebook") }

    val platforms = listOf("Facebook", "Instagram", "YouTube", "Telegram")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NovaBackgroundIce,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "সোশ্যাল মিডিয়া সহকারী",
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
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "প্ল্যাটফর্ম নির্বাচন করুন:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = NovaTextPrimary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        platforms.forEach { platform ->
                            val isSelected = selectedPlatform == platform
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedPlatform = platform },
                                label = { Text(platform) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NovaElectricBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = topicInput,
                        onValueChange = { topicInput = it },
                        label = { Text("পোস্টের বিষয় বা আইডিয়া") },
                        placeholder = { Text("যেমন: নতুন প্রযুক্তি, ভ্রমণের অভিজ্ঞতা...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            if (topicInput.isNotBlank()) {
                                captionResult = when (selectedPlatform) {
                                    "Instagram" -> "✨ Embracing new perspectives and innovation with #NOVA_AI! 🚀 Keep pushing boundaries.\n\n#Technology #AI #Future #Innovation #Creativity #DailyInspiration"
                                    "YouTube" -> "In this video, we explore $topicInput and how cutting-edge tools can transform your workflow. Don't forget to like, share, and subscribe! 🔔"
                                    "Telegram" -> "📢 আপডেট: $topicInput সম্পর্কিত বিস্তারিত তথ্য প্রকাশিত হয়েছে। গ্রুপে আপনার মতামত জানান!"
                                    else -> "আজকের ভাবনা: $topicInput\nপ্রযুক্তির সঠিক ব্যবহারের মাধ্যমে আমরা প্রতিদিন আরও এক ধাপ এগিয়ে যেতে পারি। আপনার অভিমত কী?\n\n#NOVA_AI #Bangladesh #Technology #Inspiration"
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NovaElectricBlue),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ক্যাপশন ও হ্যাশট্যাগ তৈরি করুন", fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (captionResult.isNotBlank()) {
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
                            text = "প্রস্তুতকৃত পোস্ট ($selectedPlatform):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = NovaElectricBlue
                        )

                        Text(
                            text = captionResult,
                            fontSize = 14.sp,
                            color = NovaTextPrimary,
                            lineHeight = 20.sp
                        )

                        Button(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, captionResult)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "$selectedPlatform এ শেয়ার করুন")
                                context.startActivity(shareIntent)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NovaElectricBlue),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("শেয়ার বা পাবলিশ করুন")
                        }
                    }
                }
            }
        }
    }
}
