package com.example.nutrivision.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.example.nutrivision.MainActivity
import com.example.nutrivision.R
import com.example.nutrivision.data.local.SettingPreferences
import com.example.nutrivision.data.local.dataStore
import com.example.nutrivision.databinding.ActivityLoginBinding
import com.example.nutrivision.ui.signup.SignupActivity
import com.example.nutrivision.utils.showToast
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val loginViewModel: LoginViewModel by viewModels {
        LoginViewModelFactory()
    }

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val pref = SettingPreferences.getInstance(application.dataStore)

        loginViewModel.loginResponse.observe(this) { response ->
            if (response != null) {
                lifecycleScope.launch {
                    pref.saveTokens(
                        response.accessToken.orEmpty(),
                        response.refreshToken.orEmpty()
                    )
                }

                showToast(this@LoginActivity, "Login successful!")

                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }

        loginViewModel.errorMessage.observe(this) { errorMsg ->
            if (errorMsg != null) {
                when {
                    errorMsg.contains("User not found") -> {
                        binding.edtEmailLogin.error = errorMsg
                        binding.edtPasswordLogin.error = null
                    }

                    errorMsg.contains("Invalid password") -> {
                        binding.edtPasswordLogin.error = errorMsg
                        binding.edtEmailLogin.error = null
                    }

                    else -> {
                        Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                        binding.edtEmailLogin.error = null
                        binding.edtPasswordLogin.error = null
                    }
                }
            } else {
                binding.edtEmailLogin.error = null
                binding.edtPasswordLogin.error = null
            }
        }

        binding.tvSignupLink.setOnClickListener {
            val intent = Intent(this@LoginActivity, SignupActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.edtEmailLogin.text.toString().trim()
            val password = binding.edtPasswordLogin.text.toString().trim()

            if (validateInputs(email, password)) {
                lifecycleScope.launch {
                    loginViewModel.login(email, password)
                }
            }
        }

        val hankenTypeface = ResourcesCompat.getFont(this, R.font.hanken_grotesk_light)

        binding.btnEye.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                binding.edtPasswordLogin.inputType = InputType.TYPE_CLASS_TEXT
                binding.btnEye.setImageResource(R.drawable.ic_eye)
            } else {
                binding.edtPasswordLogin.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.btnEye.setImageResource(R.drawable.ic_eye_slash)
            }

            binding.edtPasswordLogin.typeface = hankenTypeface
            binding.edtPasswordLogin.setSelection(binding.edtPasswordLogin.text.length)
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            binding.edtEmailLogin.error = "Email cannot be empty"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edtEmailLogin.error = "Please enter a valid email"
            isValid = false
        } else {
            binding.edtEmailLogin.error = null
        }

        if (password.isEmpty()) {
            binding.edtPasswordLogin.error = "Password cannot be empty"
            isValid = false
        } else {
            binding.edtPasswordLogin.error = null
        }

        return isValid
    }
}