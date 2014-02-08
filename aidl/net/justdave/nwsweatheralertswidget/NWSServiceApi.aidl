package net.justdave.nwsweatheralertswidget;

import net.justdave.nwsweatheralertswidget.NWSAlertList;
import net.justdave.nwsweatheralertswidget.NWSServiceListener;

interface NWSServiceApi {

    NWSAlertList getFeedData();
    String getRawData();
    
    void addListener(NWSServiceListener listener);

    void removeListener(NWSServiceListener listener);
}