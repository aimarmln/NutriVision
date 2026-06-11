package com.example.nutrivision.ui.chat

import android.text.Spanned

sealed class ChatUiState {
    object Idle : ChatUiState()
    object Loading : ChatUiState()

    data class Success(
        val data: List<ChatListItem>,
        val isLoadMore: Boolean = false
    ) : ChatUiState()

    data class Error(val message: String) : ChatUiState()
}

sealed class ChatListItem {

    abstract val id: String

    data class UserMessage(
        override val id: String,
        val message: String
    ) : ChatListItem()

    data class AiMessage(
        override val id: String,
        val message: Spanned
    ) : ChatListItem()

    data class AiError(
        override val id: String,
        val message: String
    ) : ChatListItem()

    object AiLoading : ChatListItem() {
        override val id: String = "ai_loading"
    }

    object HistoryLoading : ChatListItem() {
        override val id: String = "history_loading"
    }
}
