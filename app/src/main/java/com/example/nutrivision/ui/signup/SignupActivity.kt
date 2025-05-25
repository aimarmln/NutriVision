package com.example.nutrivision.ui.signup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.nutrivision.ui.login.LoginActivity
import com.example.nutrivision.databinding.ActivitySignupBinding
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    private val signupViewModel: SignupViewModel by viewModels {
        SignupViewModelFactory()
    }

    companion object {
        const val EXTRA_SIGNUP_USER = "EXTRA_SIGNUP_USER"
    }

    private lateinit var signupUser: SignupUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        signupViewModel.checkEmailResponse.observe(this) { emailResponse ->
            if (emailResponse != null) {
                if (emailResponse.exists == true) {
                    binding.edtEmailSignup.error = emailResponse.msg
                } else {
                    binding.edtEmailSignup.error = null

                    signupUser = SignupUser(
                        email = binding.edtEmailSignup.text.toString().trim(),
                        password = binding.edtPasswordSignup.text.toString().trim()
                    )
                    Log.d("SignupActivity", "Email response value:$signupUser")

                    val intent = Intent(this, NameActivity::class.java)
                    intent.putExtra(EXTRA_SIGNUP_USER, signupUser)
                    startActivity(intent)
                }
            } else {
                Log.d("SignupActivity", "Data is empty for email response")
            }
        }

        binding.tvLoginLink.setOnClickListener {
            val intent = Intent(this@SignupActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.btnSignup.setOnClickListener {
            val email = binding.edtEmailSignup.text.toString().trim()
            val password = binding.edtPasswordSignup.text.toString().trim()
            val confirmPassword = binding.edtConfirmPasswordSignup.text.toString().trim()

            var isValid = true

            if (email.isEmpty()) {
                binding.edtEmailSignup.error = "Email cannot be empty"
                isValid = false
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.edtEmailSignup.error = "Please enter a valid email"
                isValid = false
            } else {
                binding.edtEmailSignup.error = null
            }

            if (password.isEmpty()) {
                binding.edtPasswordSignup.error = "Password cannot be empty"
                isValid = false
            } else if (password.length < 8) {
                binding.edtPasswordSignup.error = "Password must be at least 8 characters"
                isValid = false
            } else {
                binding.edtPasswordSignup.error = null
            }

            if (confirmPassword.isEmpty()) {
                binding.edtConfirmPasswordSignup.error = "Confirm password cannot be empty"
                isValid = false
            } else if (confirmPassword != password) {
                binding.edtConfirmPasswordSignup.error = "Passwords do not match"
                isValid = false
            } else {
                binding.edtConfirmPasswordSignup.error = null
            }

            if (isValid) {
                lifecycleScope.launch {
                    signupViewModel.checkEmailExists(email)
                }
            }
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}