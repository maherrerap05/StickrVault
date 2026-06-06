package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.Product
import com.example.myapplication.domain.repository.ProductRepository

class GetProductByOcrIdentifierUseCase(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(identifier: String): Product? =
        repository.getProductByOcrIdentifier(identifier)
}