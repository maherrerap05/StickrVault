package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.local.database.AppDatabase
import com.example.myapplication.data.remote.RetrofitClient
import com.example.myapplication.data.repository.*
import com.example.myapplication.domain.usecase.*
import com.example.myapplication.presentation.auth.AuthViewModel
import com.example.myapplication.presentation.auth.AuthViewModelFactory
import com.example.myapplication.presentation.catalog.CatalogViewModel
import com.example.myapplication.presentation.catalog.CatalogViewModelFactory
import com.example.myapplication.presentation.home.HomeViewModel
import com.example.myapplication.presentation.home.HomeViewModelFactory
import com.example.myapplication.presentation.navigation.BottomNavigationBar
import com.example.myapplication.presentation.navigation.Routes
import com.example.myapplication.presentation.navigation.StickrVaultNavHost
import com.example.myapplication.presentation.reports.ReportsViewModel
import com.example.myapplication.presentation.reports.ReportsViewModelFactory
import com.example.myapplication.presentation.scanner.ScannerViewModel
import com.example.myapplication.presentation.scanner.ScannerViewModelFactory
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db               = AppDatabase.getInstance(applicationContext)
        val productDao       = db.productDao()
        val stockMovementDao = db.stockMovementDao()

        val productRepository       = ProductRepositoryImpl(RetrofitClient.apiService, productDao)
        val stockMovementRepository = StockMovementRepositoryImpl(RetrofitClient.apiService, stockMovementDao)
        val authRepository          = AuthRepositoryImpl(RetrofitClient.apiService)

        val getProductsUseCase       = GetProductsUseCase(productRepository)
        val getStockMovementsUseCase = GetStockMovementsUseCase(stockMovementRepository)
        val getUsersUseCase          = GetUsersUseCase(authRepository)

        val authFactory    = AuthViewModelFactory(LoginUseCase(authRepository))
        val homeFactory    = HomeViewModelFactory(
            getProductsUseCase       = getProductsUseCase,
            getStockMovementsUseCase = getStockMovementsUseCase,
            getUsersUseCase          = getUsersUseCase
        )
        val catalogFactory = CatalogViewModelFactory(
            getProductsUseCase              = getProductsUseCase,
            searchProductsUseCase           = SearchProductsUseCase(productRepository),
            filterProductsByCategoryUseCase = FilterProductsByCategoryUseCase(productRepository),
            addProductUseCase               = AddProductUseCase(productRepository)
        )
        val reportsFactory = ReportsViewModelFactory(
            getProductsUseCase       = getProductsUseCase,
            getStockMovementsUseCase = getStockMovementsUseCase
        )
        val scannerFactory = ScannerViewModelFactory(
            getProductByOcrIdentifier = GetProductByOcrIdentifierUseCase(productRepository)
        )

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()

                val authViewModel    : AuthViewModel    = viewModel(factory = authFactory)
                val catalogViewModel : CatalogViewModel = viewModel(factory = catalogFactory)
                val homeViewModel    : HomeViewModel    = viewModel(factory = homeFactory)
                val reportsViewModel : ReportsViewModel = viewModel(factory = reportsFactory)
                val scannerViewModel : ScannerViewModel = viewModel(factory = scannerFactory)

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute  = navBackStackEntry?.destination?.route
                val showBottomBar = currentRoute != Routes.LOGIN

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) BottomNavigationBar(navController = navController)
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        StickrVaultNavHost(
                            navController    = navController,
                            authViewModel    = authViewModel,
                            catalogViewModel = catalogViewModel,
                            homeViewModel    = homeViewModel,
                            scannerViewModel = scannerViewModel,
                            reportsViewModel = reportsViewModel
                        )
                    }
                }
            }
        }
    }
}