package com.example.myapplication.presentation.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.domain.usecase.AddProductUseCase
import com.example.myapplication.domain.usecase.FilterProductsByCategoryUseCase
import com.example.myapplication.domain.usecase.GetProductsUseCase
import com.example.myapplication.domain.usecase.SearchProductsUseCase

class CatalogViewModelFactory(
    private val getProductsUseCase: GetProductsUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val filterProductsByCategoryUseCase: FilterProductsByCategoryUseCase,
    private val addProductUseCase: AddProductUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CatalogViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CatalogViewModel(
                getProductsUseCase, searchProductsUseCase,
                filterProductsByCategoryUseCase, addProductUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}