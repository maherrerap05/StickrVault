package com.example.myapplication.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val isSessionReady by authViewModel.isSessionReady.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    if (!isSessionReady) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination: Any = if (currentUser != null) Routes.Home else Routes.Login

    NavHost(navController = navController, startDestination = startDestination) {

        composable<Routes.Login> {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.Home) {
                        popUpTo<Routes.Login> { inclusive = true }
                    }
                }
            )
        }

        composable<Routes.Home> {
            HomeScreen(
                viewModel = homeViewModel,
                currentUser = currentUser,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Routes.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateCatalog = { navController.navigate(Routes.Catalog) },
                onNavigateScanner = { navController.navigate(Routes.Scanner) },
                onNavigateReports = { navController.navigate(Routes.Reports) }
            )
        }

        composable<Routes.Catalog> {
            CatalogScreen(
                viewModel = catalogViewModel,
                currentUser = currentUser
            )
        }

        composable<Routes.Scanner> {
            ScannerScreen(viewModel = scannerViewModel)
        }

        composable<Routes.Reports> {
            ReportsScreen(viewModel = reportsViewModel)
        }
    }
}
