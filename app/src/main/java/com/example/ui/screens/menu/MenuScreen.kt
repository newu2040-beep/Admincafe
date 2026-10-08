package com.example.ui.screens.menu

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
import com.example.data.model.Category
import com.example.data.model.FoodItem
import com.example.data.model.KitchenStation
import com.example.ui.theme.*
import com.example.ui.viewmodel.AdminCafeViewModel
import java.util.Locale

@Composable
fun MenuScreen(
    viewModel: AdminCafeViewModel
) {
    val categories by viewModel.categories.collectAsState()
    val foodItems by viewModel.foodItems.collectAsState()
    val stations by viewModel.kitchenStations.collectAsState()
    val restaurant by viewModel.restaurant.collectAsState()

    val currency = restaurant?.currency ?: "Rs."

    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddFoodDialog by remember { mutableStateOf(false) }
    var editingFoodItem by remember { mutableStateOf<FoodItem?>(null) }

    val filteredItems = remember(foodItems, selectedCategoryId, searchQuery) {
        foodItems.filter { item ->
            (selectedCategoryId == null || item.categoryId == selectedCategoryId) &&
                    (searchQuery.isBlank() || item.name.contains(searchQuery, ignoreCase = true) || item.description.contains(searchQuery, ignoreCase = true))
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
                            text = "Menu Management",
                            color = CafeGold,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${foodItems.size} food items across ${categories.size} categories",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { showAddCategoryDialog = true },
                            modifier = Modifier
                                .background(CafeDarkSurfaceElevated, CircleShape)
                                .border(1.dp, CafeGold.copy(alpha = 0.4f), CircleShape)
                        ) {
                            Icon(Icons.Default.CreateNewFolder, contentDescription = "Add Category", tint = CafeGold)
                        }

                        Button(
                            onClick = {
                                editingFoodItem = null
                                showAddFoodDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CafeGold, contentColor = Color(0xFF1E1712)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Food", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search dishes, variants, ingredients...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CafeGold) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CafeDarkSurfaceElevated,
                        unfocusedContainerColor = CafeDarkSurfaceElevated
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category pill strip
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryId == null,
                            onClick = { selectedCategoryId = null },
                            label = { Text("All (${foodItems.size})", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CafeGold,
                                selectedLabelColor = Color(0xFF1E1712)
                            )
                        )
                    }
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategoryId == category.id,
                            onClick = { selectedCategoryId = category.id },
                            label = { Text(category.name, fontSize = 12.sp) },
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
        if (filteredItems.isEmpty()) {
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
                        imageVector = Icons.Default.RestaurantMenu,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Text("No items found in this section", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(
                        onClick = { showAddFoodDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CafeGold, contentColor = Color(0xFF1E1712))
                    ) {
                        Text("Add First Item")
                    }
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
                items(filteredItems, key = { it.id }) { item ->
                    FoodItemCard(
                        item = item,
                        currency = currency,
                        onToggleAvailability = {
                            viewModel.toggleFoodAvailability(item.id, item.isAvailable)
                        },
                        onEdit = {
                            editingFoodItem = item
                            showAddFoodDialog = true
                        },
                        onDelete = {
                            viewModel.deleteFoodItem(item.id)
                        }
                    )
                }
            }
        }
    }

    // Add / Edit Food Item Dialog
    if (showAddFoodDialog) {
        FoodItemFormDialog(
            initialItem = editingFoodItem,
            categories = categories,
            stations = stations,
            currency = currency,
            onDismiss = { showAddFoodDialog = false },
            onSave = { name, desc, price, prepTime, catId, statId, statName, variants, addons, ingredients ->
                viewModel.addOrUpdateFoodItem(
                    id = editingFoodItem?.id ?: "",
                    categoryId = catId,
                    name = name,
                    description = desc,
                    price = price,
                    prepTimeMinutes = prepTime,
                    stationId = statId,
                    stationName = statName,
                    variants = variants,
                    addons = addons,
                    ingredients = ingredients
                )
                showAddFoodDialog = false
            }
        )
    }

    // Add Category Dialog
    if (showAddCategoryDialog) {
        CategoryFormDialog(
            onDismiss = { showAddCategoryDialog = false },
            onSave = { name, desc, order ->
                viewModel.addOrUpdateCategory(name = name, description = desc, displayOrder = order)
                showAddCategoryDialog = false
            }
        )
    }
}

@Composable
private fun FoodItemCard(
    item: FoodItem,
    currency: String,
    onToggleAvailability: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isAvailable) CafeDarkSurfaceElevated else CafeDarkSurfaceElevated.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = item.name,
                            color = if (item.isAvailable) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (item.stationName.isNotBlank()) {
                            Text(
                                text = "• ${item.stationName}",
                                color = CafeGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (item.description.isNotBlank()) {
                        Text(
                            text = item.description,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            maxLines = 2
                        )
                    }
                }

                // Instant Availability Toggle (PRD Section 14)
                Column(horizontalAlignment = Alignment.End) {
                    Switch(
                        checked = item.isAvailable,
                        onCheckedChange = { onToggleAvailability() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CafeGold,
                            checkedTrackColor = CafeGold.copy(alpha = 0.3f),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.DarkGray
                        )
                    )
                    Text(
                        text = if (item.isAvailable) "Available" else "86'd / Off",
                        color = if (item.isAvailable) StatusReadyColor else StatusCancelledColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Variants & Add-ons tags if present
            if (item.variants.isNotBlank() || item.addons.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (item.variants.isNotBlank()) {
                        Text(
                            text = "Variants: ${item.variants}",
                            color = CafeGoldLight,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .background(CafeEspresso, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (item.addons.isNotBlank()) {
                        Text(
                            text = "Addons: ${item.addons}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .background(CafeEspresso, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "$currency ${String.format(Locale.US, "%.1f", item.price)}",
                        color = CafeGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "⏱ ${item.prepTimeMinutes}m prep",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusCancelledColor.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodItemFormDialog(
    initialItem: FoodItem?,
    categories: List<Category>,
    stations: List<KitchenStation>,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (name: String, desc: String, price: Double, prepTime: Int, catId: String, statId: String, statName: String, variants: String, addons: String, ingredients: String) -> Unit
) {
    var name by remember { mutableStateOf(initialItem?.name ?: "") }
    var desc by remember { mutableStateOf(initialItem?.description ?: "") }
    var priceStr by remember { mutableStateOf(initialItem?.price?.toString() ?: "150") }
    var prepTimeStr by remember { mutableStateOf(initialItem?.prepTimeMinutes?.toString() ?: "12") }
    var selectedCatId by remember { mutableStateOf(initialItem?.categoryId ?: categories.firstOrNull()?.id ?: "") }
    var selectedStatId by remember { mutableStateOf(initialItem?.stationId ?: stations.firstOrNull()?.id ?: "") }
    var variants by remember { mutableStateOf(initialItem?.variants ?: "") }
    var addons by remember { mutableStateOf(initialItem?.addons ?: "") }
    var ingredients by remember { mutableStateOf(initialItem?.ingredients ?: "") }

    val selectedStation = stations.find { it.id == selectedStatId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initialItem == null) "Add Food Item" else "Edit Food Item", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp)
                    .background(Color.Transparent),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name *") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Base Price ($currency) *") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = prepTimeStr,
                        onValueChange = { prepTimeStr = it },
                        label = { Text("Prep (mins)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                // Category selector
                Text("Category *", fontSize = 12.sp, color = CafeGold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCatId == cat.id,
                            onClick = { selectedCatId = cat.id },
                            label = { Text(cat.name, fontSize = 11.sp) }
                        )
                    }
                }

                // Station selector
                Text("Kitchen Station (Routing)", fontSize = 12.sp, color = CafeGold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(stations) { stat ->
                        FilterChip(
                            selected = selectedStatId == stat.id,
                            onClick = { selectedStatId = stat.id },
                            label = { Text(stat.name, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = variants,
                    onValueChange = { variants = it },
                    label = { Text("Variants (e.g. Veg:150, Chicken:180)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = addons,
                    onValueChange = { addons = it },
                    label = { Text("Add-ons (e.g. Extra Cheese:50)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceStr.toDoubleOrNull() ?: 0.0
                    val prepTime = prepTimeStr.toIntOrNull() ?: 10
                    onSave(name, desc, price, prepTime, selectedCatId, selectedStatId, selectedStation?.name ?: "", variants, addons, ingredients)
                },
                enabled = name.isNotBlank() && selectedCatId.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CafeGold, contentColor = Color(0xFF1E1712))
            ) {
                Text("Save Item")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun CategoryFormDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, desc: String, order: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var orderStr by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Food Category", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = orderStr,
                    onValueChange = { orderStr = it },
                    label = { Text("Display Order") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(name, desc, orderStr.toIntOrNull() ?: 1)
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CafeGold, contentColor = Color(0xFF1E1712))
            ) {
                Text("Save Category")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
