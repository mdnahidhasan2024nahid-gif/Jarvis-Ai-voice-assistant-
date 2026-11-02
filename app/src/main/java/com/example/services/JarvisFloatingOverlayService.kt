package com.example.services

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class JarvisFloatingOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()
        if (!Settings.canDrawOverlays(this)) {
            Log.w(TAG, "Cannot draw overlays: permission missing")
            stopSelf()
            return
        }

        instance = this
        try {
            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

            val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val density = resources.displayMetrics.density
            val orbSizePx = (76 * density).toInt()

            val prefs = getSharedPreferences("jarvis_overlay_prefs", Context.MODE_PRIVATE)
            val savedX = prefs.getInt("orb_x", (density * 24).toInt())
            val savedY = prefs.getInt("orb_y", (density * 220).toInt())

            val params = WindowManager.LayoutParams(
                orbSizePx,
                orbSizePx,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = savedX
                y = savedY
            }

            // Create Animated Custom JARVIS Orb View
            val orbView = JarvisOrbCanvasView(this).apply {
                layoutParams = FrameLayout.LayoutParams(orbSizePx, orbSizePx)
            }

            var initialX = 0
            var initialY = 0
            var initialTouchX = 0f
            var initialTouchY = 0f
            var isClick = false

            orbView.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        isClick = true
                        true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = (event.rawX - initialTouchX).toInt()
                        val dy = (event.rawY - initialTouchY).toInt()
                        if (Math.abs(dx) > 12 || Math.abs(dy) > 12) {
                            isClick = false
                        }
                        params.x = initialX + dx
                        params.y = initialY + dy
                        windowManager?.updateViewLayout(orbView, params)
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        prefs.edit().putInt("orb_x", params.x).putInt("orb_y", params.y).apply()
                        if (isClick) {
                            v.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                            val launchIntent = Intent(this, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                putExtra("EXTRA_START_VOICE_ASSIST", true)
                            }
                            startActivity(launchIntent)
                        }
                        true
                    }
                    else -> false
                }
            }

            windowManager?.addView(orbView, params)
            floatingView = orbView
            _isOverlayActive.value = true
            Log.d(TAG, "Floating animated JARVIS orb overlay active")
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying floating overlay", e)
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingView?.let {
            if (it is JarvisOrbCanvasView) {
                it.stopAnimation()
            }
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing floating overlay view", e)
            }
        }
        floatingView = null
        if (instance == this) {
            instance = null
        }
        _isOverlayActive.value = false
        Log.d(TAG, "Floating overlay destroyed")
    }

    // High performance custom animated Canvas Orb for floating overlay
    private class JarvisOrbCanvasView(context: Context) : View(context) {
        private var phase = 0f
        private var animator: android.animation.ValueAnimator? = null

        private val paintHalo = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
        private val paintCore = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
        private val paintRings = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.STROKE
            color = android.graphics.Color.parseColor("#38CFFF")
        }
        private val paintText = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        init {
            animator = android.animation.ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 2400L
                repeatCount = android.animation.ValueAnimator.INFINITE
                addUpdateListener {
                    phase = it.animatedValue as Float
                    invalidate()
                }
                start()
            }
        }

        fun stopAnimation() {
            animator?.cancel()
            animator = null
        }

        override fun onDraw(canvas: android.graphics.Canvas) {
            super.onDraw(canvas)
            val cx = width / 2f
            val cy = height / 2f
            val radius = Math.min(cx, cy) * 0.9f

            val pulse = 1f + 0.08f * kotlin.math.sin(phase * 2 * Math.PI).toFloat()

            // Outer cyan glow halo
            paintHalo.shader = android.graphics.RadialGradient(
                cx, cy, radius * pulse,
                intArrayOf(
                    android.graphics.Color.parseColor("#5038CFFF"),
                    android.graphics.Color.parseColor("#251769FF"),
                    android.graphics.Color.TRANSPARENT
                ),
                floatArrayOf(0f, 0.7f, 1f),
                android.graphics.Shader.TileMode.CLAMP
            )
            canvas.drawCircle(cx, cy, radius * pulse, paintHalo)

            // Inner dark neon core
            paintCore.shader = android.graphics.RadialGradient(
                cx, cy, radius * 0.75f,
                intArrayOf(
                    android.graphics.Color.parseColor("#1769FF"),
                    android.graphics.Color.parseColor("#0A1C38")
                ),
                floatArrayOf(0f, 1f),
                android.graphics.Shader.TileMode.CLAMP
            )
            canvas.drawCircle(cx, cy, radius * 0.75f, paintCore)

            // Energy concentric rings
            paintRings.strokeWidth = 2.5f
            paintRings.alpha = (160 + 90 * kotlin.math.sin(phase * 2 * Math.PI)).toInt()
            canvas.drawCircle(cx, cy, radius * 0.78f, paintRings)

            // Center double sparkle symbol ✦✦
            paintText.textSize = radius * 0.42f
            val textY = cy - ((paintText.descent() + paintText.ascent()) / 2)
            canvas.drawText("✦✦", cx, textY, paintText)
        }
    }

    companion object {
        private const val TAG = "JarvisFloatingOverlay"

        private val _isOverlayActive = MutableStateFlow(false)
        val isOverlayActive: StateFlow<Boolean> = _isOverlayActive.asStateFlow()

        private var instance: JarvisFloatingOverlayService? = null

        fun setAppInForeground(inForeground: Boolean) {
            try {
                instance?.floatingView?.visibility = if (inForeground) View.GONE else View.VISIBLE
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling overlay visibility", e)
            }
        }

        fun start(context: Context) {
            if (Settings.canDrawOverlays(context)) {
                try {
                    val intent = Intent(context, JarvisFloatingOverlayService::class.java)
                    context.startService(intent)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to start floating overlay service", e)
                }
            }
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, JarvisFloatingOverlayService::class.java)
                context.stopService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop floating overlay service", e)
            }
        }
    }
}
