package com.example.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FoodItem
import com.example.data.model.OrderItem
import com.example.data.model.RestaurantOrder
import com.example.data.model.TableItem
import com.example.data.repository.RestaurantRepository
import com.example.ui.components.OrderStatusBadge
import com.example.ui.components.WarningConfirmationDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.AdminCafeViewModel
import java.util.Locale
import java.util.UUID

@Composable
fun OrdersScreen(
    viewModel: AdminCafeViewModel,
    preSelectedTable: TableItem? = null
) {
    val orders by viewModel.orders.collectAsState()
    val tables by viewModel.tables.collectAsState()
    val foodItems by viewModel.foodItems.collectAsState()
    val restaurant by viewModel.restaurant.collectAsState()

    val currency = restaurant?.currency ?: "Rs."

    var selectedFilter by remember { mutableStateOf("Active") } // All, Active, Completed, Cancelled
    var selectedOrderForDetails by remember { mutableStateOf<RestaurantOrder?>(null) }
    var showCreateOrderDialog by remember { mutableStateOf(false) }

    // If preSelectedTable was passed, open create order immediately
    LaunchedEffect(preSelectedTable) {
        if (preSelectedTable != null) {
            val existing = orders.find { it.tableId == preSelectedTable.id && it.status in listOf("NEW", "ACCEPTED", "PREPARING", "READY") }
            if (existing != null) {
                selectedOrderForDetails = existing
            } else {
                showCreateOrderDialog = true
            }
        }
    }

    val filteredOrders = remember(orders, selectedFilter) {
        when (selectedFilter) {
            "Active" -> orders.filter { it.status in listOf("NEW", "ACCEPTED", "PREPARING", "READY") }
            "Completed" -> orders.filter { it.status == "COMPLETED" }
            "Cancelled" -> orders.filter { it.status == "CANCELLED" }
            else -> orders
        }
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
                            text = "Orders & Service",
                            color = CafeGold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${orders.size} tickets recorded",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = { showCreateOrderDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CafeGold, contentColor = Color(0xFF1E1712)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Ticket", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filter tabs
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("Active", "All", "Completed", "Cancelled").forEach { filter ->
                        item {
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CafeGold,
                                    selectedLabelColor = Color(0xFF1E1712)
                                )
                            )
                        }
                    }
                }
            }
        },
        containerColor = CafeEspresso
    ) { padding ->
        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Text("No $selectedFilter orders", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
            ) {
                items(filteredOrders, key = { it.id }) { order ->
                    OrderTicketCard(
                        order = order,
                        currency = currency,
                        onClick = { selectedOrderForDetails = order }
                    )
                }
            }
        }
    }

    // Detailed Order Modal with per-item cancellation & advance workflow
    selectedOrderForDetails?.let { order ->
        OrderDetailBottomSheet(
            order = order,
            currency = currency,
            onDismiss = { selectedOrderForDetails = null },
            onAdvanceStatus = { nextStatus ->
                viewModel.updateOrderStatus(order, nextStatus)
                selectedOrderForDetails = null
            },
            onCancelEntireOrder = { reason ->
                viewModel.cancelEntireOrder(order, reason)
                selectedOrderForDetails = null
            },
            onCancelItem = { item, reason ->
                viewModel.cancelOrderItem(order, item, reason)
                selectedOrderForDetails = null
            },
            onUpdateItemStatus = { itemId, newStatus ->
                viewModel.updateOrderItemStatus(order, itemId, newStatus)
            },
            onSettlePayment = { paymentMethod ->
                viewModel.settleOrderPayment(order, paymentMethod)
                selectedOrderForDetails = null
            }
        )
    }

    // Create New Order Dialog
    if (showCreateOrderDialog) {
        CreateOrderDialog(
            tables = tables,
            foodItems = foodItems,
            currency = currency,
            preSelectedTable = preSelectedTable,
            onDismiss = { showCreateOrderDialog = false },
            onSubmitOrder = { table, items, notes ->
                viewModel.createNewOrder(table, items, notes)
                showCreateOrderDialog = false
            }
        )
    }
}

