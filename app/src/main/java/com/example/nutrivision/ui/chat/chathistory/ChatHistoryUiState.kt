package com.example.nutrivision.ui.chat.chathistory

import com.example.nutrivision.data.remote.response.chat.ChatSessionsListResponseItem

sealed class ChatHistoryUiState {
    object Idle : ChatHistoryUiState()

    object Loading : ChatHistoryUiState()

    data class Success(
        val data: List<ChatHistoryListItem>,
        val isLoadMore: Boolean = false
    ) : ChatHistoryUiState()

    data class Error(val message: String) : ChatHistoryUiState()
}

sealed class ChatHistoryListItem {
    data class Item(
        val data: ChatSessionsListResponseItem,
        val isDeleting: Boolean = false,
        val isActive: Boolean = false
    ) : ChatHistoryListItem()

    object Loading : ChatHistoryListItem()
}