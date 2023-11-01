package net.justdave.nwsweatheralertswidget.objects

import android.os.Parcel
import android.os.Parcelable
import java.util.*

class NWSAlertList() : ArrayList<NWSAlert>(), Parcelable {

    constructor(parcel: Parcel) : this() {
        arrayListOf<NWSAlert>().apply {
            parcel.readList(this, NWSAlert::class.java.classLoader)
        }
    }

    override fun toString(): String {
        var result = ""
        val numAlerts = size
        result += "There are currently "
        result += numAlerts.toString()
        result = "$result active alerts"
        for (entry in this) {
            result = """
                $result
                $entry
                """.trimIndent()
        }
        return result
    }

    override fun equals(other: Any?): Boolean {
        //formatter:off
        if (this === other) {
            return true
        }
        if (other !is NWSAlertList) {
            return false
        }
        if (size != other.size) {
            return false
        }
        val n = size
        for (i in 0 until n) {
            if (get(i) != other[i]) {
                return false
            }
        }
        //formatter:on
        return true
    }

    @Suppress("EmptyMethod")
    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeList(this as List<NWSAlert?>)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<NWSAlertList> {
        override fun createFromParcel(parcel: Parcel): NWSAlertList {
            return NWSAlertList(parcel)
        }

        override fun newArray(size: Int): Array<NWSAlertList?> {
            return arrayOfNulls(size)
        }
    }

}
