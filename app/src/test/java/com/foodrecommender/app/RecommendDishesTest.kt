package com.foodrecommender.app

import com.foodrecommender.app.domain.models.Diet
import com.foodrecommender.app.domain.models.Dish
import com.foodrecommender.app.domain.usecases.RecommendDishes
import org.junit.Assert.assertEquals
import org.junit.Test

class RecommendDishesTest {
    private val recommend = RecommendDishes()
    private val dishes = listOf(
        dish("taco", "Beef taco", diets = emptySet()),
        dish("soup", "Lentil soup", diets = setOf(Diet.VEGAN, Diet.GLUTEN_FREE)),
        dish("pizza", "Margherita pizza", diets = setOf(Diet.VEGETARIAN)),
        dish("curry", "Chickpea curry", "South Asian", diets = setOf(Diet.VEGAN, Diet.GLUTEN_FREE)),
    )

    @Test
    fun anyDietSortsByName() {
        val names = recommend.recommend(dishes, "  ", Diet.ANY).map { it.name }
        assertEquals(
            listOf("Beef taco", "Chickpea curry", "Lentil soup", "Margherita pizza"),
            names,
        )
    }

    @Test
    fun vegetarianIncludesVeganDishes() {
        val names = recommend.recommend(dishes, "", Diet.VEGETARIAN).map { it.name }
        assertEquals(listOf("Chickpea curry", "Lentil soup", "Margherita pizza"), names)
    }

    @Test
    fun veganExcludesCheeseAndMeat() {
        val names = recommend.recommend(dishes, "", Diet.VEGAN).map { it.name }
        assertEquals(listOf("Chickpea curry", "Lentil soup"), names)
    }

    @Test
    fun queryMatchesCuisineWithoutMarkup() {
        val names = recommend.recommend(dishes, "<b>south</b>", Diet.ANY).map { it.name }
        assertEquals(listOf("Chickpea curry"), names)
    }

    @Test
    fun unknownQueryIsEmpty() {
        assertEquals(emptyList<Dish>(), recommend.recommend(dishes, "sushi", Diet.ANY))
    }

    private fun dish(
        id: String,
        name: String,
        cuisine: String = "Kitchen",
        diets: Set<Diet>,
    ) = Dish(id, name, cuisine, "A plain description.", diets)
}
