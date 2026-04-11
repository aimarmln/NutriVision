package com.example.nutrivision.ui.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrivision.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<HomeUiState>(HomeUiState.Idle)
    val uiState: LiveData<HomeUiState> = _uiState

    fun fetchUserDailySummary() {
        _uiState.value = HomeUiState.Loading

        viewModelScope.launch {
            val result = userRepository.getUserDailySummary()

            result
                .onSuccess {
                    val data = it.data ?: return@onSuccess
                    _uiState.value = HomeUiState.Success(data)
                }
                .onFailure {
                    _uiState.value =
                        HomeUiState.Error(it.message ?: "Unknown error")
                }
        }
    }
}
