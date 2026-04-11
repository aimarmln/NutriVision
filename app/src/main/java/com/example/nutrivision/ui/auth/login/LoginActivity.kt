package com.example.nutrivision.ui.auth.login

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.nutrivision.ui.main.MainActivity
import com.example.nutrivision.R
import com.example.nutrivision.data.local.SettingPreferences
import com.example.nutrivision.data.local.dataStore
import com.example.nutrivision.data.remote.request.auth.LoginRequest
import com.example.nutrivision.data.remote.response.auth.LoginResponse
import com.example.nutrivision.databinding.ActivityLoginBinding
import com.example.nutrivision.ui.auth.register.RegisterActivity
import com.example.nutrivision.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val pref by lazy { SettingPreferences.getInstance(application.dataStore) }
    private var isPasswordVisible = false

    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupToolbar() = binding.toolbar.apply {
        setSupportActionBar(this)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }

        setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupClickListeners() {
        binding.btnEye.setOnClickListener {
            togglePasswordVisibility()
        }

        binding.tvSignupLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        binding.btnLogin.setOnClickListener {
            val request = validateLoginData() ?: return@setOnClickListener
            loginViewModel.login(request)
        }
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible

        binding.edtPassword.inputType = if (isPasswordVisible) {
            InputType.TYPE_CLASS_TEXT
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        binding.btnEye.setImageResource(if (isPasswordVisible) R.drawable.ic_eye else R.drawable.ic_eye_slash)
        binding.edtPassword.setSelection(binding.edtPassword.text.length)
    }

    private fun observeViewModel() {
        loginViewModel.uiState.observe(this) { state ->
            binding.progressBar.visibility =
                if (state is LoginUiState.Loading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = state !is LoginUiState.Loading
            binding.btnLogin.alpha = if (state !is LoginUiState.Loading) 1f else 0.5f

            when (state) {
                is LoginUiState.Success -> handleLoginSuccess()
                is LoginUiState.Error -> handleLoginError(state.message)
                else -> Unit
            }
        }
    }

    private fun handleLoginSuccess() {
        showToast(this, "Login successful!")
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    private fun handleLoginError(message: String) {
        when {
            message.contains("User not found") -> binding.edtEmail.error = message
            message.contains("Invalid password") -> binding.edtPassword.error = message
            else -> showToast(this, message)
        }
    }

    private fun validateLoginData(): LoginRequest? {
        val email = binding.edtEmail.text.toString().trim()
        val password = binding.edtPassword.text.toString().trim()

        return when {
            email.isEmpty() ->
                binding.edtEmail.apply { error = "Email cannot be empty" }.let { null }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                binding.edtEmail.apply { error = "Please enter a valid email" }.let { null }
            password.isEmpty() ->
                binding.edtPassword.apply { error = "Password cannot be empty" }.let { null }
            else -> {
                binding.edtEmail.error = null
                binding.edtPassword.error = null
                LoginRequest(email, password)
            }
        }
    }
}