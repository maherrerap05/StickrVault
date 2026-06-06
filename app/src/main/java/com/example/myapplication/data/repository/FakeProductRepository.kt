package com.example.myapplication.data.repository

import com.example.myapplication.domain.model.Product
import com.example.myapplication.domain.model.ProductCategory
import com.example.myapplication.domain.repository.ProductRepository

class FakeProductRepository : ProductRepository {

    private val products = mutableListOf(
        Product(
            id = "1", name = "Cromo Messi",
            category = ProductCategory.STICKER_INDIVIDUAL,
            description = "Cromo oficial Argentina",
            currentStock = 120, minimumStock = 20,
            imageUrl = null, ocrIdentifier = "ARG10",
            lastUpdated = System.currentTimeMillis(), isSynced = true
        ),
        Product(
            id = "2", name = "Álbum Mundial 2026",
            category = ProductCategory.ALBUM,
            description = "Álbum tapa dura",
            currentStock = 35, minimumStock = 10,
            imageUrl = null, ocrIdentifier = "ALB2026",
            lastUpdated = System.currentTimeMillis(), isSynced = true
        ),
        Product(
            id = "3", name = "Balón Oficial",
            category = ProductCategory.BALL,
            description = "Balón edición FIFA 2026",
            currentStock = 8, minimumStock = 5,
            imageUrl = null, ocrIdentifier = "BALL01",
            lastUpdated = System.currentTimeMillis(), isSynced = false
        )
    )

    override suspend fun getProducts() = products.toList()
    override suspend fun getProductById(id: String) = products.find { it.id == id }
    override suspend fun searchProducts(query: String) = products.filter { it.name.contains(query, ignoreCase = true) }
    override suspend fun filterProductsByCategory(category: ProductCategory) = products.filter { it.category == category }
    override suspend fun getProductByOcrIdentifier(identifier: String) = products.find { it.ocrIdentifier == identifier }
    override suspend fun addProduct(product: Product): Product { products.add(product); return product }
    override suspend fun updateProduct(product: Product): Product { val i = products.indexOfFirst { it.id == product.id }; if (i >= 0) products[i] = product; return product }
    override suspend fun deleteProduct(id: String) { products.removeAll { it.id == id } }
}