package com.foodrecommender.app.data.repository

import androidx.room.withTransaction
import com.foodrecommender.app.data.local.AppDatabase
import com.foodrecommender.app.data.local.toDish
import com.foodrecommender.app.data.local.toEntity
import com.foodrecommender.app.domain.DishCatalog
import com.foodrecommender.app.domain.models.Dish

class RoomDishCatalog(
    private val database: AppDatabase,
    private val seed: List<Dish>,
) : DishCatalog {
    override suspend fun dishes(): List<Dish> {
        val dao = database.dishes()
        database.withTransaction {
            dao.clear()
            dao.insertAll(seed.map { it.toEntity() })
        }
        return dao.all().map { it.toDish() }
    }
}
