package com.example.nutrivision.ui.main.profile

import com.example.nutrivision.data.remote.response.user.UserProfileResponse

sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Loading : ProfileUiState() // fetch profile

    object UpdateLoading : ProfileUiState()
    object LogoutLoading : ProfileUiState()

    data class Success(val data: UserProfileResponse) : ProfileUiState()
    object UpdateSuccess : ProfileUiState()
    object LogoutSuccess : ProfileUiState()

    data class Error(val message: String) : ProfileUiState()
}