package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.Product
import com.example.myapplication.domain.repository.ProductRepository

class SearchProductsUseCase(
    private val repository: ProductRepository
) {

    suspend operator fun invoke(
        query: String
    ): List<Product> {

        return repository.searchProducts(query)
    }
}