package com.example.nutrivision.ui.recipedetail

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrivision.data.remote.request.recipe.PostRecipeCommentRequest
import com.example.nutrivision.databinding.ActivityRecipeDetailBinding
import com.example.nutrivision.utils.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecipeDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "extra_id"
    }

    private lateinit var binding: ActivityRecipeDetailBinding
    private val viewModel: RecipeDetailViewModel by viewModels()
    private lateinit var adapter: RecipeDetailAdapter
    private lateinit var recipeId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recipeId = recipeId()

        setupToolbar()
        setupRecyclerView()
        observeViewModel()
        setupInfiniteScroll()

        fetchRecipe()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = RecipeDetailAdapter(
            onPostComment = { comment ->
                viewModel.postComment(recipeId(), PostRecipeCommentRequest(comment))
            },
            onDeleteComment = { commentId ->
                viewModel.deleteComment(commentId)
            },
            onTextChanged = {
                viewModel.updateCommentInput(it)
            }
        )

        binding.rvMain.apply {
            layoutManager = LinearLayoutManager(this@RecipeDetailActivity)
            adapter = this@RecipeDetailActivity.adapter
        }
    }

    private fun observeViewModel() {
        viewModel.recipeUiState.observe(this) { recipeState ->
            mergeAndSubmitList(recipeState, viewModel.comments.value ?: emptyList())
        }

        viewModel.comments.observe(this) { comments ->
            mergeAndSubmitList(viewModel.recipeUiState.value ?: RecipeDetailUiState.Idle, comments)
        }

        viewModel.postCommentState.observe(this) { state ->
            when (state) {
                is PostCommentState.Loading -> {
                    adapter.setPostingState(true)
                }

                is PostCommentState.Success -> {
                    showToast(this, "Comment posted")
                    adapter.setPostingState(false)
                }

                is PostCommentState.Error -> {
                    showToast(this, state.message)
                    adapter.setPostingState(false)
                }

                else -> Unit
            }
        }

        viewModel.commentInput.observe(this) { input ->
            adapter.setCommentInput(input)
        }
    }

    private fun mergeAndSubmitList(
        recipeState: RecipeDetailUiState,
        comments: List<CommentListItem>
    ) {
        val items = mutableListOf<RecipeDetailListItem>()

        if (recipeState !is RecipeDetailUiState.Success) {
            items.add(RecipeDetailListItem.ShimmerRecipe)
            items.add(RecipeDetailListItem.ShimmerCommentForm)

            adapter.submitList(items)
            return
        }

        items.add(RecipeDetailListItem.Recipe(recipeState.data))
        items.add(RecipeDetailListItem.CommentInput)

        val realComments = comments.filterIsInstance<CommentListItem.Item>()

        if (realComments.isEmpty()) {
            items.add(RecipeDetailListItem.NoComments)
        } else {
            realComments.forEach {
                items.add(RecipeDetailListItem.Comment(it))
            }
        }

        if (comments.any { it is CommentListItem.Loading }) {
            items.add(RecipeDetailListItem.Loading)
        }

        adapter.submitList(items)
    }

    private fun fetchRecipe() {
        viewModel.fetchRecipeDetail(recipeId)
        viewModel.fetchComments(recipeId)
    }

    private fun recipeId(): String = requireNotNull(intent.getStringExtra(EXTRA_ID))

    private fun setupInfiniteScroll() {
        binding.rvMain.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = rv.layoutManager as LinearLayoutManager
                val totalItem = layoutManager.itemCount
                val lastVisible = layoutManager.findLastVisibleItemPosition()

                if (totalItem <= lastVisible + 3) {
                    // load more comments
                    viewModel.fetchComments(recipeId(), loadMore = true)
                }
            }
        })
    }
}