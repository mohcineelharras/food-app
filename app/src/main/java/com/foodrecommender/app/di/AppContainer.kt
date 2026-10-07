package com.foodrecommender.app.di

import android.content.Context
import androidx.room.Room
import com.foodrecommender.app.data.local.AppDatabase
import com.foodrecommender.app.data.local.CatalogSeed
import com.foodrecommender.app.data.local.PreferenceFileStore
import com.foodrecommender.app.data.repository.RoomDishCatalog
import com.foodrecommender.app.domain.usecases.RecommendDishes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        AppDatabase.NAME,
    ).build()

    val catalog = RoomDishCatalog(database, CatalogSeed.dishes)
    val preferences = PreferenceFileStore(appContext.filesDir)
    val recommend = RecommendDishes()

    /**
     * Outlives the activity so a rotation cannot cancel a preference write.
     * One worker keeps snapshots ordered.
     */
    val preferenceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))
}
