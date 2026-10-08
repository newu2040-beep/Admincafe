package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.*
import com.example.data.util.OperationType
import com.example.data.util.handleFirestoreError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class RestaurantRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be authenticated before performing database operations.")
    }

    private fun nowIso(): String {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
    }

    // --- User Profile ---
    suspend fun getUserProfile(userId: String): UserProfile? {
        val path = "users/$userId"
        return try {
            val doc = db.collection("users").document(userId).get().await()
            doc.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            null
        }
    }

    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        val path = "users/${profile.id}"
        return try {
            db.collection("users").document(profile.id).set(profile).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    // --- Restaurant Profile ---
    fun observeRestaurant(restaurantId: String): Flow<Restaurant?> = callbackFlow {
        if (restaurantId.isBlank()) {
            trySend(null)
            close()
            return@callbackFlow
        }
        val path = "restaurants/$restaurantId"
        val registration = db.collection("restaurants").document(restaurantId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.GET, path)
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(Restaurant::class.java))
            }
        awaitClose { registration.remove() }
    }

    suspend fun saveRestaurant(restaurant: Restaurant): Result<Unit> {
        val path = "restaurants/${restaurant.id}"
        return try {
            db.collection("restaurants").document(restaurant.id).set(restaurant).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    // --- Categories ---
    fun observeCategories(restaurantId: String): Flow<List<Category>> = callbackFlow {
        if (restaurantId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val path = "restaurants/$restaurantId/categories"
        val registration = db.collection("restaurants").document(restaurantId)
            .collection("categories")
            .orderBy("displayOrder")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(Category::class.java) ?: emptyList()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    suspend fun saveCategory(restaurantId: String, category: Category): Result<Unit> {
        val path = "restaurants/$restaurantId/categories/${category.id}"
        return try {
            val item = if (category.id.isBlank()) category.copy(id = UUID.randomUUID().toString(), restaurantId = restaurantId, createdAt = nowIso()) else category.copy(updatedAt = nowIso())
            db.collection("restaurants").document(restaurantId).collection("categories").document(item.id).set(item).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(restaurantId: String, categoryId: String): Result<Unit> {
        val path = "restaurants/$restaurantId/categories/$categoryId"
        return try {
            db.collection("restaurants").document(restaurantId).collection("categories").document(categoryId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    // --- Food Items ---
    fun observeFoodItems(restaurantId: String): Flow<List<FoodItem>> = callbackFlow {
        if (restaurantId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val path = "restaurants/$restaurantId/foodItems"
        val registration = db.collection("restaurants").document(restaurantId)
            .collection("foodItems")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(FoodItem::class.java) ?: emptyList()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    suspend fun saveFoodItem(restaurantId: String, item: FoodItem): Result<Unit> {
        val path = "restaurants/$restaurantId/foodItems/${item.id}"
        return try {
            val toSave = if (item.id.isBlank()) item.copy(id = UUID.randomUUID().toString(), restaurantId = restaurantId, createdAt = nowIso()) else item.copy(updatedAt = nowIso())
            db.collection("restaurants").document(restaurantId).collection("foodItems").document(toSave.id).set(toSave).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun updateFoodItemAvailability(restaurantId: String, itemId: String, isAvailable: Boolean): Result<Unit> {
        val path = "restaurants/$restaurantId/foodItems/$itemId"
        return try {
            db.collection("restaurants").document(restaurantId).collection("foodItems").document(itemId)
                .update("isAvailable", isAvailable, "updatedAt", nowIso()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteFoodItem(restaurantId: String, itemId: String): Result<Unit> {
        val path = "restaurants/$restaurantId/foodItems/$itemId"
        return try {
            db.collection("restaurants").document(restaurantId).collection("foodItems").document(itemId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    // --- Tables ---
    fun observeTables(restaurantId: String): Flow<List<TableItem>> = callbackFlow {
        if (restaurantId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val path = "restaurants/$restaurantId/tables"
        val registration = db.collection("restaurants").document(restaurantId)
            .collection("tables")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(TableItem::class.java) ?: emptyList()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    suspend fun saveTable(restaurantId: String, table: TableItem): Result<Unit> {
        val path = "restaurants/$restaurantId/tables/${table.id}"
        return try {
            val toSave = if (table.id.isBlank()) {
                val newId = UUID.randomUUID().toString()
                table.copy(
                    id = newId,
                    restaurantId = restaurantId,
                    qrCode = "ADMINCAFE-QR:$restaurantId:$newId:${table.tableNumber}",
                    createdAt = nowIso()
                )
            } else {
                table.copy(updatedAt = nowIso())
            }
            db.collection("restaurants").document(restaurantId).collection("tables").document(toSave.id).set(toSave).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun updateTableStatus(restaurantId: String, tableId: String, status: String): Result<Unit> {
        val path = "restaurants/$restaurantId/tables/$tableId"
        return try {
            db.collection("restaurants").document(restaurantId).collection("tables").document(tableId)
                .update("status", status, "updatedAt", nowIso()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- Kitchen Stations ---
    fun observeKitchenStations(restaurantId: String): Flow<List<KitchenStation>> = callbackFlow {
        if (restaurantId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val path = "restaurants/$restaurantId/kitchenStations"
        val registration = db.collection("restaurants").document(restaurantId)
            .collection("kitchenStations")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(KitchenStation::class.java) ?: emptyList()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    suspend fun saveKitchenStation(restaurantId: String, station: KitchenStation): Result<Unit> {
        val path = "restaurants/$restaurantId/kitchenStations/${station.id}"
        return try {
            val toSave = if (station.id.isBlank()) station.copy(id = UUID.randomUUID().toString(), restaurantId = restaurantId, createdAt = nowIso()) else station.copy(updatedAt = nowIso())
            db.collection("restaurants").document(restaurantId).collection("kitchenStations").document(toSave.id).set(toSave).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    // --- Staff ---
    fun observeStaff(restaurantId: String): Flow<List<StaffMember>> = callbackFlow {
        if (restaurantId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val path = "restaurants/$restaurantId/staff"
        val registration = db.collection("restaurants").document(restaurantId)
            .collection("staff")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(StaffMember::class.java) ?: emptyList()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    suspend fun saveStaff(restaurantId: String, staff: StaffMember): Result<Unit> {
        val path = "restaurants/$restaurantId/staff/${staff.id}"
        return try {
            val toSave = if (staff.id.isBlank()) staff.copy(id = UUID.randomUUID().toString(), restaurantId = restaurantId, createdAt = nowIso()) else staff.copy(updatedAt = nowIso())
            db.collection("restaurants").document(restaurantId).collection("staff").document(toSave.id).set(toSave).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun updateStaffStatus(restaurantId: String, staffId: String, status: String): Result<Unit> {
        val path = "restaurants/$restaurantId/staff/$staffId"
        return try {
            db.collection("restaurants").document(restaurantId).collection("staff").document(staffId)
                .update("status", status, "updatedAt", nowIso()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- Orders & Realtime Lifecycle ---
    fun observeOrders(restaurantId: String): Flow<List<RestaurantOrder>> = callbackFlow {
        if (restaurantId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val path = "restaurants/$restaurantId/orders"
        val registration = db.collection("restaurants").document(restaurantId)
            .collection("orders")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(RestaurantOrder::class.java) ?: emptyList()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    suspend fun createOrder(restaurantId: String, order: RestaurantOrder, userName: String): Result<RestaurantOrder> {
        val path = "restaurants/$restaurantId/orders/${order.id}"
        return try {
            val newId = if (order.id.isBlank()) UUID.randomUUID().toString() else order.id
            val orderNum = if (order.orderNumber.isBlank()) (1000 + (System.currentTimeMillis() % 9000)).toString() else order.orderNumber
            val toSave = order.copy(
                id = newId,
                orderNumber = orderNum,
                restaurantId = restaurantId,
                createdAt = if (order.createdAt.isBlank()) nowIso() else order.createdAt,
                updatedAt = nowIso()
            )
            db.collection("restaurants").document(restaurantId).collection("orders").document(newId).set(toSave).await()

            // Also update table to OCCUPIED if tableId present
            if (order.tableId.isNotBlank()) {
                db.collection("restaurants").document(restaurantId).collection("tables").document(order.tableId)
                    .update("status", "OCCUPIED", "currentOrderId", newId, "updatedAt", nowIso()).await()
            }

            // Log audit
            recordAuditLog(
                restaurantId = restaurantId,
                action = "ORDER_CREATED",
                user = userName,
                previousState = "NONE",
                newState = toSave.status,
                reason = "New order placed for Table ${toSave.tableNumber}",
                details = "Order #${toSave.orderNumber}, Total: ${toSave.total}"
            )

            Result.success(toSave)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(
        restaurantId: String,
        orderId: String,
        newStatus: String,
        userName: String,
        previousStatus: String
    ): Result<Unit> {
        val path = "restaurants/$restaurantId/orders/$orderId"
        return try {
            db.collection("restaurants").document(restaurantId).collection("orders").document(orderId)
                .update("status", newStatus, "updatedAt", nowIso()).await()

            recordAuditLog(
                restaurantId = restaurantId,
                action = "ORDER_STATUS_CHANGED",
                user = userName,
                previousState = previousStatus,
                newState = newStatus,
                reason = "Order status updated to $newStatus",
                details = "Order ID: $orderId"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun settlePayment(
        restaurantId: String,
        orderId: String,
        paymentMethod: String,
        userName: String,
        tableId: String? = null
    ): Result<Unit> {
        val path = "restaurants/$restaurantId/orders/$orderId"
        return try {
            db.collection("restaurants").document(restaurantId).collection("orders").document(orderId)
                .update(
                    "paymentStatus", "PAID",
                    "paymentMethod", paymentMethod,
                    "status", "COMPLETED",
                    "updatedAt", nowIso()
                ).await()

            if (!tableId.isNullOrBlank()) {
                db.collection("restaurants").document(restaurantId).collection("tables").document(tableId)
                    .update("status", "AVAILABLE", "currentOrderId", "", "updatedAt", nowIso()).await()
            }

            recordAuditLog(
                restaurantId = restaurantId,
                action = "PAYMENT_SETTLED",
                user = userName,
                previousState = "PENDING",
                newState = "PAID",
                reason = "Settled via $paymentMethod",
                details = "Order ID: $orderId"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun cancelEntireOrder(
        restaurantId: String,
        orderId: String,
        orderNumber: String,
        reason: String,
        userName: String,
        tableId: String? = null
    ): Result<Unit> {
        val path = "restaurants/$restaurantId/orders/$orderId"
        return try {
            db.collection("restaurants").document(restaurantId).collection("orders").document(orderId)
                .update(
                    "status", "CANCELLED",
                    "cancelReason", reason,
                    "cancelledBy", userName,
                    "updatedAt", nowIso()
                ).await()

            if (!tableId.isNullOrBlank()) {
                db.collection("restaurants").document(restaurantId).collection("tables").document(tableId)
                    .update("status", "AVAILABLE", "currentOrderId", "", "updatedAt", nowIso()).await()
            }

            recordAuditLog(
                restaurantId = restaurantId,
                action = "ORDER_CANCELLED",
                user = userName,
                previousState = "ACTIVE",
                newState = "CANCELLED",
                reason = reason,
                details = "Order #$orderNumber cancelled"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // Cancel specific item in order with mandatory reason
    suspend fun cancelOrderItem(
        restaurantId: String,
        orderId: String,
        orderNumber: String,
        itemId: String,
        itemName: String,
        reason: String,
        userName: String,
        currentItemsJson: String
    ): Result<Unit> {
        val path = "restaurants/$restaurantId/orders/$orderId"
        return try {
            val jsonArray = JSONArray(currentItemsJson)
            val updatedArray = JSONArray()
            var subtotalDeduction = 0.0

            for (i in 0 until jsonArray.length()) {
                val itemObj = jsonArray.getJSONObject(i)
                if (itemObj.optString("id") == itemId) {
                    itemObj.put("status", "CANCELLED")
                    itemObj.put("cancelReason", reason)
                    itemObj.put("cancelledBy", userName)
                    itemObj.put("cancelledAt", nowIso())
                    subtotalDeduction = itemObj.optDouble("totalPrice", 0.0)
                }
                updatedArray.put(itemObj)
            }

            val doc = db.collection("restaurants").document(restaurantId).collection("orders").document(orderId).get().await()
            val currentOrder = doc.toObject(RestaurantOrder::class.java)
            val newSubtotal = maxOf(0.0, (currentOrder?.subtotal ?: 0.0) - subtotalDeduction)
            val taxRate = (currentOrder?.tax ?: 0.0) / maxOf(1.0, currentOrder?.subtotal ?: 1.0)
            val newTax = newSubtotal * taxRate
            val newTotal = newSubtotal + newTax + (currentOrder?.serviceCharge ?: 0.0)

            db.collection("restaurants").document(restaurantId).collection("orders").document(orderId)
                .update(
                    "itemsJson", updatedArray.toString(),
                    "subtotal", newSubtotal,
                    "total", newTotal,
                    "updatedAt", nowIso()
                ).await()

            recordAuditLog(
                restaurantId = restaurantId,
                action = "ORDER_ITEM_CANCELLED",
                user = userName,
                previousState = "ACTIVE_ITEM",
                newState = "CANCELLED",
                reason = reason,
                details = "Item: $itemName from Order #$orderNumber"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // Update single item status in kitchen (e.g. READY or SERVED)
    suspend fun updateOrderItemStatus(
        restaurantId: String,
        orderId: String,
        itemId: String,
        newStatus: String,
        currentItemsJson: String
    ): Result<Unit> {
        val path = "restaurants/$restaurantId/orders/$orderId"
        return try {
            val jsonArray = JSONArray(currentItemsJson)
            val updatedArray = JSONArray()
            for (i in 0 until jsonArray.length()) {
                val itemObj = jsonArray.getJSONObject(i)
                if (itemObj.optString("id") == itemId) {
                    itemObj.put("status", newStatus)
                }
                updatedArray.put(itemObj)
            }
            db.collection("restaurants").document(restaurantId).collection("orders").document(orderId)
                .update("itemsJson", updatedArray.toString(), "updatedAt", nowIso()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    // --- Inventory ---
    fun observeInventory(restaurantId: String): Flow<List<InventoryItem>> = callbackFlow {
        if (restaurantId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val path = "restaurants/$restaurantId/inventory"
        val registration = db.collection("restaurants").document(restaurantId)
            .collection("inventory")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(InventoryItem::class.java) ?: emptyList()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    suspend fun saveInventoryItem(restaurantId: String, item: InventoryItem): Result<Unit> {
        val path = "restaurants/$restaurantId/inventory/${item.id}"
        return try {
            val status = if (item.currentStock <= item.minStock) "LOW_STOCK" else "GOOD"
            val toSave = if (item.id.isBlank()) item.copy(id = UUID.randomUUID().toString(), restaurantId = restaurantId, status = status, updatedAt = nowIso())
            else item.copy(status = status, updatedAt = nowIso())
            db.collection("restaurants").document(restaurantId).collection("inventory").document(toSave.id).set(toSave).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    // --- Expenses ---
    fun observeExpenses(restaurantId: String): Flow<List<ExpenseItem>> = callbackFlow {
        if (restaurantId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val path = "restaurants/$restaurantId/expenses"
        val registration = db.collection("restaurants").document(restaurantId)
            .collection("expenses")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(ExpenseItem::class.java) ?: emptyList()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    suspend fun saveExpense(restaurantId: String, expense: ExpenseItem): Result<Unit> {
        val path = "restaurants/$restaurantId/expenses/${expense.id}"
        return try {
            val toSave = if (expense.id.isBlank()) expense.copy(id = UUID.randomUUID().toString(), restaurantId = restaurantId, createdAt = nowIso()) else expense
            db.collection("restaurants").document(restaurantId).collection("expenses").document(toSave.id).set(toSave).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    // --- Customers ---
    fun observeCustomers(restaurantId: String): Flow<List<CustomerRecord>> = callbackFlow {
        if (restaurantId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val path = "restaurants/$restaurantId/customers"
        val registration = db.collection("restaurants").document(restaurantId)
            .collection("customers")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(CustomerRecord::class.java) ?: emptyList()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    suspend fun saveCustomer(restaurantId: String, customer: CustomerRecord): Result<Unit> {
        val path = "restaurants/$restaurantId/customers/${customer.id}"
        return try {
            val toSave = if (customer.id.isBlank()) customer.copy(id = UUID.randomUUID().toString(), restaurantId = restaurantId, createdAt = nowIso()) else customer
            db.collection("restaurants").document(restaurantId).collection("customers").document(toSave.id).set(toSave).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    // --- Audit Logs ---
    fun observeAuditLogs(restaurantId: String): Flow<List<AuditLogRecord>> = callbackFlow {
        if (restaurantId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val path = "restaurants/$restaurantId/auditLogs"
        val registration = db.collection("restaurants").document(restaurantId)
            .collection("auditLogs")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    handleFirestoreError(error, OperationType.LIST, path)
                    close(error)
                    return@addSnapshotListener
                }
                val items = snapshot?.toObjects(AuditLogRecord::class.java) ?: emptyList()
                trySend(items)
            }
        awaitClose { registration.remove() }
    }

    suspend fun recordAuditLog(
        restaurantId: String,
        action: String,
        user: String,
        previousState: String,
        newState: String,
        reason: String,
        details: String
    ): Result<Unit> {
        val logId = UUID.randomUUID().toString()
        val path = "restaurants/$restaurantId/auditLogs/$logId"
        return try {
            val record = AuditLogRecord(
                id = logId,
                restaurantId = restaurantId,
                action = action,
                user = user,
                previousState = previousState,
                newState = newState,
                reason = reason,
                details = details,
                createdAt = nowIso()
            )
            db.collection("restaurants").document(restaurantId).collection("auditLogs").document(logId).set(record).await()
            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    // --- Initial Restaurant Setup Seeding ---
    suspend fun seedInitialRestaurantData(restaurantId: String, ownerName: String): Result<Unit> {
        return try {
            // Categories
            val momoCatId = UUID.randomUUID().toString()
            val grillCatId = UUID.randomUUID().toString()
            val drinksCatId = UUID.randomUUID().toString()

            val catMomo = Category(id = momoCatId, restaurantId = restaurantId, name = "Momos & Dumplings", description = "Steamed, fried & kothey momos", displayOrder = 1, isActive = true, createdAt = nowIso())
            val catGrill = Category(id = grillCatId, restaurantId = restaurantId, name = "Grill & BBQ", description = "Freshly charcoal grilled meats & veggies", displayOrder = 2, isActive = true, createdAt = nowIso())
            val catDrinks = Category(id = drinksCatId, restaurantId = restaurantId, name = "Beverages & Coffee", description = "Cold brew, artisan teas and soft drinks", displayOrder = 3, isActive = true, createdAt = nowIso())

            saveCategory(restaurantId, catMomo)
            saveCategory(restaurantId, catGrill)
            saveCategory(restaurantId, catDrinks)

            // Stations
            val momoStationId = UUID.randomUUID().toString()
            val grillStationId = UUID.randomUUID().toString()
            val barStationId = UUID.randomUUID().toString()

            val stationMomo = KitchenStation(id = momoStationId, restaurantId = restaurantId, name = "Momo Station", description = "Dedicated steaming and prep counter", isActive = true, createdAt = nowIso())
            val stationGrill = KitchenStation(id = grillStationId, restaurantId = restaurantId, name = "Grill Counter", description = "Charcoal grill and BBQ skewers", isActive = true, createdAt = nowIso())
            val stationBar = KitchenStation(id = barStationId, restaurantId = restaurantId, name = "Bar & Drinks", description = "Beverages, juices, and coffee bar", isActive = true, createdAt = nowIso())

            saveKitchenStation(restaurantId, stationMomo)
            saveKitchenStation(restaurantId, stationGrill)
            saveKitchenStation(restaurantId, stationBar)

            // Food Items
            val momoItem = FoodItem(
                id = UUID.randomUUID().toString(),
                restaurantId = restaurantId,
                categoryId = momoCatId,
                name = "Signature Chicken Momo",
                description = "Handmade juicy minced chicken dumplings with spicy sesame chutney",
                price = 180.0,
                prepTimeMinutes = 12,
                isAvailable = true,
                stationId = momoStationId,
                stationName = "Momo Station",
                variants = "Steamed:180,Fried:200,Kothey:210",
                addons = "Extra Spicy Dip:30,Extra Momo Sauce:20",
                ingredients = "Minced chicken, onion, scallions, ginger, homemade spices",
                createdAt = nowIso()
            )
            val sekuwaItem = FoodItem(
                id = UUID.randomUUID().toString(),
                restaurantId = restaurantId,
                categoryId = grillCatId,
                name = "Charcoal Chicken Sekuwa",
                description = "Traditional Nepali marinated chicken skewers grilled over natural wood charcoal",
                price = 260.0,
                prepTimeMinutes = 18,
                isAvailable = true,
                stationId = grillStationId,
                stationName = "Grill Counter",
                variants = "Spicy:260,Mild Herb:260",
                addons = "Bhatmas Sadeko:60,Green Chutney:30",
                ingredients = "Chicken boneless, mustard oil, cumin, coriander, fenugreek",
                createdAt = nowIso()
            )
            val coffeeItem = FoodItem(
                id = UUID.randomUUID().toString(),
                restaurantId = restaurantId,
                categoryId = drinksCatId,
                name = "Iced Caramel Cold Brew",
                description = "Single-origin Arabica cold brew with artisan caramel and oat milk",
                price = 150.0,
                prepTimeMinutes = 5,
                isAvailable = true,
                stationId = barStationId,
                stationName = "Bar & Drinks",
                variants = "Regular:150,Large:180",
                addons = "Extra Shot:50,Whipped Cream:30",
                ingredients = "Arabica coffee beans, caramel syrup, milk",
                createdAt = nowIso()
            )

            saveFoodItem(restaurantId, momoItem)
            saveFoodItem(restaurantId, sekuwaItem)
            saveFoodItem(restaurantId, coffeeItem)

            // Tables
            val t1 = TableItem(id = UUID.randomUUID().toString(), restaurantId = restaurantId, tableNumber = "T-01", capacity = 4, floor = "Ground Floor", status = "AVAILABLE", createdAt = nowIso())
            val t2 = TableItem(id = UUID.randomUUID().toString(), restaurantId = restaurantId, tableNumber = "T-02", capacity = 2, floor = "Ground Floor", status = "AVAILABLE", createdAt = nowIso())
            val t3 = TableItem(id = UUID.randomUUID().toString(), restaurantId = restaurantId, tableNumber = "T-03", capacity = 6, floor = "Ground Floor", status = "AVAILABLE", createdAt = nowIso())
            val t4 = TableItem(id = UUID.randomUUID().toString(), restaurantId = restaurantId, tableNumber = "R-01", capacity = 4, floor = "Rooftop Lounge", status = "AVAILABLE", createdAt = nowIso())

            saveTable(restaurantId, t1)
            saveTable(restaurantId, t2)
            saveTable(restaurantId, t3)
            saveTable(restaurantId, t4)

            // Staff
            val staffWaiter = StaffMember(id = UUID.randomUUID().toString(), restaurantId = restaurantId, name = "Aarav Sharma", phone = "+977-9811223344", email = "aarav@admincafe.apps", role = "WAITER", status = "ACTIVE", createdAt = nowIso())
            val staffChef = StaffMember(id = UUID.randomUUID().toString(), restaurantId = restaurantId, name = "Chef Bikram", phone = "+977-9822334455", email = "bikram@admincafe.apps", role = "CHEF", status = "ACTIVE", assignedStation = "Grill Counter", createdAt = nowIso())

            saveStaff(restaurantId, staffWaiter)
            saveStaff(restaurantId, staffChef)

            // Inventory
            val inv1 = InventoryItem(id = UUID.randomUUID().toString(), restaurantId = restaurantId, ingredientName = "Fresh Chicken Breast", currentStock = 24.0, unit = "kg", minStock = 10.0, supplier = "Valley Farm Meats", costPerUnit = 380.0, status = "GOOD", updatedAt = nowIso())
            val inv2 = InventoryItem(id = UUID.randomUUID().toString(), restaurantId = restaurantId, ingredientName = "Momo Flour Blend", currentStock = 40.0, unit = "kg", minStock = 15.0, supplier = "Apex Agro Mills", costPerUnit = 95.0, status = "GOOD", updatedAt = nowIso())
            val inv3 = InventoryItem(id = UUID.randomUUID().toString(), restaurantId = restaurantId, ingredientName = "Arabica Coffee Beans", currentStock = 4.5, unit = "kg", minStock = 5.0, supplier = "Himalayan Roast", costPerUnit = 1200.0, status = "LOW_STOCK", updatedAt = nowIso())

            saveInventoryItem(restaurantId, inv1)
            saveInventoryItem(restaurantId, inv2)
            saveInventoryItem(restaurantId, inv3)

            // Sample Customer
            val cust = CustomerRecord(id = UUID.randomUUID().toString(), restaurantId = restaurantId, name = "Sujit Karki", phone = "+977-9841000111", email = "sujit@gmail.com", orderCount = 8, totalSpent = 3450.0, lastOrderDate = nowIso(), favoriteItems = "Signature Chicken Momo, Iced Caramel Cold Brew", createdAt = nowIso())
            saveCustomer(restaurantId, cust)

            // Sample Order for immediate live dashboard preview
            val sampleItems = listOf(
                OrderItem(
                    id = UUID.randomUUID().toString(),
                    foodItemId = momoItem.id,
                    name = momoItem.name,
                    variantName = "Steamed",
                    quantity = 2,
                    unitPrice = 180.0,
                    totalPrice = 360.0,
                    stationId = momoStationId,
                    stationName = "Momo Station",
                    status = "PREPARING"
                ),
                OrderItem(
                    id = UUID.randomUUID().toString(),
                    foodItemId = coffeeItem.id,
                    name = coffeeItem.name,
                    variantName = "Regular",
                    quantity = 1,
                    unitPrice = 150.0,
                    totalPrice = 150.0,
                    stationId = barStationId,
                    stationName = "Bar & Drinks",
                    status = "READY"
                )
            )

            val sampleOrder = RestaurantOrder(
                id = UUID.randomUUID().toString(),
                orderNumber = "1048",
                restaurantId = restaurantId,
                tableId = t1.id,
                tableNumber = "T-01",
                status = "PREPARING",
                paymentStatus = "PENDING",
                paymentMethod = "CASH",
                subtotal = 510.0,
                discount = 0.0,
                tax = 66.3,
                serviceCharge = 51.0,
                total = 627.3,
                itemsJson = serializeOrderItems(sampleItems),
                serverName = "Aarav Sharma",
                notes = "Table requested momo spicy sauce on the side",
                createdAt = nowIso()
            )
            createOrder(restaurantId, sampleOrder, ownerName)

            // Mark restaurant setupCompleted = true
            db.collection("restaurants").document(restaurantId).update("setupCompleted", true, "updatedAt", nowIso()).await()

            Result.success(Unit)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "restaurants/$restaurantId/seed")
            Result.failure(e)
        }
    }

    companion object {
        fun serializeOrderItems(items: List<OrderItem>): String {
            val array = JSONArray()
            items.forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("foodItemId", item.foodItemId)
                    put("name", item.name)
                    put("variantName", item.variantName)
                    put("quantity", item.quantity)
                    put("unitPrice", item.unitPrice)
                    put("totalPrice", item.totalPrice)
                    put("stationId", item.stationId)
                    put("stationName", item.stationName)
                    put("status", item.status)
                    put("cancelReason", item.cancelReason)
                    put("cancelledBy", item.cancelledBy)
                    put("cancelledAt", item.cancelledAt)
                }
                array.put(obj)
            }
            return array.toString()
        }

        fun parseOrderItems(json: String): List<OrderItem> {
            if (json.isBlank()) return emptyList()
            return try {
                val array = JSONArray(json)
                val list = mutableListOf<OrderItem>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        OrderItem(
                            id = obj.optString("id"),
                            foodItemId = obj.optString("foodItemId"),
                            name = obj.optString("name"),
                            variantName = obj.optString("variantName"),
                            quantity = obj.optInt("quantity", 1),
                            unitPrice = obj.optDouble("unitPrice", 0.0),
                            totalPrice = obj.optDouble("totalPrice", 0.0),
                            stationId = obj.optString("stationId"),
                            stationName = obj.optString("stationName"),
                            status = obj.optString("status", "PREPARING"),
                            cancelReason = obj.optString("cancelReason"),
                            cancelledBy = obj.optString("cancelledBy"),
                            cancelledAt = obj.optString("cancelledAt")
                        )
                    )
                }
                list
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
