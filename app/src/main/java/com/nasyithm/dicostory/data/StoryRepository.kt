package com.nasyithm.dicostory.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.google.gson.Gson
import com.nasyithm.dicostory.data.local.entity.Story
import com.nasyithm.dicostory.data.local.room.StoryDao
import com.nasyithm.dicostory.data.local.pref.UserModel
import com.nasyithm.dicostory.data.local.pref.UserPreference
import com.nasyithm.dicostory.data.remote.response.StoryDetailResponse
import com.nasyithm.dicostory.data.remote.response.ErrorResponse
import com.nasyithm.dicostory.data.remote.response.LoginResponse
import com.nasyithm.dicostory.data.remote.response.StoriesResponse
import com.nasyithm.dicostory.data.remote.retrofit.ApiService
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException

class StoryRepository private constructor(
    private val userPreference: UserPreference,
    private val apiService: ApiService,
    private val storyDao: StoryDao
){
    fun register(name: String, email: String, password: String): LiveData<Result<ErrorResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.register(name, email, password)
            emit(Result.Success(response))
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            val errorMessage = errorBody.message

            Log.d("StoryRepository", "register: $errorMessage")
            emit(Result.Error(errorMessage.toString()))
        }
    }

    fun login(email: String, password: String): LiveData<Result<LoginResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.login(email, password)
            emit(Result.Success(response))
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            val errorMessage = errorBody.message

            Log.d("StoryRepository", "login: $errorMessage")
            emit(Result.Error(errorMessage.toString()))
        }
    }

    fun getStories(): LiveData<Result<StoriesResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.getStories()
            emit(Result.Success(response))
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            val errorMessage = errorBody.message

            Log.d("StoryRepository", "getStories: $errorMessage")
            emit(Result.Error(errorMessage.toString()))
        }
    }

    fun getStoryDetail(storyId: String): LiveData<Result<StoryDetailResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.getStoryDetail(storyId)
            emit(Result.Success(response))
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            val errorMessage = errorBody.message

            Log.d("StoryRepository", "getStories: $errorMessage")
            emit(Result.Error(errorMessage.toString()))
        }
    }

    fun addStory(multipartBody: MultipartBody.Part, requestBody: RequestBody): LiveData<Result<ErrorResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.addStory(multipartBody, requestBody)
            emit(Result.Success(response))
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            val errorMessage = errorBody.message

            Log.d("StoryRepository", "getStories: $errorMessage")
            emit(Result.Error(errorMessage.toString()))
        }
    }

    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }

    suspend fun insertStories(story: Story) = storyDao.insertStories(story)

    suspend fun deleteAllStories() = storyDao.deleteAllStories()

    companion object {
        fun getInstance(
            userPreference: UserPreference,
            apiService: ApiService,
            storyDao: StoryDao
        ): StoryRepository = StoryRepository(userPreference, apiService, storyDao)
    }
}