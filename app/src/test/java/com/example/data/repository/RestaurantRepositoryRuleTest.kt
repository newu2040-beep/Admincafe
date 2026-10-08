package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Category
import com.example.data.model.FoodItem
import com.example.data.model.Restaurant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class RestaurantRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun unauthenticatedUser_cannotWriteRestaurant() = runBlocking {
    auth.signOut()
    val repository = RestaurantRepository(firestore, auth)
    val restId = "unauth_rest_" + UUID.randomUUID()
    val rest = Restaurant(
      id = restId,
      ownerId = "unauth_owner",
      name = "Unauth Cafe",
      currency = "Rs."
    )
    val result = repository.saveRestaurant(rest)
    assertTrue("Unauthenticated write should fail", result.isFailure)
  }

  @Test
  fun authenticatedUser_canSaveAndQueryRestaurantAndMenu() = runBlocking {
    val uid = signInTestUser("owner_${System.currentTimeMillis()}@admincafe.com")
    val repository = RestaurantRepository(firestore, auth)

    val restId = "rest_" + UUID.randomUUID()
    val rest = Restaurant(
      id = restId,
      ownerId = uid,
      name = "Grand Cafe",
      currency = "Rs."
    )

    val restResult = repository.saveRestaurant(rest)
    assertTrue("Authenticated restaurant write should succeed: ${restResult.exceptionOrNull()}", restResult.isSuccess)

    // Category write
    val cat = Category(
      id = "cat_test",
      restaurantId = restId,
      name = "Beverages",
      isActive = true
    )
    val catResult = repository.saveCategory(restId, cat)
    assertTrue("Category write should succeed: ${catResult.exceptionOrNull()?.message}", catResult.isSuccess)

    // Food item write
    val food = FoodItem(
      id = "food_test",
      restaurantId = restId,
      categoryId = "cat_test",
      name = "Chai",
      price = 50.0,
      isAvailable = true
    )
    val foodResult = repository.saveFoodItem(restId, food)
    assertTrue("Food item write should succeed", foodResult.isSuccess)
  }
}
