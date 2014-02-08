package net.justdave.nwsweatheralertswidget;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.ListView;

public class MainActivity extends Activity {

    private NWSAlertListViewAdapter adapter;
    private NWSAlertList nwsData = new NWSAlertList();
    private static final String TAG = MainActivity.class.getSimpleName();

    private Handler handler;

    TextView raw_text;
    ListView parsed_events;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler(); // handler will be bound to the current thread
                                 // (UI)
        raw_text = (TextView) findViewById(R.id.raw_text);
        parsed_events = (ListView) findViewById(R.id.parsed_events);
        adapter = new NWSAlertListViewAdapter(getBaseContext(), nwsData);
        parsed_events.setAdapter(adapter);
        Intent intent = new Intent(NWSBackgroundService.class.getName());
        startService(intent);
        bindService(intent, serviceConnection, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            api.removeListener(serviceListener);
            unbindService(serviceConnection);
        } catch (Throwable t) {
            // catch any issues, typical for destroy routines
            // even if we failed to destroy something, we need to continue
            // destroying
            Log.w(TAG, "Failed to unbind from the service", t);
        }

        Log.i(TAG, "Activity destroyed");
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    private void updateMainView() {
        // doing this in a Handler allows to call this method safely from any
        // thread
        // see Handler docs for more info
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    raw_text.setText(api.getRawData());
                } catch (Throwable t) {
                    Log.w(TAG,
                            "Failed to retrieve updated raw data from the background service");
                    Log.w(TAG, t);
                }
                try {
                    nwsData = api.getFeedData();
                    adapter.clear();
                    adapter.addAll(nwsData);
                    adapter.notifyDataSetChanged();
                    Log.i(TAG, "parsed data updated:");
                    Log.i(TAG, nwsData.toString());
                } catch (Throwable t) {
                    Log.w(TAG,
                            "Failed to retrieve updated parsed data from the background service");
                    Log.w(TAG, t);
                }
            }
        });
    }
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "Service connection established");

            // that's how we get the client side of the IPC connection
            api = NWSServiceApi.Stub.asInterface(service);
            try {
                api.addListener(serviceListener);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to add listener", e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "Service connection closed");
        }
    };

    private NWSServiceApi api;

    private NWSServiceListener.Stub serviceListener = new NWSServiceListener.Stub() {
        @Override
        public void handleFeedUpdated() throws RemoteException {
            updateMainView();
        }
    };
}
