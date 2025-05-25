package com.example.nutrivision.ui.scanmeal

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
import com.example.nutrivision.data.remote.response.FoodsResponseItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ScanMealViewModel(private val application: Application) : AndroidViewModel(application) {

    private val pref = SettingPreferences.getInstance(application.dataStore)

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> =  _loading

    private val _predictResponse = MutableLiveData<List<FoodsResponseItem>?>()
    val predictResponse: LiveData<List<FoodsResponseItem>?> = _predictResponse

    fun predictFood(
        accessToken: String,
        refreshToken: String,
        file: File,
    ) {
        val apiService = ApiConfig.getApiService()
        _loading.value = true

        viewModelScope.launch {
            try {
                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                val multipart = MultipartBody.Part.createFormData("image", file.name, requestBody)

                val response = apiService.predictFood("Bearer $accessToken", multipart)
                if (response.isSuccessful) {
                    _predictResponse.value = response.body()
                } else if (response.code() == 401) {
                    refreshToken(refreshToken) { success ->
                        if (success) {
                            viewModelScope.launch {
                                val newAccessToken = pref.accessToken.first()
                                try {
                                    val retryResponse = apiService.predictFood("Bearer $newAccessToken", multipart)
                                    if (retryResponse.isSuccessful) {
                                        _predictResponse.value = response.body()
                                    } else {
                                        _predictResponse.value = null
                                    }
                                } catch (retryError: Exception) {
                                    Log.e("ScanMealViewModel", "Retry failed: ${retryError.message}")
                                } finally {
                                    _loading.value = false
                                }
                            }
                        } else {
                            _predictResponse.value = null
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ScanMealViewModel", "Exception: ${e.message}")
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
                Log.e("ScanMealViewModel", "Token refresh failed: ${e.message}")
                onResult(false)
            }
        }
    }
}

class ScanMealViewModelFactory(private val application: Application) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScanMealViewModel::class.java)) {
            return ScanMealViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}