package com.example.system

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.AppOpsManager
import android.app.role.RoleManager
import android.bluetooth.BluetoothManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.accessibility.JarvisAccessibilityService
import com.example.services.JarvisNotificationListenerService
import com.example.services.JarvisVoiceInteractionService
import com.example.services.JarvisVoiceService

data class SystemIntegrationStatus(
    val isDefaultAssistant: Boolean = false,
    val defaultAssistantName: String = "Unknown",
    val isAccessibilityEnabled: Boolean = false,
    val isNotificationAccessEnabled: Boolean = false,
    val isBatteryUnrestricted: Boolean = false,
    val isOverlayEnabled: Boolean = false,
    val isUsageAccessEnabled: Boolean = false,
    val isModifySettingsEnabled: Boolean = false,
    val isExactAlarmEnabled: Boolean = false,
    val isMicrophoneGranted: Boolean = false,
    val isBackgroundServiceRunning: Boolean = false,
    val isBluetoothEnabled: Boolean = false,
    val isBluetoothHeadsetConnected: Boolean = false,
    val manufacturer: String = Build.MANUFACTURER
) {
    val totalConfiguredCount: Int
        get() {
            var count = 0
            if (isDefaultAssistant) count++
            if (isAccessibilityEnabled) count++
            if (isNotificationAccessEnabled) count++
            if (isBatteryUnrestricted) count++
            if (isOverlayEnabled) count++
            if (isUsageAccessEnabled) count++
            if (isModifySettingsEnabled) count++
            if (isExactAlarmEnabled) count++
            if (isMicrophoneGranted) count++
            if (isBackgroundServiceRunning) count++
            return count
        }

    val totalMonitoredCount: Int = 10

    val readinessPercent: Int
        get() = (totalConfiguredCount * 100) / totalMonitoredCount
}

class SystemIntegrationManager(private val context: Context) {

    private val TAG = "JarvisSystemManager"

