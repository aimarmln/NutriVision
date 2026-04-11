package com.example.nutrivision.ui.main.recipe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrivision.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<RecipeUiState>(RecipeUiState.Idle)
    val uiState: LiveData<RecipeUiState> = _uiState

    private var currentQuery: String? = null
    private var currentPage: Int = 1
    private var isLastPage: Boolean = false
    private var isLoadingPage: Boolean = false
    private val pageSize: Int = 20

    private val accumulatedList = mutableListOf<RecipeListItem>()

    fun fetchRecipes(
        query: String? = currentQuery,
        isLoadMore: Boolean = false
    ) {
        if (isLoadMore && (isLoadingPage || isLastPage)) return

        if (!isLoadMore || query != currentQuery) {
            currentPage = 1
            isLastPage = false
            accumulatedList.clear()
            _uiState.value = RecipeUiState.Loading
        } else {
            accumulatedList.add(RecipeListItem.Loading)
            _uiState.value = RecipeUiState.Success(
                data = accumulatedList.toList(),
                isLoadMore = true
            )
        }

        currentQuery = query
        isLoadingPage = true

        viewModelScope.launch {
            val result = recipeRepository.getRecipesList(
                query,
                currentPage,
                pageSize
            )

            result.onSuccess { response ->
                val newList = response.data ?: emptyList()

                if (isLoadMore) removeLoadingFooter()

                if (newList.size < pageSize) {
                    isLastPage = true
                }

                accumulatedList.addAll(
                    newList.map { RecipeListItem.Item(it) }
                )

                currentPage++

                _uiState.value = RecipeUiState.Success(
                    data = accumulatedList.toList(),
                    isLoadMore = isLoadMore
                )

                isLoadingPage = false

            }.onFailure {
                if (isLoadMore) removeLoadingFooter()

                _uiState.value =
                    RecipeUiState.Error(it.message ?: "Unknown error")

                isLoadingPage = false
            }
        }
    }

    private fun removeLoadingFooter() {
        if (accumulatedList.isNotEmpty() &&
            accumulatedList.lastOrNull() is RecipeListItem.Loading
        ) {
            accumulatedList.removeAt(accumulatedList.lastIndex)
        }
    }

}
