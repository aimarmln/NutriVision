package com.example.nutrivision.ui.chat.chathistory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrivision.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.addAll
import kotlin.collections.copy
import kotlin.text.clear

@HiltViewModel
class ChatHistoryViewModel @Inject constructor(
    private val chatRepository: ChatRepository
): ViewModel() {

    private val _uiState = MutableLiveData<ChatHistoryUiState>(ChatHistoryUiState.Idle)
    val uiState: LiveData<ChatHistoryUiState> = _uiState

    private var currentPage: Int = 1
    private var isLastPage: Boolean = false
    private var isLoadingPage: Boolean = false
    private val pageSize: Int = 20

    private val accumulatedList = mutableListOf<ChatHistoryListItem>()

    private var activeSessionId: Int? = null

    fun getChatSessionsList(
        isLoadMore: Boolean = false
    ) {
        if (isLoadMore && (isLoadingPage || isLastPage)) return

        if (!isLoadMore) {
            currentPage = 1
            isLastPage = false
            accumulatedList.clear()
            _uiState.value = ChatHistoryUiState.Loading
        } else {
            accumulatedList.add(ChatHistoryListItem.Loading)
            _uiState.value = ChatHistoryUiState.Success(
                data = accumulatedList.toList(),
                isLoadMore = true
            )
        }

        isLoadingPage = true

        viewModelScope.launch {
            val result = chatRepository.getChatSessionsList(
                currentPage,
                pageSize
            )

            result.onSuccess { response ->
                val newList = response.data ?: emptyList()

                if (isLoadMore) removeLoadingFooter()

                if (newList.size < pageSize) {
                    isLastPage = true
                }

                accumulatedList.addAll(
                    newList.map {
                        ChatHistoryListItem.Item(
                            data = it,
                            isActive = it.sessionId == activeSessionId
                        )
                    }
                )

                currentPage++

                _uiState.value = ChatHistoryUiState.Success(
                    data = accumulatedList.toList(),
                    isLoadMore = isLoadMore
                )

                isLoadingPage = false

            }.onFailure {
                if (isLoadMore) removeLoadingFooter()

                _uiState.value =
                    ChatHistoryUiState.Error(it.message ?: "Unknown error")

                isLoadingPage = false
            }
        }
    }

    fun deleteChatSession(sessionId: Int) {
        val index = accumulatedList.indexOfFirst {
            it is ChatHistoryListItem.Item && it.data.sessionId == sessionId
        }
        if (index == -1) return

        accumulatedList[index] = (accumulatedList[index] as ChatHistoryListItem.Item).copy(isDeleting = true)
        _uiState.value = ChatHistoryUiState.Success(
            data = accumulatedList.toList(),
            isLoadMore = true
        )

        viewModelScope.launch {
            val result = chatRepository.deleteChatSession(sessionId)

            result.onSuccess {
                accumulatedList.removeAt(index)
                _uiState.value = ChatHistoryUiState.Success(
                    data = accumulatedList.toList(),
                    isLoadMore = true
                )
            }.onFailure {
                accumulatedList[index] = (accumulatedList[index] as ChatHistoryListItem.Item).copy(isDeleting = false)
                _uiState.value = ChatHistoryUiState.Success(
                    data = accumulatedList.toList(),
                    isLoadMore = true
                )
            }
        }
    }

    fun setActiveSessionId(id: Int?) {
        activeSessionId = id
    }

    fun setSelectedSession(sessionId: Int) {
        val updated = accumulatedList.map { item ->
            if (item is ChatHistoryListItem.Item) {
                item.copy(isActive = item.data.sessionId == sessionId)
            } else item
        }

        accumulatedList.clear()
        accumulatedList.addAll(updated)

        _uiState.value = ChatHistoryUiState.Success(
            data = accumulatedList.toList(),
            isLoadMore = true
        )
    }

    private fun removeLoadingFooter() {
        if (accumulatedList.isNotEmpty() &&
            accumulatedList.lastOrNull() is ChatHistoryListItem.Loading
        ) {
            accumulatedList.removeAt(accumulatedList.lastIndex)
        }
    }
}

