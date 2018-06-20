package me.marvinyan.ralendac.utilities;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import me.marvinyan.ralendac.models.Event;

public class JsonUtils {

    public static Event[] getEventsFromJson(JSONObject jsonObj) throws JSONException {
        JSONArray jsonArr = jsonObj.getJSONArray("events");
        Event[] eventsArr = new Event[jsonArr.length()];

        for (int i = 0; i < jsonArr.length(); i++) {
            eventsArr[i] = parse(jsonArr.getJSONObject(i));
        }

        return eventsArr;
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
