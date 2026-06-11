package com.example.nutrivision.ui.chat.chathistory

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrivision.databinding.ActivityChatHistoryBinding
import com.example.nutrivision.ui.chat.ChatActivity.Companion.EXTRA_SESSION_DELETED
import com.example.nutrivision.ui.chat.ChatActivity.Companion.EXTRA_SESSION_ID
import com.example.nutrivision.utils.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatHistoryBinding

    private lateinit var  chatHistoryAdapter: ChatHistoryAdapter

    private val viewModel: ChatHistoryViewModel by viewModels()

    private var activeSessionId: Int? = null

    private var deletedActiveSession: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        initData()
        setupUI()
        setupRecyclerView()
        observeViewModel()

        viewModel.getChatSessionsList()
    }

    override fun finish() {
        if (deletedActiveSession) {
            val intent = Intent().apply {
                putExtra(EXTRA_SESSION_DELETED, true)
            }
            setResult(RESULT_OK, intent)
        }

        super.finish()
    }

    private fun initData() {
        activeSessionId =
            if (intent.hasExtra(EXTRA_SESSION_ID)) {
                intent.getIntExtra(
                    EXTRA_SESSION_ID,
                    0
                )
            } else {
                null
            }

        viewModel.setActiveSessionId(activeSessionId)
    }

    private fun setupUI() {
        binding = ActivityChatHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        chatHistoryAdapter = ChatHistoryAdapter(
            onDeleteSession = ::onDeleteSession,
            onSessionSelected = ::onSessionSelected
        )

        binding.rvChatHistory.apply {
            layoutManager = LinearLayoutManager(this@ChatHistoryActivity)
            adapter = chatHistoryAdapter
            itemAnimator = null

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int
                ) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                    if (lastVisibleItem >= totalItemCount - 5) {
                        viewModel.getChatSessionsList(isLoadMore = true)
                    }
                }
            })
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            when (state) {

                is ChatHistoryUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvChatHistory.visibility = View.GONE
                    binding.noChatHistory.visibility = View.GONE
                }

                is ChatHistoryUiState.Success -> {
                    chatHistoryAdapter.submitList(state.data) {
                        binding.progressBar.visibility = View.GONE

                        if (!state.isLoadMore) {
                            binding.rvChatHistory.scrollToPosition(0)
                        }

                        if (state.data.isEmpty()) {
                            binding.noChatHistory.visibility = View.VISIBLE
                            binding.rvChatHistory.visibility = View.GONE
                        } else {
                            binding.noChatHistory.visibility = View.GONE
                            binding.rvChatHistory.visibility = View.VISIBLE
                        }
                    }

                }

                is ChatHistoryUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.noChatHistory.visibility = View.VISIBLE
                    showToast(this, state.message)
                }

                else -> Unit
            }
        }
    }

    private fun onSessionSelected(sessionId: Int) {
        viewModel.setSelectedSession(sessionId)
    }

    private fun onDeleteSession(sessionId: Int) {
        if (activeSessionId == sessionId) {
            deletedActiveSession = true
        }

        viewModel.deleteChatSession(sessionId)
    }
}