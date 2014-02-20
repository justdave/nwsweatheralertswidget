package net.justdave.nwsweatheralertswidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

public class NWSWidgetProvider extends AppWidgetProvider {

    public static final String WIDGET_CLICK = "net.justdave.nwsweatheralertswidget.WIDGET_CLICK";
    public static final String EVENT_URL = "net.justdave.nwsweatheralertswidget.EVENT_URL";

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
	@Override
	public void onReceive(Context context, Intent intent) {
	    super.onReceive(context, intent);

	    if (intent.getAction() != null && intent.getAction().equals(WIDGET_CLICK)) {
	        try {
	            Intent webIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(intent.getExtras().getString(EVENT_URL)));
	            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            context.startActivity(webIntent);
	        } catch (RuntimeException e) {
	            // The url is invalid, maybe missing http://
	            e.printStackTrace();
	        }
	    }
	}
	public static RemoteViews buildRemoteViews(final Context context, final int appWidgetId) {

        // Create an Intent to launch the widget data service
        Intent serviceIntent = new Intent(context, NWSWidgetService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

        // Get the layout for the App Widget and attach a viewFactory to the ListView
        final RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.default_widget);
        rv.setRemoteAdapter(R.id.widget_parsed_events, serviceIntent);

        // Set the action for the intent.
        Intent browserIntent = new Intent(context, NWSWidgetProvider.class);
        browserIntent.setAction(WIDGET_CLICK);
        browserIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent browserPendingIntent = PendingIntent.getBroadcast(context, 0, browserIntent,
            PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.widget_parsed_events, browserPendingIntent);

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