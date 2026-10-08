package com.example.ui.screens.operations

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AdminCafeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OperationsMoreScreen(
    viewModel: AdminCafeViewModel,
    onSignOut: () -> Unit
) {
    var selectedSection by remember { mutableStateOf("Staff") } // Staff, Inventory, Sales, Expenses, Customers, Audit, Settings

    val sections = listOf(
        "Staff" to Icons.Default.Badge,
        "Inventory" to Icons.Default.Inventory2,
        "Sales" to Icons.Default.QueryStats,
        "Expenses" to Icons.Default.AccountBalanceWallet,
        "Customers" to Icons.Default.People,
        "Audit" to Icons.Default.History,
        "Settings" to Icons.Default.Settings
    )

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CafeEspresso)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Operations & Administration",
                    color = CafeGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(sections) { (name, icon) ->
                        FilterChip(
                            selected = selectedSection == name,
                            onClick = { selectedSection = name },
                            leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            label = { Text(name, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CafeGold,
                                selectedLabelColor = Color(0xFF1E1712)
                            )
                        )
                    }
                }
            }
        },
        containerColor = CafeEspresso
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedSection) {
                "Staff" -> StaffSubScreen(viewModel)
                "Inventory" -> InventorySubScreen(viewModel)
                "Sales" -> SalesSubScreen(viewModel)
                "Expenses" -> ExpensesSubScreen(viewModel)
                "Customers" -> CustomersSubScreen(viewModel)
                "Audit" -> AuditSubScreen(viewModel)
                "Settings" -> SettingsSubScreen(viewModel, onSignOut)
            }
        }
    }
}

