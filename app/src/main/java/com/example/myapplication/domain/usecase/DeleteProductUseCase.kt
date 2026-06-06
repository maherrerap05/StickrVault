package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.repository.ProductRepository

class DeleteProductUseCase(private val repository: ProductRepository) {
    suspend operator fun invoke(id: String) = repository.deleteProduct(id)
}