package com.nasyithm.dicostory.view.auth.register

import androidx.lifecycle.ViewModel
import com.nasyithm.dicostory.data.StoryRepository

class RegisterViewModel(private val repository: StoryRepository) : ViewModel() {
    fun register(name: String, email: String, password: String) = repository.register(name, email, password)
}