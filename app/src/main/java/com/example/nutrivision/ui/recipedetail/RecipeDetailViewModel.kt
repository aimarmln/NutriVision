package com.example.nutrivision.ui.recipedetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrivision.data.remote.request.recipe.PostRecipeCommentRequest
import com.example.nutrivision.data.remote.response.Cursor
import com.example.nutrivision.data.remote.response.recipe.CommentItem
import com.example.nutrivision.data.repository.CommentRepository
import com.example.nutrivision.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val commentRepository: CommentRepository
) : ViewModel() {

    // Recipe detail UI state
    private val _recipeUiState = MutableLiveData<RecipeDetailUiState>(RecipeDetailUiState.Idle)
    val recipeUiState: LiveData<RecipeDetailUiState> = _recipeUiState

    private val _postCommentState = MutableLiveData<PostCommentState>()
    val postCommentState: LiveData<PostCommentState> = _postCommentState

    private val _commentInput = MutableLiveData("")
    val commentInput: LiveData<String> = _commentInput

    // Comments list & pagination
    private val _comments = MutableLiveData<List<CommentListItem>>(emptyList())
    val comments: LiveData<List<CommentListItem>> = _comments

    private var lastCursor: Cursor? = null
    private var isLastPage = false
    private var isLoading = false
    private val accumulatedComments = mutableListOf<CommentListItem>()

    // Fetch recipe detail
    fun fetchRecipeDetail(recipeId: String) {
        _recipeUiState.value = RecipeDetailUiState.Loading

        viewModelScope.launch {
            recipeRepository.getRecipeDetail(recipeId)
                .onSuccess {
                    val data = it.data ?: return@onSuccess
                    _recipeUiState.value = RecipeDetailUiState.Success(data)
                }
                .onFailure {
                    _recipeUiState.value =
                        RecipeDetailUiState.Error(it.message ?: "Failed to load recipe")
                }
        }
    }

    // Fetch comments (paginated)
    fun fetchComments(recipeId: String, loadMore: Boolean = false) {
        if (loadMore && (isLoading || isLastPage)) return

        if (!loadMore) {
            lastCursor = null
            isLastPage = false
            accumulatedComments.clear()
        } else {
            accumulatedComments.add(CommentListItem.Loading)
            _comments.value = accumulatedComments.toList()
        }

        isLoading = true

        viewModelScope.launch {
            recipeRepository.getRecipeComments(recipeId, lastCursor?.createdAt, limit = 10)
                .onSuccess { response ->
                    if (loadMore) removeLoadingFooter()

                    response.data?.map { CommentListItem.Item(it) }?.let {
                        accumulatedComments.addAll(it)
                    }

                    lastCursor = response.pagination?.nextCursor
                    if (response.pagination?.hasMore == false) isLastPage = true

                    _comments.value = accumulatedComments.toList()
                    isLoading = false
                }
                .onFailure {
                    if (loadMore) removeLoadingFooter()
                    isLoading = false
                }
        }
    }

    private fun removeLoadingFooter() {
        if (accumulatedComments.isNotEmpty() &&
            accumulatedComments.last() is CommentListItem.Loading
        ) accumulatedComments.removeAt(accumulatedComments.lastIndex)
    }

    fun postComment(recipeId: String, body: PostRecipeCommentRequest) {
        _postCommentState.value = PostCommentState.Loading

        viewModelScope.launch {
            recipeRepository.postRecipeComment(recipeId, body)
                .onSuccess {
                    val data = it.data ?: return@onSuccess

                    val commentItem = CommentItem(
                        id = data.id,
                        name = data.userName,
                        text = data.comment,
                        sentiment = data.sentiment,
                        createdAt = data.createdAt,
                        isOwnComment = true
                    )

                    accumulatedComments.add(0, CommentListItem.Item(commentItem))
                    _comments.value = accumulatedComments.toList()

                    _postCommentState.value = PostCommentState.Success

                    val currentState = _recipeUiState.value
                    if (currentState is RecipeDetailUiState.Success) {
                        val currentRecipe = currentState.data

                        val updatedLikes =
                            if (data.sentiment == "Positive")
                                currentRecipe.positiveCommentCount + 1
                            else
                                currentRecipe.positiveCommentCount

                        val updatedRecipe = currentRecipe.copy(
                            positiveCommentCount = updatedLikes
                        )

                        _commentInput.value = ""
                        _recipeUiState.value = RecipeDetailUiState.Success(updatedRecipe)
                    }
                }
                .onFailure {
                    _postCommentState.value =
                        PostCommentState.Error(it.message ?: "Failed to post comment")
                }
        }
    }

    fun deleteComment(commentId: String) {
        val updated = accumulatedComments.map {
            if (it is CommentListItem.Item && it.data.id == commentId) {
                it.copy(isDeleting = true)
            } else it
        }

        accumulatedComments.clear()
        accumulatedComments.addAll(updated)
        _comments.value = accumulatedComments.toList()

        viewModelScope.launch {
            commentRepository.deleteComment(commentId)
                .onSuccess {
                    accumulatedComments.removeAll {
                        it is CommentListItem.Item && it.data.id == commentId
                    }

                    _comments.value = accumulatedComments.toList()
                }
                .onFailure {
                    val rollback = accumulatedComments.map {
                        if (it is CommentListItem.Item && it.data.id == commentId) {
                            it.copy(isDeleting = false)
                        } else it
                    }

                    accumulatedComments.clear()
                    accumulatedComments.addAll(rollback)
                    _comments.value = accumulatedComments.toList()
                }
        }
    }

    fun updateCommentInput(text: String) {
        _commentInput.value = text
    }
}