@Composable
private fun OrderTicketCard(
    order: RestaurantOrder,
    currency: String,
    onClick: () -> Unit
) {
    val items = remember(order.itemsJson) {
        RestaurantRepository.parseOrderItems(order.itemsJson)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Ticket #${order.orderNumber}",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .background(CafeGold.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Table ${order.tableNumber}",
                            color = CafeGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                OrderStatusBadge(status = order.status)
            }

            // Summary of items
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items.take(3).forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${item.quantity}× ${item.name} ${if (item.variantName.isNotBlank()) "(${item.variantName})" else ""}",
                            color = if (item.status == "CANCELLED") MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            style = if (item.status == "CANCELLED") androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else androidx.compose.ui.text.TextStyle()
                        )
                        Text(
                            text = "$currency ${String.format(Locale.US, "%.1f", item.totalPrice)}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
                if (items.size > 3) {
                    Text(
                        text = "+ ${items.size - 3} more items",
                        color = CafeGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total ($currency)",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "$currency ${String.format(Locale.US, "%.1f", order.total)}",
                        color = CafeGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = order.paymentStatus,
                        color = if (order.paymentStatus == "PAID") StatusReadyColor else StatusPreparingColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderDetailBottomSheet(
    order: RestaurantOrder,
    currency: String,
    onDismiss: () -> Unit,
    onAdvanceStatus: (String) -> Unit,
    onCancelEntireOrder: (String) -> Unit,
    onCancelItem: (OrderItem, String) -> Unit,
    onUpdateItemStatus: (String, String) -> Unit,
    onSettlePayment: (String) -> Unit
) {
    val items = remember(order.itemsJson) {
        RestaurantRepository.parseOrderItems(order.itemsJson)
    }

    var itemToCancel by remember { mutableStateOf<OrderItem?>(null) }
    var showCancelOrderDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CafeDarkSurfaceElevated,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Order #${order.orderNumber}",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Table ${order.tableNumber} • Server: ${order.serverName.ifBlank { "Staff" }}",
                        color = CafeGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                OrderStatusBadge(status = order.status)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            // Order items list with per-item actions & status
            Text("Order Items & Kitchen Status", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items.forEach { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CafeEspresso),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${item.quantity}× ${item.name} ${if (item.variantName.isNotBlank()) "(${item.variantName})" else ""}",
                                    color = if (item.status == "CANCELLED") MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    style = if (item.status == "CANCELLED") androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else androidx.compose.ui.text.TextStyle()
                                )
                                Text(
                                    text = "$currency ${String.format(Locale.US, "%.1f", item.totalPrice)} • Station: ${item.stationName.ifBlank { "General" }}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )

                                if (item.status == "CANCELLED" && item.cancelReason.isNotBlank()) {
                                    Text(
                                        text = "Cancelled: ${item.cancelReason} by ${item.cancelledBy}",
                                        color = StatusCancelledColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            if (item.status != "CANCELLED" && order.status != "COMPLETED" && order.status != "CANCELLED") {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Individual item status pill
                                    Text(
                                        text = item.status,
                                        color = if (item.status == "READY") StatusReadyColor else StatusPreparingColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .background(
                                                (if (item.status == "READY") StatusReadyColor else StatusPreparingColor).copy(alpha = 0.15f),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    )

                                    // Cancel item button (PRD Section 24)
                                    IconButton(
                                        onClick = { itemToCancel = item },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Cancel item",
                                            tint = StatusCancelledColor.copy(alpha = 0.8f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Financial Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CafeEspresso),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        Text("$currency ${String.format(Locale.US, "%.1f", order.subtotal)}", fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tax", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        Text("$currency ${String.format(Locale.US, "%.1f", order.tax)}", fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Service Charge", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        Text("$currency ${String.format(Locale.US, "%.1f", order.serviceCharge)}", fontSize = 12.sp)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Amount", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                        Text("$currency ${String.format(Locale.US, "%.1f", order.total)}", fontWeight = FontWeight.ExtraBold, color = CafeGold, fontSize = 16.sp)
                    }
                }
            }

            // Lifecycle Progression Action Buttons
            if (order.status != "COMPLETED" && order.status != "CANCELLED") {
                val nextStatus = when (order.status) {
                    "DRAFT" -> "NEW"
                    "NEW" -> "ACCEPTED"
                    "ACCEPTED" -> "PREPARING"
                    "PREPARING" -> "READY"
                    "READY" -> "SERVED"
                    "SERVED" -> "COMPLETED"
                    else -> null
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Cancel entire order button
                    OutlinedButton(
                        onClick = { showCancelOrderDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusCancelledColor)
                    ) {
                        Text("Cancel Ticket", fontSize = 12.sp)
                    }

                    if (nextStatus != null) {
                        Button(
                            onClick = { onAdvanceStatus(nextStatus) },
                            modifier = Modifier.weight(1.5f),
                            colors = ButtonDefaults.buttonColors(containerColor = CafeGold, contentColor = Color(0xFF1E1712))
                        ) {
                            Text("Mark $nextStatus", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    if (order.paymentStatus != "PAID") {
                        Button(
                            onClick = { showPaymentDialog = true },
                            modifier = Modifier.weight(1.2f),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusReadyColor, contentColor = Color.White)
                        ) {
                            Text("Settle Bill", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Cancel Item Reason Dialog (PRD Section 24, 25, 26)
    itemToCancel?.let { item ->
        ItemCancellationDialog(
            item = item,
            onDismiss = { itemToCancel = null },
            onConfirmCancel = { reason ->
                onCancelItem(item, reason)
                itemToCancel = null
            }
        )
    }

    // Cancel Entire Order Dialog (PRD Section 27)
    if (showCancelOrderDialog) {
        OrderCancellationDialog(
            onDismiss = { showCancelOrderDialog = false },
            onConfirmCancel = { reason ->
                onCancelEntireOrder(reason)
                showCancelOrderDialog = false
            }
        )
    }

    // Settle Payment Dialog
    if (showPaymentDialog) {
        PaymentMethodDialog(
            totalAmount = order.total,
            currency = currency,
            onDismiss = { showPaymentDialog = false },
            onConfirmPayment = { method ->
                onSettlePayment(method)
                showPaymentDialog = false
            }
        )
    }
}

@Composable
private fun ItemCancellationDialog(
    item: OrderItem,
    onDismiss: () -> Unit,
    onConfirmCancel: (reason: String) -> Unit
) {
    val reasons = listOf(
        "Wrong item",
        "Duplicate order",
        "Customer changed mind",
        "Out of stock",
        "Staff mistake",
        "Other"
    )
    var selectedReason by remember { mutableStateOf(reasons.first()) }
    val isPreparing = item.status == "PREPARING"

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            if (isPreparing) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = StatusPreparingColor)
            }
        },
        title = {
            Text("Cancel ${item.name}?", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (isPreparing) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StatusPreparingColor.copy(alpha = 0.15f))
                    ) {
                        Text(
                            text = "⚠️ Warning: This item is already being prepared by the kitchen station.",
                            color = StatusPreparingColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Text("Please select a mandatory cancellation reason for audit logging:", fontSize = 12.sp)

                reasons.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RadioButton(selected = selectedReason == reason, onClick = { selectedReason = reason })
                        Text(reason, fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmCancel(selectedReason) },
                colors = ButtonDefaults.buttonColors(containerColor = StatusCancelledColor)
            ) {
                Text("Confirm Cancellation")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Back") }
        }
    )
}

@Composable
private fun OrderCancellationDialog(
    onDismiss: () -> Unit,
    onConfirmCancel: (reason: String) -> Unit
) {
    val reasons = listOf(
        "Customer cancelled",
        "Duplicate ticket",
        "Wrong table assignment",
        "Staff error",
        "Emergency / Walk-out",
        "Other"
    )
    var selectedReason by remember { mutableStateOf(reasons.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cancel Entire Ticket?", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("This will cancel all associated tickets and notify kitchen stations. Select reason:", fontSize = 12.sp)
                reasons.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RadioButton(selected = selectedReason == reason, onClick = { selectedReason = reason })
                        Text(reason, fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmCancel(selectedReason) },
                colors = ButtonDefaults.buttonColors(containerColor = StatusCancelledColor)
            ) {
                Text("Cancel Ticket")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Back") }
        }
    )
}

@Composable
private fun PaymentMethodDialog(
    totalAmount: Double,
    currency: String,
    onDismiss: () -> Unit,
    onConfirmPayment: (String) -> Unit
) {
    val methods = listOf("CASH", "CARD", "QR", "OTHER")
    var selectedMethod by remember { mutableStateOf("CASH") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settle Payment", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Total Due: $currency ${String.format(Locale.US, "%.1f", totalAmount)}",
                    color = CafeGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text("Select payment tender:", fontSize = 12.sp)

                methods.forEach { method ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedMethod = method }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RadioButton(selected = selectedMethod == method, onClick = { selectedMethod = method })
                        Text(method, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmPayment(selectedMethod) },
                colors = ButtonDefaults.buttonColors(containerColor = StatusReadyColor)
            ) {
                Text("Confirm Payment")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun CreateOrderDialog(
    tables: List<TableItem>,
    foodItems: List<FoodItem>,
    currency: String,
    preSelectedTable: TableItem?,
    onDismiss: () -> Unit,
    onSubmitOrder: (table: TableItem, items: List<OrderItem>, notes: String) -> Unit
) {
    var selectedTable by remember { mutableStateOf(preSelectedTable ?: tables.firstOrNull()) }
    val itemQuantities = remember { mutableStateMapOf<String, Int>() }
    var notes by remember { mutableStateOf("") }

    val availableFood = remember(foodItems) { foodItems.filter { it.isAvailable } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Order Ticket", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Table selector
                Text("Select Table *", fontSize = 12.sp, color = CafeGold, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tables) { table ->
                        FilterChip(
                            selected = selectedTable?.id == table.id,
                            onClick = { selectedTable = table },
                            label = { Text("T-${table.tableNumber} (${table.status})", fontSize = 11.sp) }
                        )
                    }
                }

                // Food Items with Quantity steppers
                Text("Select Items *", fontSize = 12.sp, color = CafeGold, fontWeight = FontWeight.Bold)
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableFood) { food ->
                        val qty = itemQuantities[food.id] ?: 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CafeEspresso, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(food.name, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("$currency ${food.price}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (qty > 0) {
                                    IconButton(
                                        onClick = { itemQuantities[food.id] = qty - 1 },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = CafeGold)
                                    }
                                    Text(qty.toString(), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                IconButton(
                                    onClick = { itemQuantities[food.id] = qty + 1 },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase", tint = CafeGold)
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Special Instructions / Kitchen Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            val selectedOrderItems = availableFood.mapNotNull { food ->
                val qty = itemQuantities[food.id] ?: 0
                if (qty > 0) {
                    OrderItem(
                        id = UUID.randomUUID().toString(),
                        foodItemId = food.id,
                        name = food.name,
                        variantName = food.variants.split(",").firstOrNull()?.split(":")?.firstOrNull() ?: "",
                        quantity = qty,
                        unitPrice = food.price,
                        totalPrice = food.price * qty,
                        stationId = food.stationId,
                        stationName = food.stationName,
                        status = "PREPARING"
                    )
                } else null
            }

            Button(
                onClick = {
                    selectedTable?.let { table ->
                        onSubmitOrder(table, selectedOrderItems, notes)
                    }
                },
                enabled = selectedTable != null && selectedOrderItems.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = CafeGold, contentColor = Color(0xFF1E1712))
            ) {
                Text("Place Order")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
