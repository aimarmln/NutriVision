package com.example.nutrivision.ui.chat.chathistory

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrivision.R
import com.example.nutrivision.data.remote.response.chat.ChatSessionsListResponseItem
import com.example.nutrivision.databinding.ItemChatHistoryBinding
import com.example.nutrivision.ui.chat.ChatActivity.Companion.EXTRA_SESSION_ID
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class ChatHistoryAdapter(
    private val onDeleteSession: (sessionId: Int) -> Unit,
    private val onSessionSelected: (sessionId: Int) -> Unit
) :
    ListAdapter<ChatHistoryListItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM) {
            val binding = ItemChatHistoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            ChatHistoryViewHolder(binding, onDeleteSession, onSessionSelected)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_loading, parent, false)
            LoadingViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ChatHistoryListItem.Item -> {
                (holder as ChatHistoryViewHolder).bind(item)
            }
            is ChatHistoryListItem.Loading -> {
                // Nothing
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ChatHistoryListItem.Item -> VIEW_TYPE_ITEM
            is ChatHistoryListItem.Loading -> VIEW_TYPE_LOADING
        }
    }

    class LoadingViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class ChatHistoryViewHolder(
        private val binding: ItemChatHistoryBinding,
        private val onDeleteSession: (sessionId: Int) -> Unit,
        private val onSessionSelected: (sessionId: Int) -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatHistoryListItem.Item) {
            val context = binding.root.context

            val formattedDate = try {
                val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
                val parsedDate = LocalDateTime.parse(item.data.lastActivityAt, inputFormatter)

                val outputFormatter = DateTimeFormatter.ofPattern(
                    "EEEE, d MMMM yyyy, HH.mm",
                    Locale.ENGLISH
                )

                parsedDate.format(outputFormatter)
            } catch (e: Exception) {
                item.data.lastActivityAt
            }

            binding.lastActivityAt.text = formattedDate
            binding.lastUserMessage.text = item.data.lastUserMessage ?: "-"

            updateDeleteState(item)
            updateActiveState(item)

            binding.root.setOnClickListener {
                if (item.isDeleting) return@setOnClickListener
                onClickChatHistory(context, item)
            }

            binding.btnMenu.setOnClickListener {
                showPopup(it, item.data)
            }
        }

        private fun onClickChatHistory(context: Context, chatHistory: ChatHistoryListItem.Item) {
            onSessionSelected(chatHistory.data.sessionId)

            val intent = Intent().apply {
                putExtra(EXTRA_SESSION_ID, chatHistory.data.sessionId)
            }
            (context as Activity).setResult(Activity.RESULT_OK, intent)
            context.finish()
        }


        private fun updateActiveState(item: ChatHistoryListItem.Item) {
            binding.root.setBackgroundResource(
                if (item.isActive) {
                    R.drawable.bg_chat_history_active
                } else {
                    android.R.color.transparent
                }
            )
        }

        private fun updateDeleteState(
            item: ChatHistoryListItem.Item
        ) {

            val isDeleting = item.isDeleting

            binding.btnMenu.visibility = if (isDeleting) View.INVISIBLE else View.VISIBLE
            binding.progressDelete.visibility = if (isDeleting) View.VISIBLE else View.GONE

            binding.root.isEnabled = !isDeleting
            binding.root.isClickable = !isDeleting
            binding.root.isFocusable = !isDeleting

            binding.root.alpha =
                if (isDeleting) 0.5f else 1f
        }

        private fun showPopup(view: View, session: ChatSessionsListResponseItem) {
            val popup = PopupMenu(view.context, view, Gravity.END, 0, R.style.PopupMenuStyle)
            popup.menuInflater.inflate(R.menu.menu_chat_history_item, popup.menu)

            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_delete -> {
                        onDeleteSession(session.sessionId)
                        true
                    }
                    else -> false
                }
            }

            popup.show()
        }
    }

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_LOADING = 1
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ChatHistoryListItem>() {
            override fun areItemsTheSame(
                oldItem: ChatHistoryListItem,
                newItem: ChatHistoryListItem
            ): Boolean {
                return if (oldItem is ChatHistoryListItem.Item && newItem is ChatHistoryListItem.Item) {
                    oldItem.data.sessionId == newItem.data.sessionId
                } else {
                    oldItem == newItem
                }
            }

            override fun areContentsTheSame(
                oldItem: ChatHistoryListItem,
                newItem: ChatHistoryListItem
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
}