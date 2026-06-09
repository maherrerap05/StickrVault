package com.example.myapplication.data.mapper

import com.example.myapplication.data.local.entity.AppUserEntity
import com.example.myapplication.data.remote.dto.AppUserDto
import com.example.myapplication.domain.model.AppUser
import com.example.myapplication.domain.model.UserRole

fun AppUserDto.toDomain(): AppUser = AppUser(
    id = id,
    name = name,
    email = email,
    role = runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.AUDITOR)
)

fun AppUserEntity.toDomain(): AppUser = AppUser(
    id = id,
    name = name,
    email = email,
    role = runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.AUDITOR)
)

fun AppUser.toEntity(): AppUserEntity = AppUserEntity(
    id = id,
    name = name,
    email = email,
    role = role.name,
    lastLogin = System.currentTimeMillis()
)