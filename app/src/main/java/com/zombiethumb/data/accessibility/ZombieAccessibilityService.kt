package com.zombiethumb.data.accessibility

import android.accessibilityservice.AccessibilityService
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import com.zombiethumb.domain.model.EventType
import com.zombiethumb.domain.model.ScrollEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Accessibility service that monitors scroll and click events in social media apps.
 *
 * PRIVACY ENFORCEMENT:
 * - canRetrieveWindowContent="false" in XML config
 * - canPerformGestures="false" in XML config
 * - NEVER reads event.text, node.text, or contentDescription
 * - NEVER uses dispatchGesture or modifies scrolling
 * - Only extracts: scrollDeltaY, event type, package name, timestamp
 * - Package allowlist is configured in XML
 */
class ZombieAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        val pkg = event.packageName?.toString() ?: return

        // Only process events from monitored apps (XML allowlist handles filtering,
        // but we double-check here)
        if (pkg !in MONITORED_PACKAGES) return

        val now = SystemClock.elapsedRealtime()

        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                // PRIVACY: Only read scroll delta — NEVER read text or content
                val scrollDeltaY = if (android.os.Build.VERSION.SDK_INT >= 28) {
                    event.scrollDeltaY
                } else {
                    // Fallback: estimate from scrollY changes
                    event.scrollY
                }

                val scrollEvent = ScrollEvent(
                    timestampMs = now,
                    scrollDeltaY = scrollDeltaY,
                    packageName = pkg,
                    type = EventType.SCROLL,
                )
                _scrollEvents.tryEmit(scrollEvent)
            }

            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                val clickEvent = ScrollEvent(
                    timestampMs = now,
                    scrollDeltaY = 0,
                    packageName = pkg,
                    type = EventType.CLICK,
                )
                _scrollEvents.tryEmit(clickEvent)
            }

            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                val windowEvent = ScrollEvent(
                    timestampMs = now,
                    scrollDeltaY = 0,
                    packageName = pkg,
                    type = EventType.WINDOW_CHANGED,
                )
                _scrollEvents.tryEmit(windowEvent)
                _foregroundApp.tryEmit(pkg)
            }
        }
    }

    override fun onInterrupt() {
        // Required override — nothing to clean up
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        _instance = this
        _isRunning.tryEmit(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        _instance = null
        _isRunning.tryEmit(false)
    }

    companion object {
        /** Monitored social media packages */
        val MONITORED_PACKAGES = setOf(
            "com.instagram.android",
            "com.zhiliaoapp.musically",
            "com.ss.android.ugc.trill",
            "com.google.android.youtube",
            "com.twitter.android",
            "com.x.android",
            "com.facebook.katana",
            "com.reddit.frontpage",
        )

        private val _scrollEvents = MutableSharedFlow<ScrollEvent>(
            replay = 0,
            extraBufferCapacity = 256,
        )
        /** Stream of scroll/click/window events */
        val scrollEvents: SharedFlow<ScrollEvent> = _scrollEvents.asSharedFlow()

        private val _foregroundApp = MutableSharedFlow<String>(
            replay = 1,
            extraBufferCapacity = 16,
        )
        /** Stream of foreground app package name changes */
        val foregroundApp: SharedFlow<String> = _foregroundApp.asSharedFlow()

        private val _isRunning = MutableSharedFlow<Boolean>(
            replay = 1,
            extraBufferCapacity = 4,
        )
        /** Whether the accessibility service is currently running */
        val isRunning: SharedFlow<Boolean> = _isRunning.asSharedFlow()

        private var _instance: ZombieAccessibilityService? = null
        val instance: ZombieAccessibilityService? get() = _instance
    }
}
