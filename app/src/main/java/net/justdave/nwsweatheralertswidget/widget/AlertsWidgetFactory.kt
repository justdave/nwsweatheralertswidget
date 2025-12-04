package net.justdave.nwsweatheralertswidget.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import net.justdave.nwsweatheralertswidget.R
import net.justdave.nwsweatheralertswidget.objects.NWSAlert

class AlertsWidgetFactory(private val context: Context, intent: Intent) :
    RemoteViewsService.RemoteViewsFactory {

    private val appWidgetId: Int = intent.getIntExtra(
        AppWidgetManager.EXTRA_APPWIDGET_ID,
        AppWidgetManager.INVALID_APPWIDGET_ID
    )

    //This is a list of dummy data. Replace with real data from your data source.
    private val dummyData: List<NWSAlert> = (1..10).map { NWSAlert() }

    override fun onCreate() {
        // Connect to data source
    }

    override fun onDataSetChanged() {
        appWidgetId.let {} // fake for now to make compiler happy
        // Refresh data
    }

    override fun onDestroy() {
        // Close data source
    }

    override fun getCount(): Int {
        return dummyData.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.alerts_widget_list_item)
        views.setTextViewText(R.id.widget_title, dummyData[position].toString())
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
        return true
    }
}
