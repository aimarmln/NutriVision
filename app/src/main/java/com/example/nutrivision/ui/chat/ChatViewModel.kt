package com.example.nutrivision.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrivision.data.remote.request.chat.ChatRequest
import com.example.nutrivision.data.remote.response.Cursor
import com.example.nutrivision.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.noties.markwon.Markwon
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val markwon: Markwon
) : ViewModel() {

    private val _uiState = MutableLiveData<ChatUiState>(ChatUiState.Idle)
    val uiState: LiveData<ChatUiState> = _uiState

    private val _isSending = MutableLiveData(false)
    val isSending: LiveData<Boolean> = _isSending

    private val accumulatedList = mutableListOf<ChatListItem>()

    private var sessionId: Int? = null

    private var lastCursor: Cursor? = null
    private var isLastPage = false

    private var sendJob: Job? = null
    private var historyJob: Job? = null

    fun initSession(message: String) {
        _isSending.value = true

        accumulatedList.add(ChatListItem.UserMessage(
            id = "user-${System.nanoTime()}",
            message = message
        ))
        accumulatedList.add(ChatListItem.AiLoading) // AI loading

        _uiState.value = ChatUiState.Success(accumulatedList.toList())

        sendJob = viewModelScope.launch {
            try {
                val result = chatRepository.initChatSession(ChatRequest(message))

                result.onSuccess { response ->
                    removeAiLoading()

                    sessionId = response.data?.sessionId

                    val aiMessage = response.data?.message
                    val message = markwon.toMarkdown(
                        aiMessage?.content.orEmpty()
                    )

                    accumulatedList.add(
                        ChatListItem.AiMessage(
                            id = aiMessage?.id?.let { "ai-$it" }
                                ?: "ai-${System.nanoTime()}",
                            message = message
                        )
                    )

                    _uiState.value = ChatUiState.Success(accumulatedList.toList())
                }.onFailure { throwable ->
                    if (throwable is CancellationException) throw throwable

                    removeAiLoading()

                    accumulatedList.add(ChatListItem.AiError(
                        id = "error-${System.nanoTime()}",
                        message = "Terjadi kesalahan, coba lagi"
                    ))
                    _uiState.value = ChatUiState.Success(accumulatedList.toList())
                }
            } finally {
                removeAiLoading()
                _isSending.value = false
                sendJob = null
            }
        }
    }

    fun sendMessage(message: String) {
        if (sendJob?.isActive == true) return

        val currentSession = sessionId ?: return initSession(message)

        _isSending.value = true

        accumulatedList.add(ChatListItem.UserMessage(
            id = "user-${System.nanoTime()}",
            message = message
        ))
        accumulatedList.add(ChatListItem.AiLoading)

        _uiState.value = ChatUiState.Success(accumulatedList.toList())

        sendJob = viewModelScope.launch {
            try {
                val result = chatRepository.chatInExistingSession(
                    currentSession,
                    ChatRequest(message)
                )

                result.onSuccess { response ->
                    removeAiLoading()

                    val aiMessage = response.data?.message
                    val message = markwon.toMarkdown(
                        aiMessage?.content.orEmpty()
                    )

                    accumulatedList.add(
                        ChatListItem.AiMessage(
                            id = aiMessage?.id?.let { "ai-$it" }
                                ?: "ai-${System.nanoTime()}",
                            message = message
                        )
                    )

                    _uiState.value = ChatUiState.Success(accumulatedList.toList())
                }.onFailure { throwable ->
                    if (throwable is CancellationException) throw throwable

                    removeAiLoading()

                    accumulatedList.add(ChatListItem.AiError(
                        id = "error-${System.nanoTime()}",
                        message = "Terjadi kesalahan, coba lagi"
                    ))
                    _uiState.value = ChatUiState.Success(accumulatedList.toList())
                }
            } finally {
                removeAiLoading()
                _isSending.value = false
                sendJob = null
            }
        }
    }

    fun loadChatSession(isInitialLoad: Boolean = false, loadMore: Boolean = false) {
        if (loadMore) {
            if (historyJob?.isActive == true || isLastPage) return
        } else {
            historyJob?.cancel()
        }

        val currentSession = sessionId ?: return
        if (isInitialLoad && loadMore) return

        if (!loadMore) {
            lastCursor = null
            isLastPage = false
            accumulatedList.clear()
        }

        if (isInitialLoad) {
            _uiState.value = ChatUiState.Loading
        } else {
            accumulatedList.add(0, ChatListItem.HistoryLoading)

            _uiState.value = ChatUiState.Success(
                accumulatedList.toList(),
                isLoadMore = true
            )
        }

        historyJob = viewModelScope.launch {
            try {
                val result = chatRepository.getChatHistory(
                    currentSession,
                    lastCursor?.createdAt,
                    20
                )

                result.onSuccess { response ->
                    if (loadMore) removeHistoryLoading()

                    val messages = response.data ?: emptyList()

                    val newItems = messages.map {
                        if (it.role == "User") {
                            ChatListItem.UserMessage(
                                id = "user-${it.id}",
                                message = it.message
                            )
                        } else {
                            ChatListItem.AiMessage(
                                id = "ai-${it.id}",
                                message = markwon.toMarkdown(
                                    it.message
                                )
                            ) // Role = "Assistant"
                        }
                    }

                    lastCursor = response.pagination?.nextCursor
                    if (response.pagination?.hasMore == false) isLastPage = true

                    accumulatedList.addAll(0, newItems)

                    _uiState.value = ChatUiState.Success(
                        accumulatedList.toList(),
                        isLoadMore = loadMore
                    )
                }.onFailure { throwable ->
                    if (throwable is CancellationException) throw throwable

                    if (loadMore) removeHistoryLoading()

                    _uiState.value = ChatUiState.Error(
                        "Error loading history"
                    )
                }
            } finally {
                removeHistoryLoading()
                historyJob = null
            }
        }
    }

    fun getSessionId(): Int? {
        return sessionId
    }

    fun setSessionId(newSessionId: Int) {
        if (sessionId == newSessionId) return

        sendJob?.cancel()
        historyJob?.cancel()

        sessionId = newSessionId
        resetState()
    }

    fun startNewChat() {
        if (!hasActiveConversation()) return

        sendJob?.cancel()
        historyJob?.cancel()

        sessionId = null
        resetState()

        _uiState.value = ChatUiState.Success(emptyList())
    }

    fun hasSession(): Boolean {
        return sessionId != null
    }

    fun hasActiveConversation(): Boolean {
        return sessionId != null ||
                sendJob?.isActive == true ||
                accumulatedList.isNotEmpty()
    }

    private fun removeHistoryLoading() {
        if (accumulatedList.firstOrNull() is ChatListItem.HistoryLoading) {
            accumulatedList.removeAt(0)
        }
    }

    private fun removeAiLoading() {
        if (accumulatedList.lastOrNull() is ChatListItem.AiLoading) {
            accumulatedList.removeAt(accumulatedList.lastIndex)
        }
    }

    private fun resetState() {
        accumulatedList.clear()
        lastCursor = null
        isLastPage = false
        _isSending.value = false
    }
}