package net.justdave.nwsweatheralertswidget.objects

data class NWSZone(val id: String, val name: String) {

    //to display object as a string in spinner
    override fun toString(): String {
        return name
    }
}