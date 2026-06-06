package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.Product
import com.example.myapplication.domain.model.ProductCategory

interface ProductRepository {
    suspend fun getProducts(): List<Product>
    suspend fun getProductById(id: String): Product?
    suspend fun searchProducts(query: String): List<Product>
    suspend fun filterProductsByCategory(category: ProductCategory): List<Product>
    suspend fun getProductByOcrIdentifier(identifier: String): Product?
    suspend fun addProduct(product: Product): Product?
    suspend fun updateProduct(product: Product): Product?
    suspend fun deleteProduct(id: String)
}