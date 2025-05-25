package com.example.nutrivision.ui.signup

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.nutrivision.databinding.ActivityHeightBinding

class HeightActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHeightBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHeightBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val signupUser = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra<SignupUser>(SignupActivity.EXTRA_SIGNUP_USER, SignupUser::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<SignupUser>(SignupActivity.EXTRA_SIGNUP_USER)
        }

        binding.btnContinueHeight.setOnClickListener {
            val input = binding.edtHeight.text.toString()
            if (input.isEmpty()) {
                binding.edtHeight.error = "Height cannot be empty"
                binding.edtHeight.requestFocus()
                return@setOnClickListener
            }

            val height = input.toIntOrNull()
            if (height == null || height < 140 || height > 230) {
                binding.edtHeight.error = "Height must be between 140 and 230 cm"
                binding.edtHeight.requestFocus()
                return@setOnClickListener
            }

            signupUser?.height = height

            Log.d("HeightActivity", "SignupUser value:$signupUser")

            val intent = Intent(this@HeightActivity, WeightActivity::class.java)
            intent.putExtra(SignupActivity.EXTRA_SIGNUP_USER, signupUser)
            startActivity(intent)
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}