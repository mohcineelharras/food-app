package com.foodrecommender.app.data.local

import com.foodrecommender.app.domain.models.Diet
import com.foodrecommender.app.domain.models.Dish

object CatalogSeed {
    val dishes: List<Dish> = listOf(
        Dish(
            id = "margherita",
            name = "Margherita pizza",
            cuisine = "Italian",
            summary = "Tomato, mozzarella, and basil on a baked crust.",
            diets = setOf(Diet.VEGETARIAN),
        ),
        Dish(
            id = "lentil-soup",
            name = "Lentil soup",
            cuisine = "Pantry",
            summary = "Lentils simmered with vegetables.",
            diets = setOf(Diet.VEGAN, Diet.GLUTEN_FREE),
        ),
        Dish(
            id = "chickpea-curry",
            name = "Chickpea curry",
            cuisine = "South Asian",
            summary = "Chickpeas in a spiced tomato sauce.",
            diets = setOf(Diet.VEGAN, Diet.GLUTEN_FREE),
        ),
        Dish(
            id = "greek-salad",
            name = "Greek salad",
            cuisine = "Mediterranean",
            summary = "Tomato, cucumber, olives, and feta.",
            diets = setOf(Diet.VEGETARIAN, Diet.GLUTEN_FREE),
        ),
        Dish(
            id = "tofu-stir-fry",
            name = "Tofu vegetable stir fry",
            cuisine = "East Asian",
            summary = "Tofu and vegetables cooked in a pan.",
            diets = setOf(Diet.VEGAN),
        ),
        Dish(
            id = "chicken-salad",
            name = "Grilled chicken salad",
            cuisine = "Kitchen",
            summary = "Greens with grilled chicken.",
            diets = setOf(Diet.GLUTEN_FREE),
        ),
        Dish(
            id = "beef-taco",
            name = "Beef taco",
            cuisine = "Mexican",
            summary = "Seasoned beef in a tortilla.",
            diets = emptySet(),
        ),
        Dish(
            id = "cheese-omelette",
            name = "Cheese omelette",
            cuisine = "Kitchen",
            summary = "Eggs cooked with cheese.",
            diets = setOf(Diet.VEGETARIAN, Diet.GLUTEN_FREE),
        ),
    )
}
