package net.justdave.nwsweatheralertswidget

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.justdave.nwsweatheralertswidget.objects.NWSArea
import net.justdave.nwsweatheralertswidget.objects.NWSZone
import net.justdave.nwsweatheralertswidget.widget.AlertsWidget
import net.justdave.nwsweatheralertswidget.widget.loadWidgetPrefs
import net.justdave.nwsweatheralertswidget.widget.saveAlerts
import net.justdave.nwsweatheralertswidget.widget.saveUpdatedTimestamp
import net.justdave.nwsweatheralertswidget.widget.saveWidgetPrefs
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

class AlertsUpdateService : Service() {

    private var timer: Timer? = null
    private lateinit var nwsapi: NWSAPI

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Service creating")
        isRunning = true

        // Start the service in the foreground immediately.
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        // Launch a background coroutine to handle initialization.
        CoroutineScope(Dispatchers.IO).launch {
            // First, ensure any legacy settings are migrated.
            migrateLegacySettings()

            // After migration, initialize the API and schedule the recurring update task.
            nwsapi = NWSAPI.getInstance(applicationContext)
            timer = Timer("NWSServiceTimer")
            timer?.schedule(updateTask, 100L, 900 * 1000L) // 15 minutes
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "Service destroying")
        timer?.cancel()
        timer = null
        isRunning = false
    }

    private suspend fun migrateLegacySettings() {
        val sharedPreferencesName = "${packageName}_preferences"
        val sharedPreferences = getSharedPreferences(sharedPreferencesName, MODE_PRIVATE)

        if (sharedPreferences.contains("feed_state")) {
            Log.i(TAG, "Found legacy settings, migrating...")
            val area = sharedPreferences.getString("feed_state", "us-all") ?: "us-all"
            val zone = sharedPreferences.getString("feed_county", "all") ?: "all"

            val appWidgetManager = AppWidgetManager.getInstance(this)
            val componentName = ComponentName(this, AlertsWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            if (appWidgetIds.isNotEmpty()) {
                for (appWidgetId in appWidgetIds) {
                    // Save the legacy settings to the new DataStore format for each widget
                    saveWidgetPrefs(this, appWidgetId, area, zone, "NWS Alerts")
                }
            }

            // Clear the legacy settings to ensure migration only runs once
            sharedPreferences.edit { clear() }
            Log.i(TAG, "Migration complete.")
        }
    }

    private val updateTask = object : TimerTask() {
        override fun run() {
            Log.i(TAG, "Update task running")
            val context: Context = applicationContext
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, AlertsWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

            if (appWidgetIds.isEmpty()) {
                Log.i(TAG, "No widgets to update, stopping self.")
                stopSelf()
                return
            }

            for (appWidgetId in appWidgetIds) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val prefs = loadWidgetPrefs(context, appWidgetId)
                        val area = prefs["area"] ?: "us-all"
                        val zone = prefs["zone"] ?: "all"

                        nwsapi.getActiveAlerts(
                            NWSArea(area, ""),
                            NWSZone(zone, ""),
                            { response ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    Log.i(TAG, "Fetched ".plus(response.size).plus(" alerts for widget $appWidgetId"))
                                    val serializedAlerts = Json.encodeToString(response)
                                    saveAlerts(context, appWidgetId, serializedAlerts)
                                    val timestamp = SimpleDateFormat("h:mm a", Locale.US).format(Date())
                                    saveUpdatedTimestamp(context, appWidgetId, timestamp)
                                    @Suppress("DEPRECATION") // need this for API 24 unfortunately
                                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_parsed_events)
                                }
                            }, { error ->
                                Log.e(TAG, "Failed to fetch alerts for widget $appWidgetId: ", error)
                            }
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Update failed for widget $appWidgetId", e)
                    }
                }
            }
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
        return NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText("Fetching weather alerts...")
            .setSmallIcon(R.mipmap.app_icon)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val TAG = "AlertsUpdateService"
        private const val NOTIFICATION_ID = 1
        var isRunning = false
            private set
    }
}
