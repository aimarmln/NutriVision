package com.example.nutrivision.ui.splashscreen

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.nutrivision.MainActivity
import com.example.nutrivision.data.local.SettingPreferences
import com.example.nutrivision.data.local.dataStore
import com.example.nutrivision.databinding.ActivitySplashBinding
import com.example.nutrivision.ui.welcome.WelcomeActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val pref = SettingPreferences.getInstance(application.dataStore)

        lifecycleScope.launch {
            delay(2000)
            val isLoggedIn = pref.isLoggedIn.first()
            if (isLoggedIn) {
                startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
                finish()
            } else {
                startActivity(Intent(this@SplashScreenActivity, WelcomeActivity::class.java))
                finish()
            }
        }
    }
}