// ==================== 1. STAFF MANAGEMENT ====================
@Composable
private fun StaffSubScreen(viewModel: AdminCafeViewModel) {
    val staffList by viewModel.staff.collectAsState()
    val stations by viewModel.kitchenStations.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Restaurant Staff Members (${staffList.size})", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CafeGold, contentColor = Color(0xFF1E1712)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Staff", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(staffList, key = { it.id }) { member ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CafeDarkSurfaceElevated),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(member.name, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .background(CafeGold.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(member.role, color = CafeGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text("${member.phone} • ${member.email}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        if (member.assignedStation.isNotBlank()) {
                            Text("Station: ${member.assignedStation}", color = CafeGoldLight, fontSize = 11.sp)
                        }
                    }

                    // Status toggle
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = member.status,
                            color = if (member.status == "ACTIVE") StatusReadyColor else StatusCancelledColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = {
                                val next = if (member.status == "ACTIVE") "SUSPENDED" else "ACTIVE"
                                viewModel.updateStaffStatus(member.id, next)
                            }
                        ) {
                            Text(if (member.status == "ACTIVE") "Suspend" else "Activate", fontSize = 11.sp, color = CafeGold)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddStaffDialog(
            stations = stations,
            onDismiss = { showAddDialog = false },
            onSave = { name, phone, email, role, station ->
                viewModel.addOrUpdateStaff(name = name, phone = phone, email = email, role = role, assignedStation = station)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddStaffDialog(
    stations: List<KitchenStation>,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, email: String, role: String, station: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    val roles = listOf("WAITER", "CHEF", "CASHIER", "MANAGER", "KITCHEN_STAFF")
    var selectedRole by remember { mutableStateOf("WAITER") }
    var selectedStation by remember { mutableStateOf(stations.firstOrNull()?.name ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Restaurant Staff", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())

                Text("Role *", fontSize = 12.sp, color = CafeGold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(roles) { r ->
                        FilterChip(selected = selectedRole == r, onClick = { selectedRole = r }, label = { Text(r, fontSize = 11.sp) })
                    }
                }

                if (selectedRole == "CHEF" || selectedRole == "KITCHEN_STAFF") {
                    Text("Assigned Station", fontSize = 12.sp, color = CafeGold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(stations) { st ->
                            FilterChip(selected = selectedStation == st.name, onClick = { selectedStation = st.name }, label = { Text(st.name, fontSize = 11.sp) })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, phone, email, selectedRole, selectedStation) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CafeGold, contentColor = Color(0xFF1E1712))
            ) {
                Text("Add Staff")
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ==================== 2. INVENTORY ====================
@Composable
private fun InventorySubScreen(viewModel: AdminCafeViewModel) {
    val inventory by viewModel.inventory.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Raw Ingredient Stock (${inventory.size})", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CafeGold, contentColor = Color(0xFF1E1712)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Item", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(inventory, key = { it.id }) { item ->
            val isLowStock = item.currentStock <= item.minStock
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CafeDarkSurfaceElevated),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(item.ingredientName, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            if (isLowStock) {
                                Box(
                                    modifier = Modifier
                                        .background(StatusCancelledColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("⚠️ LOW STOCK", color = StatusCancelledColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text("Supplier: ${item.supplier.ifBlank { "Local Market" }} • Cost: Rs. ${item.costPerUnit}/${item.unit}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${item.currentStock} ${item.unit}",
                            color = if (isLowStock) StatusCancelledColor else CafeGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text("Min: ${item.minStock} ${item.unit}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddInventoryDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, stock, unit, minStock, supplier, cost ->
                viewModel.addOrUpdateInventory(name = name, stock = stock, unit = unit, minStock = minStock, supplier = supplier, cost = cost)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddInventoryDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, stock: Double, unit: String, minStock: Double, supplier: String, cost: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var stockStr by remember { mutableStateOf("10") }
    var unit by remember { mutableStateOf("kg") }
    var minStockStr by remember { mutableStateOf("5") }
    var supplier by remember { mutableStateOf("") }
    var costStr by remember { mutableStateOf("100") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Inventory Item", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Ingredient Name *") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = stockStr, onValueChange = { stockStr = it }, label = { Text("Current Stock *") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit (kg, l, pcs)") }, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = minStockStr, onValueChange = { minStockStr = it }, label = { Text("Alert Threshold *") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = costStr, onValueChange = { costStr = it }, label = { Text("Cost Per Unit") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
                OutlinedTextField(value = supplier, onValueChange = { supplier = it }, label = { Text("Supplier Name") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(name, stockStr.toDoubleOrNull() ?: 0.0, unit, minStockStr.toDoubleOrNull() ?: 0.0, supplier, costStr.toDoubleOrNull() ?: 0.0)
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CafeGold, contentColor = Color(0xFF1E1712))
            ) {
                Text("Add Item")
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ==================== 3. SALES & FINANCIALS ====================
@Composable
private fun SalesSubScreen(viewModel: AdminCafeViewModel) {
    val orders by viewModel.orders.collectAsState()
    val restaurant by viewModel.restaurant.collectAsState()
    val currency = restaurant?.currency ?: "Rs."

    val completedOrders = remember(orders) { orders.filter { it.status != "CANCELLED" && it.paymentStatus == "PAID" } }
    val grossSales = remember(completedOrders) { completedOrders.sumOf { it.total } }
    val netSubtotal = remember(completedOrders) { completedOrders.sumOf { it.subtotal } }
    val totalTaxes = remember(completedOrders) { completedOrders.sumOf { it.tax } }
    val totalServiceCharges = remember(completedOrders) { completedOrders.sumOf { it.serviceCharge } }
    val averageTicket = if (completedOrders.isNotEmpty()) grossSales / completedOrders.size else 0.0

    val cashCount = remember(completedOrders) { completedOrders.count { it.paymentMethod == "CASH" } }
    val cardCount = remember(completedOrders) { completedOrders.count { it.paymentMethod == "CARD" } }
    val qrCount = remember(completedOrders) { completedOrders.count { it.paymentMethod == "QR" } }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CafeDarkSurfaceElevated),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Executive Financial Overview", color = CafeGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Gross Revenue", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$currency ${String.format(Locale.US, "%.1f", grossSales)}", color = CafeGold, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Food & Bev Subtotal", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$currency ${String.format(Locale.US, "%.1f", netSubtotal)}", color = MaterialTheme.colorScheme.onSurface)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Taxes Collected", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$currency ${String.format(Locale.US, "%.1f", totalTaxes)}", color = MaterialTheme.colorScheme.onSurface)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Service Charges", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$currency ${String.format(Locale.US, "%.1f", totalServiceCharges)}", color = MaterialTheme.colorScheme.onSurface)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Completed Orders", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${completedOrders.size} tickets", color = MaterialTheme.colorScheme.onSurface)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Average Ticket Size", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$currency ${String.format(Locale.US, "%.1f", averageTicket)}", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CafeDarkSurfaceElevated),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Payment Tender Breakdown", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        TenderMetric(label = "Cash", count = cashCount, color = CafeGold)
                        TenderMetric(label = "Card POS", count = cardCount, color = StatusNewColor)
                        TenderMetric(label = "Digital QR", count = qrCount, color = StatusReadyColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun TenderMetric(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count.toString(), color = color, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
    }
}

// ==================== 4. EXPENSES ====================
@Composable
private fun ExpensesSubScreen(viewModel: AdminCafeViewModel) {
    val expenses by viewModel.expenses.collectAsState()
    val restaurant by viewModel.restaurant.collectAsState()
    val currency = restaurant?.currency ?: "Rs."
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Operational Expenses (${expenses.size})", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CafeGold, contentColor = Color(0xFF1E1712)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Expense", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(expenses, key = { it.id }) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CafeDarkSurfaceElevated),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(item.description, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("${item.category} • ${item.date} • by ${item.addedBy}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                    Text(
                        text = "$currency ${String.format(Locale.US, "%.1f", item.amount)}",
                        color = StatusCancelledColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddExpenseDialog(
            currency = currency,
            onDismiss = { showAddDialog = false },
            onSave = { amount, category, desc, date ->
                viewModel.addExpense(amount, category, desc, date)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddExpenseDialog(
    currency: String,
    onDismiss: () -> Unit,
    onSave: (amount: Double, category: String, description: String, date: String) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    val categories = listOf("INGREDIENTS", "RENT", "ELECTRICITY", "SALARIES", "MAINTENANCE", "OTHER")
    var selectedCat by remember { mutableStateOf("INGREDIENTS") }
    var desc by remember { mutableStateOf("") }
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Operating Expense", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = amountStr, onValueChange = { amountStr = it }, label = { Text("Amount ($currency) *") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description *") }, modifier = Modifier.fillMaxWidth())
                Text("Category *", fontSize = 12.sp, color = CafeGold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(selected = selectedCat == cat, onClick = { selectedCat = cat }, label = { Text(cat, fontSize = 11.sp) })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(amountStr.toDoubleOrNull() ?: 0.0, selectedCat, desc, today) },
                enabled = amountStr.isNotBlank() && desc.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CafeGold, contentColor = Color(0xFF1E1712))
            ) {
                Text("Save Expense")
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ==================== 5. CUSTOMERS ====================
@Composable
private fun CustomersSubScreen(viewModel: AdminCafeViewModel) {
    val customers by viewModel.customers.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Customer Profiles (${customers.size})", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CafeGold, contentColor = Color(0xFF1E1712)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Customer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(customers, key = { it.id }) { customer ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CafeDarkSurfaceElevated),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(customer.name, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("${customer.phone} • ${customer.email}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        if (customer.favoriteItems.isNotBlank()) {
                            Text("Favorites: ${customer.favoriteItems}", color = CafeGoldLight, fontSize = 11.sp)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("${customer.orderCount} visits", color = CafeGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Rs. ${customer.totalSpent}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCustomerDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, phone, email ->
                viewModel.addCustomer(name, phone, email)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, email: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Customer Profile", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name *") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone *") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, phone, email) },
                enabled = name.isNotBlank() && phone.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CafeGold, contentColor = Color(0xFF1E1712))
            ) {
                Text("Save")
            }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ==================== 6. AUDIT LOG ====================
@Composable
private fun AuditSubScreen(viewModel: AdminCafeViewModel) {
    val logs by viewModel.auditLogs.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
    ) {
        item {
            Text("Immutable Audit Log (${logs.size})", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        if (logs.isEmpty()) {
            item {
                Text("No operational events logged yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(logs, key = { it.id }) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CafeDarkSurfaceElevated),
                    shape = RoundedCornerShape(10.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(log.action, color = CafeGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(log.createdAt.take(19).replace("T", " "), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        }
                        Text("User: ${log.user}", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        if (log.reason.isNotBlank()) {
                            Text("Reason: ${log.reason}", color = if (log.action.contains("CANCEL")) StatusCancelledColor else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                        if (log.details.isNotBlank()) {
                            Text(log.details, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==================== 7. SETTINGS ====================
@Composable
private fun SettingsSubScreen(viewModel: AdminCafeViewModel, onSignOut: () -> Unit) {
    val restaurant by viewModel.restaurant.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var name by remember(restaurant) { mutableStateOf(restaurant?.name ?: "") }
    var phone by remember(restaurant) { mutableStateOf(restaurant?.phone ?: "") }
    var address by remember(restaurant) { mutableStateOf(restaurant?.address ?: "") }
    var city by remember(restaurant) { mutableStateOf(restaurant?.city ?: "") }
    var currency by remember(restaurant) { mutableStateOf(restaurant?.currency ?: "Rs.") }
    var taxRateStr by remember(restaurant) { mutableStateOf((restaurant?.taxRate ?: 13.0).toString()) }
    var serviceChargeStr by remember(restaurant) { mutableStateOf((restaurant?.serviceCharge ?: 10.0).toString()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CafeDarkSurfaceElevated),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Restaurant Profile & Tax Settings", color = CafeGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Restaurant Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(value = currency, onValueChange = { currency = it }, label = { Text("Currency") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = taxRateStr, onValueChange = { taxRateStr = it }, label = { Text("Tax Rate (%)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = serviceChargeStr, onValueChange = { serviceChargeStr = it }, label = { Text("Service (%)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }

                    Button(
                        onClick = {
                            viewModel.updateRestaurantSettings(
                                name = name,
                                phone = phone,
                                address = address,
                                city = city,
                                currency = currency,
                                taxRate = taxRateStr.toDoubleOrNull() ?: 13.0,
                                serviceCharge = serviceChargeStr.toDoubleOrNull() ?: 10.0
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CafeGold, contentColor = Color(0xFF1E1712)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Settings", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CafeDarkSurfaceElevated),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("User Session", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Logged in as: ${userProfile?.displayName} (${userProfile?.email})", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    Text("Assigned Role: ${userProfile?.role}", color = CafeGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedButton(
                        onClick = onSignOut,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusCancelledColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sign Out")
                    }
                }
            }
        }
    }
}
