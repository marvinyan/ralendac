package me.marvinyan.ralendac.models;

import org.joda.time.LocalDateTime;

public class Event {
    private int id;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Event(int id, String description, LocalDateTime startTime, LocalDateTime endTime)  {
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
