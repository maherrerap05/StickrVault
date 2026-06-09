package com.example.myapplication.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication.data.local.dao.ProductDao
import com.example.myapplication.data.local.dao.StockMovementDao
import com.example.myapplication.data.local.entity.AppUserEntity
import com.example.myapplication.data.local.entity.ProductEntity
import com.example.myapplication.data.local.entity.StockMovementEntity
import com.example.myapplication.data.local.dao.AppUserDao


@Database(
    entities = [ProductEntity::class, StockMovementEntity::class, AppUserEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun stockMovementDao(): StockMovementDao
    abstract fun appUserDao(): AppUserDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stickrvault.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}