package com.example.services

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class JarvisNotificationItem(
    val key: String,
    val packageName: String,
    val title: String,
    val text: String,
    val postTime: Long
)

class JarvisNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        _isConnected.value = true
        Log.d(TAG, "JarvisNotificationListenerService connected")
        refreshActiveNotifications()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        _isConnected.value = false
        Log.d(TAG, "JarvisNotificationListenerService disconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return
        try {
            val extras = sbn.notification.extras
            val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
            if (title.isNotBlank() || text.isNotBlank()) {
                val item = JarvisNotificationItem(
                    key = sbn.key,
                    packageName = sbn.packageName,
                    title = title,
                    text = text,
                    postTime = sbn.postTime
                )
                val current = _recentNotifications.value.toMutableList()
                current.removeAll { it.key == item.key }
                current.add(0, item)
                _recentNotifications.value = current.take(20)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling posted notification", e)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        if (sbn == null) return
        val current = _recentNotifications.value.toMutableList()
        current.removeAll { it.key == sbn.key }
        _recentNotifications.value = current
    }

    private fun refreshActiveNotifications() {
        try {
            val active = activeNotifications ?: return
            val list = mutableListOf<JarvisNotificationItem>()
            for (sbn in active.take(20)) {
                val extras = sbn.notification.extras
                val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
                val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
                if (title.isNotBlank() || text.isNotBlank()) {
                    list.add(
                        JarvisNotificationItem(
                            key = sbn.key,
                            packageName = sbn.packageName,
                            title = title,
                            text = text,
                            postTime = sbn.postTime
                        )
                    )
                }
            }
            _recentNotifications.value = list
        } catch (e: Exception) {
            Log.e(TAG, "Error reading active notifications", e)
        }
    }

    companion object {
        private const val TAG = "JarvisNotificationSvc"

        private val _isConnected = MutableStateFlow(false)
        val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

        private val _recentNotifications = MutableStateFlow<List<JarvisNotificationItem>>(emptyList())
        val recentNotifications: StateFlow<List<JarvisNotificationItem>> = _recentNotifications.asStateFlow()

        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context, JarvisNotificationListenerService::class.java)
        }
    }
}
