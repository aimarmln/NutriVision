package com.example.nutrivision.data.repository

import com.example.nutrivision.data.remote.api.ChatService
import com.example.nutrivision.data.remote.request.chat.ChatRequest
import com.example.nutrivision.utils.toResult

class ChatRepository(
    private val chatService: ChatService
) {

    suspend fun getChatSessionsList(page: Int, limit: Int) =
        chatService.getChatSessionsList(page, limit).toResult()

    suspend fun initChatSession(body: ChatRequest) =
        chatService.initChatSession(body).toResult()

    suspend fun getChatHistory(sessionId: Int, cursorCreatedAt: String?, limit: Int) =
        chatService.getChatHistory(sessionId, cursorCreatedAt, limit).toResult()

    suspend fun chatInExistingSession(sessionId: Int, body: ChatRequest) =
        chatService.chatInExistingSession(sessionId, body).toResult()

    suspend fun deleteChatSession(sessionId: Int) =
        chatService.deleteChatSession(sessionId).toResult()
}