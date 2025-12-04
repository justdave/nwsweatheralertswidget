package net.justdave.nwsweatheralertswidget.widget

import android.content.Intent
import android.widget.RemoteViewsService

class AlertsWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return AlertsWidgetFactory(applicationContext, intent)
    }
}
