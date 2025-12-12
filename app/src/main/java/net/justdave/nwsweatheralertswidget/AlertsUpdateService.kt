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
import java.util.Timer
import java.util.TimerTask

class AlertsUpdateService : Service() {

    private lateinit var timer: Timer
    private lateinit var nwsapi: NWSAPI

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Service creating")
        nwsapi = NWSAPI.getInstance(applicationContext)
        timer = Timer("NWSServiceTimer")
        timer.schedule(updateTask, 100L, 900 * 1000L) // 15 minutes

        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "Service destroying")
        timer.cancel()
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
                                    // This is deprecated, but the replacement has a different signature and
                                    // is not a drop-in replacement.
                                    @Suppress("DEPRECATION")
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
    }
}
