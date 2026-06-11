package com.example.nutrivision.ui.chat

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nutrivision.R
import com.example.nutrivision.databinding.ActivityChatBinding
import com.example.nutrivision.ui.chat.chathistory.ChatHistoryActivity
import com.example.nutrivision.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: ChatAdapter

    private val historyLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != RESULT_OK) return@registerForActivityResult

            val data = result.data
            val sessionId = data?.getIntExtra(EXTRA_SESSION_ID, 0)

            if (sessionId != null && sessionId > 0) {
                viewModel.setSessionId(sessionId)
                viewModel.loadChatSession(isInitialLoad = true)
                updateInputState()

                return@registerForActivityResult
            }

            val isDeleted = data?.getBooleanExtra(
                    EXTRA_SESSION_DELETED,
                    false
                ) ?: false

            if (isDeleted) {
                viewModel.startNewChat()
                updateInputState()
                showToast(this, "Chat session has been deleted")
            }
        }

    companion object {
        const val EXTRA_SESSION_ID = "extra_session_id"
        const val EXTRA_SESSION_DELETED = "extra_session_deleted"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupUI()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())

            val bottomPadding = maxOf(systemBars.bottom, ime.bottom)

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottomPadding)

            if (ime.bottom > 0) {
                val shouldStickToBottom = binding.rvChat.isAtBottom()

                if (shouldStickToBottom) {
                    binding.rvChat.post {
                        val itemCount = adapter.itemCount
                        if (itemCount > 0) {
                            binding.rvChat.smoothScrollToPosition(itemCount - 1)
                        }
                    }
                }
            }

            insets
        }
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_history -> {
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                    val view = currentFocus ?: binding.edtMessage
                    imm.hideSoftInputFromWindow(view.windowToken, 0)

                    val intent = Intent(this, ChatHistoryActivity::class.java).apply {
                        viewModel.getSessionId()?.let {
                            putExtra(
                                EXTRA_SESSION_ID,
                                it
                            )
                        }
                    }
                    historyLauncher.launch(intent)

                    true
                }

                R.id.new_chat -> {
                    viewModel.startNewChat()
                    updateInputState()

                    true
                }

                else -> false
            }
        }

        binding.chipGroupSuggestions.children.forEach { view ->
            val chip = view as? com.google.android.material.chip.Chip ?: return@forEach

            chip.setOnClickListener {
                val text = chip.text.toString()
                binding.edtMessage.setText(text)
                binding.edtMessage.setSelection(text.length)

                binding.edtMessage.requestFocus()
                binding.edtMessage.post {
                    val imm =
                        getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                    imm.showSoftInput(
                        binding.edtMessage,
                        android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
                    )
                }
            }
        }

        binding.edtMessage.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateInputState()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.btnSend.setOnClickListener {
            val message = binding.edtMessage.text.toString().trim()
            if (message.isEmpty()) return@setOnClickListener

            viewModel.sendMessage(message)
            binding.edtMessage.text.clear()

            val imm =
                getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(binding.edtMessage.windowToken, 0)
        }
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter()

        binding.rvChat.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = this@ChatActivity.adapter
        }

        binding.rvChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy >= 0) return

                val lm = rv.layoutManager as LinearLayoutManager
                if (lm.findFirstVisibleItemPosition() <= 3) {
                    viewModel.loadChatSession(loadMore = true)
                }
            }
        })
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            when (state) {

                is ChatUiState.Loading -> {
                    binding.rvChat.adapter = null
                    binding.rvChat.isVisible = false
                    binding.progressBar.isVisible = true

                    updateInputState()
                }

                is ChatUiState.Success -> {
                    if (binding.rvChat.adapter == null) {
                        binding.rvChat.adapter = adapter
                    }

                    adapter.submitList(state.data) {
                        binding.rvChat.post {
                            if (!state.isLoadMore && state.data.isNotEmpty()) {
                                binding.rvChat.smoothScrollToPosition(state.data.lastIndex)
                            }
                            binding.progressBar.isVisible = false
                            binding.rvChat.isVisible = true
                        }
                    }

                    updateInputState()
                }

                is ChatUiState.Error -> {
                    showToast(this, state.message)
                }

                else -> Unit
            }
        }

        viewModel.isSending.observe(this) {
            updateInputState()
        }
    }

    private fun updateInputState() {
        val text = binding.edtMessage.text.toString()
        val isNotEmpty = text.isNotBlank()
        val hasSession = viewModel.hasSession()
        val isSending = viewModel.isSending.value == true
        val isFullLoading = viewModel.uiState.value is ChatUiState.Loading

        binding.btnSend.isEnabled =
            isNotEmpty && !isSending && !isFullLoading

        binding.btnSend.alpha =
            if (binding.btnSend.isEnabled) 1f else 0.5f

        binding.chipContainer.isVisible =
            !isNotEmpty && !hasSession && !isSending
    }

    private fun RecyclerView.isAtBottom(threshold: Int = 2): Boolean {
        val lm = layoutManager as? LinearLayoutManager ?: return false
        val lastVisible = lm.findLastVisibleItemPosition()
        return lastVisible >= (adapter?.itemCount ?: 0) - 1 - threshold
    }
}