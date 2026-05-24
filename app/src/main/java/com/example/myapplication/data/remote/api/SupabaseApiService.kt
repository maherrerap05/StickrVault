package com.example.myapplication.data.remote.api

import com.example.myapplication.data.remote.dto.ProductDto
import retrofit2.http.GET

interface SupabaseApiService {

    @GET("products")
    suspend fun getProducts(): List<ProductDto>
}