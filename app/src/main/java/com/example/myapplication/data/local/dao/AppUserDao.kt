package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.myapplication.data.local.entity.AppUserEntity

@Dao
interface AppUserDao {

    @Query("SELECT * FROM app_users WHERE email = :email COLLATE NOCASE LIMIT 1")
    suspend fun getUserByEmail(email: String): AppUserEntity?

    @Query("SELECT * FROM app_users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): AppUserEntity?

    @Query("SELECT * FROM app_users ORDER BY name ASC")
    suspend fun getAllUsers(): List<AppUserEntity>

    @Upsert
    suspend fun upsertUser(user: AppUserEntity)
}