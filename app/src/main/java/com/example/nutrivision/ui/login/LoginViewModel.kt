package com.example.nutrivision.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nutrivision.data.remote.api.ApiConfig
import com.example.nutrivision.data.remote.request.LoginRequest
import com.example.nutrivision.data.remote.response.AuthResponse
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> =  _loading

    private val _loginResponse = MutableLiveData<AuthResponse?>()
    val loginResponse: LiveData<AuthResponse?> = _loginResponse

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun login(email: String, password: String) {
        val apiService = ApiConfig.getApiService()
        _loading.value = true

        viewModelScope.launch {
            try {
                val request = LoginRequest(email = email, password = password)
                val response = apiService.login(request)

                if (response.isSuccessful) {
                    _loginResponse.value = response.body()
                    _errorMessage.value = null
                    Log.d("LoginViewModel", "Login success: ${response.body()}")
                } else {
                    when (response.code()) {
                        404 -> {
                            _errorMessage.value = "User not found"
                        }
                        401 -> {
                            _errorMessage.value = "Invalid password"
                        }
                        else -> {
                            _errorMessage.value = "Unknown error: ${response.code()}"
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d("LoginViewModel", "Login error: ${e.message}")
                _loginResponse.value = null
            } finally {
                _loading.value = false
            }
        }
    }
}

class LoginViewModelFactory :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}