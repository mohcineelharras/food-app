package com.foodrecommender.app.domain.usecases

import com.foodrecommender.app.domain.models.Diet
import com.foodrecommender.app.domain.models.Dish

class RecommendDishes {
    fun recommend(dishes: List<Dish>, query: String, diet: Diet): List<Dish> {
        val needle = PlainText.sanitize(query, PlainText.QUERY_LIMIT)
        return dishes
            .asSequence()
            .filter { matchesDiet(it, diet) }
            .filter { needle.isEmpty() || matchesQuery(it, needle) }
            .sortedBy { it.name.lowercase() }
            .toList()
    }

    private fun matchesDiet(dish: Dish, diet: Diet): Boolean = when (diet) {
        Diet.ANY -> true
        Diet.VEGETARIAN -> Diet.VEGETARIAN in dish.diets || Diet.VEGAN in dish.diets
        Diet.VEGAN -> Diet.VEGAN in dish.diets
        Diet.GLUTEN_FREE -> Diet.GLUTEN_FREE in dish.diets
    }

    private fun matchesQuery(dish: Dish, needle: String): Boolean {
        return dish.name.contains(needle, ignoreCase = true) ||
            dish.cuisine.contains(needle, ignoreCase = true) ||
            dish.summary.contains(needle, ignoreCase = true)
    }
}
