package com.example.myapplication.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.presentation.auth.AuthViewModel
import com.example.myapplication.presentation.auth.LoginScreen
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
    authViewModel: AuthViewModel,
    catalogViewModel: CatalogViewModel,
    homeViewModel: HomeViewModel,
    scannerViewModel: ScannerViewModel,
    reportsViewModel: ReportsViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                viewModel = homeViewModel,
                currentUser = currentUser,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
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