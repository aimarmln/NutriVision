package com.example.nutrivision.ui.foodlog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrivision.data.remote.request.foodlog.UpdateFoodLogRequest
import com.example.nutrivision.data.remote.response.food.Serving
import com.example.nutrivision.data.repository.FoodLogRepository
import com.example.nutrivision.ui.food.fooddetail.NutritionPreview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class FoodLogViewModel @Inject constructor(
    private val foodLogRepository: FoodLogRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<FoodLogUiState>(FoodLogUiState.Idle)
    val uiState: LiveData<FoodLogUiState> = _uiState

    private val _updateState =
        MutableLiveData<UpdateFoodLogUiState>(UpdateFoodLogUiState.Idle)
    val updateState: LiveData<UpdateFoodLogUiState> = _updateState

    private val _deleteState =
        MutableLiveData<DeleteFoodLogUiState>(DeleteFoodLogUiState.Idle)
    val deleteState: LiveData<DeleteFoodLogUiState> = _deleteState

    private val _nutritionPreview = MutableLiveData<NutritionPreview>()
    val nutritionPreview: LiveData<NutritionPreview> = _nutritionPreview

    private val _selectedServing = MutableLiveData<Serving>()
    val selectedServing: LiveData<Serving> = _selectedServing

    private val _currentNumOfUnits = MutableLiveData<Float>()
    val numOfUnits: LiveData<Float> = _currentNumOfUnits

    fun fetchFoodLogDetail(foodLogId: Int) {
        _uiState.value = FoodLogUiState.Loading

        viewModelScope.launch {
            val result = foodLogRepository.getFoodLogDetail(foodLogId)

            result.onSuccess {
                val data = it.data ?: return@onSuccess
                _uiState.value = FoodLogUiState.Success(data)
            }.onFailure {
                _uiState.value = FoodLogUiState.Error(it.message ?: "Failed to load food log")
            }
        }
    }

    fun updateFoodLog(foodLogId: Int, body: UpdateFoodLogRequest) {
        _updateState.value = UpdateFoodLogUiState.Loading

        viewModelScope.launch {
            val result = foodLogRepository.updateFoodLog(foodLogId, body)

            result.onSuccess {
                _updateState.value =
                    UpdateFoodLogUiState.Success(it.message)

                launch {
                    delay(1500)
                    _updateState.value =
                        UpdateFoodLogUiState.Idle
                }
            }.onFailure {
                _updateState.value =
                    UpdateFoodLogUiState.Error(it.message ?: "Failed to update food log")

                launch {
                    delay(200)
                    _updateState.value =
                        UpdateFoodLogUiState.Idle
                }
            }
        }
    }

    fun deleteFoodLog(foodLogId: Int) {
        _deleteState.value = DeleteFoodLogUiState.Loading

        viewModelScope.launch {
            val result = foodLogRepository.deleteFoodLog(foodLogId)

            result.onSuccess {
                _deleteState.value =
                    DeleteFoodLogUiState.Success(it.message)

                launch {
                    delay(1500)
                    _deleteState.value =
                        DeleteFoodLogUiState.Idle
                }
            }.onFailure {
                _deleteState.value =
                    DeleteFoodLogUiState.Error(it.message ?: "Failed to delete food log")
                _deleteState.value =
                    DeleteFoodLogUiState.Idle
            }
        }
    }

    fun setServing(serving: Serving) {
        _selectedServing.value = serving

        val defaultWeight = serving.numberOfUnits.toFloat()
        _currentNumOfUnits.value = defaultWeight

        recalculate()
    }

    fun initData(serving: Serving, numOfUnits: Float?) {
        _selectedServing.value = serving

        val finalNumOfUnits = numOfUnits ?: serving.numberOfUnits.toFloat()
        _currentNumOfUnits.value = finalNumOfUnits

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