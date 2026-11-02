package com.example.actions

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.AlarmClock
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings

sealed class ActionResult {
    data class Success(val message: String, val actionTitle: String) : ActionResult()
    data class Failure(val reason: String) : ActionResult()
}

class PhoneActionEngine(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private var isTorchOn = false

    fun parseAndExecute(command: String): ActionResult {
        val lower = command.lowercase().trim()

        return when {
            // Flashlight
            lower.contains("flashlight") || lower.contains("টর্চ") || lower.contains("আলো") -> {
                if (lower.contains("off") || lower.contains("বন্ধ")) {
                    toggleFlashlight(false)
                } else {
                    toggleFlashlight(true)
                }
            }

            // Open Apps
            lower.contains("open youtube") || lower.contains("ইউটিউব") -> {
                openApp("com.google.android.youtube", "YouTube")
            }
            lower.contains("open whatsapp") || lower.contains("হোয়াটসঅ্যাপ") || lower.contains("whatsapp") -> {
                openApp("com.whatsapp", "WhatsApp")
            }
            lower.contains("open chrome") || lower.contains("ক্রোম") -> {
                openApp("com.android.chrome", "Google Chrome")
            }
            lower.contains("open camera") || lower.contains("ক্যামেরা") -> {
                openCamera()
            }
            lower.contains("open settings") || lower.contains("সেটিংস") -> {
                openSystemSettings(Settings.ACTION_SETTINGS, "Android Settings")
            }
            lower.contains("wifi") || lower.contains("ওয়াইফাই") -> {
                openSystemSettings(Settings.ACTION_WIFI_SETTINGS, "Wi-Fi Settings")
            }
            lower.contains("bluetooth") || lower.contains("ব্লুটুথ") -> {
                openSystemSettings(Settings.ACTION_BLUETOOTH_SETTINGS, "Bluetooth Settings")
            }

            // Calendar
            lower.contains("calendar") || lower.contains("ক্যালেন্ডার") || lower.contains("schedule") -> {
                openCalendar()
            }

            // Alarms & Timers
            lower.contains("alarm") || lower.contains("অ্যালার্ম") -> {
                setQuickAlarm(7, 0, "JARVIS Alarm")
            }
            lower.contains("timer") || lower.contains("টাইমার") -> {
                setQuickTimer(300, "JARVIS Timer")
            }

            // Battery Information
            lower.contains("battery") || lower.contains("ব্যাটারি") -> {
                getBatteryStatus()
            }

            // Storage Information
            lower.contains("storage") || lower.contains("মেমরি") || lower.contains("স্টোরেজ") -> {
                getStorageStatus()
            }

            // Search
            lower.startsWith("search") || lower.contains("খুঁজুন") -> {
                val query = lower.replace("search", "").replace("for", "").trim()
                searchWeb(query)
            }

            // Dialer / Call
            lower.contains("call") || lower.contains("কল") -> {
                openDialer("")
            }

            // Contacts
            lower.contains("contact") || lower.contains("কন্টাক্ট") -> {
                openContacts()
            }

            else -> ActionResult.Failure("কমান্ডটি সরাসরি সিস্টেমে পাওয়া যায়নি। আমি AI বুদ্ধিমত্তা দিয়ে উত্তর দিচ্ছি।")
        }
    }

    fun toggleFlashlight(turnOn: Boolean): ActionResult {
        return try {
            val cameraId = cameraManager?.cameraIdList?.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
            if (cameraId != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cameraManager?.setTorchMode(cameraId, turnOn)
                isTorchOn = turnOn
                val stateText = if (turnOn) "চালু (ON)" else "বন্ধ (OFF)"
                ActionResult.Success("টর্চলাইট সফলভাবে $stateText করা হয়েছে।", "টর্চলাইট")
            } else {
                ActionResult.Failure("ডিভাইসে ফ্ল্যাশলাইট সনাক্ত করা যায়নি।")
            }
        } catch (e: Exception) {
            ActionResult.Failure("ফ্ল্যাশলাইট ব্যবহারে ত্রুটি: ${e.localizedMessage}")
        }
    }

    fun openApp(packageName: String, appName: String): ActionResult {
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                ActionResult.Success("$appName অ্যাপ চালু করা হচ্ছে...", appName)
            } else {
                // Open in Play Store / Web
                val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(marketIntent)
                ActionResult.Success("$appName অ্যাপটি ইনস্টল করতে প্লে-স্টোরে নিয়ে যাওয়া হচ্ছে।", appName)
            }
        } catch (e: Exception) {
            ActionResult.Failure("$appName খোলার সময় ত্রুটি: ${e.localizedMessage}")
        }
    }

    fun openCamera(): ActionResult {
        return try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult.Success("ক্যামেরা ওপেন করা হচ্ছে...", "ক্যামেরা")
        } catch (e: Exception) {
            ActionResult.Failure("ক্যামেরা খুলতে সমস্যা হয়েছে।")
        }
    }

    fun openSystemSettings(action: String, title: String): ActionResult {
        return try {
            val intent = Intent(action).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult.Success("$title খোলা হয়েছে।", title)
        } catch (e: Exception) {
            ActionResult.Failure("সেটিংস খুলতে ত্রুটি হয়েছে।")
        }
    }

    fun setQuickAlarm(hour: Int, minute: Int, message: String): ActionResult {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult.Success("$hour:${String.format("%02d", minute)} এর জন্য অ্যালার্ম সেট করা হচ্ছে।", "অ্যালার্ম")
        } catch (e: Exception) {
            ActionResult.Failure("অ্যালার্ম অ্যাপ পাওয়া যায়নি।")
        }
    }

    fun setQuickTimer(seconds: Int, message: String): ActionResult {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult.Success("$seconds সেকেন্ডের টাইমার চালু করা হচ্ছে।", "টাইমার")
        } catch (e: Exception) {
            ActionResult.Failure("টাইমার খোলার ব্যবস্থা পাওয়া যায়নি।")
        }
    }

    fun searchWeb(query: String): ActionResult {
        return try {
            val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, query)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult.Success("\"$query\" দিয়ে সার্চ করা হচ্ছে...", "ওয়েব সার্চ")
        } catch (e: Exception) {
            ActionResult.Failure("ওয়েব সার্চ চালু করা যায়নি।")
        }
    }

    fun openDialer(phoneNumber: String = ""): ActionResult {
        return try {
            val intent = if (phoneNumber.isNotBlank()) {
                Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            } else {
                Intent(Intent.ACTION_DIAL)
            }.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult.Success("ডায়ালার চালু করা হচ্ছে...", "ফোন কল")
        } catch (e: Exception) {
            ActionResult.Failure("ডায়ালার চালু করা যায়নি।")
        }
    }

    fun openContacts(): ActionResult {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult.Success("কন্টাক্ট তালিকা খোলা হচ্ছে...", "কন্টাক্টস")
        } catch (e: Exception) {
            ActionResult.Failure("কন্টাক্টস অ্যাপ খোলা যায়নি।")
        }
    }

    fun getBatteryStatus(): ActionResult {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val level = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
        return if (level >= 0) {
            ActionResult.Success("বর্তমান ব্যাটারি চার্জ: $level%", "ব্যাটারি স্ট্যাটাস")
        } else {
            ActionResult.Failure("ব্যাটারি তথ্য পাওয়া যায়নি।")
        }
    }

    fun getStorageStatus(): ActionResult {
        return try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val availableBlocks = stat.availableBlocksLong
            val totalBlocks = stat.blockCountLong
            val freeGb = (availableBlocks * blockSize) / (1024 * 1024 * 1024)
            val totalGb = (totalBlocks * blockSize) / (1024 * 1024 * 1024)
            ActionResult.Success("ফ্রি স্টোরেজ: $freeGb GB / মোট: $totalGb GB", "স্টোরেজ তথ্য")
        } catch (e: Exception) {
            ActionResult.Failure("স্টোরেজ তথ্য পাওয়া যায়নি।")
        }
    }

    fun openCalendar(): ActionResult {
        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse("content://com.android.calendar/time")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult.Success("ক্যালেন্ডার ও শিডিউল খোলা হচ্ছে...", "ক্যালেন্ডার")
        } catch (e: Exception) {
            ActionResult.Failure("ক্যালেন্ডার অ্যাপ্লিকেশন পাওয়া যায়নি।")
        }
    }
}
