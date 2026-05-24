package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.domain.usecase.FilterProductsByCategoryUseCase
import com.example.myapplication.domain.usecase.GetProductsUseCase
import com.example.myapplication.domain.usecase.SearchProductsUseCase
import com.example.myapplication.presentation.catalog.CatalogViewModel
import com.example.myapplication.presentation.home.HomeViewModel
import com.example.myapplication.presentation.scanner.ScannerViewModel
import com.example.myapplication.presentation.reports.ReportsViewModel
import com.example.myapplication.presentation.catalog.CatalogViewModelFactory
import com.example.myapplication.presentation.navigation.StickrVaultNavHost
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.myapplication.presentation.navigation.BottomNavigationBar
import com.example.myapplication.data.remote.RetrofitClient
import com.example.myapplication.data.repository.ProductRepositoryImpl
import com.example.myapplication.presentation.home.HomeViewModelFactory


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = ProductRepositoryImpl(RetrofitClient.apiService)
        val getProductsUseCase = GetProductsUseCase(repository)

        val catalogFactory = CatalogViewModelFactory(
            getProductsUseCase = getProductsUseCase,
            searchProductsUseCase = SearchProductsUseCase(repository),
            filterProductsByCategoryUseCase = FilterProductsByCategoryUseCase(repository)
        )

        val homeFactory = HomeViewModelFactory(
            getProductsUseCase = getProductsUseCase
        )

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()

                val homeViewModel: HomeViewModel = viewModel(
                    factory = homeFactory
                )
                val scannerViewModel: ScannerViewModel = viewModel()
                val reportsViewModel: ReportsViewModel = viewModel()

                val catalogViewModel: CatalogViewModel = viewModel(
                    factory = catalogFactory
                )

                Scaffold(
                    bottomBar = {
                        BottomNavigationBar(
                            navController = navController
                        )
                    }
                ) { innerPadding ->

                    Box(
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        StickrVaultNavHost(
                            navController = navController,
                            catalogViewModel = catalogViewModel,
                            homeViewModel = homeViewModel,
                            scannerViewModel = scannerViewModel,
                            reportsViewModel = reportsViewModel
                        )
                    }
                }
            }
        }
    }
}