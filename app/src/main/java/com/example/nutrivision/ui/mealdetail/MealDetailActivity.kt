package com.example.nutrivision.ui.mealdetail

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.nutrivision.MainActivity
import com.example.nutrivision.data.local.SettingPreferences
import com.example.nutrivision.data.local.dataStore
import com.example.nutrivision.data.remote.request.UpdateMealRequest
import com.example.nutrivision.databinding.ActivityMealDetailBinding
import com.example.nutrivision.utils.showToast
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MealDetailActivity: AppCompatActivity() {

    companion object {
        const val EXTRA_MEAL_ID = "EXTRA_MEAL_ID"
        const val EXTRA_FOOD_ID = "EXTRA_FOOD_ID"
    }

    private val mealDetailViewModel: MealDetailViewModel by viewModels {
        MealDetailViewModelFactory(application)
    }

    private lateinit var binding: ActivityMealDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMealDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val mealId = intent.getIntExtra(EXTRA_MEAL_ID, 0)
        val foodId = intent.getIntExtra(EXTRA_FOOD_ID, 0)

        val pref = SettingPreferences.getInstance(application.dataStore)

        lifecycleScope.launch {
            val accessToken = pref.accessToken.first() ?: "Unknown access token"
            val refreshToken = pref.refreshToken.first() ?: "Unknown refresh token"
            mealDetailViewModel.fetchMealDetail(accessToken, refreshToken, mealId)
        }

        mealDetailViewModel.mealDetailData.observe(this) { mealDetail ->
            if (mealDetail != null) {
                binding.tvFoodName.text = mealDetail.foodName
                binding.tvCalories.text = mealDetail.calories.toString()
                binding.tvCarbs.text = mealDetail.carbohydrates.toString()
                binding.tvProtein.text = mealDetail.proteins.toString()
                binding.tvFat.text = mealDetail.fats.toString()
                binding.edtWeight.setText(mealDetail.weightGrams?.toString() ?: "")
            } else {
                Log.d("MealDetailActivity", "Data is empty for meal detail")
            }
        }

        mealDetailViewModel.nutritionCalcData.observe(this) { nutrition ->
            if (nutrition != null) {
                binding.tvCalories.text = nutrition.caloriesKcal.toString()
                binding.tvCarbs.text = nutrition.carbohydratesG.toString()
                binding.tvProtein.text = nutrition.proteinsG.toString()
                binding.tvFat.text = nutrition.fatsG.toString()
            } else {
                Log.d("FoodDetailActivity", "Data is empty for nutrition calculations")
            }
        }

        binding.btnUpdate.setOnClickListener {
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

            val updateMealRequest = UpdateMealRequest(weightGrams)

            lifecycleScope.launch {
                val accessToken = pref.accessToken.first() ?: "Unknown access token"
                val refreshToken = pref.refreshToken.first() ?: "Unknown refresh token"

                mealDetailViewModel.updateMealDetail(
                    accessToken,
                    refreshToken,
                    mealId,
                    updateMealRequest
                ) { success, message ->
                    if (success) {
                        runOnUiThread {
                            showToast(this@MealDetailActivity, message ?: "Meal updated successfully")
                            val intent = Intent(this@MealDetailActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                    } else {
                        runOnUiThread {
                            showToast(this@MealDetailActivity, message ?: "Meal failed to update")
                        }
                    }
                }
            }
        }

        binding.btnDelete.setOnClickListener {
            lifecycleScope.launch {
                val accessToken = pref.accessToken.first() ?: "Unknown access token"
                val refreshToken = pref.refreshToken.first() ?: "Unknown refresh token"

                mealDetailViewModel.deleteMeal(
                    accessToken,
                    refreshToken,
                    mealId
                ) { success, message ->
                    if (success) {
                        runOnUiThread {
                            showToast(this@MealDetailActivity, message ?: "Meal deleted successfully")
                            val intent = Intent(this@MealDetailActivity, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                    } else {
                        runOnUiThread {
                            showToast(this@MealDetailActivity, message ?: "Meal failed to deleted")
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

                    if (weight != null) {
                        if (weight > 1000) {
                            binding.edtWeight.setText("1000")
                            binding.edtWeight.setSelection(binding.edtWeight.text.length)
                            binding.edtWeight.error = "Max allowed weight is 1000g"
                            return
                        }

                        if (weight > 0) {
                            lifecycleScope.launch {
                                mealDetailViewModel.calculateNutrition(foodId, weight)
                            }
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