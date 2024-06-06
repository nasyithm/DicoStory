package com.nasyithm.dicostory.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nasyithm.dicostory.data.local.entity.Story

@Database(entities = [Story::class], version = 1, exportSchema = false)
abstract class StoryDatabase : RoomDatabase() {
    abstract fun storyDao(): StoryDao

    companion object {
        fun getInstance(context: Context): StoryDatabase = Room.databaseBuilder(
            context.applicationContext,
            StoryDatabase::class.java, "StoryDatabase"
        ).build()
    }
}