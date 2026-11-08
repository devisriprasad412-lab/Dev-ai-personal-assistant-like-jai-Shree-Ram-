package com.example.services

import android.app.Activity
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log

class JarvisDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.d(TAG, "Jarvis Device Admin enabled successfully.")
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.d(TAG, "Jarvis Device Admin disabled.")
    }

    companion object {
        private const val TAG = "JarvisDeviceAdmin"
        const val REQUEST_CODE_ENABLE_ADMIN = 1001

        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context, JarvisDeviceAdminReceiver::class.java)
        }

        fun isDeviceAdminActive(context: Context): Boolean {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            return dpm?.isAdminActive(getComponentName(context)) == true
        }

        fun requestDeviceAdmin(activity: Activity) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, getComponentName(activity))
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "Dev AI requires Device Admin permission to lock your screen upon voice or automation command."
                )
            }
            activity.startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
        }

        fun lockScreen(context: Context): Boolean {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            return if (dpm != null && isDeviceAdminActive(context)) {
                try {
                    dpm.lockNow()
                    true
                } catch (e: Exception) {
                    Log.e(TAG, "Error executing lockNow", e)
                    false
                }
            } else {
                false
            }
        }
    }
}
