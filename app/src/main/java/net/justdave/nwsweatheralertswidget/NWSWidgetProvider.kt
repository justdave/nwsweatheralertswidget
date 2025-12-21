package net.justdave.nwsweatheralertswidget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.justdave.nwsweatheralertswidget.objects.NWSArea
import net.justdave.nwsweatheralertswidget.objects.NWSZone
import net.justdave.nwsweatheralertswidget.widget.NWSWidgetConfigureActivity
import net.justdave.nwsweatheralertswidget.widget.NWSWidgetService
import net.justdave.nwsweatheralertswidget.widget.deleteWidgetPrefs
import net.justdave.nwsweatheralertswidget.widget.loadWidgetPrefs

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [.widget.NWSWidgetConfigureActivity]
 *
 * Theoretically this should be in the widget folder, but we lose backward
 * compatibility with existing widgets from 1.x versions if we move it. This
 * file can never be moved without breaking any existing widgets.
 */
class NWSWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_SHOW_DETAILS) {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            val alertId = intent.getStringExtra("alert_id")
            if (alertId != null) {
                val detailIntent = Intent(context, AlertDetailActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    putExtra("alert_id", alertId)
                }
                context.startActivity(detailIntent)
            }
        }
        super.onReceive(context, intent)
    }

    /**
     * This is called to update the widget at intervals defined by the updatePeriodMillis attribute in the
     * AppWidgetProviderInfo. It is also called when the user adds the widget.
     */
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            CoroutineScope(Dispatchers.Main).launch {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    /**
     * This is called when an instance of the App Widget is deleted from the App Widget host.
     */
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes a widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            CoroutineScope(Dispatchers.Main).launch {
                deleteWidgetPrefs(context, appWidgetId)
            }
        }
    }

    /**
     * This is called when the first instance of the App Widget is created.
     */
    override fun onEnabled(context: Context) {
        // Schedule an immediate alarm to kick off the update chain.
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlertsUpdateService::class.java)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), pendingIntent)
        } else {
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), pendingIntent)
        }
    }

    /**
     * This is called when the last instance of the App Widget is deleted.
     */
    override fun onDisabled(context: Context) {
        // Cancel the repeating alarm.
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlertsUpdateService::class.java)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE)
        } else {
            PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE)
        }
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    companion object {
        private const val ACTION_SHOW_DETAILS = "net.justdave.nwsweatheralertswidget.ACTION_SHOW_DETAILS"
    }
}

/**
 * Updates a single widget instance's static elements.
 */
internal suspend fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val prefs = loadWidgetPrefs(context, appWidgetId)
    val areaId = prefs["area"]
    val zoneId = prefs["zone"]
    val updatedText = prefs["updated"]
    val alertCount = prefs["alert_count"]?.toInt() ?: 0
    val theme = prefs["theme"] ?: "semitransparent"

    // Dynamically generate the widget title
    val nwsapi = NWSAPI.getInstance(context)
    val areas = nwsapi.getAreas()
    val area = areas.firstOrNull { it.id == areaId } ?: NWSArea("", "")
    val zones = nwsapi.getZones(area)
    val zone = zones.firstOrNull { it.id == zoneId } ?: NWSZone("", "")
    val locationName = if (zone.id != "all" && zone.toString().isNotEmpty()) {
        zone.toString()
    } else {
        area.toString()
    }
    // "Current NWS Weather Alerts for $locationName" but potentially localized.
    val widgetText = context.getString(R.string.widget_title_format, context.getString(R.string.widget_title), locationName)

    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.alerts_widget)

    // Set the theme
    val (backgroundColor, textColor) = when (theme) {
        "light" -> R.color.widget_background_light to R.color.widget_text_light
        "dark" -> R.color.widget_background_dark to R.color.widget_text_dark
        "semitransparent" -> R.drawable.semitransparent_background to R.color.widget_text_semitransparent
        else -> {
            // System theme
            when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> R.color.widget_background_dark to R.color.widget_text_dark
                else -> R.color.widget_background_light to R.color.widget_text_light
            }
        }
    }
    views.setInt(R.id.widget_layout, "setBackgroundResource", backgroundColor)
    views.setTextColor(R.id.widget_title, context.getColor(textColor))
    views.setTextColor(R.id.widget_updated_timestamp, context.getColor(textColor))
    views.setTextColor(R.id.widget_empty_view, context.getColor(textColor))


    views.setTextViewText(R.id.widget_title, widgetText)
    if (updatedText != null) {
        views.setTextViewText(R.id.widget_updated_timestamp, "Last updated: ".plus(updatedText))
    } else {
        views.setTextViewText(R.id.widget_updated_timestamp, context.getString(R.string.loading))
    }

    // Manually control the visibility of the list and the empty view
    if (updatedText == null) {
        views.setViewVisibility(R.id.widget_parsed_events, View.GONE)
        views.setViewVisibility(R.id.widget_empty_view, View.VISIBLE)
        views.setTextViewText(R.id.widget_empty_view, context.getString(R.string.loading))
    } else if (alertCount > 0) {
        views.setViewVisibility(R.id.widget_parsed_events, View.VISIBLE)
        views.setViewVisibility(R.id.widget_empty_view, View.GONE)
    } else {
        views.setViewVisibility(R.id.widget_parsed_events, View.GONE)
        views.setViewVisibility(R.id.widget_empty_view, View.VISIBLE)
        views.setTextViewText(R.id.widget_empty_view, context.getString(R.string.alert_empty))
    }

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        views.setViewVisibility(R.id.widget_reconfigure_button, View.VISIBLE)
        val configureIntent = Intent(context, NWSWidgetConfigureActivity::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, appWidgetId, configureIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_reconfigure_button, pendingIntent)
    } else {
        views.setViewVisibility(R.id.widget_reconfigure_button, View.GONE)
    }

    // Set up the intent that starts the NWSWidgetService, which will
    // provide the views for this collection. This intent needs to be unique for each widget.
    val intent = Intent(context, NWSWidgetService::class.java).apply {
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        // Add the app widget ID to the intent's data to make it unique
        data = Uri.fromParts("content", appWidgetId.toString(), null)
    }
    @Suppress("DEPRECATION")
    views.setRemoteAdapter(R.id.widget_parsed_events, intent)

    // This section makes the widget title clickable
    val titlePendingIntent: PendingIntent = Intent(context, MainActivity::class.java)
        .let { titleIntent ->
            PendingIntent.getActivity(context, 0, titleIntent, PendingIntent.FLAG_IMMUTABLE)
        }
    views.setOnClickPendingIntent(R.id.widget_title, titlePendingIntent)

    // This section makes the list items clickable
    val itemIntent = Intent(context, NWSWidgetProvider::class.java).apply {
        action = "net.justdave.nwsweatheralertswidget.ACTION_SHOW_DETAILS"
    }
    val itemPendingIntent = PendingIntent.getBroadcast(
        context,
        appWidgetId, // Use the appWidgetId as the request code for uniqueness
        itemIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
    )
    views.setPendingIntentTemplate(R.id.widget_parsed_events, itemPendingIntent)


    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
    @Suppress("DEPRECATION")
    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_parsed_events)
}
