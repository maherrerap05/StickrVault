package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.ProductEntity

@Dao
interface ProductDao {

    @Query("SELECT * FROM products ORDER BY name ASC")
    suspend fun getAllProducts(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: String): ProductEntity?

    @Query("SELECT * FROM products WHERE ocrIdentifier = :identifier COLLATE NOCASE LIMIT 1")
    suspend fun getProductByOcrIdentifier(identifier: String): ProductEntity?

    @Upsert
    suspend fun upsertProducts(products: List<ProductEntity>)

    @Upsert
    suspend fun upsertProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: String)

    @Query("SELECT * FROM products WHERE isSynced = 0")
    suspend fun getUnsyncedProducts(): List<ProductEntity>

    @Query("UPDATE products SET isSynced = 1 WHERE id = :id")
    suspend fun markProductAsSynced(id: String)


    @Query("""
    SELECT * FROM products 
    WHERE LOWER(TRIM(name)) = LOWER(TRIM(:name))
    AND category = :category
    LIMIT 1
    """)
    suspend fun getProductByNameAndCategory(
        name: String,
        category: String
    ): ProductEntity?


}