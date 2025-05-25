package com.example.nutrivision

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class NutriVisionApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Force light mode globally
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}