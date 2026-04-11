package com.example.nutrivision.ui.food.scanfood

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrivision.data.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ScanFoodViewModel @Inject constructor(
    private val foodRepository: FoodRepository
) : ViewModel() {
    private val _uiState = MutableLiveData<ScanFoodUiState>(ScanFoodUiState.Idle)
    val uiState: LiveData<ScanFoodUiState> = _uiState

    fun detectFoods(file: File) {
        _uiState.value = ScanFoodUiState.Loading

        viewModelScope.launch {
            val reqBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val image = MultipartBody.Part.createFormData("image", file.name, reqBody)
            val result = foodRepository.detectFoods(image)

            result.onSuccess {
                val data = it.data ?: return@onSuccess
                _uiState.value = ScanFoodUiState.Success(data)
            }.onFailure {
                _uiState.value = ScanFoodUiState.Error(
                    it.message ?: "Unknown error"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = ScanFoodUiState.Idle
    }
}