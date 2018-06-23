package me.marvinyan.ralendac.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import me.marvinyan.ralendac.models.Event;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtils {

    public static List<Event> jsonToEvents(JSONObject jsonObj) throws JSONException {
        JSONArray jsonArr = jsonObj.getJSONArray("events");
        List<Event> eventsList = new ArrayList<>();

        for (int i = 0; i < jsonArr.length(); i++) {
            eventsList.add(parse(jsonArr.getJSONObject(i)));
        }

        return eventsList;
    }

    public static JSONObject paramsToJson(Map<String, String> params) throws JSONException {
        JSONObject json = new JSONObject();

        if (params.containsKey("description")) {
            json.put("description", params.get("description"));
        }
        if (params.containsKey("start_time")) {
            json.put("start_time", params.get("start_time"));
        }
        if (params.containsKey("end_time")) {
            json.put("end_time", params.get("end_time"));
        }

        return json;
    }

    private static Event parse(JSONObject jsonObject) throws JSONException {
        int id = jsonObject.getInt("id");
        String description = jsonObject.getString("description");

        DateTime utcStartTime = new DateTime(jsonObject.getString("start_time"));
        DateTime utcEndTime = new DateTime(jsonObject.getString("end_time"));

        DateTime localStartTime = utcStartTime.withZone(DateTimeZone.getDefault());
        DateTime localEndTime = utcEndTime.withZone(DateTimeZone.getDefault());

        return new Event(id, description, localStartTime, localEndTime);
    }
}
