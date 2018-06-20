package me.marvinyan.ralendac.utilities;

import android.net.Uri;
import java.net.MalformedURLException;
import java.net.URL;

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
}
