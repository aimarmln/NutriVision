package com.example.nutrivision.ui.profile

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nutrivision.data.local.SettingPreferences
import com.example.nutrivision.data.local.dataStore
import com.example.nutrivision.data.remote.api.ApiConfig
import com.example.nutrivision.data.remote.request.UpdateProfileRequest
import com.example.nutrivision.data.remote.response.ProfileResponse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProfileViewModel(private val application: Application) : AndroidViewModel(application) {

    private val pref = SettingPreferences.getInstance(application.dataStore)

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> =  _loading

    private val _userData = MutableLiveData<ProfileResponse?>()
    val userData: LiveData<ProfileResponse?> get() = _userData

    fun fetchUserProfile(accessToken: String, refreshToken: String) {
        val apiService = ApiConfig.getApiService()
        _loading.value = true

        viewModelScope.launch {
            try {
                val response = apiService.userProfile("Bearer $accessToken")
                if (response.isSuccessful) {
                    _userData.value = response.body()
                } else if (response.code() == 401) {
                    refreshToken(refreshToken) { success ->
                        if (success) {
                            viewModelScope.launch {
                                val newAccessToken = pref.accessToken.first()
                                try {
                                    val retryResponse = apiService.userProfile("Bearer $newAccessToken")
                                    _userData.value = retryResponse.body()
                                } catch (retryError: Exception) {
                                    Log.e("ProfileViewModel", "Retry failed: ${retryError.message}")
                                } finally {
                                    _loading.value = false
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Exception: ${e.message}")
                _userData.value = null
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateUserProfile(
        accessToken: String,
        refreshToken: String,
        updateRequest: UpdateProfileRequest,
        onResult: (Boolean, String?) -> Unit
    ) {
        val apiService = ApiConfig.getApiService()
        _loading.value = true

        viewModelScope.launch {
            try {
                val response = apiService.updateUserProfile("Bearer $accessToken", updateRequest)
                if (response.isSuccessful) {
                    onResult(true, response.body()?.msg)
                } else if (response.code() == 401) {
                    refreshToken(refreshToken) { success ->
                        if (success) {
                            viewModelScope.launch {
                                val newAccessToken = pref.accessToken.first()
                                try {
                                    val retryResponse = apiService.updateUserProfile("Bearer $newAccessToken", updateRequest)
                                    if (retryResponse.isSuccessful) {
                                        onResult(true, retryResponse.body()?.msg)
                                    } else {
                                        onResult(false, "Update failed after token refresh")
                                    }
                                } catch (retryError: Exception) {
                                    Log.e("ProfileViewModel", "Retry failed: ${retryError.message}")
                                } finally {
                                    _loading.value = false
                                }
                            }
                        } else {
                            onResult(false, "Token refresh failed")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Exception: ${e.message}")
                onResult(false, e.message)
            } finally {
                _loading.value = false
            }
        }
    }

    private fun refreshToken(refreshToken: String, onResult: (Boolean) -> Unit) {
        val apiService = ApiConfig.getApiService()
        viewModelScope.launch {
            try {
                val response = apiService.refresh("Bearer $refreshToken")

                pref.saveTokens(
                    response.accessToken.orEmpty(),
                    response.refreshToken.orEmpty()
                )

                onResult(true)
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Token refresh failed: ${e.message}")
                onResult(false)
            }
        }
    }
}

class ProfileViewModelFactory(private val application: Application) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}