package com.example.nutrivision.ui.main.recipe

import com.example.nutrivision.data.remote.response.recipe.RecipesListResponseItem

sealed class RecipeUiState {
    object Idle : RecipeUiState()

    object Loading : RecipeUiState()

    data class Success(
        val data: List<RecipeListItem>,
        val isLoadMore: Boolean = false
    ) : RecipeUiState()

    data class Error(val message: String) : RecipeUiState()
}

sealed class RecipeListItem {
    data class Item(val data: RecipesListResponseItem) : RecipeListItem()
    object Loading : RecipeListItem()
}