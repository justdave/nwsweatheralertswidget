package net.justdave.nwsweatheralertswidget;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
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
        titleView.setText(values.get(position).getEvent());
        summaryView.setText(values.get(position).getTitle());
        int background = R.drawable.black_button;
        int icon = R.drawable.nws_logo;
        if (values.get(position).getEvent().contains("Winter")
                || values.get(position).getEvent().contains("Wind")) {
            background = R.drawable.blue_button;
        }
        if (values.get(position).getEvent().contains("Watch")) {
            background = R.drawable.yellow_button;
        }
        if (values.get(position).getEvent().contains("Warning")) {
            background = R.drawable.red_button;
        }
        if (values.get(position).getEvent().contains("Winter")) {
            icon = R.drawable.winter;
        }
        if (values.get(position).getEvent().contains("Wind")) {
            icon = R.drawable.windy;
        }
        if (values.get(position).getEvent().contains("Ice")) {
            icon = R.drawable.ice;
        }
        if (values.get(position).getEvent().contains("Thunderstorm")) {
            icon = R.drawable.thunderstorm;
        }
        if (values.get(position).getEvent().contains("Tornado")) {
            icon = R.drawable.tornado;
        }
        mainView.setBackgroundResource(background);
        mainView.setOnClickListener(null);
        imageView.setImageResource(icon);
        return rowView;
    }
}