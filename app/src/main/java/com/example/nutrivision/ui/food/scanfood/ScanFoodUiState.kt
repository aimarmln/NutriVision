package com.example.nutrivision.ui.food.scanfood

import com.example.nutrivision.data.remote.response.food.FoodsListResponseItem

sealed class ScanFoodUiState {
    object Idle : ScanFoodUiState()
    object Loading : ScanFoodUiState()
    data class Success(val data: List<FoodsListResponseItem>) : ScanFoodUiState()
    data class Error(val message: String) : ScanFoodUiState()
}