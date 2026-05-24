package com.example.myapplication.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.usecase.GetProductsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getProductsUseCase: GetProductsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeSummary()
    }

    fun loadHomeSummary() {
        viewModelScope.launch {
            try {
                val products = getProductsUseCase()

                _uiState.value = HomeUiState(
                    totalProducts = products.size,
                    criticalStockProducts = products.count {
                        it.currentStock <= it.minimumStock
                    },
                    pendingSyncItems = products.count {
                        !it.isSynced
                    },
                    lastSyncText = "Datos sincronizados desde Supabase"
                )

            } catch (e: Exception) {
                _uiState.value = HomeUiState(
                    lastSyncText = "Error al cargar resumen"
                )
            }
        }
    }
}