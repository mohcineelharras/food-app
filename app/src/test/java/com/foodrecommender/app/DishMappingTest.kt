package com.foodrecommender.app

import com.foodrecommender.app.data.local.toDish
import com.foodrecommender.app.data.local.toEntity
import com.foodrecommender.app.domain.models.Diet
import com.foodrecommender.app.domain.models.Dish
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class DishMappingTest {
    @Test
    fun dropsUnknownAndAnyTokens() {
        val entity = Dish(
            id = "soup",
            name = "Lentil soup",
            cuisine = "Pantry",
            summary = "Lentils.",
            diets = setOf(Diet.VEGAN, Diet.GLUTEN_FREE),
        ).toEntity()
        val corrupted = entity.copy(diets = "VEGAN,ANY,NOT_A_DIET")
        assertEquals(setOf(Diet.VEGAN), corrupted.toDish().diets)
        assertFalse(entity.diets.contains("ANY"))
    }
}
