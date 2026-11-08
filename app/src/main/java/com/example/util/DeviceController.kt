package com.example.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import com.example.data.AppInfo
import com.example.services.JarvisAccessibilityService
import com.example.services.JarvisDeviceAdminReceiver
import java.net.URLEncoder
import java.util.Locale

sealed interface LockResult {
    data object Success : LockResult
    data object NeedsAccessibilityPermission : LockResult
    data object NeedsDeviceAdminPermission : LockResult
    data class Error(val message: String) : LockResult
}

data class BatteryInfo(
    val levelPercent: Int,
    val isCharging: Boolean,
    val statusText: String
)

class DeviceController(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager

    private var isTorchOn: Boolean = false

    // 1. Screen Lock Execution
    fun lockScreen(): LockResult {
        // Method A: Accessibility Service (Preferred on Android 9+)
        if (JarvisAccessibilityService.isServiceRunning()) {
            val locked = JarvisAccessibilityService.lockScreen()
            if (locked) return LockResult.Success
        }

        // Method B: Device Admin
        if (JarvisDeviceAdminReceiver.isDeviceAdminActive(context)) {
            val locked = JarvisDeviceAdminReceiver.lockScreen(context)
            if (locked) return LockResult.Success
        }

        // Neither is enabled yet: recommend accessibility or device admin
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            LockResult.NeedsAccessibilityPermission
        } else {
            LockResult.NeedsDeviceAdminPermission
        }
    }

    // 2. Open / Wake Screen Execution
    fun wakeScreen(activity: Activity? = null): Boolean {
        return try {
            // Wake device screen using PowerManager WakeLock
            val wakeLock = powerManager?.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "DevAI:WakeScreenLock"
            )
            wakeLock?.acquire(3000)

            // Turn on screen on activity level if available
            activity?.runOnUiThread {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    activity.setShowWhenLocked(true)
                    activity.setTurnScreenOn(true)
                } else {
                    @Suppress("DEPRECATION")
                    activity.window.addFlags(
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    )
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error waking screen", e)
            false
        }
    }

    // 3. Flashlight / Torch
    fun toggleFlashlight(forceState: Boolean? = null): Boolean {
        val newState = forceState ?: !isTorchOn
        return try {
            val cameraId = cameraManager?.cameraIdList?.firstOrNull { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
            if (cameraId != null && cameraManager != null) {
                cameraManager.setTorchMode(cameraId, newState)
                isTorchOn = newState
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling flashlight", e)
            false
        }
    }

    fun isFlashlightOn(): Boolean = isTorchOn

    // 4. Audio Volume Control
    fun setVolumePercent(percent: Int): Boolean {
        return try {
            audioManager?.let { am ->
                val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val target = ((percent.coerceIn(0, 100) / 100f) * max).toInt()
                am.setStreamVolume(AudioManager.STREAM_MUSIC, target, AudioManager.FLAG_SHOW_UI)
                true
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error setting volume", e)
            false
        }
    }

    fun muteVolume(): Boolean = setVolumePercent(0)
    fun maxVolume(): Boolean = setVolumePercent(100)

    // 5. Messaging & Chatting (WhatsApp, SMS, Direct Chat)
    fun sendWhatsAppMessage(phoneNumber: String? = null, messageText: String): Boolean {
        return try {
            val encodedMessage = URLEncoder.encode(messageText, "UTF-8")
            val cleanPhone = phoneNumber?.replace(Regex("[^0-9]"), "") ?: ""
            val uriStr = if (cleanPhone.isNotBlank()) {
                "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMessage"
            } else {
                "https://api.whatsapp.com/send?text=$encodedMessage"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriStr)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch WhatsApp messaging", e)
            // Fallback to general send intent
            try {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, messageText)
                    setPackage("com.whatsapp")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(sendIntent)
                true
            } catch (ex: Exception) {
                Log.e(TAG, "WhatsApp not installed, falling back to SMS", ex)
                sendSmsMessage(phoneNumber, messageText)
            }
        }
    }

    fun sendSmsMessage(phoneNumber: String? = null, messageText: String): Boolean {
        return try {
            val uri = Uri.parse("smsto:${phoneNumber.orEmpty()}")
            val smsIntent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", messageText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(smsIntent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to compose SMS", e)
            false
        }
    }

    // 6. Installed Apps Retrieval & Launching
    fun getInstalledApps(): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_ALL
        )

        return resolveInfos.mapNotNull { resolveInfo ->
            val pkg = resolveInfo.activityInfo.packageName
            // Skip self from list
            if (pkg == context.packageName) return@mapNotNull null

            val label = resolveInfo.loadLabel(packageManager).toString()
            val icon = resolveInfo.loadIcon(packageManager)
            val isSystem = (resolveInfo.activityInfo.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0

            AppInfo(
                packageName = pkg,
                label = label,
                isSystemApp = isSystem,
                icon = icon
            )
        }.sortedBy { it.label.lowercase(Locale.ROOT) }
    }

    fun launchAppByPackage(packageName: String): Boolean {
        return try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching app: $packageName", e)
            false
        }
    }

    fun findAndLaunchApp(query: String): Pair<Boolean, String?> {
        val cleanQuery = query.trim().lowercase(Locale.ROOT)
            .removePrefix("open the ")
            .removePrefix("launch the ")
            .removePrefix("start the ")
            .removePrefix("open ")
            .removePrefix("launch ")
            .removePrefix("start ")
            .removePrefix("run ")
            .removePrefix("play ")
            .removeSuffix(" application")
            .removeSuffix(" app")
            .removeSuffix(" అప్లికేషన్")
            .removeSuffix(" యాప్")
            .removeSuffix(" ఓపెన్ చెయ్యి")
            .removeSuffix(" ఓపెన్ చేయి")
            .removeSuffix(" ఓపెన్ చేయండి")
            .removeSuffix(" తెరువు")
            .removeSuffix(" స్టార్ట్ చెయ్యి")
            .trim()

        val apps = getInstalledApps()

        // 1. Exact match on label
        val exactMatch = apps.firstOrNull { it.label.lowercase(Locale.ROOT) == cleanQuery }
        if (exactMatch != null && launchAppByPackage(exactMatch.packageName)) {
            return true to exactMatch.label
        }

        // 2. Starts with query
        val startsWithMatch = apps.firstOrNull { it.label.lowercase(Locale.ROOT).startsWith(cleanQuery) }
        if (startsWithMatch != null && launchAppByPackage(startsWithMatch.packageName)) {
            return true to startsWithMatch.label
        }

        // 3. Contains query in label or package
        val containsMatch = apps.firstOrNull {
            it.label.lowercase(Locale.ROOT).contains(cleanQuery) ||
            it.packageName.lowercase(Locale.ROOT).contains(cleanQuery)
        }
        if (containsMatch != null && launchAppByPackage(containsMatch.packageName)) {
            return true to containsMatch.label
        }

        // 4. Common app package aliases & Telugu phonetics
        val aliasPackage = when {
            cleanQuery.contains("youtube") || cleanQuery.contains("యూట్యూబ్") || cleanQuery.contains("yt") -> "com.google.android.youtube"
            cleanQuery.contains("whatsapp") || cleanQuery.contains("వాట్సాప్") -> "com.whatsapp"
            cleanQuery.contains("chrome") || cleanQuery.contains("క్రోమ్") || cleanQuery.contains("browser") || cleanQuery.contains("బ్రౌజర్") -> "com.android.chrome"
            cleanQuery.contains("camera") || cleanQuery.contains("కెమెరా") -> "com.android.camera"
            cleanQuery.contains("map") || cleanQuery.contains("మ్యాప్స్") || cleanQuery.contains("నావిగేషన్") -> "com.google.android.apps.maps"
            cleanQuery.contains("calc") || cleanQuery.contains("కాలిక్యులేటర్") -> "com.google.android.calculator"
            cleanQuery.contains("setting") || cleanQuery.contains("సెట్టింగ్స్") -> "com.android.settings"
            cleanQuery.contains("gmail") || cleanQuery.contains("మెయిల్") || cleanQuery.contains("email") -> "com.google.android.gm"
            cleanQuery.contains("phone") || cleanQuery.contains("డయలర్") || cleanQuery.contains("dialer") || cleanQuery.contains("call") -> "com.google.android.dialer"
            cleanQuery.contains("store") || cleanQuery.contains("ప్లే స్టోర్") || cleanQuery.contains("play store") -> "com.android.vending"
            cleanQuery.contains("photo") || cleanQuery.contains("ఫోటోలు") || cleanQuery.contains("gallery") || cleanQuery.contains("గ్యాలరీ") -> "com.google.android.apps.photos"
            cleanQuery.contains("clock") || cleanQuery.contains("గడియారం") || cleanQuery.contains("alarm") || cleanQuery.contains("అలారం") -> "com.google.android.deskclock"
            cleanQuery.contains("file") || cleanQuery.contains("ఫైల్స్") -> "com.google.android.documentsui"
            cleanQuery.contains("contact") || cleanQuery.contains("కాంటాక్ట్స్") -> "com.google.android.contacts"
            cleanQuery.contains("message") || cleanQuery.contains("మెసేజెస్") || cleanQuery.contains("sms") -> "com.google.android.apps.messaging"
            cleanQuery.contains("spotify") || cleanQuery.contains("మ్యూజిక్") || cleanQuery.contains("music") -> "com.spotify.music"
            cleanQuery.contains("telegram") || cleanQuery.contains("టెలిగ్రామ్") -> "org.telegram.messenger"
            cleanQuery.contains("instagram") || cleanQuery.contains("ఇన్‌స్టాగ్రామ్") || cleanQuery.contains("insta") -> "com.instagram.android"
            else -> null
        }

        if (aliasPackage != null && launchAppByPackage(aliasPackage)) {
            val matchedApp = apps.firstOrNull { it.packageName == aliasPackage }
            return true to (matchedApp?.label ?: cleanQuery)
        }

        // 5. Hardware Intent fallbacks
        if (cleanQuery.contains("camera") || cleanQuery.contains("కెమెరా") || cleanQuery.contains("photo")) {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (cameraIntent.resolveActivity(packageManager) != null) {
                context.startActivity(cameraIntent)
                return true to "Camera"
            }
        }

        if (cleanQuery.contains("call") || cleanQuery.contains("phone") || cleanQuery.contains("డయలర్")) {
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(dialIntent)
            return true to "Phone Dialer"
        }

        if (cleanQuery.contains("setting") || cleanQuery.contains("సెట్టింగ్స్")) {
            val settingsIntent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(settingsIntent)
            return true to "Settings"
        }

        return false to null
    }

    // 7. Battery Status
    fun getBatteryInfo(): BatteryInfo {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val level = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 85
        val isCharging = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val status = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
            status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        } else {
            false
        }
        val statusText = if (isCharging) "ఛార్జింగ్ అవుతోంది ($level%)" else "బ్యాటరీ $level%"
        return BatteryInfo(level, isCharging, statusText)
    }

    // 8. Set Timer / Alarm
    fun setTimer(seconds: Int, message: String): Boolean {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(packageManager) != null) {
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting timer", e)
            false
        }
    }

    // 9. Web Search
    fun searchWeb(query: String): Boolean {
        return try {
            val uri = Uri.parse("https://www.google.com/search?q=" + URLEncoder.encode(query, "UTF-8"))
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error opening web search", e)
            false
        }
    }

    // 10. Emergency SOS Dialer
    fun openEmergencyDialer(): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:112")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error launching emergency dialer", e)
            false
        }
    }

    // 11. System Settings Navigation
    fun openSettings(target: String) {
        val intent = when (target.lowercase(Locale.ROOT)) {
            "wifi" -> Intent(Settings.ACTION_WIFI_SETTINGS)
            "bluetooth" -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            "display" -> Intent(Settings.ACTION_DISPLAY_SETTINGS)
            "apps" -> Intent(Settings.ACTION_APPLICATION_SETTINGS)
            "accessibility" -> Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            else -> Intent(Settings.ACTION_SETTINGS)
        }.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun requestDeviceAdmin(activity: Activity) {
        JarvisDeviceAdminReceiver.requestDeviceAdmin(activity)
    }

    fun openAccessibilitySettings() {
        JarvisAccessibilityService.openAccessibilitySettings(context)
    }

    companion object {
        private const val TAG = "DeviceController"
    }
}
