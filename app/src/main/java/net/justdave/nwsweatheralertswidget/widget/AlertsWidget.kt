package net.justdave.nwsweatheralertswidget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.justdave.nwsweatheralertswidget.AlertsDisplayFragment
import net.justdave.nwsweatheralertswidget.R

/**
 * Implementation of App Widget functionality.
 */
class AlertsWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            CoroutineScope(Dispatchers.Main).launch {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes a widget, cancel the background worker and delete the preference
        for (appWidgetId in appWidgetIds) {
            WorkManager.getInstance(context).cancelUniqueWork("AlertsUpdateWorker_$appWidgetId")
            CoroutineScope(Dispatchers.Main).launch {
                deleteWidgetPrefs(context, appWidgetId)
            }
        }
    }
}

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

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_parsed_events)
}
