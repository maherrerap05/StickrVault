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
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(
    navController: NavHostController
) {
    val currentDestination = navController
        .currentBackStackEntryAsState()
        .value
        ?.destination

    NavigationBar {
        NavigationBarItem(
            selected = currentDestination?.hasRoute<Routes.Home>() == true,
            onClick = {
                navController.navigate(Routes.Home) {
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
            selected = currentDestination?.hasRoute<Routes.Catalog>() == true,
            onClick = {
                navController.navigate(Routes.Catalog) {
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
            selected = currentDestination?.hasRoute<Routes.Scanner>() == true,
            onClick = {
                navController.navigate(Routes.Scanner) {
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
            selected = currentDestination?.hasRoute<Routes.Reports>() == true,
            onClick = {
                navController.navigate(Routes.Reports) {
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
