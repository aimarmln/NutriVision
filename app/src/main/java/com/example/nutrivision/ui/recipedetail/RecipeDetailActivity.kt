package com.example.nutrivision.ui.recipedetail

import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutrivision.data.local.SettingPreferences
import com.example.nutrivision.data.local.dataStore
import com.example.nutrivision.data.remote.request.CommentRequest
import com.example.nutrivision.databinding.ActivityRecipeDetailBinding
import com.example.nutrivision.utils.showToast
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RecipeDetailActivity: AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "EXTRA_ID"
    }

    private val recipeDetailViewModel: RecipeDetailViewModel by viewModels {
        RecipeDetailViewModelFactory(application)
    }

    private lateinit var commentsAdapter: CommentsAdapter

    private lateinit var binding: ActivityRecipeDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        commentsAdapter = CommentsAdapter()
        binding.rvComments.layoutManager = LinearLayoutManager(this)
        binding.rvComments.adapter = commentsAdapter
        Log.d("RecipeDetailActivity", "RecyclerView adapter and layout manager has been managed")

        val recipeId = intent.getIntExtra(EXTRA_ID, 0)
        lifecycleScope.launch {
            recipeDetailViewModel.fetchRecipeDetail(recipeId)
        }

        recipeDetailViewModel.recipeDetailData.observe(this) { recipeDetail ->
            if (recipeDetail != null) {
                Log.d("RecipeDetailActivity", "Data for RecyclerView: $recipeDetail")

                val context = binding.root.context
                val imageResId = context.resources.getIdentifier(
                    "recipe_${recipeDetail.id}",
                    "drawable",
                    context.packageName
                )
                binding.recipeImage.setImageResource(imageResId)

                binding.recipeName.text = recipeDetail.recipeName ?: "Unknown recipe name"
                binding.recipeDescription.text = recipeDetail.description ?: "Unknown description"
                binding.recipeServing.text = "${recipeDetail.servingYield} Servings" ?: "Unknown serving"

                if (recipeDetail.healthCategory == "Unhealthy") {
                    binding.recipeHealthCategory.visibility = GONE
                }

                binding.recipeIngredients.text = arrayToString(recipeDetail.ingredients)
                binding.recipeInstructions.text = arrayToString(recipeDetail.instructions)

                if (!recipeDetail.comments.isNullOrEmpty()) {
                    val sortedComments = recipeDetail.comments.sortedBy { it?.id }
                    commentsAdapter.submitList(sortedComments)
                    binding.noComments.visibility = GONE
                } else {
                    binding.noComments.visibility = VISIBLE
                    commentsAdapter.submitList(emptyList())
                }
            } else {
                Log.d("RecipeDetailActivity", "Data is empty for RecyclerView")
                commentsAdapter.submitList(emptyList())
            }
        }

        val pref = SettingPreferences.getInstance(application.dataStore)

        binding.btnComment.setOnClickListener {
            val text = binding.edtComment.text.toString().trim()

            binding.edtComment.error = null

            if (text.isEmpty()) {
                binding.edtComment.error = "Comment cannot be empty"
                return@setOnClickListener
            }

            if (text.all { it.isDigit() }) {
                binding.edtComment.error = "Comment cannot contain only numbers"
                return@setOnClickListener
            }

            if (!text.any { it.isLetter() }) {
                binding.edtComment.error = "Comment must contain at least one letter"
                return@setOnClickListener
            }

            val commentRequest = CommentRequest(
                recipeId = recipeId,
                text = text
            )

            lifecycleScope.launch {
                val accessToken = pref.accessToken.first() ?: "Unknown access token"
                val refreshToken = pref.refreshToken.first() ?: "Unknown refresh token"

                recipeDetailViewModel.comment(
                    accessToken,
                    refreshToken,
                    commentRequest
                ) { success, message ->
                    if (success) {
                        runOnUiThread {
                            showToast(this@RecipeDetailActivity, message ?: "Comment posted successfully")
                            recipeDetailViewModel.fetchRecipeDetail(recipeId)
                        }
                    } else {
                        runOnUiThread {
                            showToast(this@RecipeDetailActivity, message ?: "Comment failed to post")
                        }
                    }
                    binding.edtComment.text = null
                }
            }
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun arrayToString(array: List<String?>?): String {
        return array?.joinToString("\n\n") { "• ${it ?: "Unknown item"}" } ?: "Unknown items"
    }
}