package com.example.nutrivision.ui.recipedetail

import com.example.nutrivision.data.remote.response.recipe.CommentItem
import com.example.nutrivision.data.remote.response.recipe.RecipeDetailResponse

sealed class RecipeDetailUiState {
    object Idle : RecipeDetailUiState()
    object Loading : RecipeDetailUiState()
    data class Success(val data: RecipeDetailResponse) : RecipeDetailUiState()
    data class Error(val message: String) : RecipeDetailUiState()
}

sealed class RecipeCommentsUiState {
    object Idle : RecipeCommentsUiState()
    object Loading : RecipeCommentsUiState()

    data class Success(
        val data: List<CommentListItem>,
        val isLoadMore: Boolean = false
    ) : RecipeCommentsUiState()

    data class Error(val message: String) : RecipeCommentsUiState()
}

sealed class PostCommentState {
    object Idle : PostCommentState()
    object Loading : PostCommentState()
    object Success : PostCommentState()
    data class Error(val message: String) : PostCommentState()
}

sealed class CommentListItem {
    data class Item(
        val data: CommentItem,
        val isDeleting: Boolean = false
    ) : CommentListItem()

    object Loading : CommentListItem()
}