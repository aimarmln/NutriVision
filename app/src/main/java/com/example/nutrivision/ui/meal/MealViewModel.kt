package com.example.nutrivision.ui.meal

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
import com.example.nutrivision.data.remote.request.FoodLogRequest
import com.example.nutrivision.data.remote.response.FoodsResponseItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MealViewModel(private val application: Application) : AndroidViewModel(application) {

    private val pref = SettingPreferences.getInstance(application.dataStore)

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> =  _loading

    private val _foodsData = MutableLiveData<List<FoodsResponseItem>?>()
    val foodsData: LiveData<List<FoodsResponseItem>?> get() = _foodsData

    fun fetchFoods() {
        val apiService = ApiConfig.getApiService()
        _loading.value = true

        viewModelScope.launch {
            try {
                val response = apiService.foodAll()
                _foodsData.value = response
            } catch (e: Exception) {
                Log.e("RecipesViewModel", "Exception: ${e.message}")
                _foodsData.value = null
            } finally {
                _loading.value = false
            }
        }
    }

    fun searchFood(query: String) {
        val apiService = ApiConfig.getApiService()
        _loading.value = true

        viewModelScope.launch {
            try {
                val response = apiService.searchFood(query)
                _foodsData.value = response
            } catch (e: Exception) {
                Log.e("RecipesViewModel", "Exception: ${e.message}")
                _foodsData.value = null
            } finally {
                _loading.value = false
            }
        }
    }

    fun logFood(
        accessToken: String,
        refreshToken: String,
        foodLogRequest: FoodLogRequest,
        onResult: (Boolean, String?) -> Unit
    ) {
        val apiService = ApiConfig.getApiService()
        _loading.value = true

        viewModelScope.launch {
            try {
                val response = apiService.logFood("Bearer $accessToken", foodLogRequest)
                if (response.isSuccessful) {
                    onResult(true, response.body()?.msg)
                } else if (response.code() == 401) {
                    refreshToken(refreshToken) { success ->
                        if (success) {
                            viewModelScope.launch {
                                val newAccessToken = pref.accessToken.first()
                                try {
                                    val retryResponse = apiService.logFood("Bearer $newAccessToken", foodLogRequest)
                                    if (retryResponse.isSuccessful) {
                                        onResult(true, retryResponse.body()?.msg)
                                    } else {
                                        onResult(false, "Update failed after token refresh")
                                    }
                                } catch (retryError: Exception) {
                                    Log.e("MealViewModel", "Retry failed: ${retryError.message}")
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
                Log.e("MealViewModel", "Exception: ${e.message}")
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
                Log.e("MealViewModel", "Token refresh failed: ${e.message}")
                onResult(false)
            }
        }
    }
}

class MealViewModelFactory(private val application: Application) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MealViewModel::class.java)) {
            return MealViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}