package com.foodrecommender.app.presentation.views

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.foodrecommender.app.FoodApplication
import com.foodrecommender.app.R
import com.foodrecommender.app.databinding.ActivityMainBinding
import com.foodrecommender.app.domain.models.Diet
import com.foodrecommender.app.presentation.adapters.DishAdapter
import com.foodrecommender.app.presentation.viewmodels.Notice
import com.foodrecommender.app.presentation.viewmodels.RecommendViewModel
import com.foodrecommender.app.presentation.viewmodels.UiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: RecommendViewModel
    private val adapter = DishAdapter()
    private var rendering = false
    private var noteDirty = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyInsets()
        binding.dishes.adapter = adapter

        val container = (application as FoodApplication).container
        viewModel = RecommendViewModel(
            loadDishes = { container.catalog.dishes() },
            loadPreference = { withContext(Dispatchers.IO) { container.preferences.read() } },
            savePreference = { preference -> container.preferences.write(preference) },
            recommend = container.recommend,
            scope = lifecycleScope,
            writeScope = container.preferenceScope,
        )

        binding.search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                viewModel.onQuery(s?.toString().orEmpty())
            }
        })
        binding.diets.setOnCheckedStateChangeListener { group, _ ->
            if (rendering) return@setOnCheckedStateChangeListener
            val state = viewModel.state.value
            if (state.loading || state.notice == Notice.LOAD_FAILED) {
                rendering = true
                group.check(chipFor(state.diet))
                rendering = false
                return@setOnCheckedStateChangeListener
            }
            viewModel.onDiet(dietFromChip(group.checkedChipId))
        }
        binding.note.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable?) {
                if (!rendering) noteDirty = true
            }
        })
        binding.saveNote.setOnClickListener {
            viewModel.onNote(binding.note.text?.toString().orEmpty())
            noteDirty = false
            binding.note.clearFocus()
        }

        if (savedInstanceState != null) {
            binding.search.setText(savedInstanceState.getString(KEY_QUERY).orEmpty())
            if (savedInstanceState.getBoolean(KEY_NOTE_DIRTY)) {
                viewModel.restoreUnsavedNote(savedInstanceState.getString(KEY_NOTE).orEmpty())
            }
            val restoredDiet = savedInstanceState.getString(KEY_DIET)
            Diet.entries.firstOrNull { it.name == restoredDiet }?.let(viewModel::restoreSelection)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { render(it) }
            }
        }
        viewModel.start()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_QUERY, binding.search.text?.toString().orEmpty())
        outState.putBoolean(KEY_NOTE_DIRTY, noteDirty)
        outState.putString(KEY_NOTE, binding.note.text?.toString().orEmpty())
        val state = viewModel.state.value
        if (!state.loading && state.notice != Notice.LOAD_FAILED) {
            outState.putString(KEY_DIET, state.diet.name)
        }
        super.onSaveInstanceState(outState)
    }

    private fun applyInsets() {
        val extra = (16 * resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime(),
            )
            view.setPadding(
                bars.left + extra,
                bars.top + extra,
                bars.right + extra,
                bars.bottom + extra,
            )
            insets
        }
    }

    private fun render(state: UiState) {
        rendering = true
        val chip = chipFor(state.diet)
        if (binding.diets.checkedChipId != chip) {
            binding.diets.check(chip)
        }
        rendering = false

        adapter.submitList(state.dishes)
        val inputsReady = !state.loading && state.notice != Notice.LOAD_FAILED
        binding.search.isEnabled = inputsReady
        binding.note.isEnabled = inputsReady
        binding.saveNote.isEnabled = inputsReady
        listOf(
            binding.dietAny,
            binding.dietVegetarian,
            binding.dietVegan,
            binding.dietGlutenFree,
        ).forEach { chip -> chip.isEnabled = inputsReady }
        binding.progress.isVisible = state.loading
        binding.empty.isVisible = inputsReady && state.dishes.isEmpty()
        binding.dishes.isVisible = state.dishes.isNotEmpty()
        binding.error.isVisible = state.notice != Notice.NONE
        binding.error.text = when (state.notice) {
            Notice.LOAD_FAILED -> getString(R.string.load_failed)
            Notice.SAVE_FAILED -> getString(R.string.save_failed)
            Notice.NONE -> ""
        }
        if (!noteDirty && !binding.note.isFocused && binding.note.text?.toString() != state.note) {
            rendering = true
            binding.note.setText(state.note)
            rendering = false
        }
    }

    private fun dietFromChip(id: Int): Diet = when (id) {
        R.id.diet_vegetarian -> Diet.VEGETARIAN
        R.id.diet_vegan -> Diet.VEGAN
        R.id.diet_gluten_free -> Diet.GLUTEN_FREE
        else -> Diet.ANY
    }

    private fun chipFor(diet: Diet): Int = when (diet) {
        Diet.VEGETARIAN -> R.id.diet_vegetarian
        Diet.VEGAN -> R.id.diet_vegan
        Diet.GLUTEN_FREE -> R.id.diet_gluten_free
        Diet.ANY -> R.id.diet_any
    }

    private companion object {
        const val KEY_QUERY = "query"
        const val KEY_NOTE = "note"
        const val KEY_NOTE_DIRTY = "note_dirty"
        const val KEY_DIET = "diet"
    }
}
