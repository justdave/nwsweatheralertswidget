package net.justdave.nwsweatheralertswidget.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.widget.RemoteViews
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.justdave.nwsweatheralertswidget.AlertsDisplayFragment
import net.justdave.nwsweatheralertswidget.AlertsUpdateService
import net.justdave.nwsweatheralertswidget.R

class AlertsWidget : AppWidgetProvider() {
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

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            CoroutineScope(Dispatchers.Main).launch {
                deleteWidgetPrefs(context, appWidgetId)
            }
        }
    }

    override fun onEnabled(context: Context) {
        // Use AlarmManager to reliably start the service from the background
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlertsUpdateService::class.java).apply {
            addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
        }
        val pendingIntent = PendingIntent.getForegroundService(
            context,
            1, // Unique request code
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        // Set the alarm to go off immediately
        alarmManager.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime(),
            pendingIntent
        )
    }

    override fun onDisabled(context: Context) {
        // Stop the service when the last widget is removed
        context.stopService(Intent(context, AlertsUpdateService::class.java))
    }
}

internal suspend fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val prefs = loadWidgetPrefs(context, appWidgetId)
    val widgetText = prefs["title"] ?: context.getString(R.string.appwidget_text)
    val views = RemoteViews(context.packageName, R.layout.alerts_widget)
    views.setTextViewText(R.id.widget_title, widgetText)

    val intent = Intent(context, AlertsWidgetService::class.java).apply {
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    }
    views.setRemoteAdapter(R.id.widget_parsed_events, intent)
    views.setEmptyView(R.id.widget_parsed_events, android.R.id.empty)

    val pendingIntent: PendingIntent = Intent(context, AlertsDisplayFragment::class.java)
        .let { intent ->
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }
    views.setOnClickPendingIntent(R.id.widget_title, pendingIntent)

    appWidgetManager.updateAppWidget(appWidgetId, views)
    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_parsed_events)
}
