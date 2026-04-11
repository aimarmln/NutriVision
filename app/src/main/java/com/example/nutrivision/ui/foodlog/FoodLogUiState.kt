package com.example.nutrivision.ui.foodlog

import com.example.nutrivision.data.remote.response.foodlog.FoodLogDetailResponse

sealed class FoodLogUiState {
    object Idle : FoodLogUiState()
    object Loading : FoodLogUiState()
    data class Success(val data: FoodLogDetailResponse) : FoodLogUiState()
    data class Error(val message: String) : FoodLogUiState()
}

sealed class UpdateFoodLogUiState {
    object Idle : UpdateFoodLogUiState()
    object Loading : UpdateFoodLogUiState()
    data class Success(val message: String) : UpdateFoodLogUiState()
    data class Error(val message: String) : UpdateFoodLogUiState()
}

sealed class DeleteFoodLogUiState {
    object Idle : DeleteFoodLogUiState()
    object Loading : DeleteFoodLogUiState()
    data class Success(val message: String) : DeleteFoodLogUiState()
    data class Error(val message: String) : DeleteFoodLogUiState()
}