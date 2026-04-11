package com.example.nutrivision.ui.food.scanfood.scanresult

import com.example.nutrivision.ui.food.FoodListItem

sealed class ScanResultUiState {
    object Idle : ScanResultUiState()

    object Loading : ScanResultUiState()

    data class Success(
        val data: List<FoodListItem>,
        val isLoadMore: Boolean = false
    ) : ScanResultUiState()

    data class Error(val message: String) : ScanResultUiState()
}