    // 1. DEFAULT DIGITAL ASSISTANT
    fun isDefaultAssistant(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
                if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_ASSISTANT)) {
                    if (roleManager.isRoleHeld(RoleManager.ROLE_ASSISTANT)) return true
                }
            }

            // Fallback: check Secure Settings for "assistant" or "voice_interaction_service"
            val settingAssistant = Settings.Secure.getString(context.contentResolver, "assistant") ?: ""
            val settingVoiceService = Settings.Secure.getString(context.contentResolver, "voice_interaction_service") ?: ""
            val myPackage = context.packageName

            settingAssistant.contains(myPackage) || settingVoiceService.contains(myPackage)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking default assistant status", e)
            false
        }
    }

    fun getDefaultAssistantName(): String {
        return try {
            val settingAssistant = Settings.Secure.getString(context.contentResolver, "assistant")
                ?: Settings.Secure.getString(context.contentResolver, "voice_interaction_service")
                ?: ""

            if (settingAssistant.isBlank()) {
                return "None / Not Selected"
            }

            val pkg = if (settingAssistant.contains("/")) {
                settingAssistant.substringBefore("/")
            } else {
                settingAssistant
            }

            if (pkg == context.packageName) {
                return "JARVIS (Active Default)"
            }

            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(pkg, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            if (isDefaultAssistant()) "JARVIS" else "Google / System Default"
        }
    }

    fun openDefaultAssistantSettings(activity: Activity? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && activity != null) {
            try {
                val roleManager = context.getSystemService(Context.ROLE_SERVICE) as? RoleManager
                if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_ASSISTANT)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_ASSISTANT)
                    activity.startActivity(intent)
                    return
                }
            } catch (e: Exception) {
                Log.w(TAG, "RoleManager assist request failed, falling back to settings", e)
            }
        }

        val intents = listOf(
            Intent(Settings.ACTION_VOICE_INPUT_SETTINGS),
            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            },
            Intent(Settings.ACTION_SETTINGS)
        )
        launchFirstAvailable(intents)
    }

    // 2. ACCESSIBILITY SERVICE
    fun isAccessibilityServiceEnabled(): Boolean {
        return try {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: ""
            val myComponent = "${context.packageName}/${JarvisAccessibilityService::class.java.name}"
            val simpleComponent = "${context.packageName}/.accessibility.JarvisAccessibilityService"
            enabledServices.contains(myComponent) || enabledServices.contains(simpleComponent)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking accessibility status", e)
            false
        }
    }

    fun openAccessibilitySettings() {
        val intents = listOf(
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )
        launchFirstAvailable(intents)
    }

    // 3. NOTIFICATION LISTENER
    fun isNotificationAccessEnabled(): Boolean {
        return try {
            NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
        } catch (e: Exception) {
            val listeners = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: ""
            listeners.contains(context.packageName)
        }
    }

    fun openNotificationAccessSettings() {
        val intents = listOf(
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )
        launchFirstAvailable(intents)
    }

    // 4. BATTERY UNRESTRICTED / OPTIMIZATION
    fun isBatteryUnrestricted(): Boolean {
        return try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } catch (e: Exception) {
            false
        }
    }

    @SuppressLint("BatteryLife")
    fun requestBatteryUnrestricted() {
        val intents = listOf(
            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
            },
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS),
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        )
        launchFirstAvailable(intents)
    }

    // 5. DISPLAY OVER OTHER APPS (SYSTEM_ALERT_WINDOW)
    fun canDrawOverlays(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun openOverlaySettings() {
        val intents = listOf(
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
            },
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        )
        launchFirstAvailable(intents)
    }

    // 6. USAGE ACCESS (PACKAGE_USAGE_STATS)
    fun hasUsageAccess(): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            }
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }

    fun openUsageAccessSettings() {
        val intents = listOf(
            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        )
        launchFirstAvailable(intents)
    }

    // 7. MODIFY SYSTEM SETTINGS (WRITE_SETTINGS)
    fun canModifySystemSettings(): Boolean {
        return Settings.System.canWrite(context)
    }

    fun openModifySystemSettings() {
        val intents = listOf(
            Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            },
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        )
        launchFirstAvailable(intents)
    }

    // 8. EXACT ALARMS (SCHEDULE_EXACT_ALARM)
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intents = listOf(
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:${context.packageName}")
                },
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
            )
            launchFirstAvailable(intents)
        } else {
            openAppDetailsSettings()
        }
    }

    // 9. OEM AUTO-START SETTINGS
    fun openOemAutoStartSettings(): Boolean {
        val mfg = Build.MANUFACTURER.lowercase()
        val candidates = mutableListOf<Intent>()

        when {
            mfg.contains("xiaomi") || mfg.contains("redmi") || mfg.contains("poco") -> {
                candidates.add(Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")))
                candidates.add(Intent("miui.intent.action.OP_AUTO_START").addCategory(Intent.CATEGORY_DEFAULT))
            }
            mfg.contains("oppo") || mfg.contains("realme") -> {
                candidates.add(Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")))
                candidates.add(Intent().setComponent(ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")))
                candidates.add(Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")))
            }
            mfg.contains("vivo") || mfg.contains("iqoo") -> {
                candidates.add(Intent().setComponent(ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")))
                candidates.add(Intent().setComponent(ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")))
            }
            mfg.contains("huawei") || mfg.contains("honor") -> {
                candidates.add(Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")))
                candidates.add(Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")))
            }
            mfg.contains("samsung") -> {
                candidates.add(Intent().setComponent(ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")))
                candidates.add(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
            mfg.contains("oneplus") -> {
                candidates.add(Intent().setComponent(ComponentName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity")))
            }
        }

        // Generic fallback to app details
        candidates.add(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        })

        return launchFirstAvailable(candidates)
    }

    // 10. BLUETOOTH & CONNECTED HEADSET
    fun isBluetoothEnabled(): Boolean {
        return try {
            val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            bm?.adapter?.isEnabled == true
        } catch (e: Exception) {
            false
        }
    }

    fun isBluetoothHeadsetConnected(): Boolean {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.isBluetoothScoOn || audioManager.isBluetoothA2dpOn || audioManager.isWiredHeadsetOn
        } catch (e: Exception) {
            false
        }
    }

    fun openBluetoothSettings() {
        val intents = listOf(
            Intent(Settings.ACTION_BLUETOOTH_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )
        launchFirstAvailable(intents)
    }

    // 11. GENERAL HELPERS
    fun openAppDetailsSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            data = Uri.parse("package:${context.packageName}")
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open app details", e)
        }
    }

    fun openSystemSettings() {
        val intent = Intent(Settings.ACTION_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open settings", e)
        }
    }

    fun queryFullStatus(): SystemIntegrationStatus {
        val micGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        return SystemIntegrationStatus(
            isDefaultAssistant = isDefaultAssistant(),
            defaultAssistantName = getDefaultAssistantName(),
            isAccessibilityEnabled = isAccessibilityServiceEnabled(),
            isNotificationAccessEnabled = isNotificationAccessEnabled(),
            isBatteryUnrestricted = isBatteryUnrestricted(),
            isOverlayEnabled = canDrawOverlays(),
            isUsageAccessEnabled = hasUsageAccess(),
            isModifySettingsEnabled = canModifySystemSettings(),
            isExactAlarmEnabled = canScheduleExactAlarms(),
            isMicrophoneGranted = micGranted,
            isBackgroundServiceRunning = JarvisVoiceService.isRunning,
            isBluetoothEnabled = isBluetoothEnabled(),
            isBluetoothHeadsetConnected = isBluetoothHeadsetConnected(),
            manufacturer = Build.MANUFACTURER
        )
    }

    private fun launchFirstAvailable(intents: List<Intent>): Boolean {
        for (intent in intents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            } catch (e: Exception) {
                // Try next candidate
            }
        }
        return false
    }
}
