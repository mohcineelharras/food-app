package com.foodrecommender.app.domain

import com.foodrecommender.app.domain.models.Dish
import com.foodrecommender.app.domain.models.UserPreference

interface PreferenceStore {
    fun read(): UserPreference

    fun write(preference: UserPreference)
}

interface DishCatalog {
    suspend fun dishes(): List<Dish>
}
