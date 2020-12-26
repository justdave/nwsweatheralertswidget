package net.justdave.nwsweatheralertswidget.objects

class NWSZone(var id: String, var name: String) {

    //to display object as a string in spinner
    override fun toString(): String {
        return name
    }

    override fun equals(other: Any?): Boolean {
        if (other is NWSZone) {
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