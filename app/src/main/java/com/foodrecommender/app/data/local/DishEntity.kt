package com.foodrecommender.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.foodrecommender.app.domain.models.Diet
import com.foodrecommender.app.domain.models.Dish

@Entity(tableName = "dishes")
data class DishEntity(
    @PrimaryKey val id: String,
    val name: String,
    val cuisine: String,
    val summary: String,
    val diets: String,
)

fun Dish.toEntity(): DishEntity = DishEntity(
    id = id,
    name = name,
    cuisine = cuisine,
    summary = summary,
    diets = diets.map { it.name }.sorted().joinToString(","),
)

fun DishEntity.toDish(): Dish = Dish(
    id = id,
    name = name,
    cuisine = cuisine,
    summary = summary,
    diets = diets.split(',')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .mapNotNull { token ->
            Diet.entries.firstOrNull { it.name == token && it != Diet.ANY }
        }
        .toSet(),
)
