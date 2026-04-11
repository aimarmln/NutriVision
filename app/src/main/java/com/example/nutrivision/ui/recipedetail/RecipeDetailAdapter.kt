package com.example.nutrivision.ui.recipedetail

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrivision.R
import androidx.core.widget.addTextChangedListener
import com.example.nutrivision.data.remote.response.recipe.CommentItem
import com.example.nutrivision.data.remote.response.recipe.RecipeDetailResponse
import com.example.nutrivision.databinding.ItemCommentBinding
import com.example.nutrivision.databinding.ItemLoadingBinding
import com.example.nutrivision.databinding.ItemRecipeDetailBinding
import com.example.nutrivision.databinding.ItemRecipeDetailCommentsBinding
import com.example.nutrivision.databinding.ItemRecipeDetailsNoCommentsBinding
import com.example.nutrivision.databinding.LayoutShimmerRecipeDetailBinding
import com.example.nutrivision.databinding.LayoutShimmerRecipeDetailCommentsBinding
import com.example.nutrivision.utils.toBulletList

class RecipeDetailAdapter(
    private val onPostComment: (String) -> Unit,
    private val onDeleteComment: (String) -> Unit,
    private val onTextChanged: (String) -> Unit
) : ListAdapter<RecipeDetailListItem, RecyclerView.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<RecipeDetailListItem>() {
        override fun areItemsTheSame(
            oldItem: RecipeDetailListItem,
            newItem: RecipeDetailListItem
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: RecipeDetailListItem,
            newItem: RecipeDetailListItem
        ) = oldItem == newItem

        override fun getChangePayload(
            oldItem: RecipeDetailListItem,
            newItem: RecipeDetailListItem
        ): Any? {
            if (oldItem is RecipeDetailListItem.Comment &&
                newItem is RecipeDetailListItem.Comment
            ) {
                if (oldItem.comment.isDeleting != newItem.comment.isDeleting) {
                    return "DELETE_STATE"
                }
            }

            if (oldItem is RecipeDetailListItem.CommentInput &&
                newItem is RecipeDetailListItem.CommentInput
            ) {
                return "INPUT_STATE"
            }


            return null
        }
    }

    var isPostingComment: Boolean = false
        private set

    fun setPostingState(isLoading: Boolean) {
        isPostingComment = isLoading

        val index = currentList.indexOfFirst {
            it is RecipeDetailListItem.CommentInput
        }

        if (index != -1) {
            notifyItemChanged(index)
        }
    }

    var currentInputText: String = ""
        private set

    fun setCommentInput(text: String) {
        currentInputText = text

        val index = currentList.indexOfFirst {
            it is RecipeDetailListItem.CommentInput
        }

        if (index != -1) {
            notifyItemChanged(index, "INPUT_STATE")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is RecipeDetailListItem.ShimmerRecipe -> 0
            is RecipeDetailListItem.ShimmerCommentForm -> 1
            is RecipeDetailListItem.Recipe -> 2
            is RecipeDetailListItem.CommentInput -> 3
            is RecipeDetailListItem.Comment -> 4
            is RecipeDetailListItem.Loading -> 5
            is RecipeDetailListItem.NoComments -> 6
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        when (viewType) {
            0 -> ShimmerRecipeViewHolder(
                LayoutShimmerRecipeDetailBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            1 -> ShimmerCommentFormViewHolder(
                LayoutShimmerRecipeDetailCommentsBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            2 -> RecipeViewHolder(
                ItemRecipeDetailBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            3 -> CommentInputViewHolder(
                ItemRecipeDetailCommentsBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false),
                onPostComment,
                onTextChanged
            )
            4 -> CommentViewHolder(
                ItemCommentBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                ),
                onDeleteComment
            )
            5 -> LoadingViewHolder(
                ItemLoadingBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            6 -> NoCommentsViewHolder(
                ItemRecipeDetailsNoCommentsBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
            else -> throw IllegalArgumentException("Unknown view type")
        }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            val item = getItem(position)
            if (holder is CommentViewHolder && item is RecipeDetailListItem.Comment) {
                holder.updateDeleteState(item.comment)
                return
            }

            if (holder is CommentInputViewHolder &&
                item is RecipeDetailListItem.CommentInput
            ) {
                holder.bindPostingState(isPostingComment, currentInputText)
                return
            }
        }
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is RecipeDetailListItem.Recipe -> (holder as RecipeViewHolder).bind(item.data)
            is RecipeDetailListItem.Comment -> (holder as CommentViewHolder).bind(item.comment)
            is RecipeDetailListItem.CommentInput -> {
                val holder = holder as CommentInputViewHolder
                holder.bindPostingState(isPostingComment, currentInputText)
            }
            is RecipeDetailListItem.NoComments -> Unit
            is RecipeDetailListItem.Loading -> Unit
            is RecipeDetailListItem.ShimmerRecipe -> Unit
            is RecipeDetailListItem.ShimmerCommentForm -> Unit
        }
    }

    class ShimmerRecipeViewHolder(binding: LayoutShimmerRecipeDetailBinding) :
        RecyclerView.ViewHolder(binding.root)

    class ShimmerCommentFormViewHolder(binding: LayoutShimmerRecipeDetailCommentsBinding) :
        RecyclerView.ViewHolder(binding.root)

    class RecipeViewHolder(private val binding: ItemRecipeDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(recipe: RecipeDetailResponse) {
            val context = binding.root.context
            val imageResId = context.resources.getIdentifier(
                recipe.imagePath,
                "drawable",
                context.packageName
            )
            binding.recipeImage.setImageResource(imageResId)

            binding.recipeName.text = recipe.recipeName
            binding.recipeLikes.text = "🤍 ${recipe.positiveCommentCount}"
            binding.recipeDescription.text = recipe.description
            binding.recipeServing.text = "${recipe.servingYield} Servings"

            binding.recipeHealthCategory.visibility =
                if (recipe.healthCategory == "Unhealthy") View.GONE else View.VISIBLE

            binding.recipeIngredients.text = recipe.ingredients.toBulletList()
            binding.recipeInstructions.text = recipe.instructions.toBulletList()
        }
    }

    class CommentInputViewHolder(
        private val binding: ItemRecipeDetailCommentsBinding,
        private val onPostComment: (String) -> Unit,
        private val onTextChanged: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.edtComment.addTextChangedListener {
                if (!isBinding) {
                    onTextChanged(it.toString())
                }
            }

            binding.btnComment.setOnClickListener {
                val comment = binding.edtComment.text.toString().trim()
                if (comment.isNotEmpty()) {
                    onPostComment(comment)
                }
            }
        }

        private var isBinding = false

        fun bindPostingState(isLoading: Boolean, text: String) {
            isBinding = true

            if (binding.edtComment.text.toString() != text) {
                binding.edtComment.setText(text)
                binding.edtComment.setSelection(text.length)
            }

            isBinding = false

            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnComment.alpha = if (isLoading) 0.5f else 1f
            binding.btnComment.isEnabled = !isLoading
        }
    }

    class CommentViewHolder(
        private val binding: ItemCommentBinding,
        private val onDeleteComment: (String) -> Unit
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CommentListItem.Item) {
            val comment = item.data

            binding.username.text = comment.name
            binding.userComments.text = comment.text

            if (comment.sentiment == "Negative") {
                binding.lovinItLabel.visibility = View.GONE
            }

            updateDeleteState(item)

            binding.btnMore.setOnClickListener {
                showPopup(it, comment)
            }
        }

        fun updateDeleteState(item: CommentListItem.Item) {
            if (item.isDeleting) {
                binding.btnMore.isVisible = false
                binding.progressDelete.isVisible = true
            } else {
                binding.btnMore.isVisible = item.data.isOwnComment
                binding.progressDelete.isVisible = false
            }
        }

        private fun showPopup(view: View, comment: CommentItem) {
            val popup = PopupMenu(view.context, view, Gravity.END, 0, R.style.PopupMenuStyle)
            popup.menuInflater.inflate(R.menu.menu_comment, popup.menu)

            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_delete -> {
                        onDeleteComment(comment.id)
                        true
                    }
                    else -> false
                }
            }

            popup.show()
        }
    }

    class NoCommentsViewHolder(binding: ItemRecipeDetailsNoCommentsBinding) :
        RecyclerView.ViewHolder(binding.root)

    class LoadingViewHolder(binding: ItemLoadingBinding) :
        RecyclerView.ViewHolder(binding.root)

}