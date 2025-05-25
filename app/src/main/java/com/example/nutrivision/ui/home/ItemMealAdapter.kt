package com.example.nutrivision.ui.home

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrivision.data.remote.response.MealsItem
import com.example.nutrivision.databinding.ItemMealBinding
import com.example.nutrivision.ui.mealdetail.MealDetailActivity

class ItemMealAdapter : ListAdapter<MealsItem, ItemMealAdapter.ItemMealViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemMealAdapter.ItemMealViewHolder {
        val binding = ItemMealBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemMealViewHolder(binding)
    }

    class ItemMealViewHolder(private val binding: ItemMealBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(meal: MealsItem) {
            Log.d("ItemMealAdapter", "Data binding: $meal")

            binding.mealName.text = meal.foodName
            binding.mealCalories.text = "${meal.calories.toString()} Kcal >"

            binding.root.setOnClickListener {
                val intent = Intent(binding.root.context, MealDetailActivity::class.java)
                intent.putExtra(MealDetailActivity.EXTRA_MEAL_ID, meal.mealId)
                intent.putExtra(MealDetailActivity.EXTRA_FOOD_ID, meal.foodId)
                binding.root.context.startActivity(intent)
            }
        }
    }

    override fun onBindViewHolder(holder: ItemMealAdapter.ItemMealViewHolder, position: Int) {
        val meal = getItem(position)
        holder.bind(meal)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MealsItem>() {
            override fun areItemsTheSame(oldItem: MealsItem, newItem: MealsItem): Boolean {
                return oldItem.foodId == newItem.foodId
            }

            override fun areContentsTheSame(
                oldItem: MealsItem,
                newItem: MealsItem
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
}