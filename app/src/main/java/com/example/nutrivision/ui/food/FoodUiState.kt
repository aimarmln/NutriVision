package com.example.nutrivision.ui.food

import com.example.nutrivision.data.remote.response.food.FoodsListResponseItem

sealed class FoodUiState {
    object Idle : FoodUiState()

    object Loading : FoodUiState()

    data class Success(
        val data: List<FoodListItem>,
        val isLoadMore: Boolean = false
    ) : FoodUiState()

    data class Error(val message: String) : FoodUiState()
}

sealed class FoodListItem {
    data class Item(
        val data: FoodsListResponseItem,
        val isLogging: Boolean = false,
        val isLogSuccess: Boolean = false,
        val isLogError: Boolean = false
    ) : FoodListItem()

    object Loading : FoodListItem()
}
