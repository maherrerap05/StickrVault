package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.AppUserDao
import com.example.myapplication.data.local.session.SessionPreferences
import com.example.myapplication.data.mapper.toDomain
import com.example.myapplication.data.mapper.toEntity
import com.example.myapplication.data.remote.api.SupabaseApiService
import com.example.myapplication.domain.model.AppUser
import com.example.myapplication.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val apiService: SupabaseApiService,
    private val appUserDao: AppUserDao,
    private val sessionPreferences: SessionPreferences
) : AuthRepository {

    override suspend fun getSavedSession(): AppUser? = withContext(Dispatchers.IO) {
        val userId = sessionPreferences.getSavedUserId()
        if (userId != null) {
            appUserDao.getUserById(userId)?.toDomain()
        } else {
            sessionPreferences.getSavedEmail()
                ?.let { email -> appUserDao.getUserByEmail(email)?.toDomain() }
        }
    }

    override suspend fun saveSession(user: AppUser) = withContext(Dispatchers.IO) {
        sessionPreferences.saveSession(user.id, user.email)
        appUserDao.upsertUser(user.toEntity())
    }

    override suspend fun clearSession() = withContext(Dispatchers.IO) {
        sessionPreferences.clearSession()
    }

    override suspend fun login(email: String): AppUser? = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim()

        try {
            val remoteUser = apiService.getUserByEmail("eq.$cleanEmail")
                .firstOrNull()
                ?.toDomain()

            if (remoteUser != null) {
                saveSession(remoteUser)
                remoteUser
            } else {
                null
            }
        } catch (e: Exception) {
            appUserDao.getUserByEmail(cleanEmail)?.toDomain()?.also { localUser ->
                saveSession(localUser)
            }
        }
    }

    override suspend fun getUsers(): List<AppUser> = withContext(Dispatchers.IO) {
        try {
            val remoteUsers = apiService.getUsers().map { it.toDomain() }
            remoteUsers.forEach { appUserDao.upsertUser(it.toEntity()) }
            remoteUsers
        } catch (e: Exception) {
            appUserDao.getAllUsers().map { it.toDomain() }
        }
    }
}
