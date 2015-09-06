package net.justdave.nwsweatheralertswidget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.content.ServiceConnection;

public class NWSWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.i("NWSWidgetServiceFactory", "Got ".concat(intent.toString()));
        return new NWSRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class NWSRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final String TAG = NWSWidgetService.class.getSimpleName();
    private Context mContext;
    private int mAppWidgetId;
    public static NWSAlertList nwsData;
    private final Object NWSDataLock = new Object();

    public NWSRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        Log.i(TAG, "NWSWidgetListViewAdapter enabled for widget ID ".concat(String.valueOf(mAppWidgetId)));
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Log.i(TAG, "getViewAt(".concat(String.valueOf(position)).concat(") called"));
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.event_listitem);
        synchronized (NWSDataLock) {
            if (nwsData.get(position).getEvent().equals("")) {
                // If there is no "event" put the title in the big textfield
                // (typically the case with the fake entry in an empty list)
                rv.setTextViewText(R.id.alert_title, nwsData.get(position).getTitle());
                rv.setTextViewText(R.id.alert_summary, "");
            } else {
                rv.setTextViewText(R.id.alert_title, nwsData.get(position).getEvent());
                rv.setTextViewText(R.id.alert_summary, nwsData.get(position).getTitle());
            }
            rv.setImageViewResource(R.id.icon, nwsData.get(position).getIcon());
            rv.setInt(R.id.eventlistitemview, "setBackgroundResource", nwsData.get(position).getBackground());

            Bundle extras = new Bundle();
            extras.putString(NWSWidgetProvider.EVENT_URL, nwsData.get(position).getLink());
            Intent fillInIntent = new Intent();
            fillInIntent.putExtras(extras);
            rv.setOnClickFillInIntent(R.id.eventlistitemview, fillInIntent);

            Log.i(TAG, nwsData.get(position).toString());
        }
        return rv;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate() called");
        Intent intent = new Intent(NWSBackgroundService.class.getName());;
        mContext.startService(intent);
        mContext.bindService(intent, serviceConnection, 0);
    }

    @Override
    public void onDataSetChanged() {
        Log.i(TAG, "onDataSetChanged() called");
        try {
            if (api == null) {
                Log.w(TAG, "We don't appear to be connected to the background service yet... waiting for connection");
                // attempt to re-bind - it won't hurt anything, and will kick it if it stalled
                Intent intent = new Intent(NWSBackgroundService.class.getName());;
                mContext.bindService(intent, serviceConnection, 0);
            }
            // and then wait for it to connect before continuing - I'm guessing this will hang if it never connects,
            // but if that happens we probably have bigger problems. Docs say we can do expensive ops here.
            // http://developer.android.com/reference/android/widget/RemoteViewsService.RemoteViewsFactory.html#onDataSetChanged%28%29
            while (api == null) {
                Thread.sleep(100);
            }
            Log.i(TAG, "Fetching updated data");
            synchronized (NWSDataLock) {
                nwsData = api.getFeedData();
            }
            Log.i(TAG, "Retrieved updated data");
        } catch (Throwable t) {
            Log.w(TAG, "Failed to retrieve updated parsed data from the background service");
            Log.w(TAG, t);
        }
    }

    @Override
    public void onDestroy() {

        Log.i(TAG, "onDestroy() called");
        try {
            mContext.unbindService(serviceConnection);
        } catch (Throwable t) {
            // catch any issues, typical for destroy routines even if we failed to destroy something, we need to continue destroying
            Log.w(TAG, "Failed to unbind from the service", t);
        }

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        Log.i(TAG, "getCount() called");
        synchronized (NWSDataLock) {
            if (nwsData != null) {
                Log.i(TAG, String.format("count = %d", nwsData.size()));
                return nwsData.size();
            }
        }
        Log.i(TAG, "count = 0");
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "Service connection established");

            // get the client side of the IPC connection
            api = NWSServiceApi.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "Service connection closed");
            // attempt to reconnect to the background service because it probably just restarted
            Intent intent = new Intent(NWSBackgroundService.class.getName());;
            mContext.bindService(intent, serviceConnection, 0);
        }
    };

    private NWSServiceApi api;

}
