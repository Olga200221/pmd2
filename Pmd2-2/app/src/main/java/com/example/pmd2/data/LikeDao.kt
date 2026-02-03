package com.example.pmd2.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LikeDao {

    @Query("SELECT likes FROM likes WHERE newsId = :newsId")
    suspend fun getLikes(newsId: Int): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLikes(entity: LikeEntity)
}
