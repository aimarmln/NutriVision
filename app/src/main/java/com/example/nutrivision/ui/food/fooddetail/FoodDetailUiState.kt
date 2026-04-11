package com.example.nutrivision.ui.food.fooddetail

import com.example.nutrivision.data.remote.response.food.FoodDetailResponse
import com.example.nutrivision.data.remote.response.food.Serving

sealed class FoodDetailUiState {
    object Idle : FoodDetailUiState()
    object Loading : FoodDetailUiState()
    data class Success(val data: FoodDetailResponse) : FoodDetailUiState()
    data class Error(val message: String) : FoodDetailUiState()
}

sealed class LogFoodUiState {
    object Idle : LogFoodUiState()
    object Loading : LogFoodUiState()
    data class Success(val message: String) : LogFoodUiState()
    data class Error(val message: String) : LogFoodUiState()
}

data class ServingItem(
    val data: Serving,
    val isSelected: Boolean
)

data class NutritionPreview(
    val calories: Int = 0,
    val carbs: Float = 0f,
    val protein: Float = 0f,
    val fat: Float = 0f,
    val numberOfUnits: Float = 0f
)