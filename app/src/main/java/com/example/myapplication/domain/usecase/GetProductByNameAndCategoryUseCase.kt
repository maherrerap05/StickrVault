package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.Product
import com.example.myapplication.domain.model.ProductCategory
import com.example.myapplication.domain.repository.ProductRepository

class GetProductByNameAndCategoryUseCase(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(
        name: String,
        category: ProductCategory
    ): Product? = repository.getProductByNameAndCategory(name, category)
}