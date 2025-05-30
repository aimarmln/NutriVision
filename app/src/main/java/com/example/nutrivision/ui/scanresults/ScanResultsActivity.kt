package com.example.nutrivision.ui.scanresults

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutrivision.MainActivity
import com.example.nutrivision.data.local.SettingPreferences
import com.example.nutrivision.data.local.dataStore
import com.example.nutrivision.data.remote.request.FoodLogRequest
import com.example.nutrivision.data.remote.response.FoodsResponseItem
import com.example.nutrivision.databinding.ActivityScanResultsBinding
import com.example.nutrivision.ui.fooddetail.FoodDetailActivity
import com.example.nutrivision.ui.home.HomeFragment
import com.example.nutrivision.ui.meal.FoodsAdapter
import com.example.nutrivision.ui.meal.MealViewModel
import com.example.nutrivision.ui.meal.MealViewModelFactory
import com.example.nutrivision.ui.scanmeal.ScanMealActivity
import com.example.nutrivision.utils.showToast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

class ScanResultsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanResultsBinding

    private val mealViewModel: MealViewModel by viewModels {
        MealViewModelFactory(application)
    }

    private lateinit var foodsAdapter: FoodsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val pref = SettingPreferences.getInstance(application.dataStore)

        val scanResultsJson = intent.getStringExtra(ScanMealActivity.SCAN_RESULTS)
        val scanResults: List<FoodsResponseItem>? = if (!scanResultsJson.isNullOrEmpty()) {
            val gson = Gson()
            val type = object : TypeToken<List<FoodsResponseItem>>() {}.type
            gson.fromJson(scanResultsJson, type)
        } else {
            null
        }

        val imagePath = intent.getStringExtra(ScanMealActivity.IMAGE_PATH)
        val mealType = intent.getStringExtra(HomeFragment.EXTRA_MEAL_TYPE)

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

        binding.rvResults.layoutManager = LinearLayoutManager(this)
        binding.rvResults.adapter = foodsAdapter

        if (!imagePath.isNullOrEmpty()) {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            binding.scanImage.setImageBitmap(bitmap)

            val imageFile = File(imagePath)
            if (imageFile.exists()) {
                imageFile.delete()
                Log.d("ScanResultsActivity", "Image cache deleted: $imagePath")
            }
        }

        clearImageCache()

        if (!scanResults.isNullOrEmpty()) {
            binding.noResults.visibility = GONE
            val sortedList = scanResults.sortedBy { it.id }
            foodsAdapter.submitList(sortedList)
        } else {
            binding.noResults.visibility = VISIBLE
            foodsAdapter.submitList(emptyList())
        }

        binding.btnDone.setOnClickListener {
            val intent = Intent(this@ScanResultsActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
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
                        showToast(this@ScanResultsActivity, message ?: "Food logged successfully")
                    }
                } else {
                    runOnUiThread {
                        showToast(this@ScanResultsActivity, message ?: "Failed to log food")
                    }
                }
            }
        }
    }

    private fun clearImageCache() {
        val cacheDir = cacheDir
        val imageExtensions = listOf("jpg", "jpeg", "png")

        cacheDir.listFiles()?.forEach { file ->
            if (file.isFile && imageExtensions.any { file.name.endsWith(it, ignoreCase = true) }) {
                val deleted = file.delete()
                Log.d("ScanResultsActivity", "Deleted cache image: ${file.name}, success: $deleted")
            }
        }
    }
}