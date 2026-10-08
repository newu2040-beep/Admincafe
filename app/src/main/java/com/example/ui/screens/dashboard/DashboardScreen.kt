package com.example.ui.screens.dashboard

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RestaurantOrder
import com.example.ui.components.OrderStatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.AdminCafeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: AdminCafeViewModel,
    onNavigateToMenu: () -> Unit,
    onNavigateToTables: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToKitchen: () -> Unit,
    onOpenOrderDetails: (RestaurantOrder) -> Unit
) {
    val restaurant by viewModel.restaurant.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val tables by viewModel.tables.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var chartRange by remember { mutableStateOf("Today") } // Today, 7 Days, 30 Days

    val currency = restaurant?.currency ?: "Rs."

    // Metrics calculations
    val todayOrders = remember(orders) {
        val todayPrefix = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        orders.filter { it.createdAt.startsWith(todayPrefix) }
    }

    val todaySales = remember(todayOrders) {
        todayOrders.filter { it.status != "CANCELLED" && it.paymentStatus == "PAID" }.sumOf { it.total }
    }

    val activeOrders = remember(orders) {
        orders.filter { it.status in listOf("NEW", "ACCEPTED", "PREPARING", "READY") }
    }

    val occupiedTablesCount = remember(tables) {
        tables.count { it.status == "OCCUPIED" }
    }

    // Kitchen summary
    val kitchenNewCount = remember(orders) { orders.count { it.status == "NEW" } }
    val kitchenCookingCount = remember(orders) { orders.count { it.status in listOf("ACCEPTED", "PREPARING") } }
    val kitchenReadyCount = remember(orders) { orders.count { it.status == "READY" } }

    val recentOrders = remember(orders) { orders.take(5) }

    val currentDateStr = remember {
        SimpleDateFormat("EEE, dd MMM yyyy", Locale.US).format(Date())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CafeEspresso)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
    ) {
        // Executive Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = restaurant?.name ?: "ADMINCAFE",
                        color = CafeGold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "$currentDateStr • ${userProfile?.role ?: "OWNER"}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(CafeDarkSurfaceElevated, CircleShape)
                        .border(1.dp, CafeGold.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Alerts",
                        tint = CafeGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Live KPI Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiStatCard(
                        title = "Today's Sales",
                        value = "$currency ${String.format(Locale.US, "%.1f", todaySales)}",
                        icon = Icons.Default.Payments,
                        accentColor = CafeGold,
                        modifier = Modifier.weight(1f)
                    )
                    KpiStatCard(
                        title = "Today's Orders",
                        value = todayOrders.size.toString(),
                        icon = Icons.Default.Receipt,
                        accentColor = StatusNewColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiStatCard(
                        title = "Active Tickets",
                        value = activeOrders.size.toString(),
                        icon = Icons.Default.PendingActions,
                        accentColor = StatusPreparingColor,
                        modifier = Modifier.weight(1f)
                    )
                    KpiStatCard(
                        title = "Occupied Tables",
                        value = "$occupiedTablesCount / ${tables.size}",
                        icon = Icons.Default.TableBar,
                        accentColor = if (occupiedTablesCount > 0) StatusOccupiedColor else StatusAvailableColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Kitchen Station Summary Strip
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToKitchen() },
                colors = CardDefaults.cardColors(containerColor = CafeDarkSurfaceElevated),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OutdoorGrill,
                                contentDescription = null,
                                tint = CafeGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Kitchen Summary",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Open KDS →",
                            color = CafeGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        KitchenMetricBadge(label = "New Tickets", count = kitchenNewCount, color = StatusNewColor)
                        KitchenMetricBadge(label = "Cooking", count = kitchenCookingCount, color = StatusPreparingColor)
                        KitchenMetricBadge(label = "Ready to Serve", count = kitchenReadyCount, color = StatusReadyColor)
                    }
                }
            }
        }

        // Quick Action Shortcuts
        item {
            Text(
                text = "Quick Operations",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionChip(
                    icon = Icons.Default.AddShoppingCart,
                    label = "New Order",
                    onClick = onNavigateToOrders,
                    modifier = Modifier.weight(1f)
                )
                QuickActionChip(
                    icon = Icons.Default.RestaurantMenu,
                    label = "Menu",
                    onClick = onNavigateToMenu,
                    modifier = Modifier.weight(1f)
                )
                QuickActionChip(
                    icon = Icons.Default.TableRestaurant,
                    label = "Tables",
                    onClick = onNavigateToTables,
                    modifier = Modifier.weight(1f)
                )
                QuickActionChip(
                    icon = Icons.Default.SoupKitchen,
                    label = "Kitchen",
                    onClick = onNavigateToKitchen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Sales Trend Visualizer
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CafeDarkSurfaceElevated),
                shape = RoundedCornerShape(14.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sales Performance",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Segmented buttons
                        Row(
                            modifier = Modifier
                                .background(CafeEspresso, RoundedCornerShape(20.dp))
                                .padding(2.dp)
                        ) {
                            listOf("Today", "7 Days", "30 Days").forEach { range ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (chartRange == range) CafeGold else Color.Transparent)
                                        .clickable { chartRange = range }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = range,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (chartRange == range) Color(0xFF1E1712) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stylized bar chart representation
                    SalesBarChartPreview(range = chartRange, currency = currency)
                }
            }
        }

        // Recent Orders Header & List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Orders",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "View All (${orders.size})",
                    color = CafeGold,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigateToOrders() }
                )
            }
        }

        if (recentOrders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CafeDarkSurfaceElevated),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No orders recorded yet",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(recentOrders) { order ->
                RecentOrderCard(
                    order = order,
                    currency = currency,
                    onClick = { onOpenOrderDetails(order) }
                )
            }
        }
    }
}

@Composable
private fun KpiStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CafeDarkSurfaceElevated),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = value,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun KitchenMetricBadge(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            color = color,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun QuickActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CafeDarkSurfaceElevated),
        shape = RoundedCornerShape(10.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CafeGold,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SalesBarChartPreview(range: String, currency: String) {
    val bars = when (range) {
        "Today" -> listOf(
            "10:00" to 0.2f, "12:00" to 0.7f, "14:00" to 0.9f, "16:00" to 0.4f, "18:00" to 0.85f, "20:00" to 1.0f
        )
        "7 Days" -> listOf(
            "Mon" to 0.5f, "Tue" to 0.65f, "Wed" to 0.45f, "Thu" to 0.7f, "Fri" to 0.95f, "Sat" to 1.0f, "Sun" to 0.8f
        )
        else -> listOf(
            "W1" to 0.7f, "W2" to 0.85f, "W3" to 0.9f, "W4" to 1.0f
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            bars.forEach { (label, fraction) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .fillMaxHeight(fraction)
                            .background(
                                if (fraction >= 0.9f) CafeGold else CafeGold.copy(alpha = 0.4f),
                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            bars.forEach { (label, _) ->
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun RecentOrderCard(
    order: RestaurantOrder,
    currency: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CafeDarkSurfaceElevated),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Order #${order.orderNumber}",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Table ${order.tableNumber}",
                        color = CafeGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "$currency ${String.format(Locale.US, "%.1f", order.total)} • ${order.paymentStatus}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            OrderStatusBadge(status = order.status)
        }
    }
}
