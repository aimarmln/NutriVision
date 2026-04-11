package com.example.nutrivision.utils

import android.util.Log
import com.example.nutrivision.data.remote.response.ApiResponse
import com.google.gson.Gson
import retrofit2.Response

/**
 * Convert Retrofit Response<ApiResponse<T>> to Result<ApiResponse<T>>
 */
fun <T, P> Response<ApiResponse<T, P>>.toResult(gson: Gson = Gson()): Result<ApiResponse<T, P>> {
    return if (isSuccessful) {
        val body = body()
        if (body != null) {
            Result.success(body)
        } else {
            Result.failure(Exception("Response body is null"))
        }
    } else {
        // Parse errorBody JSON into ApiResponse<T> to get message
        val errorJson = errorBody()?.string()
        val errorResponse = try {
            gson.fromJson(errorJson, ApiResponse::class.java) as ApiResponse<T, P>?
        } catch (e: Exception) {
            Log.e("ResponseExtensions", "Error parsing error body", e)
            null
        }

        val message = errorResponse?.message ?: "Unknown error: ${code()}"
        Result.failure(Exception(message))
    }
}