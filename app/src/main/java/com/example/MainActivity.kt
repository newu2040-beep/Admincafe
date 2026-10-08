package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.example.data.model.RestaurantOrder
import com.example.data.model.TableItem
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.kitchen.KitchenScreen
import com.example.ui.screens.menu.MenuScreen
import com.example.ui.screens.operations.OperationsMoreScreen
import com.example.ui.screens.orders.OrdersScreen
import com.example.ui.screens.setup.RestaurantSetupWizardScreen
import com.example.ui.screens.tables.TablesScreen
import com.example.ui.theme.AdminCafeTheme
import com.example.ui.theme.CafeDarkSurfaceElevated
import com.example.ui.theme.CafeEspresso
import com.example.ui.theme.CafeGold
import com.example.ui.viewmodel.AdminCafeViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: AdminCafeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AdminCafeTheme(darkTheme = true) {
                AdminCafeApp(viewModel = viewModel)
            }
        }
    }
}

enum class NavigationTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    MENU("Menu", Icons.Default.RestaurantMenu),
    TABLES("Tables", Icons.Default.TableBar),
    ORDERS("Orders", Icons.Default.ReceiptLong),
    MORE("Operations", Icons.Default.MoreHoriz)
}

@Composable
fun AdminCafeApp(viewModel: AdminCafeViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val currentUser by viewModel.currentUser.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val restaurant by viewModel.restaurant.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUiMessage()
        }
    }

    if (currentUser == null) {
        AuthScreen(
            onAuthSuccess = {},
            onAuthError = { errorMsg ->
                scope.launch { snackbarHostState.showSnackbar("Auth: $errorMsg") }
            }
        )
    } else {
        // Check if restaurant is registered or needs initial registration
        if (restaurant == null && userProfile == null) {
            InitialRestaurantRegistrationCard(
                userEmail = currentUser?.email ?: "",
                userName = currentUser?.displayName ?: "",
                onRegister = { restName, ownerName, phone, city, currency ->
                    viewModel.registerRestaurantAndOwner(
                        restaurantName = restName,
                        ownerName = ownerName,
                        email = currentUser?.email ?: "",
                        phone = phone,
                        city = city,
                        currency = currency
                    )
                }
            )
        } else if (restaurant?.setupCompleted != true) {
            // First-time Restaurant Setup Wizard (PRD Section 10)
            RestaurantSetupWizardScreen(
                viewModel = viewModel,
                onSetupComplete = {
                    // Completing or auto-seeding advances setupCompleted to true
                }
            )
        } else {
            // Main Application Shell
            MainAppShell(
                viewModel = viewModel,
                snackbarHostState = snackbarHostState,
                onSignOut = {
                    FirebaseAuth.getInstance().signOut()
                    scope.launch {
                        try {
                            credentialManager.clearCredentialState(ClearCredentialStateRequest())
                        } catch (e: Exception) {
                            // Ignored
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun InitialRestaurantRegistrationCard(
    userEmail: String,
    userName: String,
    onRegister: (restName: String, ownerName: String, phone: String, city: String, currency: String) -> Unit
) {
    var restName by remember { mutableStateOf("Himalayan Bistro & Lounge") }
    var ownerName by remember { mutableStateOf(userName.ifBlank { "Restaurant Owner" }) }
    var phone by remember { mutableStateOf("+977-9811223344") }
    var city by remember { mutableStateOf("Kathmandu") }
    var currency by remember { mutableStateOf("Rs.") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CafeEspresso)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(20.dp),
        contentAlignment = Alignment.Center
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
                Text(
                    text = "Welcome to ADMINCAFE",
                    color = CafeGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Register your physical restaurant property to begin managing tables, menu, and live kitchen operations.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )

                OutlinedTextField(
                    value = restName,
                    onValueChange = { restName = it },
                    label = { Text("Restaurant Name *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("Owner / Manager Name *") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        modifier = Modifier.weight(1.2f)
                    )
                    OutlinedTextField(
                        value = currency,
                        onValueChange = { currency = it },
                        label = { Text("Currency") },
                        modifier = Modifier.weight(0.8f)
                    )
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact Phone") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        onRegister(restName, ownerName, phone, city, currency)
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CafeGold, contentColor = Color(0xFF1E1712)),
                    shape = RoundedCornerShape(10.dp),
                    enabled = restName.isNotBlank() && ownerName.isNotBlank()
                ) {
                    Text("Create Restaurant Profile", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MainAppShell(
    viewModel: AdminCafeViewModel,
    snackbarHostState: SnackbarHostState,
    onSignOut: () -> Unit
) {
    var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }
    var subScreenKitchen by remember { mutableStateOf(false) }
    var tableForOrder by remember { mutableStateOf<TableItem?>(null) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = CafeEspresso,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationTab.entries.forEach { tab ->
                    val isSelected = (currentTab == tab && !subScreenKitchen)
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            subScreenKitchen = false
                            currentTab = tab
                        },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF1E1712),
                            selectedTextColor = CafeGold,
                            indicatorColor = CafeGold,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        },
        containerColor = CafeEspresso,
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (subScreenKitchen) {
                KitchenScreen(viewModel = viewModel)
            } else {
                when (currentTab) {
                    NavigationTab.DASHBOARD -> {
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToMenu = { currentTab = NavigationTab.MENU },
                            onNavigateToTables = { currentTab = NavigationTab.TABLES },
                            onNavigateToOrders = { currentTab = NavigationTab.ORDERS },
                            onNavigateToKitchen = { subScreenKitchen = true },
                            onOpenOrderDetails = {
                                currentTab = NavigationTab.ORDERS
                            }
                        )
                    }

                    NavigationTab.MENU -> {
                        MenuScreen(viewModel = viewModel)
                    }

                    NavigationTab.TABLES -> {
                        TablesScreen(
                            viewModel = viewModel,
                            onOpenOrderForTable = { table ->
                                tableForOrder = table
                                currentTab = NavigationTab.ORDERS
                            }
                        )
                    }

                    NavigationTab.ORDERS -> {
                        OrdersScreen(
                            viewModel = viewModel,
                            preSelectedTable = tableForOrder
                        )
                    }

                    NavigationTab.MORE -> {
                        OperationsMoreScreen(
                            viewModel = viewModel,
                            onSignOut = onSignOut
                        )
                    }
                }
            }
        }
    }
}
