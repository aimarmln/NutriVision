package com.example.nutrivision.ui.signup

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.nutrivision.databinding.ActivityWeightBinding

class WeightActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWeightBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWeightBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val signupUser = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra<SignupUser>(SignupActivity.EXTRA_SIGNUP_USER, SignupUser::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<SignupUser>(SignupActivity.EXTRA_SIGNUP_USER)
        }

        binding.btnContinueWeight.setOnClickListener {
            val input = binding.edtWeight.text.toString()
            if (input.isEmpty()) {
                binding.edtWeight.error = "Weight cannot be empty"
                binding.edtWeight.requestFocus()
                return@setOnClickListener
            }

            val weight = input.toIntOrNull()
            if (weight == null || weight < 40) {
                binding.edtWeight.error = "Weight must be greater than 40 kg"
                binding.edtWeight.requestFocus()
                return@setOnClickListener
            }

            signupUser?.weight = weight

            Log.d("WeightActivity", "SignupUser value:$signupUser")

            val intent = Intent(this@WeightActivity, ActivityLevelActivity::class.java)
            intent.putExtra(SignupActivity.EXTRA_SIGNUP_USER, signupUser)
            startActivity(intent)
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

}