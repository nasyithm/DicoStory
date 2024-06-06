package com.nasyithm.dicostory.view.main

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.nasyithm.dicostory.R
import com.nasyithm.dicostory.data.remote.response.ListStoryItem
import com.nasyithm.dicostory.databinding.ActivityMainBinding
import com.nasyithm.dicostory.data.Result
import com.nasyithm.dicostory.data.local.entity.Story
import com.nasyithm.dicostory.view.ViewModelFactory
import com.nasyithm.dicostory.view.auth.login.LoginActivity
import com.nasyithm.dicostory.view.story.add.AddStoryActivity
import com.nasyithm.dicostory.view.story.detail.StoryDetailActivity

class MainActivity : AppCompatActivity() {
    private val mainViewModel: MainViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityMainBinding

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            val message = if (isGranted) getString(R.string.camera_permission_granted)
            else getString(R.string.camera_permission_rejected)
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        val layoutManager = LinearLayoutManager(this)
        binding.rvStories.layoutManager = layoutManager

        mainViewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        changeLanguage()
        logout()
        getStoriesData()
        swipeToRefresh()
        showAddStory()
        playAnimation()
    }

    private fun changeLanguage() {
        binding.btnChangeLanguage.setOnClickListener {
            val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
            startActivity(intent)
        }
    }

    private fun logout() {
        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setTitle(getString(R.string.logout))
                setMessage(getString(R.string.confirm_logout))
                setNegativeButton(getString(R.string.no)) { _, _ -> }
                setPositiveButton(getString(R.string.yes)) { _, _ ->
                    mainViewModel.logout()
                }
                create()
                show()
            }
        }
    }

    private fun getStoriesData() {
        mainViewModel.dataLoaded.observe(this) { isDataLoaded ->
            if (!isDataLoaded) {
                getStories()
            } else {
                mainViewModel.storiesData.observe(this) {
                    setStoriesData(it)
                }
            }
        }
    }

    private fun getStories() {
        mainViewModel.getStories().observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    showLoading(true)
                }
                is Result.Success -> {
                    showLoading(false)
                    setStoriesData(result.data.listStory)
                    mainViewModel.setStoriesData(result.data.listStory)
                    mainViewModel.setDataLoaded(true)
                    insertStories(result.data.listStory)
                }
                is Result.Error -> {
                    showLoading(false)
                    AlertDialog.Builder(this).apply {
                        setTitle(getString(R.string.get_data_failed))
                        setMessage(result.error)
                        setPositiveButton(getString(R.string.ok)) { _, _ -> }
                        create()
                        show()
                    }
                    mainViewModel.setDataLoaded(true)
                }
            }
        }
    }

    private fun setStoriesData(stories: List<ListStoryItem>) {
        val adapter = StoriesAdapter()
        adapter.submitList(stories)
        binding.rvStories.adapter = adapter

        adapter.setOnItemClickCallback(object : StoriesAdapter.OnItemClickCallback {
            override fun onItemClicked(data: ListStoryItem) {
                showStoryDetail(data.id.toString())
            }
        })
    }

    private fun swipeToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            getStories()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showStoryDetail(storyId: String) {
        val showStoryDetailIntent = Intent(
            this@MainActivity, StoryDetailActivity::class.java
        )
        showStoryDetailIntent.putExtra(StoryDetailActivity.EXTRA_STORY_ID, storyId)
        startActivity(showStoryDetailIntent)
    }

    private fun showAddStory() {
        binding.btnAddStory.setOnClickListener {
            val addStoryIntent = Intent(
                this@MainActivity, AddStoryActivity::class.java
            )
            startActivity(addStoryIntent)
        }
    }

    private fun insertStories(stories: List<ListStoryItem>) {
        mainViewModel.deleteAllStories()
        stories.forEach { story ->
            val id = story.id.toString()
            val name = story.name.toString()
            val description = story.description.toString()
            val photoUrl = story.photoUrl.toString()
            val createdAt = story.createdAt.toString()
            val lat = story.lat
            val lon = story.lon
            val storyEntity = Story(id, name, description, photoUrl, createdAt, lat, lon)
            mainViewModel.insertStories(storyEntity)
        }
    }

    private fun playAnimation() {
        val tvTitle = ObjectAnimator.ofFloat(binding.tvTitle, View.ALPHA, 1f).setDuration(200)
        val btnLanguage = ObjectAnimator.ofFloat(binding.btnChangeLanguage, View.ALPHA, 1f).setDuration(200)
        val btnLogout = ObjectAnimator.ofFloat(binding.btnLogout, View.ALPHA, 1f).setDuration(200)
        val swipeRefresh = ObjectAnimator.ofFloat(binding.swipeRefreshLayout, View.ALPHA, 1f).setDuration(200)
        val btnAddStory = ObjectAnimator.ofFloat(binding.btnAddStory, View.ALPHA, 1f).setDuration(200)

        AnimatorSet().apply {
            playSequentially(tvTitle, btnLanguage, btnLogout, swipeRefresh, btnAddStory)
            startDelay = 100
        }.start()
    }
}