package com.nasyithm.dicostory.di

import android.content.Context
import com.nasyithm.dicostory.data.StoryRepository
import com.nasyithm.dicostory.data.local.room.StoryDatabase
import com.nasyithm.dicostory.data.local.pref.UserPreference
import com.nasyithm.dicostory.data.local.pref.dataStore
import com.nasyithm.dicostory.data.remote.retrofit.ApiConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object Injection {
    fun provideRepository(context: Context): StoryRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val user = runBlocking { pref.getSession().first() }
        val apiService = ApiConfig.getApiService(user.token)
        val storyDatabase = StoryDatabase.getInstance(context)
        val storyDao = storyDatabase.storyDao()
        return StoryRepository.getInstance(pref, apiService, storyDao)
    }
}