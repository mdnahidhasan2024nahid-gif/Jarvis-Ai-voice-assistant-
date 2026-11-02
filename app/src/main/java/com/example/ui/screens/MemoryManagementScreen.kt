package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryManagementScreen(
    viewModel: NovaViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val memories by viewModel.memories.collectAsState()
    val isMemoryEnabled by viewModel.isMemoryEnabled.collectAsState()

    var newTitle by remember { mutableStateOf("") }
    var newValue by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("preference") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NovaBackgroundIce,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "মেমরি ও ব্যক্তিগতকরণ",
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Memory Master Switch
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text("স্মৃতি সংরক্ষণ ব্যবস্থা (Memory)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            "নোভাকে আপনার পছন্দ ও তথ্য মনে রাখতে দিন",
                            fontSize = 12.sp,
                            color = NovaTextSecondary
                        )
                    }

                    Switch(
                        checked = isMemoryEnabled,
                        onCheckedChange = {
                            scope.launch { viewModel.preferences.setMemoryEnabled(it) }
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = NovaElectricBlue)
                    )
                }
            }

            // Add new memory
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("নতুন তথ্য যোগ করুন", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("শিরোনাম (যেমন: প্রিয় গান বা কাজের সময়)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = newValue,
                        onValueChange = { newValue = it },
                        label = { Text("বিস্তারিত তথ্য") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            if (newTitle.isNotBlank() && newValue.isNotBlank()) {
                                viewModel.addMemory(newCategory, newTitle, newValue)
                                newTitle = ""
                                newValue = ""
                                Toast.makeText(context, "মেমরিতে সংরক্ষিত হয়েছে", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NovaElectricBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("মেমরিতে সেভ করুন")
                    }
                }
            }

            // Existing memories list
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "সংরক্ষিত মেমরি (${memories.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = NovaTextPrimary
                )

                if (memories.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearAllMemories() }) {
                        Text("সব মুছুন", color = NovaErrorRed, fontSize = 12.sp)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(memories, key = { it.id }) { mem ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, NovaBorderLight)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(mem.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(mem.value, fontSize = 12.sp, color = NovaTextSecondary)
                            }

                            IconButton(onClick = { viewModel.deleteMemory(mem.id) }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = NovaErrorRed)
                            }
                        }
                    }
                }
            }
        }
    }
}
