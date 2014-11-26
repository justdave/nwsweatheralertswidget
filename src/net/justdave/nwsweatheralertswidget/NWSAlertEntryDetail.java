package net.justdave.nwsweatheralertswidget;

import android.os.Parcel;
import android.os.Parcelable;

/*

 <alert xmlns = 'urn:oasis:names:tc:emergency:cap:1.1'>

 <!-- http-date = Tue, 25 Nov 2014 09:27:00 GMT -->
 <identifier>NOAA-NWS-ALERTS-MI1251789D1A1C.LakeEffectSnowAdvisory.125178BAA9B0MI.MQTWSWMQT.3d0d3874140d30ee7d3c0c4b085be682</identifier>
 <sender>w-nws.webmaster@noaa.gov</sender>
 <sent>2014-11-25T16:27:00-05:00</sent>
 <status>Actual</status>
 <msgType>Alert</msgType>
 <scope>Public</scope>
 <note>Alert for Gogebic; Ontonagon (Michigan) Issued by the National Weather Service</note>
 <info>
 <category>Met</category>
 <event>Lake Effect Snow Advisory</event>
 <urgency>Expected</urgency>
 <severity>Minor</severity>
 <certainty>Likely</certainty>
 <eventCode>
 <valueName>SAME</valueName>
 <value></value>
 </eventCode>
 <effective>2014-11-25T16:27:00-05:00</effective>
 <expires>2014-11-27T10:00:00-05:00</expires>
 <senderName>NWS Marquette (Northern Michigan)</senderName>
 <headline>Lake Effect Snow Advisory issued November 25 at 4:27PM EST until November 27 at 10:00AM EST by NWS Marquette</headline>
 <description>...MORE LAKE EFFECT SNOW LATE WEDNESDAY INTO THANKSGIVING...
 ...LAKE EFFECT SNOW ADVISORY IN EFFECT FROM 3 PM EST /2 PM CST/
 WEDNESDAY TO 10 AM EST /9 AM CST/ THURSDAY...
 THE NATIONAL WEATHER SERVICE IN MARQUETTE HAS ISSUED A LAKE EFFECT
 SNOW ADVISORY FOR LAKE EFFECT SNOW...AND BLOWING SNOW...WHICH IS IN
 EFFECT FROM 3 PM EST /2 PM CST/ WEDNESDAY TO 10 AM EST /9 AM CST/
 THURSDAY.
 HAZARDOUS WEATHER...
 * LIGHT TO MODERATE LAKE EFFECT SNOW SHOWERS WILL DEVELOP WEDNESDAY
 AFTERNOON AND CONTINUE THROUGH THANKSGIVING MORNING.
 * NORTH WINDS GUSTING AS HIGH AS 25 MPH WEDNESDAY AFTERNOON AND
 NIGHT WILL CAUSE PATCHY BLOWING SNOW...MAINLY NEAR LAKE
 SUPERIOR.
 * BY THE TIME THE LAKE EFFECT SNOW SHOWERS DIMINISH BY THANKSGIVING
 AFTERNOON...PLAN ON TOTAL SNOW ACCUMULATIONS RANGING FROM 6 TO 8
 INCHES OVER THE HIGHER TERRAIN FROM IRONWOOD AND WAKEFIELD TO
 WHITE PINE AND ROCKLAND. THERE WILL BE NO MORE THAN 2 TO 4 INCHES
 WELL INLAND FROM WATERSMEET TO PAULDING.
 IMPACTS...
 * THE SNOW AND PATCHY BLOWING SNOW WILL CREATE HAZARDOUS DRIVING
 CONDITIONS LATE WEDNESDAY AFTERNOON INTO THANKSGIVING DAY.</description>
 <instruction>* A LAKE EFFECT SNOW ADVISORY MEANS LAKE EFFECT SNOW IS FORECAST
 THAT WILL MAKE TRAVEL DIFFICULT IN SOME AREAS.
 * PREPARE...PLAN...AND STAY INFORMED. VISIT WWW.WEATHER.GOV/MQT</instruction>
 <parameter>
 <valueName>WMOHEADER</valueName>
 <value></value>
 </parameter>
 <parameter>
 <valueName>UGC</valueName>
 <value>MIZ002-009</value>
 </parameter>
 <parameter>
 <valueName>VTEC</valueName>
 <value>/O.NEW.KMQT.LE.Y.0016.141126T2000Z-141127T1500Z/</value>
 </parameter>
 <parameter>
 <valueName>TIME...MOT...LOC</valueName>
 <value></value>
 </parameter>
 <area>
 <areaDesc>Gogebic; Ontonagon</areaDesc>
 <polygon></polygon>
 <geocode>
 <valueName>FIPS6</valueName>
 <value>026053</value>
 </geocode>
 <geocode>
 <valueName>FIPS6</valueName>
 <value>026131</value>
 </geocode>
 <geocode>
 <valueName>UGC</valueName>
 <value>MIZ002</value>
 </geocode>
 <geocode>
 <valueName>UGC</valueName>
 <value>MIZ009</value>
 </geocode>
 </area>
 </info>
 </alert>

 */

