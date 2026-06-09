package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.ProductDao
import com.example.myapplication.data.mapper.toDomain
import com.example.myapplication.data.mapper.toEntity
import com.example.myapplication.data.remote.api.SupabaseApiService
import com.example.myapplication.data.remote.dto.ProductDto
import com.example.myapplication.domain.model.Product
import com.example.myapplication.domain.model.ProductCategory
import com.example.myapplication.domain.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductRepositoryImpl(
    private val apiService: SupabaseApiService,
    private val productDao: ProductDao
) : ProductRepository {

    override suspend fun getProducts(): List<Product> = withContext(Dispatchers.IO) {
        val localProducts = productDao.getAllProducts().map { it.toDomain() }

        try {
            syncPendingProducts()

            val remote = apiService.getProducts().map { it.toDomain().copy(isSynced = true) }
            productDao.upsertProducts(remote.map { it.toEntity() })

            val pendingLocal = productDao.getUnsyncedProducts().map { it.toDomain() }

            (remote + pendingLocal)
                .distinctBy { it.id }
                .sortedBy { it.name }

        } catch (e: Exception) {
            localProducts
        }
    }

    override suspend fun getProductById(id: String): Product? = withContext(Dispatchers.IO) {
        productDao.getProductById(id)?.toDomain()
            ?: runCatching { getProducts().find { it.id == id } }.getOrNull()
    }

    override suspend fun searchProducts(query: String): List<Product> = withContext(Dispatchers.IO) {
        try {
            syncPendingProducts()

            apiService.searchProductsRemote(
                orFilter = "(name.ilike.*$query*,description.ilike.*$query*,ocr_identifier.ilike.*$query*)"
            ).map { it.toDomain().copy(isSynced = true) }

        } catch (e: Exception) {
            productDao.getAllProducts().map { it.toDomain() }.filter { p ->
                p.name.contains(query, ignoreCase = true) ||
                        p.description.contains(query, ignoreCase = true) ||
                        p.ocrIdentifier?.contains(query, ignoreCase = true) == true
            }
        }
    }

    override suspend fun filterProductsByCategory(category: ProductCategory): List<Product> =
        withContext(Dispatchers.IO) {
            try {
                syncPendingProducts()

                apiService.filterProductsByCategoryRemote(
                    categoryFilter = "eq.${category.name}"
                ).map { it.toDomain().copy(isSynced = true) }

            } catch (e: Exception) {
                productDao.getAllProducts().map { it.toDomain() }
                    .filter { it.category == category }
            }
        }

    override suspend fun getProductByOcrIdentifier(identifier: String): Product? =
        withContext(Dispatchers.IO) {
            productDao.getProductByOcrIdentifier(identifier)?.toDomain()
                ?: runCatching {
                    getProducts().find { it.ocrIdentifier.equals(identifier, ignoreCase = true) }
                }.getOrNull()
        }

    override suspend fun addProduct(product: Product): Product? = withContext(Dispatchers.IO) {
        val localProduct = product.copy(isSynced = false)

        productDao.upsertProduct(localProduct.toEntity())

        try {
            val remoteProduct = apiService.addProduct(
                localProduct.copy(isSynced = true).toDto()
            ).firstOrNull()?.toDomain()

            if (remoteProduct != null) {
                productDao.upsertProduct(remoteProduct.copy(isSynced = true).toEntity())
                remoteProduct.copy(isSynced = true)
            } else {
                localProduct
            }

        } catch (e: Exception) {
            localProduct
        }
    }

    override suspend fun updateProduct(product: Product): Product? = withContext(Dispatchers.IO) {
        runCatching {
            val result = apiService.updateProduct("eq.${product.id}", product.toDto())
                .firstOrNull()?.toDomain()
            result?.let { productDao.upsertProduct(it.toEntity()) }
            result
        }.getOrNull()
    }

    override suspend fun deleteProduct(id: String) = withContext(Dispatchers.IO) {
        runCatching {
            apiService.deleteProduct("eq.$id")
            productDao.deleteProductById(id)
        }
        Unit
    }

    override suspend fun syncPendingProducts() = withContext(Dispatchers.IO) {
        val pendingProducts = productDao.getUnsyncedProducts()

        pendingProducts.forEach { entity ->
            runCatching {
                val product = entity.toDomain()

                val result = apiService.addProduct(
                    product.copy(isSynced = true).toDto()
                ).firstOrNull()?.toDomain()

                if (result != null) {
                    productDao.upsertProduct(result.copy(isSynced = true).toEntity())
                }
            }
        }
    }

    private fun Product.toDto() = ProductDto(
        id = id, name = name, category = category.name,
        description = description, currentStock = currentStock,
        minimumStock = minimumStock, imageUrl = imageUrl,
        ocrIdentifier = ocrIdentifier,
        lastUpdated = System.currentTimeMillis(), isSynced = true
    )
}