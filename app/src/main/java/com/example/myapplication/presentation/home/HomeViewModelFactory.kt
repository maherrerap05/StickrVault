package com.example.myapplication.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.domain.usecase.GetProductsUseCase
import com.example.myapplication.domain.usecase.GetStockMovementsUseCase
import com.example.myapplication.domain.usecase.GetUsersUseCase

class HomeViewModelFactory(
    private val getProductsUseCase: GetProductsUseCase,
    private val getStockMovementsUseCase: GetStockMovementsUseCase,
    private val getUsersUseCase: GetUsersUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(getProductsUseCase, getStockMovementsUseCase, getUsersUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}