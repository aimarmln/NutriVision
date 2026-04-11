package com.example.nutrivision.ui.main.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrivision.data.local.SettingPreferences
import com.example.nutrivision.data.remote.request.user.UpdateUserProfileRequest
import com.example.nutrivision.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val preferences: SettingPreferences,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<ProfileUiState>(ProfileUiState.Idle)
    val uiState: LiveData<ProfileUiState> = _uiState

    fun fetchUserProfile() {
        _uiState.value = ProfileUiState.Loading

        viewModelScope.launch {
            val result = userRepository.getUserProfile()

            result.onSuccess {
                val data = it.data ?: return@onSuccess
                _uiState.value = ProfileUiState.Success(data)
            }.onFailure {
                _uiState.value = ProfileUiState.Error(it.message ?: "Error")
            }
        }
    }

    fun updateUserProfile(body: UpdateUserProfileRequest) {
        _uiState.value = ProfileUiState.UpdateLoading

        viewModelScope.launch {
            val result = userRepository.updateUserProfile(body)

            result.onSuccess {
                val data = it.data ?: return@onSuccess

                _uiState.value = ProfileUiState.UpdateSuccess
                _uiState.value = ProfileUiState.Success(data)
            }.onFailure {
                _uiState.value = ProfileUiState.Error(it.message ?: "Update failed")
            }
        }
    }

    fun logout() {
        _uiState.value = ProfileUiState.LogoutLoading

        viewModelScope.launch {
            try {
                preferences.clearUser()
                _uiState.value = ProfileUiState.LogoutSuccess
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error("Logout failed")
            }
        }
    }
}