package net.justdave.nwsweatheralertswidget;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

public class AlertDetailActivity extends Activity {

    private static final String TAG = AlertDetailActivity.class.getSimpleName();

    private Uri EventUri;
    private TextView event;
    private TextView expires;
    private TextView description;
    private TextView instructions;
    private TextView target;
    private TextView raw_xml;
    private static String rawdata;
    private static NWSAlertEntryDetail data;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        EventUri = intent.getData();

        setContentView(R.layout.activity_alertdetail);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        event = (TextView) findViewById(R.id.detail_event);
        expires = (TextView) findViewById(R.id.detail_expires);
        description = (TextView) findViewById(R.id.detail_description);
        instructions = (TextView) findViewById(R.id.detail_instructions);
        target = (TextView) findViewById(R.id.detail_target);
        raw_xml = (TextView) findViewById(R.id.event_raw_xml);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Timer task fetching ".concat(EventUri.toString()));
                String result = sendHttpRequest(EventUri.toString());
                rawdata = result.toString();
                NWSEventHandler myXMLHandler = new NWSEventHandler();
                try {
                    /**
                     * Create the Handler to handle each of the XML tags.
                     **/
                    Xml.parse(result, myXMLHandler);
                    data = myXMLHandler.getXMLData();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Date event_expires = null;
                            ScrollView scroller = (ScrollView) findViewById(R.id.detail_main_scroller);
                            scroller.setVisibility(View.VISIBLE);
                            event.setText(data.getEvent());
                            description.setText(data.getDescription());
                            instructions.setText(data.getInstruction());
                            target.setText(data.getAreaDesc());
                            
                            // parse the expires date and reformat it to be human-readable
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"); 
                            try {
                                event_expires = format.parse(data.getExpires());
                            } catch (ParseException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            // include day of week and timezone abbrev.
                            format = (SimpleDateFormat) SimpleDateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.LONG);
                            expires.setText(format.format(event_expires));

                            raw_xml.setText(rawdata);
                            NWSAlertEntry entry = new NWSAlertEntry();
                            entry.setEvent(data.getEvent());
                            ImageView image = (ImageView) findViewById(R.id.detail_icon);
                            image.setImageResource(entry.getIcon());
                            Log.i(TAG, "Activity Updated.");
                        }
                    });
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        });
        thread.start();
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Activity destroyed");
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem showxml = menu.findItem(R.id.detail_action_showxml);
        MenuItem hidexml = menu.findItem(R.id.detail_action_hidexml);
        ScrollView scroller = (ScrollView) findViewById(R.id.event_raw_scroller);
        showxml.setVisible(scroller.getVisibility() == View.GONE);
        hidexml.setVisible(scroller.getVisibility() == View.VISIBLE);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuitem) {
        Log.i(TAG, "onDetailMenuItemSelected() called");
        ScrollView scroller = (ScrollView) findViewById(R.id.event_raw_scroller);
        switch (menuitem.getItemId()) {
            case R.id.detail_action_showxml :
                scroller.setVisibility(View.VISIBLE);
                invalidateOptionsMenu();
                return true;
            case R.id.detail_action_hidexml :
                scroller.setVisibility(View.GONE);
                invalidateOptionsMenu();
                return true;
            case android.R.id.home :
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(menuitem);
    }

    private String sendHttpRequest(String url) {
        StringBuilder buffer = new StringBuilder();
        try {
            HttpURLConnection con = (HttpURLConnection) (new URL(url)).openConnection();
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.setDoOutput(false);
            con.connect();

            InputStream is = con.getInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = r.readLine()) != null) {
                buffer.append(line);
                buffer.append('\n');
            }
            con.disconnect();
        } catch (Throwable t) {
            t.printStackTrace();
        }

        Log.i(TAG, "URL retrieval complete.");
        return buffer.toString();
    }

}
