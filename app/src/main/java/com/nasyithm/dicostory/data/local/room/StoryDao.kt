package com.nasyithm.dicostory.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nasyithm.dicostory.data.local.entity.Story

@Dao
interface StoryDao {
    @Query("SELECT * FROM story")
    fun getAllStories(): List<Story>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertStories(stories: Story)

    @Query("DELETE FROM story")
    suspend fun deleteAllStories()
}
