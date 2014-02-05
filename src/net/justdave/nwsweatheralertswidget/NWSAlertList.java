package net.justdave.nwsweatheralertswidget;

import java.util.ArrayList;

public class NWSAlertList extends ArrayList<NWSAlertEntry> {

	private static final long serialVersionUID = 1L;
    @Override
    public String toString() {
        String result = "";
        Integer numAlerts = this.size();
        result = result.concat("There are currently ");
        result = result.concat(numAlerts.toString());
        result = result.concat(" active alerts");
        for (NWSAlertEntry entry: this) {
            result = result.concat("\n").concat(entry.toString());
        }
        return result;
    }

}
