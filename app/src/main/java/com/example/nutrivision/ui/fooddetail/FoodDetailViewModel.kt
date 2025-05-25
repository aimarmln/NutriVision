package com.example.nutrivision.ui.fooddetail

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
import com.example.nutrivision.data.remote.response.FoodDetailResponse
import com.example.nutrivision.data.remote.response.NutritionCalcResponse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FoodDetailViewModel(private val application: Application) : AndroidViewModel(application) {

    private val pref = SettingPreferences.getInstance(application.dataStore)

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _foodDetailData = MutableLiveData<FoodDetailResponse?>()
    val foodDetailData: LiveData<FoodDetailResponse?> get() = _foodDetailData

    private val _nutritionCalcData = MutableLiveData<NutritionCalcResponse?>()
    val nutritionCalcData: LiveData<NutritionCalcResponse?> get() = _nutritionCalcData

    fun fetchFoodDetail(foodId: Int) {
        val apiService = ApiConfig.getApiService()
        _loading.value = true

        viewModelScope.launch {
            try {
                val response = apiService.foodDetail(foodId)
                _foodDetailData.value = response
            } catch (e: Exception) {
                Log.e("RecipesViewModel", "Exception: ${e.message}")
                _foodDetailData.value = null
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
                                    Log.e("FoodDetailViewModel", "Retry failed: ${retryError.message}")
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
                Log.e("FoodDetailViewModel", "Exception: ${e.message}")
                onResult(false, e.message)
            } finally {
                _loading.value = false
            }
        }
    }

    fun calculateNutrition(foodId: Int, weightGrams: Int) {
        val apiService = ApiConfig.getApiService()
        _loading.value = true

        viewModelScope.launch {
            try {
                val response = apiService.foodNutritionCalc(foodId, weightGrams)
                _nutritionCalcData.value = response
            } catch (e: Exception) {
                Log.e("FoodDetailViewModel", "Exception: ${e.message}")
                _nutritionCalcData.value = null
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

class FoodDetailViewModelFactory(private val application: Application) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FoodDetailViewModel::class.java)) {
            return FoodDetailViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}