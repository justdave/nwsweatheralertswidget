package net.justdave.nwsweatheralertswidget;

import java.net.URL;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;
import android.util.Xml;
import java.net.HttpURLConnection;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private String url = "http://alerts.weather.gov/cap/wwaatmget.php?x=MIC139&y=0";
    public ArrayList<NWSAlertEntry> nwsData = null;

    TextView raw_text;
    TextView parsed_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        raw_text = (TextView) findViewById(R.id.raw_text);
        parsed_text = (TextView) findViewById(R.id.parsed_text);
        SendHttpRequestTask task = new SendHttpRequestTask();
        String[] params = new String[]{url};
        task.execute(params);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    private class SendHttpRequestTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            String data = sendHttpRequest(url);

            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            NWSFeedHandler myXMLHandler = new NWSFeedHandler();
            raw_text.setText(result);
            try {

                /**
                 * Create the Handler to handle each of the XML tags.
                 **/
                Xml.parse(result, myXMLHandler);
                nwsData = myXMLHandler.getXMLData();
                parsed_text.setText(nwsData.toString());
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }
    private String sendHttpRequest(String url) {
        StringBuffer buffer = new StringBuffer();
        try {
            HttpURLConnection con = (HttpURLConnection) (new URL(url))
                    .openConnection();
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

        return buffer.toString();
    }
}
