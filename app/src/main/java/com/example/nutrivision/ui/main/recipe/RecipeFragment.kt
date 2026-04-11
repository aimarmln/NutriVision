package com.example.nutrivision.ui.main.recipe

import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrivision.databinding.FragmentRecipeBinding
import com.example.nutrivision.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecipeFragment : Fragment() {

    private var _binding: FragmentRecipeBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var recipeAdapter: RecipeAdapter

    private var searchJob: Job? = null
    private var shouldTriggerSearch = false

    private val recipesViewModel: RecipeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeBinding.inflate(inflater, container, false)

        setupWindowInsets()
        setupRecyclerView()
        observeUiState()
        setupSearch()
        fetchRecipesIfRequired()

        return binding.root
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)

            binding.statusBarBackground.updateLayoutParams {
                height = systemBars.top
            }

            insets
        }
    }

    private fun setupRecyclerView() {
        recipeAdapter = RecipeAdapter()

        val layoutManager = LinearLayoutManager(requireContext())

        binding.rvRecipes.apply {
            this.layoutManager = layoutManager
            adapter = recipeAdapter
            itemAnimator = null

            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val position = parent.getChildAdapterPosition(view)
                    val spacing = (16 * resources.displayMetrics.density).toInt()

                    outRect.left = spacing
                    outRect.right = spacing
                    outRect.bottom = spacing

                    if (position == 0) {
                        outRect.top = spacing
                    }
                }
            })

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int
                ) {
                    if (dy <= 0) return

                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition =
                        layoutManager.findFirstVisibleItemPosition()

                    if ((visibleItemCount + firstVisibleItemPosition)
                        >= totalItemCount - 5
                    ) {
                        recipesViewModel.fetchRecipes(
                            isLoadMore = true
                        )
                    }
                }
            })
        }
    }

    private fun observeUiState() {
        recipesViewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {

                is RecipeUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvRecipes.visibility = View.GONE
                    binding.noRecipes.visibility = View.GONE
                }

                is RecipeUiState.Success -> {
                    recipeAdapter.submitList(state.data) {
                        binding.progressBar.visibility = View.GONE

                        if (!state.isLoadMore) {
                            binding.rvRecipes.scrollToPosition(0)
                        }

                        if (state.data.isEmpty()) {
                            binding.noRecipes.visibility = View.VISIBLE
                            binding.rvRecipes.visibility = View.GONE
                        } else {
                            binding.noRecipes.visibility = View.GONE
                            binding.rvRecipes.visibility = View.VISIBLE
                        }
                    }

                }

                is RecipeUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.noRecipes.visibility = View.VISIBLE
                    showToast(requireContext(), state.message)
                }

                else -> Unit
            }
        }
    }

    private fun setupSearch() {
        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!shouldTriggerSearch) return

                binding.progressBar.visibility = View.VISIBLE
                binding.rvRecipes.visibility = View.GONE
                binding.noRecipes.visibility = View.GONE

                searchJob?.cancel()

                searchJob = lifecycleScope.launch {
                    delay(500)

                    val query = s?.toString()?.takeIf { it.isNotBlank() }

                    recipesViewModel.fetchRecipes(
                        query = query,
                        isLoadMore = false
                    )
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.edtSearch.post {
            shouldTriggerSearch = true
        }
    }

    private fun fetchRecipesIfRequired() {
        val state = recipesViewModel.uiState.value

        val isNotLoading = state !is RecipeUiState.Loading
        val isNotSuccess = state !is RecipeUiState.Success

        if (isNotLoading && isNotSuccess) {
            recipesViewModel.fetchRecipes()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}