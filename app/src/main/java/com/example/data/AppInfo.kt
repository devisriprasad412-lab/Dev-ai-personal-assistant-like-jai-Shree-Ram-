package com.example.data

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val label: String,
    val isSystemApp: Boolean = false,
    val icon: Drawable? = null
)
