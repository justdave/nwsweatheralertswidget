package net.justdave.nwsweatheralertswidget

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.justdave.nwsweatheralertswidget.objects.NWSArea
import net.justdave.nwsweatheralertswidget.objects.NWSZone
import net.justdave.nwsweatheralertswidget.widget.loadWidgetPrefs
import net.justdave.nwsweatheralertswidget.widget.saveAlerts
import net.justdave.nwsweatheralertswidget.widget.saveUpdatedTimestamp
import net.justdave.nwsweatheralertswidget.widget.saveWidgetPrefs
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlertsUpdateService : Service() {

    private lateinit var nwsapi: NWSAPI

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Service starting")
        // Check if an update is already in progress
        if (isUpdateInProgress) {
            Log.i(TAG, "Update already in progress, stopping duplicate service call.")
            stopSelf(startId)
            return START_NOT_STICKY
        }
        isUpdateInProgress = true
        isRunning = true

        // Start the service in the foreground immediately.
        val notification = createNotification()
        try {
            startForeground(NOTIFICATION_ID, notification)
            Log.i(TAG, "Started foreground service")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start foreground service, continuing in background: ${e.message}")
        }

        // Launch a background coroutine to handle initialization and the main work.
        CoroutineScope(Dispatchers.IO).launch {
            nwsapi = NWSAPI.getInstance(applicationContext)
            migrateLegacySettings()
            updateTask()

            // Schedule the next alarm and then stop the service
            scheduleNextUpdate()
            isUpdateInProgress = false
            stopSelf(startId)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "Service destroying")
        isRunning = false
    }

    private suspend fun migrateLegacySettings() {
        val sharedPreferencesName = "${packageName}_preferences"
        val sharedPreferences = getSharedPreferences(sharedPreferencesName, MODE_PRIVATE)

        if (sharedPreferences.contains("feed_state")) {
            Log.i(TAG, "Found legacy settings, migrating...")
            val areaId = sharedPreferences.getString("feed_state", "us-all") ?: "us-all"
            val legacyCountyUrl = sharedPreferences.getString("feed_county", null)
            Log.i(TAG, "Legacy area ID: $areaId")
            Log.i(TAG, "Legacy county URL: $legacyCountyUrl")

            val zoneId = if (legacyCountyUrl != null) {
                try {
                    val parsedZone = legacyCountyUrl.toUri().getQueryParameter("x")
                    // The legacy value for 'Entire State' was a URL with x=0
                    if (parsedZone == null || parsedZone == "0") "all" else parsedZone
                } catch (e: Exception) {
                    Log.w(TAG, "Could not parse legacy county URL, defaulting to 'all'", e)
                    "all"
                }
            } else {
                "all"
            }
            Log.i(TAG, "Legacy zone ID: $zoneId")

            val appWidgetManager = AppWidgetManager.getInstance(this)
            val componentName = ComponentName(this, NWSWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            if (appWidgetIds.isNotEmpty()) {
                for (appWidgetId in appWidgetIds) {
                    Log.i(TAG, "Migrating widget $appWidgetId")
                    // Save the legacy settings to the new DataStore format for each widget
                    saveWidgetPrefs(this, appWidgetId, areaId, zoneId, "semitransparent")
                }
            }

            // Clear the legacy settings to ensure migration only runs once
            sharedPreferences.edit { clear() }
            Log.i(TAG, "Migration complete.")
        }
    }

    private suspend fun updateTask() {
        Log.i(TAG, "Update task running")
        val context: Context = applicationContext
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val thisWidget = ComponentName(context, NWSWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

        if (appWidgetIds.isEmpty()) {
            Log.i(TAG, "No widgets to update, stopping self.")
            return
        }

        for (appWidgetId in appWidgetIds) {
            try {
                val prefs = loadWidgetPrefs(context, appWidgetId)
                val areaId = prefs["area"] ?: "us-all"
                val zoneId = prefs["zone"] ?: "all"

                val alerts = nwsapi.getActiveAlerts(NWSArea(areaId, ""), NWSZone(zoneId, ""))
                Log.i(TAG, "Fetched ".plus(alerts.size).plus(" alerts for widget $appWidgetId"))
                val serializedAlerts = lenientJson.encodeToString(alerts)
                saveAlerts(context, appWidgetId, serializedAlerts)
                val timestamp = SimpleDateFormat("h:mm a", Locale.US).format(Date())
                saveUpdatedTimestamp(context, appWidgetId, timestamp)

                // Send the standard APPWIDGET_UPDATE broadcast to trigger the widget's onUpdate method.
                val intent = Intent(context, NWSWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
                }
                context.sendBroadcast(intent)

            } catch (e: Exception) {
                Log.e(TAG, "Update failed for widget $appWidgetId", e)
            }
        }
    }

    private fun scheduleNextUpdate() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlertsUpdateService::class.java)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }

        val triggerAtMillis = SystemClock.elapsedRealtime() + (5 * 60 * 1000)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pendingIntent)
            Log.i(TAG, "Next exact update scheduled for 5 minutes from now.")
        } else {
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pendingIntent)
            Log.i(TAG, "Next inexact update scheduled for 5 minutes from now.")
        }
    }

    private fun createNotification(): Notification {
        val channelId = "NWSWeatherAlertsWidget"
        val title = "NWS Weather Alerts Widget"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, title, NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent = Intent(this, MainActivity::class.java).let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }

        return NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.mipmap.app_icon)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val TAG = "AlertsUpdateService"
        private const val NOTIFICATION_ID = 1
        var isRunning = false
            private set
        var isUpdateInProgress = false
    }
}
