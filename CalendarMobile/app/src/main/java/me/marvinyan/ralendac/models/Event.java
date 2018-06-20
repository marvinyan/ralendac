package me.marvinyan.ralendac.models;

import org.joda.time.DateTime;

public class Event {

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
}
