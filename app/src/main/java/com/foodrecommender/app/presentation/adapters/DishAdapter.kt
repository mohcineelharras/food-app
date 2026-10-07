package com.foodrecommender.app.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.foodrecommender.app.R
import com.foodrecommender.app.databinding.ItemDishBinding
import com.foodrecommender.app.domain.models.Diet
import com.foodrecommender.app.domain.models.Dish
import com.foodrecommender.app.domain.usecases.PlainText

class DishAdapter : ListAdapter<Dish, DishAdapter.Holder>(DIFF) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemDishBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    class Holder(private val binding: ItemDishBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(dish: Dish) {
            val context = binding.root.context
            binding.name.text = PlainText.sanitize(dish.name, PlainText.NOTE_LIMIT)
            binding.cuisine.text = PlainText.sanitize(dish.cuisine, PlainText.NOTE_LIMIT)
            binding.summary.text = PlainText.sanitize(dish.summary, PlainText.NOTE_LIMIT)
            binding.diets.text = dietLabel(dish.diets)
            binding.root.contentDescription = context.getString(
                R.string.dish_row_description,
                binding.name.text,
                binding.cuisine.text,
            )
        }

        private fun dietLabel(diets: Set<Diet>): String {
            val context = binding.root.context
            if (diets.isEmpty()) return context.getString(R.string.no_diet_tags)
            return diets
                .sortedBy { it.name }
                .joinToString(", ") { diet ->
                    when (diet) {
                        Diet.VEGETARIAN -> context.getString(R.string.diet_vegetarian)
                        Diet.VEGAN -> context.getString(R.string.diet_vegan)
                        Diet.GLUTEN_FREE -> context.getString(R.string.diet_gluten_free)
                        Diet.ANY -> context.getString(R.string.diet_any)
                    }
                }
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<Dish>() {
            override fun areItemsTheSame(oldItem: Dish, newItem: Dish): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Dish, newItem: Dish): Boolean = oldItem == newItem
        }
    }
}
