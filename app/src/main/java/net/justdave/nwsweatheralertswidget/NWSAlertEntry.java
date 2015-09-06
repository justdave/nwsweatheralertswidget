package net.justdave.nwsweatheralertswidget;

import android.os.Parcel;
import android.os.Parcelable;

public class NWSAlertEntry extends Object implements Parcelable {
    private String id = "";
    private String updated = "";
    private String published = "";
    private String title = "";
    private String link = "";
    private String summary = "";
    private String event = "";
    private String effective = "";
    private String expires = "";
    private String status = "";
    private String msgType = "";
    private String category = "";
    private String urgency = "";
    private String severity = "";
    private String certainty = "";
    private String areaDesc = "";

    public static final Creator<NWSAlertEntry> CREATOR = new Creator<NWSAlertEntry>() {
        @Override
        public NWSAlertEntry[] newArray(int size) {
            return new NWSAlertEntry[size];
        }

        @Override
        public NWSAlertEntry createFromParcel(Parcel source) {
            return new NWSAlertEntry(source);
        }
    };

    private NWSAlertEntry(Parcel source) {
        id = source.readString();
        updated = source.readString();
        published = source.readString();
        title = source.readString();
        link = source.readString();
        summary = source.readString();
        event = source.readString();
        effective = source.readString();
        expires = source.readString();
        status = source.readString();
        msgType = source.readString();
        category = source.readString();
        urgency = source.readString();
        severity = source.readString();
        certainty = source.readString();
        areaDesc = source.readString();
    }

    public NWSAlertEntry() {
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getPublished() {
        return published;
    }

    public void setPublished(String published) {
        this.published = published;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public int getIcon() {
        int icon = R.drawable.nws_logo;
        if (event.contains("Fire") || event.contains("Red Flag")) {
            icon = R.drawable.fire;
        }
        if (event.contains("Surf") || event.contains("Tsunami")) {
            icon = R.drawable.wave;
        }
        if (event.contains("Winter") || event.contains("Snow")) {
            icon = R.drawable.winter;
        }
        if (event.contains("Blizzard")) {
            icon = R.drawable.blizzard;
        }
        if (event.contains("Wind")) {
            icon = R.drawable.windy;
        }
        if (event.contains("Flood")) {
            icon = R.drawable.flood;
        }
        if (event.contains("Ice") || event.contains("Freezing") || event.contains("Freeze") || event.contains("Frost")
                || event.contains("Sleet")) {
            icon = R.drawable.ice;
        }
        if (event.contains("Thunderstorm")) {
            icon = R.drawable.thunderstorm;
        }
        if (event.contains("Tornado")) {
            icon = R.drawable.tornado;
        }
        return icon;
    }

    public int getBackground() {
        int background = R.drawable.black_button;
        if (event.contains("Fire") || event.contains("Dust")) {
            background = R.drawable.orange_button;
        }
        if (event.contains("Winter") || event.contains("Wind") || event.contains("Blizzard") || event.contains("Flood")
                || event.contains("Hydro") || event.contains("Snow") || event.contains("Rain") || event.contains("Marine")
                || event.contains("Surf")) {
            background = R.drawable.blue_button;
        }
        if (event.contains("Watch")) {
            background = R.drawable.yellow_button;
        }
        if (event.contains("Warning")) {
            background = R.drawable.red_button;
        }
        return background;
    }

    @Override
    public boolean equals(Object o) {
        //formatter:off
        if (this == o)                             { return true; }
        if (!(o instanceof NWSAlertEntry))         { return false; }
        NWSAlertEntry n = (NWSAlertEntry) o;
        if (!(id.equals(n.getId())))               { return false; }
        if (!(published.equals(n.getPublished()))) { return false; }
        if (!(title.equals(n.getTitle())))         { return false; }
        if (!(link.equals(n.getLink())))           { return false; }
        if (!(summary.equals(n.getSummary())))     { return false; }
        if (!(event.equals(n.getEvent())))         { return false; }
        if (!(effective.equals(n.getEffective()))) { return false; }
        if (!(expires.equals(n.getExpires())))     { return false; }
        if (!(status.equals(n.getStatus())))       { return false; }
        if (!(msgType.equals(n.getMsgType())))     { return false; }
        if (!(category.equals(n.getCategory())))   { return false; }
        if (!(urgency.equals(n.getUrgency())))     { return false; }
        if (!(severity.equals(n.getSeverity())))   { return false; }
        if (!(certainty.equals(n.getCertainty()))) { return false; }
        if (!(areaDesc.equals(n.getAreaDesc())))   { return false; }
        //formatter:on
        /*
         * Note that 'updated' is specifically NOT getting checked here, as that field will always be updated with the timestamp of
         * when you pulled the feed
         */
        return true;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(updated);
        dest.writeString(published);
        dest.writeString(title);
        dest.writeString(link);
        dest.writeString(summary);
        dest.writeString(event);
        dest.writeString(effective);
        dest.writeString(expires);
        dest.writeString(status);
        dest.writeString(msgType);
        dest.writeString(category);
        dest.writeString(urgency);
        dest.writeString(severity);
        dest.writeString(certainty);
        dest.writeString(areaDesc);
    }

}
