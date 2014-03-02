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
    //private Handler handler;

    public NWSRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        Log.i(TAG, "NWSWidgetListViewAdapter enabled for widget ID ".concat(String.valueOf(mAppWidgetId)));
    }

    @Override
    public boolean hasStableIds() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Log.i(TAG, "getViewAt(".concat(String.valueOf(position)).concat(") called"));
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.event_listitem);
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
        return rv;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate() called");
        Intent intent = new Intent(NWSBackgroundService.class.getName());;
        mContext.startService(intent);
        mContext.bindService(intent, serviceConnection, 0);
        //handler = new Handler(); // handler will be bound to the current thread (UI)
    }

    @Override
    public void onDataSetChanged() {
        Log.i(TAG, "onDataSetChanged() called");
        // doing this in a Handler allows to call this method safely from any
        // thread see Handler docs for more info
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
                try {
                    if (api != null) {
                        Log.i(TAG, "Fetching updated data");
                        nwsData = api.getFeedData();
                        Log.i(TAG, "Retrieved updated data");
                    } else {
                        Log.w(TAG, "Skipping update because the backend service doesn't appear to be connected yet");
                        // attempt to re-bind - it won't hurt anything, and will kick it if it stalled
                        Intent intent = new Intent(NWSBackgroundService.class.getName());;
                        mContext.bindService(intent, serviceConnection, 0);
                    }
                } catch (Throwable t) {
                    Log.w(TAG, "Failed to retrieve updated parsed data from the background service");
                    Log.w(TAG, t);
                }
//            }
//        });
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
        if (nwsData != null) {
            Log.i(TAG, String.format("count = %d", nwsData.size()));
            return nwsData.size();
        }
        return 0;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getViewTypeCount() {
        // TODO Auto-generated method stub
        return 1;
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "Service connection established");

            // get the client side of the IPC connection
            api = NWSServiceApi.Stub.asInterface(service);
            // force an update since we just now got access to the data
            final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext.getApplicationContext());
            ComponentName thisWidget = new ComponentName(mContext.getApplicationContext(), NWSWidgetProvider.class);
            final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_parsed_events);
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
