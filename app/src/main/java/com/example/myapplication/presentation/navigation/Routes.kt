package com.example.myapplication.presentation.navigation

import kotlinx.serialization.Serializable

object Routes {
    @Serializable
    data object Login

    @Serializable
    data object Home

    @Serializable
    data object Catalog

    @Serializable
    data object Scanner

    @Serializable
    data object Reports
}
