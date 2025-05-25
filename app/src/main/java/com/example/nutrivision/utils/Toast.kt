package com.example.nutrivision.utils

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.nutrivision.R

fun showToast(context: Context, message: String) {
    val inflater = LayoutInflater.from(context)
    val layout = inflater.inflate(R.layout.custom_toast, null)

    val textView = layout.findViewById<TextView>(R.id.toast_text)
    val imageView = layout.findViewById<ImageView>(R.id.toast_icon)

    textView.text = message
    imageView.setImageResource(R.drawable.ic_nutrivision)

    val toast = Toast(context)
    toast.view = layout
    toast.duration = Toast.LENGTH_SHORT

    toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 140)
    toast.show()
}