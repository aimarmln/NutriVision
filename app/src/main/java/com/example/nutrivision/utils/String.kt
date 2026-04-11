package com.example.nutrivision.utils

fun List<String>.toBulletList(): String = joinToString("\n\n") { "• $it" }