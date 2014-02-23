package net.justdave.nwsweatheralertswidget;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class NWSAlertListViewAdapter extends ArrayAdapter<NWSAlertEntry> {

    private final Context context;
    private final ArrayList<NWSAlertEntry> values;

    public NWSAlertListViewAdapter(Context context,
            ArrayList<NWSAlertEntry> alertList) {
        super(context, R.layout.event_listitem, alertList);
        this.context = context;
        this.values = alertList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.event_listitem, parent, false);
        RelativeLayout mainView = (RelativeLayout) rowView
                .findViewById(R.id.eventlistitemview);
        TextView titleView = (TextView) rowView.findViewById(R.id.alert_title);
        TextView summaryView = (TextView) rowView
                .findViewById(R.id.alert_summary);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        if (values.get(position).getEvent().equals("")) {
            // If there is no "event" put the title in the big textfield
            // (typically the case with the fake entry in an empty list)
            titleView.setText(values.get(position).getTitle());
            summaryView.setText("");
        } else {
            titleView.setText(values.get(position).getEvent());
            summaryView.setText(values.get(position).getTitle());
        }
        mainView.setBackgroundResource(values.get(position).getBackground());
        imageView.setImageResource(values.get(position).getIcon());
        final Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(values.get(position).getLink()));
        browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        OnClickListener myListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                 context.startActivity(browserIntent);
            }
        };
        mainView.setOnClickListener(myListener);
        return rowView;
    }
}