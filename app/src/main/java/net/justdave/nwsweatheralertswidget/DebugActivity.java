package net.justdave.nwsweatheralertswidget;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import androidx.core.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class DebugActivity extends Activity {

    private static final String TAG = DebugActivity.class.getSimpleName();

    private Handler handler;

    TextView raw_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xmldebug);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        handler = new Handler(); // handler will be bound to the current thread
                                 // (UI)
        raw_text = (TextView) findViewById(R.id.raw_text);
        Intent intent = new Intent(NWSBackgroundService.class.getName());
        intent.setPackage(this.getPackageName());
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
        // getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home :
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
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
                    if (timer != null) {
                        timer.cancel();
                        timer = null;
                    }
                } catch (Throwable t) {
                    Log.w(TAG,
                            "Failed to retrieve updated raw data from the background service");
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
