package net.justdave.nwsweatheralertswidget

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.android.volley.Response
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.justdave.nwsweatheralertswidget.objects.NWSAlert
import net.justdave.nwsweatheralertswidget.widget.AlertsWidget
import net.justdave.nwsweatheralertswidget.widget.loadWidgetPrefs
import net.justdave.nwsweatheralertswidget.widget.saveAlerts
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AlertsUpdateWorker(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return createForegroundInfo()
    }

    override suspend fun doWork(): Result = coroutineScope {
        Log.i(TAG, "Global worker task running")

        val appWidgetManager = AppWidgetManager.getInstance(appContext)
        val thisWidget = ComponentName(appContext, AlertsWidget::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

        if (appWidgetIds.isEmpty()) {
            Log.i(TAG, "No widgets to update, work is complete.")
            return@coroutineScope Result.success()
        }

        val results = appWidgetIds.map {
            appWidgetId -> async { updateSingleWidget(appContext, appWidgetId) }
        }.awaitAll()

        if (results.any { !it }) {
            Result.failure()
        } else {
            Result.success()
        }
    }

    private suspend fun updateSingleWidget(context: Context, appWidgetId: Int): Boolean {
        Log.i(TAG, "Updating widget $appWidgetId")
        return try {
            val nwsapi = NWSAPI.getInstance(context)
            val prefs = loadWidgetPrefs(context, appWidgetId)

            val alertList = fetchAlerts(nwsapi, prefs["area"]!!, prefs["zone"]!!)
            Log.i(TAG, "Fetched ".plus(alertList.size).plus(" alerts for widget $appWidgetId"))

            val serializedAlerts = Json.encodeToString(alertList)
            saveAlerts(context, appWidgetId, serializedAlerts)

            val appWidgetManager = AppWidgetManager.getInstance(context)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_parsed_events)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Worker failed for widget $appWidgetId", e)
            false
        }
    }

    private suspend fun fetchAlerts(nwsapi: NWSAPI, area: String, zone: String): List<NWSAlert> =
        suspendCoroutine { continuation ->
            val listener = Response.Listener<List<NWSAlert>> { response ->
                continuation.resume(response)
            }
            val errorListener = Response.ErrorListener { error ->
                continuation.resumeWithException(error)
            }
            nwsapi.getActiveAlerts(net.justdave.nwsweatheralertswidget.objects.NWSArea(area, ""), net.justdave.nwsweatheralertswidget.objects.NWSZone(zone, ""), listener, errorListener)
        }

    private fun createForegroundInfo(): ForegroundInfo {
        val channelId = "NWSWeatherAlertsWidget"
        val title = "NWS Weather Alerts Widget"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, title, NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(appContext, channelId)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText("Fetching weather alerts...")
            .setSmallIcon(R.mipmap.app_icon)
            .setOngoing(true)
            .build()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    companion object {
        private const val TAG = "AlertsUpdateWorker"
        private const val NOTIFICATION_ID = 1
    }
}
