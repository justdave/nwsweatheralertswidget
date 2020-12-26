package net.justdave.nwsweatheralertswidget.objects

class NWSArea(var id: String, var name: String) {

    //to display object as a string in spinner
    override fun toString(): String {
        return name
    }

    override fun equals(other: Any?): Boolean {
        if (other is NWSArea) {
            if (other.name === name && other.id === id) return true
        }
        return false
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }
}