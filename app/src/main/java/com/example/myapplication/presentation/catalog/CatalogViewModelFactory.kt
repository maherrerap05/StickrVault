package com.example.myapplication.presentation.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.domain.usecase.AddProductUseCase
import com.example.myapplication.domain.usecase.AddStockMovementUseCase
import com.example.myapplication.domain.usecase.FilterProductsByCategoryUseCase
import com.example.myapplication.domain.usecase.GetProductByNameAndCategoryUseCase
import com.example.myapplication.domain.usecase.GetProductsUseCase
import com.example.myapplication.domain.usecase.SearchProductsUseCase
import com.example.myapplication.domain.usecase.UpdateProductUseCase

class CatalogViewModelFactory(
    private val getProductsUseCase: GetProductsUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val filterProductsByCategoryUseCase: FilterProductsByCategoryUseCase,
    private val addProductUseCase: AddProductUseCase,
    private val getProductByNameAndCategoryUseCase: GetProductByNameAndCategoryUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val addStockMovementUseCase: AddStockMovementUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CatalogViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CatalogViewModel(
                getProductsUseCase = getProductsUseCase,
                searchProductsUseCase = searchProductsUseCase,
                filterProductsByCategoryUseCase = filterProductsByCategoryUseCase,
                addProductUseCase = addProductUseCase,
                getProductByNameAndCategoryUseCase = getProductByNameAndCategoryUseCase,
                updateProductUseCase = updateProductUseCase,
                addStockMovementUseCase = addStockMovementUseCase
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}