package com.example.myapplication.data.remote.api

import com.example.myapplication.data.remote.dto.AppUserDto
import com.example.myapplication.data.remote.dto.ProductDto
import com.example.myapplication.data.remote.dto.StockMovementDto
import retrofit2.http.*

interface SupabaseApiService {

    // Products — read
    @GET("products")
    suspend fun getProducts(): List<ProductDto>

    // Products — write
    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @POST("products")
    suspend fun addProduct(@Body product: ProductDto): List<ProductDto>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @PATCH("products")
    suspend fun updateProduct(
        @Query("id") idFilter: String,
        @Body product: ProductDto
    ): List<ProductDto>

    @DELETE("products")
    suspend fun deleteProduct(@Query("id") idFilter: String)

    // Users
    @GET("app_users")
    suspend fun getUserByEmail(@Query("email") emailFilter: String): List<AppUserDto>

    // Stock movements
    @GET("stock_movements")
    suspend fun getStockMovements(
        @Query("order") order: String = "timestamp.desc",
        @Query("limit") limit: Int = 10
    ): List<StockMovementDto>

    @Headers("Content-Type: application/json", "Prefer: return=representation")
    @POST("stock_movements")
    suspend fun addStockMovement(@Body movement: StockMovementDto): List<StockMovementDto>
}