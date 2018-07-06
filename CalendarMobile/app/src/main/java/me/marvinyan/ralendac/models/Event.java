package me.marvinyan.ralendac.models;

import android.os.Parcel;
import android.os.Parcelable;
import org.joda.time.DateTime;

public class Event implements Parcelable {

    private int id;
    private String description;
    private DateTime startTime;
    private DateTime endTime;

    public Event(int id, String description, DateTime startTime, DateTime endTime) {
        this.id = id;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(DateTime startTime) {
        this.startTime = startTime;
    }

    public DateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(DateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "Event "
                + id
                + ": "
                + description
                + " ("
                + getStartTime().toString()
                + " - "
                + getEndTime().toString()
                + ")";
    }

    protected Event(Parcel in) {
        id = in.readInt();
        description = in.readString();
        startTime = (DateTime) in.readValue(DateTime.class.getClassLoader());
        endTime = (DateTime) in.readValue(DateTime.class.getClassLoader());
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(description);
        dest.writeValue(startTime);
        dest.writeValue(endTime);
    }
}