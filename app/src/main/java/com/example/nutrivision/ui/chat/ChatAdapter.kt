package com.example.nutrivision.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrivision.R
import com.example.nutrivision.databinding.ItemChatAiBinding
import com.example.nutrivision.databinding.ItemChatAiErrorBinding
import com.example.nutrivision.databinding.ItemChatUserBinding
import io.noties.markwon.Markwon
import io.noties.markwon.ext.tables.TablePlugin

class ChatAdapter :
    ListAdapter<ChatListItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ChatListItem.UserMessage -> 0
            is ChatListItem.AiMessage -> 1
            is ChatListItem.AiLoading -> 2
            is ChatListItem.HistoryLoading -> 3
            is ChatListItem.AiError -> 4

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            0 -> UserViewHolder(
                ItemChatUserBinding.inflate(inflater, parent, false)
            )
            1 -> AiViewHolder(
                ItemChatAiBinding.inflate(inflater, parent, false)
            )
            2 -> AiLoadingViewHolder(
                inflater.inflate(R.layout.item_chat_ai_loading, parent, false)
            )

            3 -> HistoryLoadingViewHolder(
                inflater.inflate(R.layout.item_loading, parent, false)
            )
            4 -> AiErrorViewHolder(
                ItemChatAiErrorBinding.inflate(inflater, parent, false)
            )
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ChatListItem.UserMessage -> (holder as UserViewHolder).bind(item)
            is ChatListItem.AiMessage -> (holder as AiViewHolder).bind(item)
            is ChatListItem.AiError -> (holder as AiErrorViewHolder).bind(item)
            else -> {}
        }
    }

    class UserViewHolder(private val binding: ItemChatUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatListItem.UserMessage) {
            binding.tvMessage.text = item.message
        }
    }

    class AiViewHolder(
        private val binding: ItemChatAiBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatListItem.AiMessage) {
//            markwon.setParsedMarkdown(binding.tvMessage, item.message)
            binding.tvMessage.setText(item.message, TextView.BufferType.SPANNABLE)
        }
    }

    class AiLoadingViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class HistoryLoadingViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class AiErrorViewHolder(
        private val binding: ItemChatAiErrorBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ChatListItem.AiError) {
            binding.tvErrorMessage.text = item.message
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ChatListItem>() {
            override fun areItemsTheSame(oldItem: ChatListItem, newItem: ChatListItem) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: ChatListItem, newItem: ChatListItem) =
                oldItem == newItem
        }
    }
}