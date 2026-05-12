package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.Product
import com.example.myapplication.domain.model.ProductCategory
import com.example.myapplication.domain.repository.ProductRepository

class FilterProductsByCategoryUseCase(
    private val repository: ProductRepository
) {

    suspend operator fun invoke(
        category: ProductCategory
    ): List<Product> {

        return repository.filterProductsByCategory(category)
    }
}