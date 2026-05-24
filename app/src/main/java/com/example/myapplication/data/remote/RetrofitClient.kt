package com.example.myapplication.data.remote

import com.example.myapplication.data.remote.api.SupabaseApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL =
        "https://refgjedcfxjgaftbktpy.supabase.co/rest/v1/"

    private const val SUPABASE_API_KEY =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJlZmdqZWRjZnhqZ2FmdGJrdHB5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzk1Njc0MzMsImV4cCI6MjA5NTE0MzQzM30.xX3JGe6z1UQWcyUzQrZgnzefzpbJcCxZcFm18zOwwsM"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->

            val request = chain.request().newBuilder()
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer $SUPABASE_API_KEY")
                .addHeader("Content-Type", "application/json")
                .build()

            chain.proceed(request)
        }
        .build()

    val apiService: SupabaseApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupabaseApiService::class.java)
    }
}