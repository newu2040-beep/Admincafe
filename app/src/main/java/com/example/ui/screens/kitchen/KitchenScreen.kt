package com.example.ui.screens.kitchen

import androidx.compose.foundation.BorderStroke
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
import com.example.data.model.OrderItem
import com.example.data.model.RestaurantOrder
import com.example.data.repository.RestaurantRepository
import com.example.ui.theme.*
import com.example.ui.viewmodel.AdminCafeViewModel

@Composable
fun KitchenScreen(
    viewModel: AdminCafeViewModel
) {
    val orders by viewModel.orders.collectAsState()
    val stations by viewModel.kitchenStations.collectAsState()

    var selectedStationId by remember { mutableStateOf<String?>(null) } // null = All Stations

    // Filter active orders that have kitchen items
    val activeOrders = remember(orders) {
        orders.filter { it.status in listOf("NEW", "ACCEPTED", "PREPARING") }
    }

    // Check for recent cancellations to show kitchen notification banner (PRD Section 25)
    val cancelledItemsAlerts = remember(orders) {
        val alerts = mutableListOf<Triple<String, String, OrderItem>>() // orderNumber, tableNumber, item
        orders.take(10).forEach { order ->
            val parsed = RestaurantRepository.parseOrderItems(order.itemsJson)
            parsed.filter { it.status == "CANCELLED" && it.cancelledAt.isNotBlank() }.forEach { cancelledItem ->
                alerts.add(Triple(order.orderNumber, order.tableNumber, cancelledItem))
            }
        }
        alerts.take(3)
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
                            text = "Kitchen Display (KDS)",
                            color = CafeGold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${activeOrders.size} active tickets on line",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(StatusPreparingColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, StatusPreparingColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(modifier = Modifier.size(8.dp).background(StatusPreparingColor, CircleShape))
                            Text("LIVE KDS", color = StatusPreparingColor, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Station Filter Strip
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedStationId == null,
                            onClick = { selectedStationId = null },
                            label = { Text("All Stations", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CafeGold,
                                selectedLabelColor = Color(0xFF1E1712)
                            )
                        )
                    }
                    items(stations) { station ->
                        FilterChip(
                            selected = selectedStationId == station.id,
                            onClick = { selectedStationId = station.id },
                            label = { Text(station.name, fontSize = 12.sp) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
        ) {
            // Kitchen Cancellation Warning Banner (PRD Section 25)
            if (cancelledItemsAlerts.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        cancelledItemsAlerts.forEach { (orderNum, tableNum, item) ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = StatusCancelledColor.copy(alpha = 0.15f)),
                                border = BorderStroke(1.dp, StatusCancelledColor),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = StatusCancelledColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "⚠️ DO NOT COOK: ${item.quantity}× ${item.name} cancelled!",
                                            color = StatusCancelledColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "Order #$orderNum (Table $tableNum) • Reason: ${item.cancelReason.ifBlank { "Misorder" }}",
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (activeOrders.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusReadyColor,
                                modifier = Modifier.size(48.dp)
                            )
                            Text("Kitchen line is all clear!", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Text("No pending tickets to cook.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(activeOrders, key = { it.id }) { order ->
                    val allItems = remember(order.itemsJson) {
                        RestaurantRepository.parseOrderItems(order.itemsJson)
                    }
                    val visibleItems = remember(allItems, selectedStationId) {
                        if (selectedStationId == null) allItems
                        else allItems.filter { it.stationId == selectedStationId || it.stationId.isBlank() }
                    }

                    if (visibleItems.isNotEmpty()) {
                        KitchenTicketCard(
                            order = order,
                            items = visibleItems,
                            onUpdateItemStatus = { itemId, newStatus ->
                                viewModel.updateOrderItemStatus(order, itemId, newStatus)
                            },
                            onMarkAllReady = {
                                viewModel.updateOrderStatus(order, "READY")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KitchenTicketCard(
    order: RestaurantOrder,
    items: List<OrderItem>,
    onUpdateItemStatus: (itemId: String, newStatus: String) -> Unit,
    onMarkAllReady: () -> Unit
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Ticket #${order.orderNumber}",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Box(
                        modifier = Modifier
                            .background(CafeGold, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "TABLE ${order.tableNumber}",
                            color = Color(0xFF1E1712),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Text(
                    text = order.status,
                    color = if (order.status == "READY") StatusReadyColor else StatusPreparingColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (order.notes.isNotBlank()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CafeEspresso),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Note: ${order.notes}",
                        color = CafeGoldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            // Items line
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CafeEspresso, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${item.quantity}× ${item.name} ${if (item.variantName.isNotBlank()) "(${item.variantName})" else ""}",
                                color = if (item.status == "CANCELLED") StatusCancelledColor else MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                style = if (item.status == "CANCELLED") androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else androidx.compose.ui.text.TextStyle()
                            )
                            Text(
                                text = "Station: ${item.stationName.ifBlank { "General" }} • Status: ${item.status}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }

                        if (item.status != "CANCELLED") {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (item.status != "READY") {
                                    Button(
                                        onClick = { onUpdateItemStatus(item.id, "READY") },
                                        colors = ButtonDefaults.buttonColors(containerColor = StatusReadyColor, contentColor = Color.White),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Ready", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Icon(Icons.Default.Check, contentDescription = "Ready", tint = StatusReadyColor)
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onMarkAllReady,
                    colors = ButtonDefaults.buttonColors(containerColor = StatusReadyColor, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mark Whole Ticket Ready", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
