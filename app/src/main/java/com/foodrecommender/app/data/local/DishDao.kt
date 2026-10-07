package com.foodrecommender.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DishDao {
    @Query("SELECT * FROM dishes")
    suspend fun all(): List<DishEntity>

    @Query("DELETE FROM dishes")
    suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rows: List<DishEntity>)
}
