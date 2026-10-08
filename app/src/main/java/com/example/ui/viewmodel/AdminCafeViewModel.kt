package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.RestaurantRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AdminCafeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RestaurantRepository(application.applicationContext)
    private val auth = FirebaseAuth.getInstance()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _restaurantId = MutableStateFlow<String>("")
    val restaurantId: StateFlow<String> = _restaurantId.asStateFlow()

    private val _restaurant = MutableStateFlow<Restaurant?>(null)
    val restaurant: StateFlow<Restaurant?> = _restaurant.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _foodItems = MutableStateFlow<List<FoodItem>>(emptyList())
    val foodItems: StateFlow<List<FoodItem>> = _foodItems.asStateFlow()

    private val _tables = MutableStateFlow<List<TableItem>>(emptyList())
    val tables: StateFlow<List<TableItem>> = _tables.asStateFlow()

    private val _kitchenStations = MutableStateFlow<List<KitchenStation>>(emptyList())
    val kitchenStations: StateFlow<List<KitchenStation>> = _kitchenStations.asStateFlow()

    private val _staff = MutableStateFlow<List<StaffMember>>(emptyList())
    val staff: StateFlow<List<StaffMember>> = _staff.asStateFlow()

    private val _orders = MutableStateFlow<List<RestaurantOrder>>(emptyList())
    val orders: StateFlow<List<RestaurantOrder>> = _orders.asStateFlow()

    private val _inventory = MutableStateFlow<List<InventoryItem>>(emptyList())
    val inventory: StateFlow<List<InventoryItem>> = _inventory.asStateFlow()

    private val _expenses = MutableStateFlow<List<ExpenseItem>>(emptyList())
    val expenses: StateFlow<List<ExpenseItem>> = _expenses.asStateFlow()

    private val _customers = MutableStateFlow<List<CustomerRecord>>(emptyList())
    val customers: StateFlow<List<CustomerRecord>> = _customers.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditLogRecord>>(emptyList())
    val auditLogs: StateFlow<List<AuditLogRecord>> = _auditLogs.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _currentUser.value = user
            if (user != null) {
                loadUserData(user.uid)
            } else {
                _userProfile.value = null
                _restaurant.value = null
                _restaurantId.value = ""
            }
        }
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun setUiMessage(message: String) {
        _uiMessage.value = message
    }

    private fun loadUserData(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val profile = repository.getUserProfile(userId)
            _userProfile.value = profile

            val rId = profile?.restaurantId ?: ""
            _restaurantId.value = rId

            if (rId.isNotBlank()) {
                observeRestaurantData(rId)
            }
            _isLoading.value = false
        }
    }

    fun registerRestaurantAndOwner(
        restaurantName: String,
        ownerName: String,
        email: String,
        phone: String,
        city: String,
        currency: String
    ) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val newRestId = UUID.randomUUID().toString()
            val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())

            val newRestaurant = Restaurant(
                id = newRestId,
                ownerId = user.uid,
                name = restaurantName.ifBlank { "AdminCafe Bistro" },
                phone = phone,
                city = city.ifBlank { "Kathmandu" },
                currency = currency.ifBlank { "Rs." },
                taxRate = 13.0,
                serviceCharge = 10.0,
                setupCompleted = false,
                createdAt = now,
                updatedAt = now
            )

            val profile = UserProfile(
                id = user.uid,
                email = email.ifBlank { user.email ?: "owner@admincafe.apps" },
                displayName = ownerName.ifBlank { user.displayName ?: "Restaurant Owner" },
                restaurantId = newRestId,
                role = "OWNER",
                createdAt = now
            )

            val restResult = repository.saveRestaurant(newRestaurant)
            if (restResult.isSuccess) {
                repository.saveUserProfile(profile)
                _restaurantId.value = newRestId
                _userProfile.value = profile
                _restaurant.value = newRestaurant
                observeRestaurantData(newRestId)
                _uiMessage.value = "Restaurant profile registered successfully!"
            } else {
                _uiMessage.value = "Failed to register restaurant: ${restResult.exceptionOrNull()?.message}"
            }
            _isLoading.value = false
        }
    }

    fun completeSetupAndSeed() {
        val rId = _restaurantId.value
        val ownerName = _userProfile.value?.displayName ?: "Owner"
        if (rId.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            val seedResult = repository.seedInitialRestaurantData(rId, ownerName)
            if (seedResult.isSuccess) {
                _uiMessage.value = "Setup completed! Initial menu, stations, and tables are ready."
            } else {
                _uiMessage.value = "Setup error: ${seedResult.exceptionOrNull()?.message}"
            }
            _isLoading.value = false
        }
    }

    private fun observeRestaurantData(restId: String) {
        viewModelScope.launch {
            repository.observeRestaurant(restId).collect {
                _restaurant.value = it
            }
        }
        viewModelScope.launch {
            repository.observeCategories(restId).collect {
                _categories.value = it
            }
        }
        viewModelScope.launch {
            repository.observeFoodItems(restId).collect {
                _foodItems.value = it
            }
        }
        viewModelScope.launch {
            repository.observeTables(restId).collect {
                _tables.value = it
            }
        }
        viewModelScope.launch {
            repository.observeKitchenStations(restId).collect {
                _kitchenStations.value = it
            }
        }
        viewModelScope.launch {
            repository.observeStaff(restId).collect {
                _staff.value = it
            }
        }
        viewModelScope.launch {
            repository.observeOrders(restId).collect {
                _orders.value = it
            }
        }
        viewModelScope.launch {
            repository.observeInventory(restId).collect {
                _inventory.value = it
            }
        }
        viewModelScope.launch {
            repository.observeExpenses(restId).collect {
                _expenses.value = it
            }
        }
        viewModelScope.launch {
            repository.observeCustomers(restId).collect {
                _customers.value = it
            }
        }
        viewModelScope.launch {
            repository.observeAuditLogs(restId).collect {
                _auditLogs.value = it
            }
        }
    }

    // --- Menu Actions ---
    fun addOrUpdateCategory(id: String = "", name: String, description: String, displayOrder: Int, isActive: Boolean = true) {
        val rId = _restaurantId.value
        if (rId.isBlank() || name.isBlank()) return
        viewModelScope.launch {
            val cat = Category(id = id, restaurantId = rId, name = name, description = description, displayOrder = displayOrder, isActive = isActive)
            repository.saveCategory(rId, cat)
            _uiMessage.value = "Category '$name' saved"
        }
    }

    fun deleteCategory(categoryId: String) {
        val rId = _restaurantId.value
        if (rId.isBlank()) return
        viewModelScope.launch {
            repository.deleteCategory(rId, categoryId)
            _uiMessage.value = "Category deleted"
        }
    }

    fun addOrUpdateFoodItem(
        id: String = "",
        categoryId: String,
        name: String,
        description: String,
        price: Double,
        prepTimeMinutes: Int,
        stationId: String,
        stationName: String,
        variants: String,
        addons: String,
        ingredients: String
    ) {
        val rId = _restaurantId.value
        if (rId.isBlank() || name.isBlank()) return
        viewModelScope.launch {
            val item = FoodItem(
                id = id,
                restaurantId = rId,
                categoryId = categoryId,
                name = name,
                description = description,
                price = price,
                prepTimeMinutes = prepTimeMinutes,
                isAvailable = true,
                stationId = stationId,
                stationName = stationName,
                variants = variants,
                addons = addons,
                ingredients = ingredients
            )
            repository.saveFoodItem(rId, item)
            _uiMessage.value = "Food item '$name' saved"
        }
    }

    fun toggleFoodAvailability(itemId: String, currentStatus: Boolean) {
        val rId = _restaurantId.value
        if (rId.isBlank()) return
        viewModelScope.launch {
            repository.updateFoodItemAvailability(rId, itemId, !currentStatus)
        }
    }

    fun deleteFoodItem(itemId: String) {
        val rId = _restaurantId.value
        if (rId.isBlank()) return
        viewModelScope.launch {
            repository.deleteFoodItem(rId, itemId)
            _uiMessage.value = "Food item removed"
        }
    }

    // --- Table Actions ---
    fun addOrUpdateTable(id: String = "", tableNumber: String, capacity: Int, floor: String) {
        val rId = _restaurantId.value
        if (rId.isBlank() || tableNumber.isBlank()) return
        viewModelScope.launch {
            val table = TableItem(
                id = id,
                restaurantId = rId,
                tableNumber = tableNumber,
                capacity = capacity,
                floor = floor,
                status = "AVAILABLE"
            )
            repository.saveTable(rId, table)
            _uiMessage.value = "Table $tableNumber saved"
        }
    }

    fun updateTableStatus(tableId: String, status: String) {
        val rId = _restaurantId.value
        if (rId.isBlank()) return
        viewModelScope.launch {
            repository.updateTableStatus(rId, tableId, status)
        }
    }

    // --- Kitchen Station Actions ---
    fun addOrUpdateKitchenStation(id: String = "", name: String, description: String) {
        val rId = _restaurantId.value
        if (rId.isBlank() || name.isBlank()) return
        viewModelScope.launch {
            val station = KitchenStation(id = id, restaurantId = rId, name = name, description = description, isActive = true)
            repository.saveKitchenStation(rId, station)
            _uiMessage.value = "Station '$name' saved"
        }
    }

    // --- Staff Actions ---
    fun addOrUpdateStaff(id: String = "", name: String, phone: String, email: String, role: String, assignedStation: String) {
        val rId = _restaurantId.value
        if (rId.isBlank() || name.isBlank()) return
        viewModelScope.launch {
            val member = StaffMember(id = id, restaurantId = rId, name = name, phone = phone, email = email, role = role, status = "ACTIVE", assignedStation = assignedStation)
            repository.saveStaff(rId, member)
            _uiMessage.value = "Staff '$name' saved"
        }
    }

    fun updateStaffStatus(staffId: String, status: String) {
        val rId = _restaurantId.value
        if (rId.isBlank()) return
        viewModelScope.launch {
            repository.updateStaffStatus(rId, staffId, status)
        }
    }

    // --- Orders Actions ---
    fun createNewOrder(table: TableItem, items: List<OrderItem>, notes: String) {
        val rId = _restaurantId.value
        val rest = _restaurant.value
        val user = _userProfile.value?.displayName ?: "Staff"
        if (rId.isBlank() || items.isEmpty()) return

        val subtotal = items.sumOf { it.totalPrice }
        val taxRate = (rest?.taxRate ?: 13.0) / 100.0
        val serviceRate = (rest?.serviceCharge ?: 10.0) / 100.0
        val tax = subtotal * taxRate
        val serviceCharge = subtotal * serviceRate
        val total = subtotal + tax + serviceCharge

        val order = RestaurantOrder(
            restaurantId = rId,
            tableId = table.id,
            tableNumber = table.tableNumber,
            status = "NEW",
            paymentStatus = "PENDING",
            subtotal = subtotal,
            discount = 0.0,
            tax = tax,
            serviceCharge = serviceCharge,
            total = total,
            itemsJson = RestaurantRepository.serializeOrderItems(items),
            serverName = user,
            notes = notes
        )

        viewModelScope.launch {
            repository.createOrder(rId, order, user)
            _uiMessage.value = "Order placed for Table ${table.tableNumber}"
        }
    }

    fun updateOrderStatus(order: RestaurantOrder, newStatus: String) {
        val rId = _restaurantId.value
        val user = _userProfile.value?.displayName ?: "Staff"
        if (rId.isBlank()) return
        viewModelScope.launch {
            repository.updateOrderStatus(rId, order.id, newStatus, user, order.status)
        }
    }

    fun cancelEntireOrder(order: RestaurantOrder, reason: String) {
        val rId = _restaurantId.value
        val user = _userProfile.value?.displayName ?: "Staff"
        if (rId.isBlank()) return
        viewModelScope.launch {
            repository.cancelEntireOrder(rId, order.id, order.orderNumber, reason, user, order.tableId)
            _uiMessage.value = "Order #${order.orderNumber} cancelled"
        }
    }

    fun cancelOrderItem(order: RestaurantOrder, item: OrderItem, reason: String) {
        val rId = _restaurantId.value
        val user = _userProfile.value?.displayName ?: "Staff"
        if (rId.isBlank()) return
        viewModelScope.launch {
            repository.cancelOrderItem(
                restaurantId = rId,
                orderId = order.id,
                orderNumber = order.orderNumber,
                itemId = item.id,
                itemName = item.name,
                reason = reason,
                userName = user,
                currentItemsJson = order.itemsJson
            )
            _uiMessage.value = "Item '${item.name}' cancelled ($reason)"
        }
    }

    fun updateOrderItemStatus(order: RestaurantOrder, itemId: String, newStatus: String) {
        val rId = _restaurantId.value
        if (rId.isBlank()) return
        viewModelScope.launch {
            repository.updateOrderItemStatus(rId, order.id, itemId, newStatus, order.itemsJson)
        }
    }

    fun settleOrderPayment(order: RestaurantOrder, paymentMethod: String) {
        val rId = _restaurantId.value
        val user = _userProfile.value?.displayName ?: "Cashier"
        if (rId.isBlank()) return
        viewModelScope.launch {
            repository.settlePayment(rId, order.id, paymentMethod, user, order.tableId)
            _uiMessage.value = "Order #${order.orderNumber} settled with $paymentMethod"
        }
    }

    // --- Inventory & Expense Actions ---
    fun addOrUpdateInventory(id: String = "", name: String, stock: Double, unit: String, minStock: Double, supplier: String, cost: Double) {
        val rId = _restaurantId.value
        if (rId.isBlank() || name.isBlank()) return
        viewModelScope.launch {
            val item = InventoryItem(id = id, restaurantId = rId, ingredientName = name, currentStock = stock, unit = unit, minStock = minStock, supplier = supplier, costPerUnit = cost)
            repository.saveInventoryItem(rId, item)
            _uiMessage.value = "Inventory item '$name' updated"
        }
    }

    fun addExpense(amount: Double, category: String, description: String, date: String) {
        val rId = _restaurantId.value
        val user = _userProfile.value?.displayName ?: "Owner"
        if (rId.isBlank() || amount <= 0.0) return
        viewModelScope.launch {
            val expense = ExpenseItem(restaurantId = rId, amount = amount, category = category, description = description, date = date, addedBy = user)
            repository.saveExpense(rId, expense)
            _uiMessage.value = "Expense of Rs. $amount logged"
        }
    }

    fun addCustomer(name: String, phone: String, email: String) {
        val rId = _restaurantId.value
        if (rId.isBlank() || name.isBlank()) return
        viewModelScope.launch {
            val customer = CustomerRecord(restaurantId = rId, name = name, phone = phone, email = email)
            repository.saveCustomer(rId, customer)
            _uiMessage.value = "Customer '$name' added"
        }
    }

    fun updateRestaurantSettings(name: String, phone: String, address: String, city: String, currency: String, taxRate: Double, serviceCharge: Double) {
        val curr = _restaurant.value ?: return
        val rId = _restaurantId.value
        viewModelScope.launch {
            val updated = curr.copy(
                name = name,
                phone = phone,
                address = address,
                city = city,
                currency = currency,
                taxRate = taxRate,
                serviceCharge = serviceCharge,
                updatedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
            )
            repository.saveRestaurant(updated)
            _uiMessage.value = "Settings updated"
        }
    }
}
