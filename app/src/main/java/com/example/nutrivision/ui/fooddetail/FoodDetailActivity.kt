package com.example.nutrivision.ui.fooddetail

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.nutrivision.data.local.SettingPreferences
import com.example.nutrivision.data.local.dataStore
import com.example.nutrivision.data.remote.request.FoodLogRequest
import com.example.nutrivision.databinding.ActivityFoodDetailBinding
import com.example.nutrivision.utils.showToast
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FoodDetailActivity: AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "EXTRA_ID"
        const val EXTRA_MEAL_TYPE = "EXTRA_MEAL_TYPE"
    }

    private val foodDetailViewModel: FoodDetailViewModel by viewModels {
        FoodDetailViewModelFactory(application)
    }

    private lateinit var binding: ActivityFoodDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityFoodDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val foodId = intent.getIntExtra(EXTRA_ID, 0)
        val mealType = intent.getStringExtra(EXTRA_MEAL_TYPE)

        lifecycleScope.launch {
            foodDetailViewModel.fetchFoodDetail(foodId)
        }

        foodDetailViewModel.foodDetailData.observe(this) { foodDetail ->
            if (foodDetail != null) {
                binding.foodName.text = foodDetail.foodName ?: "Unknown food name"
                binding.foodCalories.text = foodDetail.caloriesPer100gKcal.toString() ?: "Unknown calories"
                binding.foodCarbs.text = foodDetail.carbohydratePer100gG.toString() ?: "Unknown carbs"
                binding.foodProtein.text = foodDetail.proteinPer100gG.toString() ?: "Unknown protein"
                binding.foodFat.text = foodDetail.fatPer100gG.toString() ?: "Unknown fat"
            } else {
                Log.d("FoodDetailActivity", "Data is empty for food detail")
            }
        }

        foodDetailViewModel.nutritionCalcData.observe(this) { nutrition ->
            if (nutrition != null) {
                binding.foodCalories.text = nutrition.caloriesKcal.toString()
                binding.foodCarbs.text = nutrition.carbohydratesG.toString()
                binding.foodProtein.text = nutrition.proteinsG.toString()
                binding.foodFat.text = nutrition.fatsG.toString()
            } else {
                Log.d("FoodDetailActivity", "Data is empty for nutrition calculations")
            }
        }

        val pref = SettingPreferences.getInstance(application.dataStore)

        binding.logButton.setOnClickListener {
            val weightText = binding.edtWeight.text.toString()

            if (weightText.isBlank()) {
                binding.edtWeight.error = "Weight cannot be empty"
                return@setOnClickListener
            }

            val weightGrams = weightText.toIntOrNull()
            if (weightGrams == null || weightGrams <= 0) {
                binding.edtWeight.error = "Please enter a valid weight (greater than 0)"
                return@setOnClickListener
            }

            val foodLogRequest = FoodLogRequest(
                foodId = foodId,
                mealType = mealType,
                weightGrams = weightGrams
            )

            lifecycleScope.launch {
                val accessToken = pref.accessToken.first() ?: "Unknown access token"
                val refreshToken = pref.refreshToken.first() ?: "Unknown refresh token"

                foodDetailViewModel.logFood(
                    accessToken,
                    refreshToken,
                    foodLogRequest
                ) { success, message ->
                    if (success) {
                        runOnUiThread {
                            showToast(this@FoodDetailActivity, message ?: "Food logged successfully")
                        }
                    } else {
                        runOnUiThread {
                            showToast(this@FoodDetailActivity, message ?: "Failed to log food")
                        }
                    }
                }
            }
        }
        
        binding.edtWeight.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val weightText = s.toString()

                if (weightText.isNotEmpty()) {
                    val weight = weightText.toIntOrNull()
                    if (weight != null && weight > 0) {
                        lifecycleScope.launch {
                            foodDetailViewModel.calculateNutrition(foodId, weight)
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}