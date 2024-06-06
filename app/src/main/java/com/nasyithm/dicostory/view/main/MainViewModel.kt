package com.nasyithm.dicostory.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.nasyithm.dicostory.data.StoryRepository
import com.nasyithm.dicostory.data.local.entity.Story
import com.nasyithm.dicostory.data.pref.UserModel
import com.nasyithm.dicostory.data.remote.response.ListStoryItem
import kotlinx.coroutines.launch

class MainViewModel(private val repository: StoryRepository) : ViewModel() {
    private val _storiesData = MutableLiveData<List<ListStoryItem>>()
    val storiesData: LiveData<List<ListStoryItem>> = _storiesData

    private val _dataLoaded = MutableLiveData<Boolean>()
    val dataLoaded: LiveData<Boolean> = _dataLoaded

    init {
        setDataLoaded(false)
    }

    fun setDataLoaded(isDataLoaded: Boolean) {
        _dataLoaded.value = isDataLoaded
    }

    fun setStoriesData(stories: List<ListStoryItem>) {
        _storiesData.value = stories
    }

    fun getStories() = repository.getStories()

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun insertStories(stories: Story) {
        viewModelScope.launch {
            repository.insertStories(stories)
        }
    }

    fun deleteAllStories() {
        viewModelScope.launch {
            repository.deleteAllStories()
        }
    }
}