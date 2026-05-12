package com.example.myapplication.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.presentation.catalog.CatalogScreen
import com.example.myapplication.presentation.catalog.CatalogViewModel
import com.example.myapplication.presentation.home.HomeScreen
import com.example.myapplication.presentation.home.HomeViewModel
import com.example.myapplication.presentation.reports.ReportsScreen
import com.example.myapplication.presentation.reports.ReportsViewModel
import com.example.myapplication.presentation.scanner.ScannerScreen
import com.example.myapplication.presentation.scanner.ScannerViewModel

@Composable
fun StickrVaultNavHost(
    navController: NavHostController,
    catalogViewModel: CatalogViewModel,
    homeViewModel: HomeViewModel,
    scannerViewModel: ScannerViewModel,
    reportsViewModel: ReportsViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(viewModel = homeViewModel)
        }

        composable(Routes.CATALOG) {
            CatalogScreen(viewModel = catalogViewModel)
        }

        composable(Routes.SCANNER) {
            ScannerScreen(viewModel = scannerViewModel)
        }

        composable(Routes.REPORTS) {
            ReportsScreen(viewModel = reportsViewModel)
        }
    }
}