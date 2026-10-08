package com.example.ui.screens.setup

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.QrCodeView
import com.example.ui.theme.CafeDarkSurfaceElevated
import com.example.ui.theme.CafeEspresso
import com.example.ui.theme.CafeGold
import com.example.ui.viewmodel.AdminCafeViewModel

@Composable
fun RestaurantSetupWizardScreen(
    viewModel: AdminCafeViewModel,
    onSetupComplete: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    val totalSteps = 8

    // Form states
    var restName by remember { mutableStateOf("Himalayan Bistro & Lounge") }
    var city by remember { mutableStateOf("Kathmandu") }
    var currency by remember { mutableStateOf("Rs.") }
    var phone by remember { mutableStateOf("+977-9841234567") }

    var catName by remember { mutableStateOf("Momos & Dumplings") }
    var catDesc by remember { mutableStateOf("Handcrafted fresh momos") }

    var foodName by remember { mutableStateOf("Chicken Steamed Momo") }
    var foodPrice by remember { mutableStateOf("180") }
    var foodPrepTime by remember { mutableStateOf("12") }

    var tableNumber by remember { mutableStateOf("T-01") }
    var tableCapacity by remember { mutableStateOf("4") }
    var tableFloor by remember { mutableStateOf("Ground Floor") }

    var stationName by remember { mutableStateOf("Momo Station") }
    var stationDesc by remember { mutableStateOf("Main steaming counter") }

    var staffName by remember { mutableStateOf("Suman Thapa") }
    var staffRole by remember { mutableStateOf("WAITER") }
    var staffPhone by remember { mutableStateOf("+977-9812345678") }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CafeEspresso)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Restaurant Setup Wizard",
                            color = CafeGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Step $step of $totalSteps",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }

                    TextButton(
                        onClick = {
                            viewModel.completeSetupAndSeed()
                            onSetupComplete()
                        }
                    ) {
                        Text("Auto-Seed & Finish", color = CafeGold, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { step / totalSteps.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = CafeGold,
                    trackColor = CafeDarkSurfaceElevated,
                )
            }
        },
        containerColor = CafeEspresso
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                when (step) {
                    1 -> {
                        StepCard(
                            stepNumber = 1,
                            title = "Restaurant Information",
                            description = "Configure your restaurant name, location, and currency settings."
                        ) {
                            OutlinedTextField(
                                value = restName,
                                onValueChange = { restName = it },
                                label = { Text("Restaurant Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = city,
                                onValueChange = { city = it },
                                label = { Text("City / Region") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = currency,
                                    onValueChange = { currency = it },
                                    label = { Text("Currency (e.g. Rs., $)") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = phone,
                                    onValueChange = { phone = it },
                                    label = { Text("Phone Number") },
                                    modifier = Modifier.weight(1.5f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                                )
                            }
                        }
                    }

                    2 -> {
                        StepCard(
                            stepNumber = 2,
                            title = "Create First Food Category",
                            description = "Group menu items into sections like Starters, Mains, or Beverages."
                        ) {
                            OutlinedTextField(
                                value = catName,
                                onValueChange = { catName = it },
                                label = { Text("Category Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = catDesc,
                                onValueChange = { catDesc = it },
                                label = { Text("Short Description") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    3 -> {
                        StepCard(
                            stepNumber = 3,
                            title = "Add First Food Item",
                            description = "Add a hero dish with price and preparation time."
                        ) {
                            OutlinedTextField(
                                value = foodName,
                                onValueChange = { foodName = it },
                                label = { Text("Food Item Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = foodPrice,
                                    onValueChange = { foodPrice = it },
                                    label = { Text("Price ($currency)") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                OutlinedTextField(
                                    value = foodPrepTime,
                                    onValueChange = { foodPrepTime = it },
                                    label = { Text("Prep Time (mins)") },
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                        }
                    }

                    4 -> {
                        StepCard(
                            stepNumber = 4,
                            title = "Create Dining Tables",
                            description = "Add tables with capacity and floor locations."
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = tableNumber,
                                    onValueChange = { tableNumber = it },
                                    label = { Text("Table Number (e.g. T-01)") },
                                    modifier = Modifier.weight(1.2f)
                                )
                                OutlinedTextField(
                                    value = tableCapacity,
                                    onValueChange = { tableCapacity = it },
                                    label = { Text("Seats") },
                                    modifier = Modifier.weight(0.8f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }
                            OutlinedTextField(
                                value = tableFloor,
                                onValueChange = { tableFloor = it },
                                label = { Text("Floor / Area (Ground, Rooftop, Patio)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    5 -> {
                        StepCard(
                            stepNumber = 5,
                            title = "Create Kitchen Station",
                            description = "Routing prep tasks to dedicated kitchen counters."
                        ) {
                            OutlinedTextField(
                                value = stationName,
                                onValueChange = { stationName = it },
                                label = { Text("Station Name (e.g. Momo, Grill, Bar)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = stationDesc,
                                onValueChange = { stationDesc = it },
                                label = { Text("Station Description") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    6 -> {
                        StepCard(
                            stepNumber = 6,
                            title = "Invite / Add Staff",
                            description = "Create accounts for Waiters, Chefs, or Cashiers."
                        ) {
                            OutlinedTextField(
                                value = staffName,
                                onValueChange = { staffName = it },
                                label = { Text("Staff Full Name") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = staffPhone,
                                onValueChange = { staffPhone = it },
                                label = { Text("Phone Number") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text("Role: Waiter (Can be modified later in Staff tab)", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                    }

                    7 -> {
                        StepCard(
                            stepNumber = 7,
                            title = "Generate Table QR Code",
                            description = "Each table gets a unique QR identifier for ordering and tracking."
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                QrCodeView(
                                    data = "ADMINCAFE-QR:REST-01:TABLE-01:$tableNumber",
                                    label = "TABLE $tableNumber • $tableCapacity Seats • $tableFloor",
                                    size = 160.dp
                                )
                            }
                        }
                    }

                    8 -> {
                        StepCard(
                            stepNumber = 8,
                            title = "Setup Complete!",
                            description = "Your restaurant structure is ready. You can now operate tables, manage orders, and view sales in real time."
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(CafeGold.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = CafeGold,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = restName,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$city • $currency currency",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (step > 1) {
                    OutlinedButton(
                        onClick = { step-- },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Back")
                    }
                }

                Button(
                    onClick = {
                        if (step < totalSteps) {
                            step++
                        } else {
                            viewModel.completeSetupAndSeed()
                            onSetupComplete()
                        }
                    },
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CafeGold, contentColor = Color(0xFF1E1712))
                ) {
                    Text(
                        text = if (step == totalSteps) "Launch Dashboard" else "Save & Continue",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StepCard(
    stepNumber: Int,
    title: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CafeDarkSurfaceElevated),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(CafeGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stepNumber.toString(),
                        color = Color(0xFF1E1712),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            content()
        }
    }
}
