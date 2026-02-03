package com.example.pmd2.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [LikeEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun likeDao(): LikeDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pmd2.db"
                ).build().also { INSTANCE = it }
            }
    }
}
