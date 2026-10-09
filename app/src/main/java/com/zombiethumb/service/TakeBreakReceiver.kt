package com.zombiethumb.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Handles "Take a break" notification action — sends user to the launcher.
 */
class TakeBreakReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(launcherIntent)
    }
}
