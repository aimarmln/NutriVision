package com.example.nutrivision.ui.fooddetail

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
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
        const val EXTRA_WEIGHT = "EXTRA_WEIGHT"
    }

    private val foodDetailViewModel: FoodDetailViewModel by viewModels {
        FoodDetailViewModelFactory(application)
    }

    private lateinit var binding: ActivityFoodDetailBinding

    private var isFoodDetailLoaded = false
    private var isNutritionCalculated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityFoodDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setDisplay(false)

        val foodId = intent.getIntExtra(EXTRA_ID, 0)
        val mealType = intent.getStringExtra(EXTRA_MEAL_TYPE)
        val weight = intent.getIntExtra(EXTRA_WEIGHT, 0)
        Log.d("FoodDetailActivity", "$weight")

        lifecycleScope.launch {
            foodDetailViewModel.fetchFoodDetail(foodId)
            if (weight > 0) {
                Log.d("FoodDetailActivity", "weight greater than 0")
                binding.edtWeight.setText(weight.toString())
                foodDetailViewModel.calculateNutrition(foodId, weight)
            }
        }

        foodDetailViewModel.foodDetailData.observe(this) { foodDetail ->
            if (foodDetail != null) {
                isFoodDetailLoaded = true
                binding.foodName.text = foodDetail.foodName ?: "Unknown food name"
                checkIfReadyToDisplay()
            } else {
                Log.d("FoodDetailActivity", "Data is empty for food detail")
            }
        }

        foodDetailViewModel.nutritionCalcData.observe(this) { nutrition ->
            if (nutrition != null) {
                isNutritionCalculated = true
                binding.foodCalories.text = nutrition.caloriesKcal.toString()
                binding.foodCarbs.text = nutrition.carbohydratesG.toString()
                binding.foodProtein.text = nutrition.proteinsG.toString()
                binding.foodFat.text = nutrition.fatsG.toString()
                checkIfReadyToDisplay()
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

                    if (weight != null) {
                        if (weight > 1000) {
                            binding.edtWeight.setText("1000")
                            binding.edtWeight.setSelection(binding.edtWeight.text.length)
                            binding.edtWeight.error = "Max allowed weight is 1000g"
                            return
                        }

                        if (weight > 0) {
                            lifecycleScope.launch {
                                foodDetailViewModel.calculateNutrition(foodId, weight)
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

    private fun setDisplay(isVisible: Boolean) {
        val viewVisibility: Int = if (isVisible) View.VISIBLE else View.GONE
        val progressBarVisibility: Int = if (isVisible) View.GONE else View.VISIBLE

        binding.foodName.visibility = viewVisibility
        binding.materialCardView.visibility = viewVisibility
        binding.imageView10.visibility = viewVisibility
        binding.edtWeight.visibility = viewVisibility
        binding.edtGrams.visibility = viewVisibility
        binding.progressBar.visibility = progressBarVisibility
    }

    private fun checkIfReadyToDisplay() {
        if (isFoodDetailLoaded && isNutritionCalculated) {
            setDisplay(true)
        }
    }
}