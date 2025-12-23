package net.justdave.nwsweatheralertswidget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.util.Log

class BroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i("BroadcastReceiver", "Intent received: ".plus(intent.action))
        
        // Schedule an immediate alarm to kick off the update chain.
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val serviceIntent = Intent(context, AlertsUpdateService::class.java).apply {
            addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
        }
        
        // Use getService instead of getForegroundService. This allows the service to start
        // as a background service (whitelisted by the exact alarm) without creating a
        // strict contract to become a foreground service immediately, avoiding crashes
        // if startForeground() is restricted (e.g. dataSync type at boot on Android 14+).
        val pendingIntent = PendingIntent.getService(context, 0, serviceIntent, PendingIntent.FLAG_IMMUTABLE)
        
        // How long to wait before triggering the initial update task
        val triggerTime = SystemClock.elapsedRealtime()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            Log.i("BroadcastReceiver","Exactly scheduling initial update task in 0 seconds.")
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pendingIntent)
        } else {
            Log.i("BroadcastReceiver","Inexactly setting initial update tast in 0 seconds.")
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pendingIntent)
        }
    }
}
