package net.justdave.nwsweatheralertswidget;

import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

public class NWSWidgetProvider extends AppWidgetProvider {

	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];
            Log.i("NWSWidgetProvider", "onUpdate() called");

            final RemoteViews rv = buildRemoteViews(context, appWidgetId);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, rv);

            //Updates the collection view, not necessary the first time
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_parsed_events);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
	public static RemoteViews buildRemoteViews(final Context context, final int appWidgetId) {

        // Create an Intent to launch the widget data service
        Intent serviceIntent = new Intent(context, NWSWidgetService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

        // Get the layout for the App Widget and attach a viewFactory to the ListView
        final RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.default_widget);
        rv.setRemoteAdapter(R.id.widget_parsed_events, serviceIntent);

        // The empty view is displayed when the collection has no items. It should be a sibling
        // of the collection view.
        rv.setEmptyView(R.id.widget_parsed_events, android.R.id.empty);

        return rv;
	}
	public static void updateAppWidget(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId) {
	    final RemoteViews views = buildRemoteViews(context, appWidgetId);
	    appWidgetManager.updateAppWidget(appWidgetId, views);
	}
}