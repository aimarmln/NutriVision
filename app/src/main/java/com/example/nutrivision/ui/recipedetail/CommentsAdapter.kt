package com.example.nutrivision.ui.recipedetail

import android.util.Log
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrivision.data.remote.response.CommentsItem
import com.example.nutrivision.databinding.ItemCommentBinding

class CommentsAdapter : ListAdapter<CommentsItem, CommentsAdapter.CommentsViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CommentsAdapter.CommentsViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentsViewHolder(binding)
    }

    class CommentsViewHolder(private val binding: ItemCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: CommentsItem) {
            Log.d("CommentsAdapter", "Data binding: $comment")

            binding.username.text = comment.name
            binding.userComments.text = comment.text


            if (comment.sentiment == "Negative") {
                binding.lovinItLabel.visibility = GONE
            }
        }
    }

    override fun onBindViewHolder(holder: CommentsAdapter.CommentsViewHolder, position: Int) {
        val comment = getItem(position)
        holder.bind(comment)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CommentsItem>() {
            override fun areItemsTheSame(oldItem: CommentsItem, newItem: CommentsItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: CommentsItem,
                newItem: CommentsItem
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
}