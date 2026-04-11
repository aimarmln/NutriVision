package com.example.nutrivision.ui.auth.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrivision.data.local.SettingPreferences
import com.example.nutrivision.data.remote.request.auth.CheckEmailRequest
import com.example.nutrivision.data.remote.request.auth.RegisterRequest
import com.example.nutrivision.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val pref: SettingPreferences,
    private val authRepository: AuthRepository
) : ViewModel() {

    var formState = RegisterFormState()
        private set

    private val _registerUiState = MutableLiveData<RegisterUiState>(RegisterUiState.Idle)
    val registerUiState: LiveData<RegisterUiState> = _registerUiState

    private val _checkEmailUiState = MutableLiveData<CheckEmailUiState>(CheckEmailUiState.Idle)
    val checkEmailUiState: LiveData<CheckEmailUiState> = _checkEmailUiState

    var personalizedCalories: Int? = null

    fun updateName(name: String) {
        formState = formState.copy(name = name)
    }

    fun updateGender(gender: String) {
        formState = formState.copy(gender = gender)
    }

    fun updateBirthday(birthday: String) {
        formState = formState.copy(birthday = birthday)
    }

    fun updateHeight(height: Int) {
        formState = formState.copy(heightCm = height)
    }

    fun updateWeight(weight: Int) {
        formState = formState.copy(weightKg = weight)
    }

    fun updateActivityLevel(level: String) {
        formState = formState.copy(activityLevel = level)
    }

    fun updateMainGoal(goal: String) {
        formState = formState.copy(mainGoal = goal)
    }

    fun updateEmail(email: String) {
        formState = formState.copy(email = email)
    }

    fun updatePassword(password: String) {
        formState = formState.copy(password = password)
    }

    fun resetPersonalization() {
        personalizedCalories = null
    }

    fun buildRequest(): RegisterRequest? {
        val s = formState
        return if (
            s.name != null &&
            s.gender != null &&
            s.birthday != null &&
            s.heightCm != null &&
            s.weightKg != null &&
            s.activityLevel != null &&
            s.mainGoal != null &&
            s.email != null &&
            s.password != null
        ) {
            RegisterRequest(
                name = s.name,
                gender = s.gender,
                birthday = s.birthday,
                heightCm = s.heightCm,
                weightKg = s.weightKg,
                activityLevel = s.activityLevel,
                mainGoal = s.mainGoal,
                email = s.email,
                password = s.password
            )
        } else null
    }

    fun resetCheckEmailState() {
        _checkEmailUiState.value = CheckEmailUiState.Idle
    }

    fun checkEmail(email: String) {
        _checkEmailUiState.value = CheckEmailUiState.Loading

        viewModelScope.launch {
            val result = authRepository.checkEmail(CheckEmailRequest(email))

            result
                .onSuccess {
                    val data = it.message
                    _checkEmailUiState.value = CheckEmailUiState.Success(data)
                }
                .onFailure { exception ->
                    _checkEmailUiState.value =
                        CheckEmailUiState.Error(exception.message ?: "Unknown error")
                }
        }
    }

    fun resetRegisterState() {
        _registerUiState.value = RegisterUiState.Idle
    }

    fun register() {
        val body = buildRequest() ?: return

        _registerUiState.value = RegisterUiState.Loading

        viewModelScope.launch {
            val result = authRepository.register(body)

            result
                .onSuccess {
                    val data = it.data ?: return@onSuccess
                    pref.saveTokens(data.accessToken, data.refreshToken)
                    _registerUiState.value = RegisterUiState.Success(data)
                }
                .onFailure { exception ->
                    _registerUiState.value =
                        RegisterUiState.Error(exception.message ?: "Unknown error")
                }
        }
    }
}
