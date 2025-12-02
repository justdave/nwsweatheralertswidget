package net.justdave.nwsweatheralertswidget.objects

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

class NWSAlert() : Parcelable {
    private var blob = JSONObject()
    private var properties = JSONObject()
    private var headline = ""
    private var description = ""
    private var id = ""
    private var updated = ""
    private var published = ""
    private var title = ""
    private var link = ""
    private var summary = ""
    private var event = ""
    private var effective = ""
    private var expires = ""
    private var status = ""
    private var msgType = ""
    private var category = ""
    private var urgency = ""
    private var severity = ""
    private var certainty = ""
    private var areaDesc = ""

    constructor(parcel: Parcel) : this(JSONObject(parcel.readString()!!))

    constructor(data: JSONObject) : this() {
        blob = data
        properties = blob.getJSONObject(("properties"))
        headline = properties.optString("headline", "Unknown Alert")
        description = properties.optString("description", "No description provided")
    }

    override fun toString(): String {
        return headline
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(blob.toString())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<NWSAlert> {
        override fun createFromParcel(parcel: Parcel): NWSAlert {
            return NWSAlert(parcel)
        }

        override fun newArray(size: Int): Array<NWSAlert?> {
            return arrayOfNulls(size)
        }
    }

}