package com.example.newsreading.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertData(follow: Follow)

    @Query("SELECT * FROM favorites WHERE url = :url LIMIT 1")
    fun getFollowByUrl(url: String): Follow?

    @Query("SELECT * FROM favorites")
    fun getAllData(): Flow<List<Follow>>

    @Query("DELETE FROM favorites WHERE url = :url")
    suspend fun deleteByUrl(url: String)

    @Query("SELECT COUNT(*) FROM favorites WHERE title = :title")
    suspend fun isArticleInFavorites(title: String): Boolean
}