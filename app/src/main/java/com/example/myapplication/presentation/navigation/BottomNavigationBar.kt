package com.example.myapplication.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(
    navController: NavHostController
) {
    val currentRoute = navController
        .currentBackStackEntryAsState()
        .value
        ?.destination
        ?.route

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute == Routes.HOME,
            onClick = {
                navController.navigate(Routes.HOME) {
                    launchSingleTop = true
                }
            },
            icon = {
                Icon(Icons.Default.Home, contentDescription = "Home")
            },
            label = {
                Text("Home")
            }
        )

        NavigationBarItem(
            selected = currentRoute == Routes.CATALOG,
            onClick = {
                navController.navigate(Routes.CATALOG) {
                    launchSingleTop = true
                }
            },
            icon = {
                Icon(Icons.Default.Inventory, contentDescription = "Catálogo")
            },
            label = {
                Text("Catálogo")
            }
        )

        NavigationBarItem(
            selected = currentRoute == Routes.SCANNER,
            onClick = {
                navController.navigate(Routes.SCANNER) {
                    launchSingleTop = true
                }
            },
            icon = {
                Icon(Icons.Default.QrCodeScanner, contentDescription = "Escáner")
            },
            label = {
                Text("Escáner")
            }
        )

        NavigationBarItem(
            selected = currentRoute == Routes.REPORTS,
            onClick = {
                navController.navigate(Routes.REPORTS) {
                    launchSingleTop = true
                }
            },
            icon = {
                Icon(Icons.Default.Assessment, contentDescription = "Reportes")
            },
            label = {
                Text("Reportes")
            }
        )
    }
}