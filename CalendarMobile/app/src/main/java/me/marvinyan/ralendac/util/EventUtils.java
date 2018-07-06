package me.marvinyan.ralendac.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.marvinyan.ralendac.model.Event;
import org.joda.time.DateTime;

public class EventUtils {

    public static Map<DateTime, List<Event>> getMappedEvents(List<Event> allEvents) {
        Map<DateTime, List<Event>> mappedEvents = new HashMap<>();

        for (Event event : allEvents) {
            DateTime dateOfEvent = event.getStartTime().withTimeAtStartOfDay();

            if (!mappedEvents.containsKey(dateOfEvent)) {
                mappedEvents.put(dateOfEvent, new ArrayList<Event>());
            }
            mappedEvents.get(dateOfEvent).add(event);
        }

        return mappedEvents;
    }
}
