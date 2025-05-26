package com.example.nutrivision.ui.recipes

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutrivision.databinding.FragmentRecipesBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RecipesFragment : Fragment() {

    private var _binding: FragmentRecipesBinding? = null

    private val recipesViewModel: RecipesViewModel by viewModels {
        RecipesViewModelFactory()
    }

    private lateinit var recipesAdapter: RecipesAdapter

    private var searchJob: Job? = null
    private var shouldTriggerSearch = false

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        recipesAdapter = RecipesAdapter()
        binding.rvRecipes.layoutManager = LinearLayoutManager(requireActivity())
        binding.rvRecipes.adapter = recipesAdapter

        lifecycleScope.launch {
            recipesViewModel.fetchRecipes()
        }

        recipesViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) VISIBLE else GONE
            if (isLoading) binding.rvRecipes.visibility = GONE
            if (isLoading) binding.noRecipes.visibility = GONE
        }

        recipesViewModel.recipesData.observe(viewLifecycleOwner) { listRecipes ->
            if (listRecipes != null) {
                if (listRecipes.isNotEmpty()) {
                    val sortedList = listRecipes.sortedBy { it.id }
                    recipesAdapter.submitList(sortedList)
                    binding.rvRecipes.visibility = VISIBLE
                } else {
                    recipesAdapter.submitList(emptyList())
                    binding.noRecipes.visibility = VISIBLE
                }
            } else {
                binding.noRecipes.visibility = VISIBLE
                recipesAdapter.submitList(emptyList())
            }
        }

        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!shouldTriggerSearch) return

                searchJob?.cancel()

                searchJob = lifecycleScope.launch {
                    binding.progressBar.visibility = VISIBLE
                    binding.rvRecipes.visibility = GONE
                    binding.noRecipes.visibility = GONE
                    delay(400)

                    val query = s.toString()
                    if (query.isNotEmpty()) {
                        recipesViewModel.searchRecipe(query)
                    } else {
                        Log.d("RecipesFragment", "onTextChanged: ")
                        recipesViewModel.fetchRecipes()
                        delay(500)
                        binding.rvRecipes.smoothScrollToPosition(0)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.edtSearch.post {
            shouldTriggerSearch = true
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}