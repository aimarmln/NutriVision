package com.example.nutrivision.ui.food.scanfood.scanresult

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrivision.data.remote.request.foodlog.LogFoodRequest
import com.example.nutrivision.data.remote.response.food.FoodsListResponseItem
import com.example.nutrivision.data.repository.FoodLogRepository
import com.example.nutrivision.ui.food.FoodListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScanResultViewModel @Inject constructor(
    private val foodLogRepository: FoodLogRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<ScanResultUiState>(ScanResultUiState.Idle)
    val uiState: LiveData<ScanResultUiState> = _uiState

    private var currentList = mutableListOf<FoodListItem.Item>()

    fun setInitialData(list: List<FoodsListResponseItem>) {
        currentList = list.map { FoodListItem.Item(it) }.toMutableList()
        _uiState.value = ScanResultUiState.Success(currentList)
    }

    fun logFood(body: LogFoodRequest) {
        val foodId = body.foodId

        updateItem(foodId) {
            it.copy(isLogging = true, isLogSuccess = false, isLogError = false)
        }

        viewModelScope.launch {
            val result = foodLogRepository.logFood(body)

            result.onSuccess {
                updateItem(foodId) {
                    it.copy(isLogging = false, isLogSuccess = true)
                }

                delay(1500)

                updateItem(foodId) {
                    it.copy(isLogSuccess = false)
                }
            }

            result.onFailure {
                updateItem(foodId) {
                    it.copy(isLogging = false, isLogError = true)
                }

                delay(500)

                updateItem(foodId) {
                    it.copy(isLogError = false)
                }
            }
        }
    }

    private fun updateItem(
        foodId: Int,
        transform: (FoodListItem.Item) -> FoodListItem.Item
    ) {
        currentList = currentList.map {
            if (it.data.id == foodId) transform(it) else it
        }.toMutableList()

        _uiState.value = ScanResultUiState.Success(currentList)
    }
}