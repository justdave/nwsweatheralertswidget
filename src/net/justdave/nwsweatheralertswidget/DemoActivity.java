package net.justdave.nwsweatheralertswidget;

import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.ListView;

public class DemoActivity extends Activity {

    private NWSAlertListViewAdapter adapter;
    private NWSAlertList nwsData = new NWSAlertList();
    private static final String TAG = DemoActivity.class.getSimpleName();

    ListView parsed_events;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        parsed_events = (ListView) findViewById(R.id.parsed_events);
        adapter = new NWSAlertListViewAdapter(getBaseContext(), nwsData);
        parsed_events.setAdapter(adapter);
        updateMainView();
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Activity destroyed");
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    private void updateMainView() {
        String packageName = getApplicationInfo().packageName;
        String[] demo_type = getResources().getStringArray(getResources().getIdentifier("demo_alert_types", "array", packageName));
        final int N = demo_type.length;
        adapter.clear();
        for (int i = 0; i < N; i++) {
            NWSAlertEntry entry = new NWSAlertEntry();
            entry.setEvent(demo_type[i]);
            adapter.add(entry);
        }
        adapter.notifyDataSetChanged();
        Log.i(TAG, "parsed data updated:");
    }
}
