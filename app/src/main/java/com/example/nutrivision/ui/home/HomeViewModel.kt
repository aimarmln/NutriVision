package com.example.nutrivision.ui.home


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
import com.example.nutrivision.data.remote.response.HomeResponse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeViewModel(private val application: Application) : AndroidViewModel(application) {

    private val pref = SettingPreferences.getInstance(application.dataStore)

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> =  _loading

    private val _homeData = MutableLiveData<HomeResponse?>()
    val homeData: LiveData<HomeResponse?> get() = _homeData

    fun home(accessToken: String, refreshToken: String) {
        val apiService = ApiConfig.getApiService()
        _loading.value = true

        viewModelScope.launch {
            try {
                val response = apiService.home("Bearer $accessToken")
                if (response.isSuccessful) {
                    _homeData.value = response.body()
                } else if (response.code() == 401) {
                    refreshToken(refreshToken) { success ->
                        if (success) {
                            viewModelScope.launch {
                                val newAccessToken = pref.accessToken.first()
                                try {
                                    val retryResponse = apiService.home("Bearer $newAccessToken")
                                    _homeData.value = retryResponse.body()
                                } catch (retryError: Exception) {
                                    Log.e("HomeViewModel", "Retry failed: ${retryError.message}")
                                } finally {
                                    _loading.value = false
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Exception: ${e.message}")
                _homeData.value = null
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
                Log.e("HomeViewModel", "Token refresh failed: ${e.message}")
                onResult(false)
            }
        }
    }
}

class HomeViewModelFactory(private val application: Application) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}