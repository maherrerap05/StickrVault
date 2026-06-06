package com.example.myapplication.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.usecase.GetProductsUseCase
import com.example.myapplication.domain.usecase.GetStockMovementsUseCase
import com.example.myapplication.domain.usecase.GetUsersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getProductsUseCase: GetProductsUseCase,
    private val getStockMovementsUseCase: GetStockMovementsUseCase,
    private val getUsersUseCase: GetUsersUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { loadHomeSummary() }

    fun loadHomeSummary() {
        viewModelScope.launch {
            try {
                val products  = getProductsUseCase()
                val movements = getStockMovementsUseCase(limit = 5)
                val users     = getUsersUseCase()
                _uiState.value = HomeUiState(
                    totalProducts         = products.size,
                    criticalStockProducts = products.count { it.currentStock <= it.minimumStock },
                    pendingSyncItems      = products.count { !it.isSynced },
                    lastSyncText          = "Sincronizado con Supabase",
                    recentMovements       = movements,
                    users                 = users
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState(lastSyncText = "Error al sincronizar")
            }
        }
    }
}