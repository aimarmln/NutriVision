package com.example.nutrivision.ui.food.fooddetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrivision.data.remote.request.foodlog.LogFoodRequest
import com.example.nutrivision.data.remote.response.food.Serving
import com.example.nutrivision.data.repository.FoodLogRepository
import com.example.nutrivision.data.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class FoodDetailViewModel @Inject constructor(
    private val foodRepository: FoodRepository,
    private val foodLogRepository: FoodLogRepository,
) : ViewModel() {

    private val _uiState = MutableLiveData<FoodDetailUiState>(FoodDetailUiState.Idle)
    val uiState: LiveData<FoodDetailUiState> = _uiState

    private val _logFoodState = MutableLiveData<LogFoodUiState>(LogFoodUiState.Idle)
    val logFoodState: LiveData<LogFoodUiState> = _logFoodState

    private val _nutritionPreview = MutableLiveData<NutritionPreview>()
    val nutritionPreview: LiveData<NutritionPreview> = _nutritionPreview

    private val _selectedServing = MutableLiveData<Serving>()
    val selectedServing: LiveData<Serving> = _selectedServing

    private val _currentNumOfUnits = MutableLiveData<Float>()
    val currentNumOfUnits: LiveData<Float> = _currentNumOfUnits


    fun fetchFoodDetail(foodId: Int) {
        _uiState.value = FoodDetailUiState.Loading

        viewModelScope.launch {
            val result = foodRepository.getFoodDetail(foodId)

            result
                .onSuccess {
                    val data = it.data ?: return@onSuccess
                    _uiState.value = FoodDetailUiState.Success(data)
                }
                .onFailure {
                    _uiState.value =
                        FoodDetailUiState.Error(it.message ?: "Unknown error")
                }
        }
    }

    fun logFood(body: LogFoodRequest) {
        _logFoodState.value = LogFoodUiState.Loading

        viewModelScope.launch {
            val result = foodLogRepository.logFood(body)

            result
                .onSuccess {
                    _logFoodState.value =
                        LogFoodUiState.Success(it.message)

                    launch {
                        delay(1500)
                        _logFoodState.value =
                            LogFoodUiState.Idle
                    }
                }
                .onFailure {
                    _logFoodState.value =
                        LogFoodUiState.Error(it.message ?: "Unknown error")

                    launch {
                        delay(200)
                        _logFoodState.value =
                            LogFoodUiState.Idle
                    }
                }
        }
    }

    fun initData(serving: Serving, numOfUnits: Float?) {
        _selectedServing.value = serving

        val finalNumOfUnits = numOfUnits ?: serving.numberOfUnits.toFloat()
        _currentNumOfUnits.value = finalNumOfUnits

        recalculate()
    }

    fun setServing(serving: Serving) {
        _selectedServing.value = serving

        val defaultNumOfUnits = serving.numberOfUnits.toFloat()
        _currentNumOfUnits.value = defaultNumOfUnits

        recalculate()
    }

    fun setNumOfUnits(numOfUnits: Float) {
        _currentNumOfUnits.value = numOfUnits
        recalculate()
    }

    private fun recalculate() {
        val serving = _selectedServing.value ?: return
        val numOfUnits = _currentNumOfUnits.value ?: return

        val ratio = numOfUnits / serving.numberOfUnits.toFloat()

        _nutritionPreview.value = NutritionPreview(
            calories = (serving.caloriesKcal * ratio).roundToInt(),
            carbs = serving.carbohydratesG.toFloat() * ratio,
            protein = serving.proteinsG.toFloat() * ratio,
            fat = serving.fatsG.toFloat() * ratio,
            numberOfUnits = numOfUnits
        )
    }
}