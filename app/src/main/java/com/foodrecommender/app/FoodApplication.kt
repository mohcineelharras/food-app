package com.foodrecommender.app

import android.app.Application
import com.foodrecommender.app.di.AppContainer

class FoodApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
