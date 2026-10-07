package com.foodrecommender.app

import com.foodrecommender.app.domain.models.Diet
import com.foodrecommender.app.domain.models.Dish
import com.foodrecommender.app.domain.models.UserPreference
import com.foodrecommender.app.domain.usecases.RecommendDishes
import com.foodrecommender.app.presentation.viewmodels.Notice
import com.foodrecommender.app.presentation.viewmodels.RecommendViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendViewModelTest {
    @Test
    fun loadsThenFiltersAndStoresAPlainNote() {
        val saved = mutableListOf<UserPreference>()
        val model = model(
            scope = background(),
            dishes = listOf(
                Dish("soup", "Lentil soup", "Pantry", "Lentils.", setOf(Diet.VEGAN)),
                Dish("taco", "Beef taco", "Mexican", "Beef.", emptySet()),
            ),
            onSave = { saved += it },
        )
        model.start()
        assertFalse(model.state.value.loading)
        assertEquals(2, model.state.value.dishes.size)

        model.onDiet(Diet.VEGAN)
        assertEquals(listOf("Lentil soup"), model.state.value.dishes.map { it.name })

        model.onNote("<script>alert(1)</script>")
        assertEquals("alert(1)", model.state.value.note)
        assertEquals("alert(1)", saved.last().note)
        assertEquals(Diet.VEGAN, saved.last().diet)
    }

    @Test
    fun ignoresDietChangesUntilTheCatalogLoads() {
        val model = model(scope = background(), dishes = emptyList(), onSave = {})
        model.onDiet(Diet.VEGAN)
        assertTrue(model.state.value.loading)
        assertEquals(Diet.ANY, model.state.value.diet)
    }

    @Test
    fun loadFailureDoesNotExposeTheException() {
        val model = RecommendViewModel(
            loadDishes = { error("database path /secret/food.db") },
            loadPreference = { UserPreference(Diet.ANY, "") },
            savePreference = {},
            recommend = RecommendDishes(),
            scope = background(),
            writeScope = background(),
        )
        model.start()
        assertEquals(Notice.LOAD_FAILED, model.state.value.notice)
        assertTrue(model.state.value.dishes.isEmpty())
    }

    @Test
    fun unsavedNoteSurvivesTheLoad() {
        val model = model(
            scope = background(),
            dishes = emptyList(),
            saved = UserPreference(Diet.GLUTEN_FREE, "from disk"),
            onSave = {},
        )
        model.restoreUnsavedNote("<b>draft</b>")
        model.restoreSelection(Diet.VEGAN)
        model.start()
        assertEquals("draft", model.state.value.note)
        assertEquals(Diet.VEGAN, model.state.value.diet)
    }

    private fun background(): CoroutineScope = CoroutineScope(Dispatchers.Unconfined)

    private fun model(
        scope: CoroutineScope,
        dishes: List<Dish>,
        saved: UserPreference = UserPreference(Diet.ANY, ""),
        onSave: (UserPreference) -> Unit,
    ): RecommendViewModel {
        return RecommendViewModel(
            loadDishes = { dishes },
            loadPreference = { saved },
            savePreference = { onSave(it) },
            recommend = RecommendDishes(),
            scope = scope,
            writeScope = scope,
        )
    }
}
