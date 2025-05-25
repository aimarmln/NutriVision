package com.example.nutrivision.ui.signup

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.nutrivision.databinding.ActivityNameBinding

class NameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val signupUser = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra<SignupUser>(SignupActivity.EXTRA_SIGNUP_USER, SignupUser::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<SignupUser>(SignupActivity.EXTRA_SIGNUP_USER)
        }

        binding.btnContinueName.setOnClickListener {
            val name = binding.edtName.text.toString().trim()

            if (name.isEmpty()) {
                binding.edtName.error = "Name cannot be empty"
                binding.edtName.requestFocus()
                return@setOnClickListener
            }

            if (name.length < 3) {
                binding.edtName.error = "Name must be at least 3 characters"
                binding.edtName.requestFocus()
                return@setOnClickListener
            }

            val containsLetter = name.any { it.isLetter() }
            if (!containsLetter) {
                binding.edtName.error = "Name must contain letters"
                binding.edtName.requestFocus()
                return@setOnClickListener
            }

            signupUser?.name = name

            Log.d("NameActivity", "SignupUser value:$signupUser")

            val intent = Intent(this@NameActivity, GenderActivity::class.java)
            intent.putExtra(SignupActivity.EXTRA_SIGNUP_USER, signupUser)
            startActivity(intent)
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}