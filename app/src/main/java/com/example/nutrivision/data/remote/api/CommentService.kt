package com.example.nutrivision.data.remote.api

import com.example.nutrivision.data.remote.response.ApiResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.Path

interface CommentService {

    @DELETE("/api/comments/{commentId}")
    suspend fun deleteComment(
        @Path("commentId") commentId: String
    ): Response<ApiResponse<Nothing, Unit>>
}