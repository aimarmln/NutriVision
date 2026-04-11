package com.example.nutrivision.ui.auth.login

import com.example.nutrivision.data.remote.response.ApiResponse
import com.example.nutrivision.data.remote.response.auth.LoginResponse

sealed class LoginUiState {

    object Idle : LoginUiState()

    object Loading : LoginUiState()

    data class Success(val data: LoginResponse) : LoginUiState()

    data class Error(val message: String) : LoginUiState()
}