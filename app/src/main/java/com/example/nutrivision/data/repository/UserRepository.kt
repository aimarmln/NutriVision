package com.example.nutrivision.data.repository

import com.example.nutrivision.data.remote.api.UserService
import com.example.nutrivision.data.remote.request.user.UpdateUserProfileRequest
import com.example.nutrivision.utils.toResult

class UserRepository(
    private val userService: UserService
) {

    suspend fun getUserDailySummary() =
        userService.getUserDailySummary().toResult()

    suspend fun getUserProfile() =
        userService.getUserProfile().toResult()

    suspend fun updateUserProfile(body: UpdateUserProfileRequest) =
        userService.updateUserProfile(body).toResult()
}