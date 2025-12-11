package net.justdave.nwsweatheralertswidget.objects

import android.os.Parcel
import android.os.Parcelable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.json.JSONObject

@Serializable
class NWSAlert() : Parcelable {
    @Transient private var blob = JSONObject()
    @Transient private var properties = JSONObject()
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
        properties = data.optJSONObject("properties") ?: JSONObject()
        headline = properties.optString("headline", "Unknown Alert")
        description = properties.optString("description", "No description provided")
        id = properties.optString("id", "")
        updated = properties.optString("sent", "")
        published = properties.optString("sent", "")
        title = properties.optString("event", "")
        link = blob.optString("id", "")
        summary = properties.optString("description", "")
        event = properties.optString("event", "")
        effective = properties.optString("effective", "")
        expires = properties.optString("expires", "")
        status = properties.optString("status", "")
        msgType = properties.optString("messageType", "")
        category = properties.optString("category", "")
        urgency = properties.optString("urgency", "")
        severity = properties.optString("severity", "")
        certainty = properties.optString("certainty", "")
        areaDesc = properties.optString("areaDesc", "")
    }

    fun getBlob(): JSONObject = blob
    fun getProperties(): JSONObject = properties
    fun getHeadline(): String = headline
    fun getDescription(): String = description
    fun getId(): String = id
    fun getUpdated(): String = updated
    fun getPublished(): String = published
    fun getTitle(): String = title
    fun getLink(): String = link
    fun getSummary(): String = summary
    fun getEvent(): String = event
    fun getEffective(): String = effective
    fun getExpires(): String = expires
    fun getStatus(): String = status
    fun getMsgType(): String = msgType
    fun getCategory(): String = category
    fun getUrgency(): String = urgency
    fun getSeverity(): String = severity
    fun getCertainty(): String = certainty
    fun getAreaDesc(): String = areaDesc

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
