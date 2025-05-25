package com.example.nutrivision.ui.mealdetail

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
import com.example.nutrivision.data.remote.request.UpdateMealRequest
import com.example.nutrivision.data.remote.response.MealDetailResponse
import com.example.nutrivision.data.remote.response.NutritionCalcResponse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MealDetailViewModel(private val application: Application) : AndroidViewModel(application) {

    private val pref = SettingPreferences.getInstance(application.dataStore)

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> =  _loading

    private val _mealDetailData = MutableLiveData<MealDetailResponse?>()
    val mealDetailData: LiveData<MealDetailResponse?> get() = _mealDetailData

    private val _nutritionCalcData = MutableLiveData<NutritionCalcResponse?>()
    val nutritionCalcData: LiveData<NutritionCalcResponse?> get() = _nutritionCalcData

    fun fetchMealDetail(accessToken: String, refreshToken: String, mealId: Int) {
        val apiService = ApiConfig.getApiService()
        _loading.value = true

        viewModelScope.launch {
            try {
                val response = apiService.mealDetail("Bearer $accessToken", mealId)
                if (response.isSuccessful) {
                    _mealDetailData.value = response.body()
                } else if (response.code() == 401) {
                    // Token expired, try refresh
                    refreshToken(refreshToken) { success ->
                        if (success) {
                            viewModelScope.launch {
                                val newAccessToken = pref.accessToken.first()
                                try {
                                    val retryResponse = apiService.mealDetail("Bearer $newAccessToken", mealId)
                                    if (retryResponse.isSuccessful) {
                                        _mealDetailData.value = retryResponse.body()
                                    } else {
                                        Log.e("MealDetailViewModel", "Retry failed with code: ${retryResponse.code()}")
                                        _mealDetailData.value = null
                                    }
                                } catch (retryError: Exception) {
                                    Log.e("MealDetailViewModel", "Retry exception: ${retryError.message}")
                                    _mealDetailData.value = null
                                } finally {
                                    _loading.value = false
                                }
                            }
                        } else {
                            Log.e("MealDetailViewModel", "Token refresh failed")
                            _mealDetailData.value = null
                            _loading.value = false
                        }
                    }
                } else {
                    Log.e("MealDetailViewModel", "Unexpected error code: ${response.code()}")
                    _mealDetailData.value = null
                    _loading.value = false
                }
            } catch (e: Exception) {
                Log.e("MealDetailViewModel", "Exception: ${e.message}")
                _mealDetailData.value = null
                _loading.value = false
            }
        }
    }

    fun updateMealDetail(
        accessToken: String,
        refreshToken: String,
        mealId: Int,
        updateMealRequest: UpdateMealRequest,
        onResult: (Boolean, String?) -> Unit
    ) {
        val apiService = ApiConfig.getApiService()
        _loading.value = true

        viewModelScope.launch {
            try {
                val response = apiService.updateMeal("Bearer $accessToken", mealId,  updateMealRequest)
                if (response.isSuccessful) {
                    onResult(true, response.body()?.msg)
                } else if (response.code() == 401) {
                    refreshToken(refreshToken) { success ->
                        if (success) {
                            viewModelScope.launch {
                                val newAccessToken = pref.accessToken.first()
                                try {
                                    val retryResponse = apiService.updateMeal("Bearer $newAccessToken", mealId, updateMealRequest)
                                    if (retryResponse.isSuccessful) {
                                        onResult(true, retryResponse.body()?.msg)
                                    } else {
                                        onResult(false, "Update failed after token refresh")
                                    }
                                } catch (retryError: Exception) {
                                    Log.e("MealDetailViewModel", "Retry failed: ${retryError.message}")
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
                Log.e("MealDetailViewModel", "Exception: ${e.message}")
                onResult(false, e.message)
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteMeal(
        accessToken: String,
        refreshToken: String,
        mealId: Int,
        onResult: (Boolean, String?) -> Unit
    ) {
        val apiService = ApiConfig.getApiService()
        _loading.value = true

        viewModelScope.launch {
            try {
                val response = apiService.deleteMeal("Bearer $accessToken", mealId)
                if (response.isSuccessful) {
                    onResult(true, response.body()?.msg)
                } else if (response.code() == 401) {
                    refreshToken(refreshToken) { success ->
                        if (success) {
                            viewModelScope.launch {
                                val newAccessToken = pref.accessToken.first()
                                try {
                                    val retryResponse = apiService.deleteMeal("Bearer $newAccessToken", mealId)
                                    if (retryResponse.isSuccessful) {
                                        onResult(true, retryResponse.body()?.msg)
                                    } else {
                                        onResult(false, "Update failed after token refresh")
                                    }
                                } catch (retryError: Exception) {
                                    Log.e("MealDetailViewModel", "Retry failed: ${retryError.message}")
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
                Log.e("MealDetailViewModel", "Exception: ${e.message}")
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

class MealDetailViewModelFactory(private val application: Application) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MealDetailViewModel::class.java)) {
            return MealDetailViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}