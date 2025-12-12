package net.justdave.nwsweatheralertswidget.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import net.justdave.nwsweatheralertswidget.R
import net.justdave.nwsweatheralertswidget.objects.NWSAlert

class AlertsWidgetFactory(private val context: Context, intent: Intent) :
    RemoteViewsService.RemoteViewsFactory {

    private val appWidgetId: Int = intent.getIntExtra(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID
    )
    private var alerts: List<NWSAlert> = emptyList()

    override fun onCreate() {
        // No-op. All initialization is done in onDataSetChanged.
    }

    override fun onDataSetChanged() {
        // This is the key lifecycle method. It's called by the system when the data has changed.
        // We load the data for our specific widget ID here.
        Log.i("AlertsWidgetFactory", "onDataSetChanged for widget $appWidgetId")
        runBlocking {
            val serializedAlerts = loadAlerts(context, appWidgetId)
            alerts = Json.decodeFromString(serializedAlerts)
        }
    }

    override fun onDestroy() {
        // No-op.
    }

    override fun getCount(): Int {
        return alerts.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.alerts_widget_list_item)

        if (position < alerts.size) {
            val alert = alerts[position]
            views.setTextViewText(R.id.alert_item_text, alert.getEvent())
            views.setImageViewResource(R.id.alert_item_icon, alert.getIcon())
            views.setInt(R.id.alert_item_layout, "setBackgroundResource", alert.getBackground())
            Log.i("AlertsWidgetFactory", "Widget $appWidgetId: loaded view for '${alert.getEvent()}' at position $position")
        }

        return views
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        // The list content can change, so IDs are not stable.
        return false
    }
}
