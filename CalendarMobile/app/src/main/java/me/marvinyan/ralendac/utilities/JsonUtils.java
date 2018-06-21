package me.marvinyan.ralendac.utilities;

import java.util.ArrayList;
import java.util.List;
import me.marvinyan.ralendac.models.Event;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtils {

    public static List<Event> getEventsFromJson(JSONObject jsonObj) throws JSONException {
        JSONArray jsonArr = jsonObj.getJSONArray("events");
        List<Event> eventsList = new ArrayList<>();

        for (int i = 0; i < jsonArr.length(); i++) {
            eventsList.add(parse(jsonArr.getJSONObject(i)));
        }

        return eventsList;
    }

    public static Event parse(JSONObject jsonObject) throws JSONException {
        int id = jsonObject.getInt("id");
        DateTime utcStartTime = new DateTime(jsonObject.getString("start_time"));
        DateTime utcEndTime = new DateTime(jsonObject.getString("end_time"));
        String description = jsonObject.getString("description");

        DateTime localStartTime = utcStartTime.withZone(DateTimeZone.getDefault());
        DateTime localEndTime = utcEndTime.withZone(DateTimeZone.getDefault());
        return new Event(id, description, localStartTime, localEndTime);
    }
}
