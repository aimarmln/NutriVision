package com.example.nutrivision.ui.auth.register

interface StepFragment {
    fun isValid(): Boolean
    fun startEntryAnimation()
    fun restoreState()
}