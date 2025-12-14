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
            nwsapi = NWSAPI.getInstance(applicationContext)
            migrateLegacySettings()

            // After migration, initialize the API and schedule the recurring update task.
            timer = Timer("NWSServiceTimer")
            timer?.schedule(updateTask, 100L, 300 * 1000L) // 15 minutes
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
                // Since all widgets shared the same settings in 1.x, we only need to determine the title once.
                val allAreas = nwsapi.getAreas()
                val matchedArea = allAreas.firstOrNull { it.id.equals(areaId, ignoreCase = true) }
                var finalAreaId = areaId
                var finalZoneId = zoneId
                val title = if (matchedArea != null) {
                    finalAreaId = matchedArea.id // Use the correctly-cased ID
                    val allZones = nwsapi.getZones(matchedArea)
                    val matchedZone = allZones.firstOrNull { it.id.equals(zoneId, ignoreCase = true) }
                    if (matchedZone != null) {
                        finalZoneId = matchedZone.id // Use the correctly-cased ID
                        if (matchedZone.id != "all") matchedZone.toString() else matchedArea.toString()
                    } else {
                        matchedArea.toString()
                    }
                } else {
                    "NWS Alerts"
                }
                for (appWidgetId in appWidgetIds) {
                    Log.i(TAG, "Migrating widget $appWidgetId with title \"$title\"")
                    // Save the legacy settings to the new DataStore format for each widget
                    saveWidgetPrefs(this, appWidgetId, finalAreaId, finalZoneId, title)
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
            val thisWidget = ComponentName(context, NWSWidgetProvider::class.java)
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
            .setContentText(getString(R.string.notification_text))
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
