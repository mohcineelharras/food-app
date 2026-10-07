package com.foodrecommender.app.presentation.viewmodels

import com.foodrecommender.app.domain.models.Diet
import com.foodrecommender.app.domain.models.Dish
import com.foodrecommender.app.domain.models.UserPreference
import com.foodrecommender.app.domain.usecases.PlainText
import com.foodrecommender.app.domain.usecases.RecommendDishes
import java.util.concurrent.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Notice {
    NONE,
    LOAD_FAILED,
    SAVE_FAILED,
}

data class UiState(
    val loading: Boolean = true,
    val dishes: List<Dish> = emptyList(),
    val diet: Diet = Diet.ANY,
    val note: String = "",
    val notice: Notice = Notice.NONE,
)

class RecommendViewModel(
    private val loadDishes: suspend () -> List<Dish>,
    private val loadPreference: suspend () -> UserPreference,
    private val savePreference: suspend (UserPreference) -> Unit,
    private val recommend: RecommendDishes,
    private val scope: CoroutineScope,
    private val writeScope: CoroutineScope,
) {
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var cache: List<Dish> = emptyList()
    private var query: String = ""
    private var diet: Diet = Diet.ANY
    private var note: String = ""
    private var dietTouched = false
    private var noteTouched = false
    private var loaded = false
    private var writeGeneration = 0

    fun start() {
        scope.launch {
            _state.update { it.copy(loading = true, notice = Notice.NONE) }
            try {
                cache = loadDishes()
                val saved = loadPreference()
                if (!dietTouched) diet = saved.diet
                if (!noteTouched) note = saved.note
                loaded = true
                publish(Notice.NONE, loading = false)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                loaded = false
                _state.update {
                    it.copy(loading = false, dishes = emptyList(), notice = Notice.LOAD_FAILED)
                }
            }
        }
    }

    fun restoreUnsavedNote(value: String) {
        noteTouched = true
        note = PlainText.sanitize(value, PlainText.NOTE_LIMIT)
    }

    fun restoreSelection(selected: Diet) {
        dietTouched = true
        diet = selected
    }

    fun onQuery(value: String) {
        query = PlainText.sanitize(value, PlainText.QUERY_LIMIT)
        if (loaded) publish(currentNotice(), loading = false)
    }

    fun onDiet(value: Diet) {
        if (!loaded) return
        dietTouched = true
        diet = value
        persist()
        publish(Notice.NONE, loading = false)
    }

    fun onNote(value: String) {
        if (!loaded) return
        noteTouched = true
        note = PlainText.sanitize(value, PlainText.NOTE_LIMIT)
        persist()
        publish(Notice.NONE, loading = false)
    }

    private fun persist() {
        val snapshot = UserPreference(diet, note)
        val generation = ++writeGeneration
        writeScope.launch {
            try {
                savePreference(snapshot)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (generation == writeGeneration) {
                    _state.update { it.copy(notice = Notice.SAVE_FAILED) }
                }
            }
        }
    }

    private fun currentNotice(): Notice = _state.value.notice

    private fun publish(notice: Notice, loading: Boolean) {
        _state.update {
            it.copy(
                loading = loading,
                dishes = recommend.recommend(cache, query, diet),
                diet = diet,
                note = note,
                notice = notice,
            )
        }
    }
}
