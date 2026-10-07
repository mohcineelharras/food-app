package com.foodrecommender.app.domain.models

enum class Diet {
    ANY,
    VEGETARIAN,
    VEGAN,
    GLUTEN_FREE,
}

data class Dish(
    val id: String,
    val name: String,
    val cuisine: String,
    val summary: String,
    val diets: Set<Diet>,
)

data class UserPreference(
    val diet: Diet,
    val note: String,
)
