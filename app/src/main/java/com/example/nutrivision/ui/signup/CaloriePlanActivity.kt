package com.example.nutrivision.ui.signup

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.nutrivision.MainActivity
import com.example.nutrivision.databinding.ActivityCaloriePlanBinding

class CaloriePlanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCaloriePlanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCaloriePlanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.caloriesPerDay.text = intent.getStringExtra(PersonalizationActivity.EXTRA_CALORIES)

        binding.btnStartYourPlan.setOnClickListener {
            val intent = Intent(this@CaloriePlanActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}