package me.marvinyan.ralendac.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/*
    Helpful link: http://www.jsonschema2pojo.org/
 */
public class Events {

    @SerializedName("events")
    @Expose
    private List<Event> events = null;

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }
}
