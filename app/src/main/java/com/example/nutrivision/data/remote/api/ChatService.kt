package com.example.nutrivision.data.remote.api

import com.example.nutrivision.data.remote.request.chat.ChatRequest
import com.example.nutrivision.data.remote.response.ApiResponse
import com.example.nutrivision.data.remote.response.CursorPagination
import com.example.nutrivision.data.remote.response.PagePagination
import com.example.nutrivision.data.remote.response.chat.ChatListResponse
import com.example.nutrivision.data.remote.response.chat.ChatResponse
import com.example.nutrivision.data.remote.response.chat.ChatSessionsListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatService {

    @GET("/api/chat")
    suspend fun getChatSessionsList(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<ApiResponse<ChatSessionsListResponse, PagePagination>>

    @POST("/api/chat")
    suspend fun initChatSession(
        @Body request: ChatRequest
    ): Response<ApiResponse<ChatResponse, Unit>>

    @GET("/api/chat/{sessionId}")
    suspend fun getChatHistory(
        @Path("sessionId") sessionId: Int,
        @Query("cursor_created_at") cursorCreatedAt: String?,
        @Query("limit") limit: Int
    ): Response<ApiResponse<ChatListResponse, CursorPagination>>

    @POST("/api/chat/{sessionId}")
    suspend fun chatInExistingSession(
        @Path("sessionId") sessionId: Int,
        @Body request: ChatRequest
    ): Response<ApiResponse<ChatResponse, Unit>>

    @DELETE("/api/chat/{sessionId}")
    suspend fun deleteChatSession(
        @Path("sessionId") sessionId: Int
    ): Response<ApiResponse<Unit, Unit>>
}
