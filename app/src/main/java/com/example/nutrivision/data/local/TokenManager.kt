package com.example.nutrivision.data.local

import kotlinx.coroutines.flow.first

class TokenManager(
    val pref: SettingPreferences
) {
    suspend fun getAccessToken(): String? {
        return pref.accessToken.first()
    }

    suspend fun getRefreshToken(): String? {
        return pref.refreshToken.first()
    }

    suspend fun saveTokens(access: String, refresh: String) {
        pref.saveTokens(access, refresh)
    }
}