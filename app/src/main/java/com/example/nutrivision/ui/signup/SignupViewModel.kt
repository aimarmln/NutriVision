package com.example.nutrivision.ui.signup

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nutrivision.data.remote.api.ApiConfig
import com.example.nutrivision.data.remote.request.CheckEmailRequest
import com.example.nutrivision.data.remote.request.SignupRequest
import com.example.nutrivision.data.remote.response.AuthResponse
import com.example.nutrivision.data.remote.response.CheckEmailResponse
import kotlinx.coroutines.launch

class SignupViewModel : ViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> =  _loading

    private val _checkEmailResponse = MutableLiveData<CheckEmailResponse?>()
    val checkEmailResponse: LiveData<CheckEmailResponse?> get() = _checkEmailResponse

    private val _signupResponse = MutableLiveData<AuthResponse?>()
    val signupResponse: LiveData<AuthResponse?> = _signupResponse

    fun checkEmailExists(email: String) {
        val apiService = ApiConfig.getApiService()
        _loading.value = true

        viewModelScope.launch {
            try {
                val request = CheckEmailRequest(email = email)
                val response = apiService.checkEmail(request)
                _checkEmailResponse.value = response
                Log.d("SignupViewModel", "checkEmail success: $response")
            } catch (e: Exception) {
                _checkEmailResponse.value = null
                Log.e("SignupViewModel", "checkEmail error: ${e.message}")
            } finally {
                _loading.value = false
            }
        }
    }

    fun signUp(user: SignupUser) {
        val apiService = ApiConfig.getApiService()
        _loading.value = true

        viewModelScope.launch {
            try {
                val request = SignupRequest(
                    email = user.email,
                    password = user.password,
                    name = user.name,
                    gender = user.gender,
                    heightCm = user.height,
                    weightKg = user.weight,
                    birthday = user.birthday,
                    activityLevel = user.activityLevel,
                    mainGoal = user.mainGoal
                )

                val response = apiService.signup(request)
                _signupResponse.value = response
                Log.d("SignupViewModel", "Signup success: $response")
            } catch (e: Exception) {
                _signupResponse.value = null
                Log.e("SignupViewModel", "Signup error: ${e.message}")
            } finally {
                _loading.value = false
            }
        }
    }
}

class SignupViewModelFactory :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignupViewModel::class.java)) {
            return SignupViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}