package com.example.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AccessibilityStatus(
    val isConnected: Boolean = false,
    val currentPackage: String = "",
    val currentActivity: String = "",
    val lastAction: String = "Idle",
    val isEmergencyStopped: Boolean = false
)

class JarvisAccessibilityService : AccessibilityService() {

    companion object {
        private var instance: JarvisAccessibilityService? = null

        private val _status = MutableStateFlow(AccessibilityStatus())
        val status: StateFlow<AccessibilityStatus> = _status.asStateFlow()

        fun isServiceRunning(): Boolean = instance != null

        fun triggerEmergencyStop() {
            _status.value = _status.value.copy(
                isEmergencyStopped = true,
                lastAction = "Emergency Stop Triggered"
            )
        }

        fun resetEmergencyStop() {
            _status.value = _status.value.copy(
                isEmergencyStopped = false,
                lastAction = "Ready"
            )
        }

        fun clickNodeByText(text: String): Boolean {
            val service = instance ?: return false
            if (_status.value.isEmergencyStopped) return false

            val root = service.rootInActiveWindow ?: return false
            val nodes = root.findAccessibilityNodeInfosByText(text)
            for (node in nodes) {
                if (node.isClickable) {
                    val clicked = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    _status.value = _status.value.copy(lastAction = "Clicked: $text")
                    return clicked
                } else {
                    var parent = node.parent
                    while (parent != null) {
                        if (parent.isClickable) {
                            val clicked = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            _status.value = _status.value.copy(lastAction = "Clicked: $text")
                            return clicked
                        }
                        parent = parent.parent
                    }
                }
            }
            return false
        }

        fun scrollForward(): Boolean {
            val service = instance ?: return false
            val root = service.rootInActiveWindow ?: return false
            return root.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
        }

        fun scrollBackward(): Boolean {
            val service = instance ?: return false
            val root = service.rootInActiveWindow ?: return false
            return root.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
        }

        fun performGlobalBack(): Boolean {
            return instance?.performGlobalAction(GLOBAL_ACTION_BACK) ?: false
        }

        fun performGlobalHome(): Boolean {
            return instance?.performGlobalAction(GLOBAL_ACTION_HOME) ?: false
        }

        fun readVisibleScreenText(): List<String> {
            val service = instance ?: return emptyList()
            val root = service.rootInActiveWindow ?: return emptyList()
            val textList = mutableListOf<String>()
            extractNodeTexts(root, textList)
            return textList
        }

        private fun extractNodeTexts(node: AccessibilityNodeInfo?, list: MutableList<String>) {
            if (node == null) return
            val text = node.text?.toString() ?: node.contentDescription?.toString()
            if (!text.isNullOrBlank() && text.length > 1) {
                list.add(text.trim())
            }
            for (i in 0 until node.childCount) {
                extractNodeTexts(node.getChild(i), list)
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _status.value = AccessibilityStatus(
            isConnected = true,
            lastAction = "Connected & Active"
        )
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || _status.value.isEmergencyStopped) return

        val pkg = event.packageName?.toString() ?: ""
        val cls = event.className?.toString() ?: ""

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            _status.value = _status.value.copy(
                currentPackage = pkg,
                currentActivity = cls
            )
        }
    }

    override fun onInterrupt() {
        _status.value = _status.value.copy(
            isConnected = false,
            lastAction = "Interrupted"
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        _status.value = AccessibilityStatus(
            isConnected = false,
            lastAction = "Service Stopped"
        )
    }
}
