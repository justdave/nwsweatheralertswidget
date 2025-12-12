package net.justdave.nwsweatheralertswidget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val serviceIntent = Intent(context, AlertsUpdateService::class.java).apply {
                addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            }
            val pendingIntent = PendingIntent.getForegroundService(
                context,
                2, // Unique request code
                serviceIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
            // Set the alarm to go off immediately
            alarmManager.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime(),
                pendingIntent
            )
        }
    }
}
