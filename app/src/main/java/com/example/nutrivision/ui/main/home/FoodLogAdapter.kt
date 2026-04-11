package com.example.nutrivision.ui.main.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrivision.data.remote.response.user.FoodLogItem
import com.example.nutrivision.databinding.ItemFoodLogBinding
import com.example.nutrivision.ui.foodlog.FoodLogActivity
import com.example.nutrivision.ui.foodlog.FoodLogActivity.Companion.EXTRA_FOOD_LOG_ID

class FoodLogAdapter :
    ListAdapter<FoodLogItem, FoodLogAdapter.ItemFoodLogViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemFoodLogViewHolder {
        val binding =
            ItemFoodLogBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return ItemFoodLogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemFoodLogViewHolder, position: Int) {
        val foodLog = getItem(position)
        holder.bind(foodLog)
    }

    class ItemFoodLogViewHolder(private val binding: ItemFoodLogBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(foodLog: FoodLogItem) {
            binding.mealName.text = foodLog.foodName
            binding.mealCalories.text = "${foodLog.calories} Kcal >"

            binding.root.setOnClickListener {
                val intent =
                    Intent(binding.root.context, FoodLogActivity::class.java)

                intent.putExtra(EXTRA_FOOD_LOG_ID, foodLog.foodLogId)
                binding.root.context.startActivity(intent)
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FoodLogItem>() {
            override fun areItemsTheSame(
                oldItem: FoodLogItem,
                newItem: FoodLogItem
            ): Boolean {
                return oldItem.foodId == newItem.foodId
            }

            override fun areContentsTheSame(
                oldItem: FoodLogItem,
                newItem: FoodLogItem
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
}