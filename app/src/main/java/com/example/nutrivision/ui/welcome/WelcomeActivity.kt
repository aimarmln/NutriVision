package com.example.nutrivision.ui.welcome

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.nutrivision.databinding.ActivityWelcomeBinding
import com.example.nutrivision.ui.login.LoginActivity
import com.example.nutrivision.ui.signup.SignupActivity

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.btnSignupWelcome.setOnClickListener {
            val intent = Intent(this@WelcomeActivity, SignupActivity::class.java)
            startActivity(intent)
        }

        binding.btnLoginWelcome.setOnClickListener {
            val intent = Intent(this@WelcomeActivity, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}