package com.example.nutrivision.ui.signup

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SignupUser(
    var email: String? = null,
    var password: String? = null,
    var name: String? = null,
    var gender: String? = null,
    var birthday: String? = null,
    var height: Int? = null,
    var weight: Int? = null,
    var activityLevel: String? = null,
    var mainGoal: String? = null
) : Parcelable