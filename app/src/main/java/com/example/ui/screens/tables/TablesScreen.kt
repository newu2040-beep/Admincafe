package com.example.ui.screens.tables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TableItem
import com.example.ui.components.QrCodeView
import com.example.ui.components.TableStatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AdminCafeViewModel

@Composable
fun TablesScreen(
    viewModel: AdminCafeViewModel,
    onOpenOrderForTable: (TableItem) -> Unit
) {
    val tables by viewModel.tables.collectAsState()
    val restaurant by viewModel.restaurant.collectAsState()

    var selectedFloor by remember { mutableStateOf<String?>(null) }
    var showAddTableDialog by remember { mutableStateOf(false) }
    var viewingQrTable by remember { mutableStateOf<TableItem?>(null) }
    var selectedTableForAction by remember { mutableStateOf<TableItem?>(null) }

    val floors = remember(tables) {
        listOf("All") + tables.map { it.floor }.distinct().filter { it.isNotBlank() }
    }

    val filteredTables = remember(tables, selectedFloor) {
        if (selectedFloor == null || selectedFloor == "All") tables
        else tables.filter { it.floor == selectedFloor }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CafeEspresso)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Table Management & QR",
                            color = CafeGold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${tables.size} tables • ${tables.count { it.status == "AVAILABLE" }} available",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = { showAddTableDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CafeGold, contentColor = Color(0xFF1E1712)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Table", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Floor filter chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(floors) { floor ->
                        FilterChip(
                            selected = (floor == "All" && selectedFloor == null) || (selectedFloor == floor),
                            onClick = { selectedFloor = if (floor == "All") null else floor },
                            label = { Text(floor, fontSize = 12.sp) },
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
        if (filteredTables.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TableBar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Text("No tables created yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(
                        onClick = { showAddTableDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CafeGold, contentColor = Color(0xFF1E1712))
                    ) {
                        Text("Create Table")
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
            ) {
                items(filteredTables, key = { it.id }) { table ->
                    TableGridCard(
                        table = table,
                        onQrClick = { viewingQrTable = table },
                        onStatusClick = { selectedTableForAction = table },
                        onOpenOrder = { onOpenOrderForTable(table) }
                    )
                }
            }
        }
    }

    // QR Preview Dialog
    viewingQrTable?.let { table ->
        TableQrPreviewDialog(
            table = table,
            restaurantName = restaurant?.name ?: "ADMINCAFE",
            onDismiss = { viewingQrTable = null },
            onShare = {
                viewModel.setUiMessage("QR Code card for Table ${table.tableNumber} ready to print/share")
                viewingQrTable = null
            }
        )
    }

    // Table Status Quick Change Dialog
    selectedTableForAction?.let { table ->
        TableStatusSelectionDialog(
            currentStatus = table.status,
            tableNumber = table.tableNumber,
            onDismiss = { selectedTableForAction = null },
            onSelectStatus = { newStatus ->
                viewModel.updateTableStatus(table.id, newStatus)
                selectedTableForAction = null
            }
        )
    }

    // Add Table Dialog
    if (showAddTableDialog) {
        AddTableFormDialog(
            onDismiss = { showAddTableDialog = false },
            onSave = { number, capacity, floor ->
                viewModel.addOrUpdateTable(tableNumber = number, capacity = capacity, floor = floor)
                showAddTableDialog = false
            }
        )
    }
}

@Composable
private fun TableGridCard(
    table: TableItem,
    onQrClick: () -> Unit,
    onStatusClick: () -> Unit,
    onOpenOrder: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CafeDarkSurfaceElevated),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Table ${table.tableNumber}",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = onQrClick,
                    modifier = Modifier
                        .size(32.dp)
                        .background(CafeGold.copy(alpha = 0.12f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "View QR",
                        tint = CafeGold,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = "${table.capacity} Seats • ${table.floor}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onStatusClick() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableStatusBadge(status = table.status)
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Change Status",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            Button(
                onClick = onOpenOrder,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (table.status == "OCCUPIED") StatusPreparingColor else CafeGold,
                    contentColor = Color(0xFF1E1712)
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = if (table.status == "OCCUPIED") "View Order" else "Take Order",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TableQrPreviewDialog(
    table: TableItem,
    restaurantName: String,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Table ${table.tableNumber} QR Standee",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                QrCodeView(
                    data = table.qrCode.ifBlank { "ADMINCAFE-QR:TABLE-${table.tableNumber}" },
                    label = "SCAN TO VIEW MENU & ORDER",
                    size = 180.dp
                )

                Text(
                    text = "$restaurantName\nTable ${table.tableNumber} • ${table.capacity} Seats • ${table.floor}",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Customers scan this code with their smartphone camera to open self-ordering menu.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onShare,
                colors = ButtonDefaults.buttonColors(containerColor = CafeGold, contentColor = Color(0xFF1E1712))
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Print / Share")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun TableStatusSelectionDialog(
    currentStatus: String,
    tableNumber: String,
    onDismiss: () -> Unit,
    onSelectStatus: (String) -> Unit
) {
    val statuses = listOf("AVAILABLE", "OCCUPIED", "RESERVED", "CLEANING", "DISABLED")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Table $tableNumber Status", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                statuses.forEach { status ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectStatus(status) }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TableStatusBadge(status = status)
                        if (currentStatus == status) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = CafeGold)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddTableFormDialog(
    onDismiss: () -> Unit,
    onSave: (number: String, capacity: Int, floor: String) -> Unit
) {
    var number by remember { mutableStateOf("") }
    var capacityStr by remember { mutableStateOf("4") }
    var floor by remember { mutableStateOf("Ground Floor") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Dining Table", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = number,
                    onValueChange = { number = it },
                    label = { Text("Table Number (e.g. T-05) *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = capacityStr,
                    onValueChange = { capacityStr = it },
                    label = { Text("Seating Capacity *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = floor,
                    onValueChange = { floor = it },
                    label = { Text("Floor / Dining Zone *") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cap = capacityStr.toIntOrNull() ?: 4
                    onSave(number, cap, floor)
                },
                enabled = number.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CafeGold, contentColor = Color(0xFF1E1712))
            ) {
                Text("Create Table")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
