package com.example.nutrivision.ui.food.scanfood.scanresult

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutrivision.data.remote.request.foodlog.LogFoodRequest
import com.example.nutrivision.data.remote.response.food.FoodsListResponseItem
import com.example.nutrivision.databinding.ActivityScanResultBinding
import com.example.nutrivision.ui.food.FoodActivity.Companion.EXTRA_MEAL_TYPE
import com.example.nutrivision.ui.food.FoodAdapter
import com.example.nutrivision.ui.food.FoodListItem
import com.example.nutrivision.ui.food.FoodUiState
import com.example.nutrivision.ui.food.FoodViewModel
import com.example.nutrivision.ui.food.scanfood.ScanFoodActivity
import com.example.nutrivision.ui.food.scanfood.ScanFoodActivity.Companion.EXTRA_IMAGE_PATH
import com.example.nutrivision.ui.main.MainActivity
import com.example.nutrivision.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class ScanResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScanResultBinding
    private val scanResultViewModel: ScanResultViewModel by viewModels()
    private lateinit var foodsAdapter: FoodAdapter

    private var scanResults: List<FoodsListResponseItem> = emptyList()
    private lateinit var imagePath: String
    private lateinit var mealType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScanResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initData()
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        handleImage()

        if (scanResults.isNotEmpty()) {
            showToast(this, "Log only the foods you ate")
        }
    }

    private fun initData() {
        imagePath = requireNotNull(intent.getStringExtra(EXTRA_IMAGE_PATH))
        mealType = requireNotNull(intent.getStringExtra(EXTRA_MEAL_TYPE))

        val result = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra(
                ScanFoodActivity.EXTRA_SCAN_RESULTS,
                FoodsListResponseItem::class.java
            )
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra(
                ScanFoodActivity.EXTRA_SCAN_RESULTS
            )
        }

        scanResults = result ?: emptyList()

        scanResultViewModel.setInitialData(scanResults)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        foodsAdapter = FoodAdapter(
            mealType = mealType,
            onLogFood = { foodId: Int, mealType: String, servingId: Int, numberOfUnits: Float ->
                scanResultViewModel.logFood(
                    LogFoodRequest(
                        foodId = foodId,
                        mealType = mealType,
                        servingId = servingId,
                        numberOfUnits = numberOfUnits
                    )
                )
            }
        )

        binding.rvResults.apply {
            layoutManager = LinearLayoutManager(this@ScanResultActivity)
            adapter = foodsAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnDone.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }
    }

    private fun handleImage() {
        if (imagePath.isNotEmpty()) {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            binding.scanImage.setImageBitmap(bitmap)

            val imageFile = File(imagePath)
            if (imageFile.exists()) {
                imageFile.delete()
                Log.d("ScanResultsActivity", "Image cache deleted: $imagePath")
            }
        }

        clearImageCache()
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

    private fun observeViewModel() {
        scanResultViewModel.uiState.observe(this) { state ->
            when (state) {
                is ScanResultUiState.Success -> {
                    foodsAdapter.submitList(state.data)

                    if (state.data.isEmpty()) {
                        binding.noResults.visibility = View.VISIBLE
                        binding.rvResults.visibility = View.GONE
                    } else {
                        binding.noResults.visibility = View.GONE
                        binding.rvResults.visibility = View.VISIBLE
                    }
                }

                is ScanResultUiState.Error -> {
                    showToast(this, state.message)
                }

                else -> Unit
            }
        }
    }
}