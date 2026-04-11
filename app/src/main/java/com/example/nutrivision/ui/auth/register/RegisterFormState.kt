package com.example.nutrivision.ui.auth.register

data class RegisterFormState(
    val name: String? = null,
    val gender: String? = null,
    val birthday: String? = null,
    val heightCm: Int? = null,
    val weightKg: Int? = null,
    val activityLevel: String? = null,
    val mainGoal: String? = null,
    val email: String? = null,
    val password: String? = null
)
