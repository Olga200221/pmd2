package com.example.pmd2.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "likes")
data class LikeEntity(
    @PrimaryKey val newsId: Int,
    val likes: Int
)
