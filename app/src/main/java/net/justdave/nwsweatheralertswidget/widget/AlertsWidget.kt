package net.justdave.nwsweatheralertswidget.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.justdave.nwsweatheralertswidget.AlertsDisplayFragment
import net.justdave.nwsweatheralertswidget.AlertsUpdateService
import net.justdave.nwsweatheralertswidget.R

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [AlertsWidgetConfigureActivity]
 */
class AlertsWidget : AppWidgetProvider() {
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
        // Use AlarmManager to reliably start the service from the background
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlertsUpdateService::class.java).apply {
            addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
        }
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(
                context,
                1, // Unique request code
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getService(
                context,
                1, // Unique request code
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
        }
        // Set the alarm to go off immediately to start the service
        alarmManager.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime(),
            pendingIntent
        )
    }

    /**
     * This is called when the last instance of the App Widget is deleted.
     */
    override fun onDisabled(context: Context) {
        // Stop the service when the last widget is removed
        context.stopService(Intent(context, AlertsUpdateService::class.java))
    }
}

/**
 * Updates a single widget instance.
 */
internal suspend fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val prefs = loadWidgetPrefs(context, appWidgetId)
    val widgetText = prefs["title"] ?: context.getString(R.string.appwidget_text)
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.alerts_widget)
    views.setTextViewText(R.id.widget_title, widgetText)

    // Set up the intent that starts the AlertsWidgetService, which will
    // provide the views for this collection.
    val intent = Intent(context, AlertsWidgetService::class.java).apply {
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    }
    views.setRemoteAdapter(R.id.widget_parsed_events, intent)

    // Set the empty view to be displayed when the collection is empty
    views.setEmptyView(R.id.widget_parsed_events, android.R.id.empty)

    // This section makes the widget title clickable
    val pendingIntent: PendingIntent = Intent(context, AlertsDisplayFragment::class.java)
        .let { titleIntent ->
            PendingIntent.getActivity(context, 0, titleIntent, PendingIntent.FLAG_IMMUTABLE)
        }
    views.setOnClickPendingIntent(R.id.widget_title, pendingIntent)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_parsed_events)
}
