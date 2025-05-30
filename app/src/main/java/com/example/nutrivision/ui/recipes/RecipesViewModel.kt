package com.example.nutrivision.ui.recipes

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nutrivision.data.remote.api.ApiConfig
import com.example.nutrivision.data.remote.response.RecipesResponseItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.pow


class RecipesViewModel : ViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> =  _loading

    private val _recipesData = MutableLiveData<List<RecipesResponseItem>?>()
    val recipesData: LiveData<List<RecipesResponseItem>?> get() = _recipesData

//    fun fetchRecipes() {
//        val apiService = ApiConfig.getApiService()
//        _loading.value = true
//
//        viewModelScope.launch {
//            try {
//                val response = apiService.recipeAll()
//                _recipesData.value = response
//            } catch (e: Exception) {
//                Log.e("RecipesViewModel", "Exception: ${e.message}")
//                _recipesData.value = null
//            } finally {
//                _loading.value = false
//            }
//        }
//    }

//    fun fetchRecipes() {
//        val apiService = ApiConfig.getApiService()
//        _loading.value = true
//
//        viewModelScope.launch {
//            var retries = 0
//            val maxRetries = 2
//            var success = false
//
//            while (retries <= maxRetries && !success) {
//                try {
//                    val response = apiService.recipeAll()
//                    _recipesData.value = response
//                    success = true
//                } catch (e: Exception) {
//                    Log.e("RecipesViewModel", "Attempt $retries failed: ${e.message}")
//                    if (retries == maxRetries) {
//                        _recipesData.value = null
//                    } else {
//                        delay(2000)
//                    }
//                    retries++
//                }
//            }
//            _loading.value = false
//        }
//    }

    fun fetchRecipes() {
        val apiService = ApiConfig.getApiService()
        _loading.value = true

        viewModelScope.launch {
            var retries = 0
            val maxRetries = 2
            var success = false

            while (retries <= maxRetries && !success) {
                try {
                    val response = apiService.recipeAll()
                    _recipesData.value = response
                    success = true
                } catch (e: Exception) {
                    when (e) {
                        is java.net.UnknownHostException -> {
                            Log.e("RecipesViewModel", "Attempt $retries failed: Unable to resolve host (possibly due to cold start or no internet connection)")
                        }
                        else -> {
                            Log.e("RecipesViewModel", "Attempt $retries failed: ${e.message}")
                        }
                    }

                    if (retries == maxRetries) {
                        Log.e("RecipesViewModel", "Max retries reached. Failing with null data.")
                        _recipesData.value = null
                    } else {
                        val backoffDelay = 2000L * (2.0.pow(retries)).toLong() // 2s, 4s, 8s
                        Log.w("RecipesViewModel", "Retrying in ${backoffDelay / 1000} seconds...")
                        delay(backoffDelay)
                    }

                    retries++
                }
            }

            _loading.value = false
        }
    }

    fun searchRecipe(query: String) {
        val apiService = ApiConfig.getApiService()
        _loading.value = true

        viewModelScope.launch {
            try {
                val response = apiService.searchRecipe(query)
                _recipesData.value = response
            } catch (e: Exception) {
                Log.e("RecipesViewModel", "Exception: ${e.message}")
                _recipesData.value = null
            } finally {
                _loading.value = false
            }
        }
    }
}

class RecipesViewModelFactory :
    ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipesViewModel::class.java)) {
            return RecipesViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}