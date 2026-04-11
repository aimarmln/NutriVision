package com.example.nutrivision.ui.food.fooddetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrivision.data.remote.response.food.Serving
import com.example.nutrivision.databinding.ItemServingBinding

class ServingAdapter(
    private val onClick: (Serving) -> Unit
) : ListAdapter<ServingItem, ServingAdapter.VH>(DIFF_CALLBACK) {

    private var selectedId: String? = null

    fun setSelected(id: String) {
        selectedId = id
    }

    class VH(val binding: ItemServingBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemServingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)

        val serving = item.data

        holder.binding.tvName.text = serving.servingUnit

        val isSelected = serving.id == selectedId
        holder.binding.root.isSelected = isSelected

        holder.itemView.setOnClickListener {
            val oldId = selectedId
            selectedId = serving.id

            notifyItemChanged(currentList.indexOfFirst { it.data.id == oldId })
            notifyItemChanged(position)

            onClick(serving)
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ServingItem>() {
            override fun areItemsTheSame(
                oldItem: ServingItem,
                newItem: ServingItem
            ): Boolean {
                return oldItem.data.id == newItem.data.id
            }

            override fun areContentsTheSame(
                oldItem: ServingItem,
                newItem: ServingItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}