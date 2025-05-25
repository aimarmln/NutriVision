package com.example.nutrivision.ui.meal

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutrivision.data.local.SettingPreferences
import com.example.nutrivision.data.local.dataStore
import com.example.nutrivision.data.remote.request.FoodLogRequest
import com.example.nutrivision.databinding.ActivityMealBinding
import com.example.nutrivision.ui.fooddetail.FoodDetailActivity
import com.example.nutrivision.ui.home.HomeFragment
import com.example.nutrivision.ui.scanmeal.ScanMealActivity
import com.example.nutrivision.utils.showToast
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MealActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMealBinding

    private val mealViewModel: MealViewModel by viewModels {
        MealViewModelFactory(application)
    }

    private lateinit var foodsAdapter: FoodsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMealBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val pref = SettingPreferences.getInstance(application.dataStore)

        val mealType = intent.getStringExtra(HomeFragment.EXTRA_MEAL_TYPE)
        binding.mealTypeTitle.text = mealType

        foodsAdapter = FoodsAdapter(
            mealType = mealType,
            onItemClickListener = object : FoodsAdapter.OnItemClickListener {
                override fun onItemClick(foodId: Int, mealType: String?) {
                    navigateToFoodDetail(foodId, mealType)
                }
            },
            onLogClickListener = object : FoodsAdapter.OnLogClickListener {
                override fun onLogClick(foodId: Int, weightGrams: Int) {
                    logFood(foodId, pref, mealType, weightGrams)
                }
            }
        )

        binding.rvFoods.layoutManager = LinearLayoutManager(this)
        binding.rvFoods.adapter = foodsAdapter

        lifecycleScope.launch {
            mealViewModel.fetchFoods()
        }

        mealViewModel.foodsData.observe(this) { listFoods ->
            if (listFoods != null) {
                if (listFoods.isNotEmpty()) {
                    binding.noFoods.visibility = GONE
                    val sortedList = listFoods.sortedBy { it.id }
                    foodsAdapter.submitList(sortedList)
                } else {
                    binding.noFoods.visibility = VISIBLE
                    foodsAdapter.submitList(emptyList())
                }
            } else {
                binding.noFoods.visibility = VISIBLE
                foodsAdapter.submitList(emptyList())
            }
        }

        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()

                if (query.isNotEmpty()) {
                    lifecycleScope.launch {
                        mealViewModel.searchFood(query)
                    }
                } else {
                    lifecycleScope.launch {
                        mealViewModel.fetchFoods()
                        delay(500)
                        binding.rvFoods.smoothScrollToPosition(0)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnScan.setOnClickListener {
            navigateToScanMealActivity(mealType)
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun navigateToFoodDetail(foodId: Int, mealType: String?) {
        val intent = Intent(this, FoodDetailActivity::class.java)
        intent.putExtra(FoodDetailActivity.EXTRA_ID, foodId)
        intent.putExtra(FoodDetailActivity.EXTRA_MEAL_TYPE, mealType)
        startActivity(intent)
    }

    private fun navigateToScanMealActivity(mealType: String?) {
        val intent = Intent(this@MealActivity, ScanMealActivity::class.java)
        intent.putExtra(HomeFragment.EXTRA_MEAL_TYPE, mealType)
        startActivity(intent)
    }

    private fun logFood(foodId: Int, pref: SettingPreferences, mealType: String?, weightGrams: Int) {
        val foodLogRequest = FoodLogRequest(
            foodId = foodId,
            mealType = mealType,
            weightGrams = weightGrams
        )

        lifecycleScope.launch {
            val accessToken = pref.accessToken.first() ?: "Unknown access token"
            val refreshToken = pref.refreshToken.first() ?: "Unknown refresh token"

            mealViewModel.logFood(
                accessToken,
                refreshToken,
                foodLogRequest
            ) { success, message ->
                if (success) {
                    runOnUiThread {
                        showToast(this@MealActivity, message ?: "Food logged successfully")
                    }
                } else {
                    runOnUiThread {
                        showToast(this@MealActivity, message ?: "Failed to log food")
                    }
                }
            }
        }
    }
}