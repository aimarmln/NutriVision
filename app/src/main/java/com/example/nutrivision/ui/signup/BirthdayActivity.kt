package com.example.nutrivision.ui.signup

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.nutrivision.databinding.ActivityBirthdayBinding

class BirthdayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBirthdayBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBirthdayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val signupUser = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra<SignupUser>(SignupActivity.EXTRA_SIGNUP_USER, SignupUser::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<SignupUser>(SignupActivity.EXTRA_SIGNUP_USER)
        }

        binding.btnContinueBirthday.setOnClickListener {
            val datePicker = binding.datePicker
            val day = datePicker.dayOfMonth
            val month = datePicker.month
            val year = datePicker.year
            val selectedDate = "$year-${month + 1}-$day"

            signupUser?.birthday = selectedDate

            Log.d("BirthdayActivity", "SignupUser value:$signupUser")

            val intent = Intent(this@BirthdayActivity, HeightActivity::class.java)
            intent.putExtra(SignupActivity.EXTRA_SIGNUP_USER, signupUser)
            startActivity(intent)
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}