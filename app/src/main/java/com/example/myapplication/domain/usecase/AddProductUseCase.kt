package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.Product
import com.example.myapplication.domain.repository.ProductRepository

class AddProductUseCase(private val repository: ProductRepository) {
    suspend operator fun invoke(product: Product): Product? = repository.addProduct(product)
}