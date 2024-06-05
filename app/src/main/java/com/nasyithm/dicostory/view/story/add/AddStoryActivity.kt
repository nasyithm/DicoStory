package com.nasyithm.dicostory.view.story.add

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nasyithm.dicostory.R
import com.nasyithm.dicostory.data.Result
import com.nasyithm.dicostory.databinding.ActivityAddStoryBinding
import com.nasyithm.dicostory.util.getImageUri
import com.nasyithm.dicostory.util.reduceFileImage
import com.nasyithm.dicostory.util.uriToFile
import com.nasyithm.dicostory.view.ViewModelFactory
import com.nasyithm.dicostory.view.main.MainActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AddStoryActivity : AppCompatActivity() {
    private val addStoryViewModel: AddStoryViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityAddStoryBinding

    private var currentImageUri: Uri? = null
    private var tempImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        getImageUriData()
        startGallery()
        startCamera()
        addStory()
        playAnimation()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun getImageUriData() {
        if (currentImageUri == null) {
            addStoryViewModel.imageUriData.observe(this) { uri ->
                currentImageUri = uri
                showImage()
            }
        }
    }

    private fun startGallery() {
        binding.btnGallery.setOnClickListener {
            launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
            addStoryViewModel.setImageUriData(uri)
        }
    }

    private fun startCamera() {
        binding.btnCamera.setOnClickListener {
            timeStamp = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(Date())
            tempImageUri = currentImageUri
            currentImageUri = getImageUri(this)
            launcherIntentCamera.launch(currentImageUri!!)
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
            addStoryViewModel.setImageUriData(currentImageUri!!)
        } else {
            currentImageUri = tempImageUri
        }
    }

    private fun showImage() {
        currentImageUri?.let { uri ->
            binding.ivPreview.setImageURI(uri)
        }
    }

    private fun addStory() {
        binding.btnUpload.setButtonText(getString(R.string.upload))
        binding.btnUpload.addOnButtonClickListener {
            currentImageUri?.let { uri ->
                val description = binding.etDescription.text.toString()
                if (description.isNotEmpty()) {
                    showLoading(true)

                    val imageFile = uriToFile(uri, this).reduceFileImage()
                    Log.d("Image File", "showImage: ${imageFile.path}")
                    val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
                    val requestBody = description.toRequestBody("text/plain".toMediaType())
                    val multipartBody = MultipartBody.Part.createFormData(
                        "photo",
                        imageFile.name,
                        requestImageFile
                    )
                    addStoryViewModel.addStory(multipartBody, requestBody).observe(this) { result ->
                        when (result) {
                            is Result.Loading -> {}
                            is Result.Success -> {
                                showLoading(false)
                                AlertDialog.Builder(this).apply {
                                    setTitle(getString(R.string.succeed))
                                    setMessage(getString(R.string.story_added))
                                    setNegativeButton(getString(R.string.back)) { _, _ -> }
                                    setPositiveButton(getString(R.string.list_story)) { _, _ ->
                                        val intent = Intent(this@AddStoryActivity, MainActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                        startActivity(intent)
                                    }
                                    create()
                                    show()
                                }
                            }

                            is Result.Error -> {
                                showLoading(false)
                                showErrorAlertDialog(result.error)
                            }
                        }
                    }
                } else {
                    showErrorAlertDialog(getString(R.string.input_desc_first))
                }
            } ?: showErrorAlertDialog(getString(R.string.input_image_first))
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.btnUpload.startLoading(getString(R.string.loading))
        } else {
            binding.btnUpload.stopLoading()
        }
    }

    private fun showErrorAlertDialog(message: String) {
        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.failed))
            setMessage(message)
            setPositiveButton(getString(R.string.ok)) { _, _ -> }
            create()
            show()
        }
    }

    private fun playAnimation() {
        val ivPreview = ObjectAnimator.ofFloat(binding.ivPreview, View.ALPHA, 1f).setDuration(200)
        val btnGallery = ObjectAnimator.ofFloat(binding.btnGallery, View.ALPHA, 1f).setDuration(200)
        val btnCamera = ObjectAnimator.ofFloat(binding.btnCamera, View.ALPHA, 1f).setDuration(200)
        val letDescription = ObjectAnimator.ofFloat(binding.letDescription, View.ALPHA, 1f).setDuration(200)
        val btnUpload = ObjectAnimator.ofFloat(binding.btnUpload, View.ALPHA, 1f).setDuration(200)

        val together = AnimatorSet().apply {
            playTogether(btnGallery, btnCamera)
        }

        AnimatorSet().apply {
            playSequentially(ivPreview, together, letDescription, btnUpload)
            startDelay = 100
        }.start()
    }

    companion object {
        private const val FILENAME_FORMAT = "yyyyMMdd_HHmmss"
        var timeStamp: String = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(Date())
    }
}