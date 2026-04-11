package com.example.nutrivision.ui.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrivision.data.local.SettingPreferences
import com.example.nutrivision.data.remote.request.auth.LoginRequest
import com.example.nutrivision.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val pref: SettingPreferences,
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<LoginUiState>(LoginUiState.Idle)
    val uiState: LiveData<LoginUiState> = _uiState

    fun login(body: LoginRequest) {
        _uiState.value = LoginUiState.Loading

        viewModelScope.launch {
            val result = repository.login(body)

            result
                .onSuccess {
                    val data = it.data ?: return@onSuccess
                    pref.saveTokens(data.accessToken, data.refreshToken)
                    _uiState.value = LoginUiState.Success(data)
                }
                .onFailure { exception ->
                    _uiState.value =
                        LoginUiState.Error(exception.message ?: "Unknown error")
                }
        }
    }
}