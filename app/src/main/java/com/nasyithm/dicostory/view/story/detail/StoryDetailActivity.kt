package com.nasyithm.dicostory.view.story.detail

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.nasyithm.dicostory.R
import com.nasyithm.dicostory.data.Result
import com.nasyithm.dicostory.data.remote.response.StoryDetail
import com.nasyithm.dicostory.databinding.ActivityStoryDetailBinding
import com.nasyithm.dicostory.view.ViewModelFactory

class StoryDetailActivity : AppCompatActivity() {
    private val storyDetailViewModel: StoryDetailViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityStoryDetailBinding
    private lateinit var storyId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryDetailBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        storyId = intent.getStringExtra(EXTRA_STORY_ID).toString()

        getStoryDetailData()
    }

    private fun getStoryDetailData() {
        storyDetailViewModel.dataLoaded.observe(this) { isDataLoaded ->
            if (!isDataLoaded) {
                getStories()
            } else {
                storyDetailViewModel.storyDetailData.observe(this) {
                    setStoryDetailData(it)
                }
            }
        }
    }

    private fun getStories() {
        storyDetailViewModel.getStoryDetail(storyId).observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    showLoading(true)
                }
                is Result.Success -> {
                    showLoading(false)
                    result.data.story?.let {
                        setStoryDetailData(it)
                        storyDetailViewModel.setStoryDetailData(it)
                    }
                    storyDetailViewModel.setDataLoaded(true)
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
                    storyDetailViewModel.setDataLoaded(true)
                }
            }
        }
    }

    private fun setStoryDetailData(storyDetail: StoryDetail) {
        Glide.with(binding.root)
            .load(storyDetail.photoUrl)
            .into(binding.ivItemPhoto)
        binding.tvItemName.text = storyDetail.name
        binding.tvItemDescription.text = storyDetail.description
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    companion object {
        const val EXTRA_STORY_ID = "extra_story_id"
    }
}