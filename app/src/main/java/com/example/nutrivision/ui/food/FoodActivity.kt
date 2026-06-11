package com.example.nutrivision.ui.food

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrivision.data.remote.request.foodlog.LogFoodRequest
import com.example.nutrivision.databinding.ActivityFoodBinding
import com.example.nutrivision.ui.chat.ChatActivity
import com.example.nutrivision.ui.food.scanfood.ScanFoodActivity
import com.example.nutrivision.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FoodActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFoodBinding

    companion object {
        const val EXTRA_MEAL_TYPE = "EXTRA_MEAL_TYPE"
    }

    private lateinit var foodsAdapter: FoodAdapter
    private var searchJob: Job? = null
    private val foodViewModel: FoodViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityFoodBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }


        setupUI()
        setupRecyclerView()
        observeViewModel()

        foodViewModel.fetchFoods()
    }

    private fun setupUI() {
        val mealType = requireNotNull(intent.getStringExtra(EXTRA_MEAL_TYPE))
        binding.toolbar.title = mealType

        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.btnScan.setOnClickListener { navigateToScanFoodActivity(mealType) }
        binding.btnChatAi.setOnClickListener { navigateToChatAiActivity() }

        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.progressBar.visibility = View.VISIBLE
                binding.rvFoods.visibility = View.GONE
                binding.noFoods.visibility = View.GONE

                // Cancel previous job if exists
                searchJob?.cancel()

                searchJob = lifecycleScope.launch {
                    delay(500)
                    val query = s?.toString()?.takeIf { it.isNotBlank() }
                    foodViewModel.fetchFoods(query = query, isLoadMore = false)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupRecyclerView() {
        foodsAdapter = FoodAdapter(
            mealType = requireNotNull(intent.getStringExtra(EXTRA_MEAL_TYPE)),
            onLogFood = { foodId: Int, mealType: String, servingId: Int, numberOfUnits: Float ->
                foodViewModel.logFood(
                    LogFoodRequest(
                        foodId = foodId,
                        mealType = mealType,
                        servingId = servingId,
                        numberOfUnits = numberOfUnits
                    )
                )
            }
        )

        binding.rvFoods.apply {
            layoutManager = LinearLayoutManager(this@FoodActivity)
            adapter = foodsAdapter
            itemAnimator = null
        }

        // Infinite scroll
        binding.rvFoods.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (lastVisibleItem >= totalItemCount - 5) {
                    foodViewModel.fetchFoods(isLoadMore = true)
                }
            }
        })
    }

    private fun observeViewModel() {
        foodViewModel.uiState.observe(this) { state ->
            when (state) {

                is FoodUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvFoods.visibility = View.GONE
                    binding.noFoods.visibility = View.GONE
                }

                is FoodUiState.Success -> {
                    foodsAdapter.submitList(state.data) {
                        binding.progressBar.visibility = View.GONE

                        if (state.shouldScrollToTop) {
                            binding.rvFoods.scrollToPosition(0)
                        }

                        if (state.data.isEmpty()) {
                            binding.noFoods.visibility = View.VISIBLE
                            binding.rvFoods.visibility = View.GONE
                        } else {
                            binding.noFoods.visibility = View.GONE
                            binding.rvFoods.visibility = View.VISIBLE
                        }
                    }
                }

                is FoodUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.noFoods.visibility = View.VISIBLE
                    showToast(this, state.message)
                }

                else -> Unit
            }
        }
    }

    private fun navigateToScanFoodActivity(mealType: String) {
        val intent = Intent(this, ScanFoodActivity::class.java)
        intent.putExtra(EXTRA_MEAL_TYPE, mealType)
        startActivity(intent)
    }

    private fun navigateToChatAiActivity() {
        val intent = Intent(this, ChatActivity::class.java)
        startActivity(intent)
    }
}