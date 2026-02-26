package com.bible.alertepsaume

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            // Reprogrammer la notification si elle était activée
            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val notificationsEnabled = prefs.getBoolean("notifications_enabled", false)
            if (notificationsEnabled) {
                NotificationUtils.scheduleDailyNotification(context)
            }
        }
    }
}