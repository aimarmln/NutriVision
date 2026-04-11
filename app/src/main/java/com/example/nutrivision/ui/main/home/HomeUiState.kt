package com.example.nutrivision.ui.main.home

import com.example.nutrivision.data.remote.response.user.DailySummaryResponse

sealed class HomeUiState {
    object Idle : HomeUiState()
    object Loading : HomeUiState()
    data class Success(val data: DailySummaryResponse) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}