public class NWSAlertEntryDetail extends Object implements Parcelable {
    private String identifier = "";
    private String sender = "";
    private String sent = "";
    private String status = "";
    private String msgType = "";
    private String scope = "";
    private String note = "";
    // <info>
    private String category = "";
    private String event = "";
    private String urgency = "";
    private String severity = "";
    private String certainty = "";
    private String effective = "";
    private String expires = "";
    private String senderName = "";
    private String headline = "";
    private String description = "";
    private String instruction = "";
    // <parameter>
    // <valueName>VTEC</valueName>
    // <value>/O.NEW.KMQT.LE.Y.0016.141126T2000Z-141127T1500Z/</value>
    private String VTEC = "";
    // </parameter>
    // <area>
    private String areaDesc = "";
    // </area>
    // </info>

    public static final Creator<NWSAlertEntryDetail> CREATOR = new Creator<NWSAlertEntryDetail>() {
        @Override
        public NWSAlertEntryDetail[] newArray(int size) {
            return new NWSAlertEntryDetail[size];
        }

        @Override
        public NWSAlertEntryDetail createFromParcel(Parcel source) {
            return new NWSAlertEntryDetail(source);
        }
    };

    private NWSAlertEntryDetail(Parcel source) {
        identifier = source.readString();
        sender = source.readString();
        sent = source.readString();
        status = source.readString();
        msgType = source.readString();
        scope = source.readString();
        note = source.readString();
        category = source.readString();
        event = source.readString();
        urgency = source.readString();
        severity = source.readString();
        certainty = source.readString();
        effective = source.readString();
        expires = source.readString();
        senderName = source.readString();
        headline = source.readString();
        description = source.readString();
        instruction = source.readString();
        VTEC = source.readString();
        areaDesc = source.readString();
    }

    public NWSAlertEntryDetail() {
        super();
    }

    @Override
    public String toString() {
        String result = "";
        result = result.concat(this.event);
        result = result.concat(" expires ");
        result = result.concat(this.expires);
        return result;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSent() {
        return sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getCertainty() {
        return certainty;
    }

    public void setCertainty(String certainty) {
        this.certainty = certainty;
    }

    public String getEffective() {
        return effective;
    }

    public void setEffective(String effective) {
        this.effective = effective;
    }

    public String getExpires() {
        return expires;
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getVTEC() {
        return VTEC;
    }

    public void setVTEC(String vTEC) {
        VTEC = vTEC;
    }

    public String getAreaDesc() {
        return areaDesc;
    }

    public void setAreaDesc(String areaDesc) {
        this.areaDesc = areaDesc;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(identifier);
        dest.writeString(sender);
        dest.writeString(sent);
        dest.writeString(status);
        dest.writeString(msgType);
        dest.writeString(scope);
        dest.writeString(note);
        dest.writeString(category);
        dest.writeString(event);
        dest.writeString(urgency);
        dest.writeString(severity);
        dest.writeString(certainty);
        dest.writeString(effective);
        dest.writeString(expires);
        dest.writeString(senderName);
        dest.writeString(headline);
        dest.writeString(description);
        dest.writeString(instruction);
        dest.writeString(VTEC);
        dest.writeString(areaDesc);
    }

}
