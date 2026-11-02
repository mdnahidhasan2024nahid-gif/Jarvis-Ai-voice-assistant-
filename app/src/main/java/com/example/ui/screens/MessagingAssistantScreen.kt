package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
fun MessagingAssistantScreen(
    viewModel: NovaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var recipientInput by remember { mutableStateOf("") }
    var messageBodyInput by remember { mutableStateOf("") }

    val quickTemplates = listOf(
        "শুভ জন্মদিন! অনেক শুভকামনা ও দোয়া রইল।",
        "আমি একটি জরুরি মিটিংয়ে আছি, পরে কল করছি।",
        "কাজটি সফলভাবে সম্পন্ন হয়েছে। রিপোর্ট চেক করুন।",
        "কেমন আছেন? অনেকদিন আপনার কোনো খোঁজ নেই।"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NovaBackgroundIce,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "মেসেজিং অ্যাসিস্ট্যান্ট",
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
                        text = "স্মার্ট বার্তা রচনা",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = NovaTextPrimary
                    )

                    OutlinedTextField(
                        value = recipientInput,
                        onValueChange = { recipientInput = it },
                        label = { Text("প্রাপকের নম্বর বা নাম") },
                        placeholder = { Text("যেমন: 01800000000") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = messageBodyInput,
                        onValueChange = { messageBodyInput = it },
                        label = { Text("বার্তার বিবরণ") },
                        minLines = 4,
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Text(
                        text = "দ্রুত টেমপ্লেট:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NovaTextSecondary
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickTemplates.forEach { template ->
                            Surface(
                                onClick = { messageBodyInput = template },
                                shape = RoundedCornerShape(12.dp),
                                color = NovaSoftSky
                            ) {
                                Text(
                                    text = template,
                                    fontSize = 11.sp,
                                    color = NovaElectricBlue,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // WhatsApp
                        Button(
                            onClick = {
                                if (messageBodyInput.isBlank()) {
                                    Toast.makeText(context, "বার্তার বিবরণ লিখুন", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val url = "https://api.whatsapp.com/send?phone=$recipientInput&text=${Uri.encode(messageBodyInput)}"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "WhatsApp ইনস্টল করা নেই", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Text("হোয়াটসঅ্যাপে পাঠান", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // SMS
                        Button(
                            onClick = {
                                if (messageBodyInput.isBlank()) {
                                    Toast.makeText(context, "বার্তার বিবরণ লিখুন", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val uri = Uri.parse("smsto:$recipientInput")
                                val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                                    putExtra("sms_body", messageBodyInput)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "এসএমএস অ্যাপ পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NovaElectricBlue),
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("এসএমএস (SMS)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
