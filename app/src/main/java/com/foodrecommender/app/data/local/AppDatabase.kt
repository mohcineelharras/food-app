package com.foodrecommender.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DishEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dishes(): DishDao

    companion object {
        const val NAME = "food_catalog.db"
    }
}
