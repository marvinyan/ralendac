package me.marvinyan.ralendac.utilities;

import android.net.Uri;

import java.net.MalformedURLException;
import java.net.URL;

import me.marvinyan.ralendac.models.Event;

public class NetworkUtils {

    private static final String BASE_URL = "https://api-cal.herokuapp.com/";
    private static final String API_VERSION = "v1";
    private static final String EVENT_ENDPOINT = "events";

    public static URL buildEventUrl(String eventId) {
        Uri.Builder builder =
                Uri.parse(BASE_URL).buildUpon().appendPath(API_VERSION).appendPath(EVENT_ENDPOINT);
        if (eventId != null) {
            builder.appendPath(eventId);
        }

        URL url = null;
        try {
            url = new URL(builder.build().toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    // Create and return new event as JSON
    //    public static String postEvent(URL url, Event event) throws IOException {
    //        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    //        connection.setRequestMethod("POST");
    //        connection.setDoOutput(true);
    //
    //        List<Pair<String, String>> params = new ArrayList<>();
    //
    //        DateTimeFormatter isoFormat = ISODateTimeFormat.dateTime();
    //        DateTime utcStartTime = event.getStartTime().toDateTime(DateTimeZone.UTC);
    //        DateTime utcEndTime = event.getEndTime().toDateTime(DateTimeZone.UTC);
    //
    //        params.add(new Pair<>("description", event.getDescription()));
    //        params.add(new Pair<>("start_time", utcStartTime.toString(isoFormat)));
    //        params.add(new Pair<>("end_time", utcEndTime.toString(isoFormat)));
    //
    //        try {
    //            InputStream in = connection.getInputStream();
    //            Scanner scanner = new Scanner(in);
    //            scanner.useDelimiter("\\A"); //
    //
    //            if (scanner.hasNext()) {
    //                return scanner.next();
    //            } else {
    //                return null;
    //            }
    //        } finally {
    //            connection.disconnect();
    //        }
    //    }

    // Edit and return updated event as JSON
    public static String putEvent(URL url, Event event) {
        return "";
    }

    // Delete and return deleted event as JSON
    public static String deleteEvent(URL url, int eventId) {
        return "";
    }
}
