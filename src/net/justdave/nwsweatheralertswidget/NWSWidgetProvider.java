package net.justdave.nwsweatheralertswidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

public class NWSWidgetProvider extends AppWidgetProvider {

    public static final String WIDGET_CLICK = "net.justdave.nwsweatheralertswidget.WIDGET_CLICK";
    public static final String EVENT_URL = "net.justdave.nwsweatheralertswidget.EVENT_URL";
    private static final String TAG = NWSWidgetProvider.class.getSimpleName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this
        // provider
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            Log.i(TAG, "onUpdate() called with widget ID ".concat(String.valueOf(appWidgetId)));
            // if (appWidgetManager.getAppWidgetInfo(appWidgetId) != null) {
            final RemoteViews rv = buildRemoteViews(context, appWidgetId);

            // Tell the AppWidgetManager to perform an update on the current
            // app widget
            appWidgetManager.updateAppWidget(appWidgetId, rv);

            // Updates the collection view, not necessary the first time
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_parsed_events);
            // }
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Intent received: ".concat(intent.toString()));
        super.onReceive(context, intent);
        if (intent.getAction() != null && intent.getAction().equals(WIDGET_CLICK)) {
            String event_url = intent.getExtras().getString(EVENT_URL);
            Log.i(TAG, "URL Launch Event received for ".concat(event_url));
            try {
                Intent webIntent = new Intent(Intent.ACTION_VIEW);
                webIntent.setData(Uri.parse(event_url));
                webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(webIntent);
            } catch (RuntimeException e) {
                // The url is invalid, maybe missing http://
                e.printStackTrace();
            }
        } else if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisAppWidget = new ComponentName(context.getPackageName(), NWSWidgetProvider.class.getName());
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_parsed_events);
            final int N = appWidgetIds.length;
            Log.i(TAG, "Update Intent Received with ".concat(String.valueOf(N)).concat(" widgetIds"));
        }
        /*
         * <action android:name="android.appwidget.action.APPWIDGET_ENABLED" />
         * <action android:name="android.appwidget.action.APPWIDGET_DELETED" />
         * <action android:name="android.appwidget.action.APPWIDGET_DISABLED" />
         */
    }
    public static RemoteViews buildRemoteViews(final Context context, final int appWidgetId) {

        // Create an Intent to launch the widget data service
        Intent serviceIntent = new Intent(context, NWSWidgetService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

        // Get the layout for the App Widget and attach a viewFactory to the
        // ListView
        final RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.default_widget);
        rv.setRemoteAdapter(R.id.widget_parsed_events, serviceIntent);

        // Set the action for the intent.
        Intent browserIntent = new Intent(context, NWSWidgetProvider.class);
        browserIntent.setAction(WIDGET_CLICK);
        browserIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent browserPendingIntent = PendingIntent.getBroadcast(context, 0, browserIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setPendingIntentTemplate(R.id.widget_parsed_events, browserPendingIntent);

        // The empty view is displayed when the collection has no items. It
        // should be a sibling of the collection view.
        rv.setEmptyView(R.id.widget_parsed_events, android.R.id.empty);

        return rv;
    }
}