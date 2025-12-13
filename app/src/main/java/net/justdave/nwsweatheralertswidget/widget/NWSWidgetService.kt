package net.justdave.nwsweatheralertswidget.widget

import android.content.Intent
import android.widget.RemoteViewsService

class NWSWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return NWSWidgetFactory(applicationContext, intent)
    }
}
