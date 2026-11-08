package com.example.services

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import java.lang.ref.WeakReference

class JarvisAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInstance = WeakReference(this)
        Log.d(TAG, "Jarvis Accessibility Service connected.")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No-op: service is used for global assistant actions
    }

    override fun onInterrupt() {
        Log.d(TAG, "Jarvis Accessibility Service interrupted.")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (serviceInstance?.get() == this) {
            serviceInstance = null
        }
    }

    companion object {
        private const val TAG = "JarvisA11yService"
        private var serviceInstance: WeakReference<JarvisAccessibilityService>? = null

        fun isServiceRunning(): Boolean {
            return serviceInstance?.get() != null
        }

        fun openAccessibilitySettings(context: Context) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }

        fun lockScreen(): Boolean {
            val service = serviceInstance?.get()
            return if (service != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    service.performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to perform global lock screen", e)
                    false
                }
            } else {
                false
            }
        }

        fun goHome(): Boolean {
            return serviceInstance?.get()?.performGlobalAction(GLOBAL_ACTION_HOME) ?: false
        }

        fun goBack(): Boolean {
            return serviceInstance?.get()?.performGlobalAction(GLOBAL_ACTION_BACK) ?: false
        }

        fun showRecents(): Boolean {
            return serviceInstance?.get()?.performGlobalAction(GLOBAL_ACTION_RECENTS) ?: false
        }
    }
}
