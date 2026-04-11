package com.example.nutrivision.ui.auth.register

import com.example.nutrivision.data.remote.response.ApiResponse
import com.example.nutrivision.data.remote.response.auth.RegisterResponse

sealed class RegisterUiState {
    object Idle : RegisterUiState()

    object Loading : RegisterUiState()

    data class Success(val data: RegisterResponse) : RegisterUiState()

    data class Error(val message: String) : RegisterUiState()
}

sealed class CheckEmailUiState {
    object Idle : CheckEmailUiState()

    object Loading : CheckEmailUiState()

    data class Success(val data: String) : CheckEmailUiState()

    data class Error(val message: String) : CheckEmailUiState()
}