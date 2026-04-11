package com.example.nutrivision.ui.food

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nutrivision.data.local.SettingPreferences
import com.example.nutrivision.data.local.dataStore
import com.example.nutrivision.data.remote.network.ApiConfig
import com.example.nutrivision.data.remote.request.FoodLogRequest
import com.example.nutrivision.data.remote.request.foodlog.LogFoodRequest
//import com.example.nutrivision.data.remote.response.FoodsResponseItem
import com.example.nutrivision.data.remote.response.food.FoodsListResponseItem
import com.example.nutrivision.data.repository.FoodLogRepository
import com.example.nutrivision.data.repository.FoodRepository
import com.example.nutrivision.ui.main.recipe.RecipeListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoodViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    private val foodLogRepository: FoodLogRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<FoodUiState>(FoodUiState.Idle)
    val uiState: LiveData<FoodUiState> = _uiState

    private var currentQuery: String? = null
    private var currentPage: Int = 1
    private var isLastPage: Boolean = false
    private var isLoadingPage: Boolean = false
    private val pageSize: Int = 20

    private val accumulatedList = mutableListOf<FoodListItem>()

    fun fetchFoods(
        query: String? = currentQuery,
        isLoadMore: Boolean = false
    ) {
        if (isLoadMore && (isLoadingPage || isLastPage)) return

        if (!isLoadMore || query != currentQuery) {
            currentPage = 1
            isLastPage = false
            accumulatedList.clear()
            _uiState.value = FoodUiState.Loading
        } else {
            accumulatedList.add(FoodListItem.Loading)
            _uiState.value = FoodUiState.Success(
                data = accumulatedList.toList(),
                isLoadMore = true
            )
        }

        currentQuery = query
        isLoadingPage = true

        viewModelScope.launch {
            val result = foodRepository.getFoodsList(
                query,
                currentPage,
                pageSize
            )

            result.onSuccess { response ->
                val newList = response.data ?: emptyList()

                if (isLoadMore) removeLoadingFooter()

                if (newList.size < pageSize) {
                    isLastPage = true
                }

                accumulatedList.addAll(
                    newList.map { FoodListItem.Item(it) }
                )

                currentPage++

                _uiState.value =
                    FoodUiState.Success(
                        data = accumulatedList.toList(),
                        isLoadMore = isLoadMore
                    )

                isLoadingPage = false

            }.onFailure {
                if (isLoadMore) removeLoadingFooter()

                _uiState.value =
                    FoodUiState.Error(it.message ?: "Unknown error")

                isLoadingPage = false
            }
        }
    }

    fun logFood(body: LogFoodRequest) {
        val foodId = body.foodId

        updateItemState(foodId) {
            it.copy(
                isLogging = true,
                isLogSuccess = false,
                isLogError = false
            )
        }

        viewModelScope.launch {
            val result = foodLogRepository.logFood(body)

            result
                .onSuccess {
                    updateItemState(foodId) {
                        it.copy(
                            isLogging = false,
                            isLogSuccess = true,
                            isLogError = false
                        )
                    }

                    launch {
                        delay(1500)
                        updateItemState(foodId) {
                            it.copy(
                                isLogSuccess = false
                            )
                        }
                    }
                }
                .onFailure {
                    updateItemState(foodId) {
                        it.copy(
                            isLogging = false,
                            isLogSuccess = false,
                            isLogError = true
                        )
                    }

                    launch {
                        delay(200)
                        updateItemState(foodId) {
                            it.copy(
                                isLogError = false
                            )
                        }
                    }
                }
        }
    }

    private fun updateItemState(
        foodId: String,
        transform: (FoodListItem.Item) -> FoodListItem.Item
    ) {
        val newList = accumulatedList.map {
            if (it is FoodListItem.Item && it.data.id == foodId) {
                transform(it)
            } else it
        }

        accumulatedList.clear()
        accumulatedList.addAll(newList)

        _uiState.value = FoodUiState.Success(accumulatedList.toList())
    }

    private fun removeLoadingFooter() {
        if (accumulatedList.isNotEmpty() &&
            accumulatedList.lastOrNull() is FoodListItem.Loading
        ) {
            accumulatedList.removeAt(accumulatedList.lastIndex)
        }
    }
}