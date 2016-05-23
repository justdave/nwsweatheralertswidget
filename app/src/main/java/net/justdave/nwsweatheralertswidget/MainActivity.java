package net.justdave.nwsweatheralertswidget;

import java.util.Timer;
import java.util.TimerTask;

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
import android.view.MenuItem;
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
        parsed_events = (ListView) findViewById(R.id.parsed_events);
        adapter = new NWSAlertListViewAdapter(getBaseContext(), nwsData);
        parsed_events.setAdapter(adapter);
        parsed_events.setEmptyView(findViewById(android.R.id.empty));
        Intent intent = new Intent(NWSBackgroundService.class.getName());
        startService(intent);
        bindService(intent, serviceConnection, 0);
        timer = new Timer("NWSInitialUpdateTimer");
        timer.schedule(updateTask, 500L, 1000L);
    }

    private Timer timer;

    private TimerTask updateTask = new TimerTask() {
        @Override
        public void run() {
            Log.i(TAG, "Timer fired");
            updateMainView();
        }
    };

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
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        Log.i(TAG, "Activity destroyed");
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }
    public boolean onOptionsItemSelected(MenuItem menuitem) {
        Log.i(TAG, "onDebugMenuItemSelected() called");
        Intent intent;
        switch (menuitem.getItemId()) {
            case R.id.action_about :
                AboutDialog about = new AboutDialog(this);
                about.setTitle(getResources().getIdentifier("action_about", "string", getPackageName()));
                about.show();
                break;
            case R.id.action_debug :
                intent = new Intent(getApplicationContext(), DebugActivity.class);
                startActivity(intent);
                break;
            case R.id.action_demo :
                intent = new Intent(getApplicationContext(), DemoActivity.class);
                startActivity(intent);
                break;
            case R.id.action_settings :
                intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                break;
        }
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
                    if (api == null) {
                        Log.w(TAG, "We don't appear to be connected to the background service yet... waiting for it to connect");
                        // attempt to re-bind - it won't hurt anything, and will kick it if it stalled
                        final Intent intent = new Intent(NWSBackgroundService.class.getName());
                        bindService(intent, serviceConnection, 0);
                    }
                    // and then wait for it to connect before continuing - I'm guessing this will hang if it never connects,
                    // but if that happens we probably have bigger problems. Docs say we can do expensive ops here.
                    // http://developer.android.com/reference/android/widget/RemoteViewsService.RemoteViewsFactory.html#onDataSetChanged%28%29
                    while (api == null) {
                        Thread.sleep(100);
                    }
                    nwsData = api.getFeedData();
                    adapter.clear();
                    adapter.addAll(nwsData);
                    adapter.notifyDataSetChanged();
                    Log.i(TAG, "parsed data updated:");
                    Log.i(TAG, nwsData.toString());
                    if (timer != null) {
                        timer.cancel();
                        timer = null;
                    }
                } catch (Throwable t) {
                    Log.w(TAG, "Failed to retrieve updated parsed data from the background service");
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
            Log.e(TAG, "Service connection closed");
            // attempt to reconnect to the service, because it probably just restarted
            Intent intent = new Intent(NWSBackgroundService.class.getName());;
            bindService(intent, serviceConnection, 0);
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
