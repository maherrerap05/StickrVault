package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.Product
import com.example.myapplication.domain.repository.ProductRepository

class GetProductsUseCase(
    private val repository: ProductRepository
) {

    suspend operator fun invoke(): List<Product> {
        return repository.getProducts()
    }
}