package net.justdave.nwsweatheralertswidget;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.util.Log;

public final class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

    private static final String TAG = SettingsFragment.class.getSimpleName();
    private static String packageName;
    private ListPreference stateList;
    private ListPreference countyList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate() called");
        packageName = getActivity().getApplicationInfo().packageName;
        addPreferencesFromResource(R.xml.preferences);
        getPreferenceScreen().getPreferenceManager().setSharedPreferencesMode(Context.MODE_MULTI_PROCESS);
        Log.i(TAG, "sharedPreferenceName = ".concat(getPreferenceScreen().getPreferenceManager().getSharedPreferencesName()));

        // Get a reference to the preferences
        stateList = (ListPreference) getPreferenceScreen().findPreference("feed_state");
        countyList = (ListPreference) getPreferenceScreen().findPreference("feed_county");

    }

    @Override
    public void onResume() {
        super.onResume();

        Log.i(TAG, "onResume() called");

        // Setup the initial values
        String stateValue = stateList.getValue();
        Log.i(TAG, "stateValue = ".concat(stateValue));
        stateValue = stateList.getValue();
        CharSequence stateEntry = stateList.getEntry();
        if (stateEntry != null) {
            stateList.setSummary(stateEntry.toString());
        }
        String entriesResName = "preference_county_entries_".concat(stateValue);
        String entryValuesResName = "preference_county_entryvalues_".concat(stateValue);
        Log.i(TAG, "entriesResName = ".concat(entriesResName));
        Log.i(TAG, "entryValuesResName = ".concat(entryValuesResName));
        Log.i(TAG,
                "getIdentifier returns ".concat(String.valueOf(getResources().getIdentifier(entriesResName, "array", packageName))));
        countyList.setEntries(getResources().getIdentifier(entriesResName, "array", packageName));
        countyList.setEntryValues(getResources().getIdentifier(entryValuesResName, "array", packageName));

        CharSequence countyEntry = countyList.getEntry();
        if (countyEntry != null) {
            countyList.setSummary(countyEntry.toString());
        }
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.i(TAG, "onPause() called");
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Set new summary, when a preference value changes
        Log.i(TAG, "onChanged() called with key ".concat(key));
        if (key.equals("feed_state")) {
            stateList.setSummary(stateList.getEntry());
            Log.i(TAG, "stateEntry = ".concat(stateList.getEntry().toString()));
            Log.i(TAG, "stateValue = ".concat(stateList.getValue()));
            String entriesResName = "preference_county_entries_".concat(stateList.getValue());
            String entryValuesResName = "preference_county_entryvalues_".concat(stateList.getValue());
            Log.i(TAG, "entriesResName = ".concat(entriesResName));
            countyList.setEntries(getResources().getIdentifier(entriesResName, "array", packageName));
            countyList.setEntryValues(getResources().getIdentifier(entryValuesResName, "array", packageName));
            countyList.setValueIndex(0);
            countyList.setSummary(countyList.getEntry().toString());
        }
        if (key.equals("feed_county")) {
            countyList.setSummary(countyList.getEntry().toString());

            // kill off the background service and restart it to pick up the new preferences
            Intent intent = new Intent(NWSBackgroundService.class.getName());
            intent.setPackage("net.justdave.nwsweatheralertswidget");
            getActivity().stopService(intent);
            getActivity().startService(intent);

        }
    }
}
