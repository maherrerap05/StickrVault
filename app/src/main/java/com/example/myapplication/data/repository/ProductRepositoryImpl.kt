package com.example.myapplication.data.repository

import com.example.myapplication.data.mapper.toDomain
import com.example.myapplication.data.remote.api.SupabaseApiService
import com.example.myapplication.domain.model.Product
import com.example.myapplication.domain.model.ProductCategory
import com.example.myapplication.domain.repository.ProductRepository

class ProductRepositoryImpl(
    private val apiService: SupabaseApiService
) : ProductRepository {

    override suspend fun getProducts(): List<Product> {
        return apiService.getProducts().map { it.toDomain() }
    }

    override suspend fun getProductById(id: String): Product? {
        return getProducts().find { it.id == id }
    }

    override suspend fun searchProducts(query: String): List<Product> {
        return getProducts().filter { product ->
            product.name.contains(query, ignoreCase = true) ||
                    product.description.contains(query, ignoreCase = true) ||
                    product.ocrIdentifier?.contains(query, ignoreCase = true) == true
        }
    }

    override suspend fun filterProductsByCategory(
        category: ProductCategory
    ): List<Product> {
        return getProducts().filter { product ->
            product.category == category
        }
    }

    override suspend fun getProductByOcrIdentifier(
        identifier: String
    ): Product? {
        return getProducts().find { product ->
            product.ocrIdentifier.equals(identifier, ignoreCase = true)
        }
    }
}