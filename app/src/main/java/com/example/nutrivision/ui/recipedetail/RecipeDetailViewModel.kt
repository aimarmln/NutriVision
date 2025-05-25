package com.example.nutrivision.ui.recipedetail

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
import com.example.nutrivision.data.remote.request.CommentRequest
import com.example.nutrivision.data.remote.response.RecipeDetailResponse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RecipeDetailViewModel(private val application: Application) : AndroidViewModel(application) {

    private val pref = SettingPreferences.getInstance(application.dataStore)

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> =  _loading

    private val _recipeDetailData = MutableLiveData<RecipeDetailResponse?>()
    val recipeDetailData: LiveData<RecipeDetailResponse?> get() = _recipeDetailData

    fun fetchRecipeDetail(recipeId: Int) {
        val apiService = ApiConfig.getApiService()
        _loading.value = true

        viewModelScope.launch {
            try {
                val response = apiService.recipeDetail(recipeId)
                _recipeDetailData.value = response
            } catch (e: Exception) {
                Log.e("RecipeDetailViewModel", "Exception: ${e.message}")
                _recipeDetailData.value = null
            } finally {
                _loading.value = false
            }
        }
    }
    
    fun comment(
        accessToken: String,
        refreshToken: String,
        commentRequest: CommentRequest,
        onResult: (Boolean, String?) -> Unit
    ) {
        val apiService = ApiConfig.getApiService()
        _loading.value

        viewModelScope.launch {
            try {
                val response = apiService.postComment("Bearer $accessToken", commentRequest)
                if (response.isSuccessful) {
                    onResult(true, response.body()?.msg)
                } else if (response.code() == 401) {
                    refreshToken(refreshToken) { success ->
                        if (success) {
                            viewModelScope.launch {
                                val newAccessToken = pref.accessToken.first()
                                try {
                                    val retryResponse = apiService.postComment("Bearer $newAccessToken", commentRequest)
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
                Log.e("RecipeDetailViewModel", "Token refresh failed: ${e.message}")
                onResult(false)
            }
        }
    }
}

class RecipeDetailViewModelFactory(private val application: Application) :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeDetailViewModel::class.java)) {
            return RecipeDetailViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}