package me.marvinyan.ralendac.utilities;

import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import me.marvinyan.ralendac.models.Event;

public class NetworkUtils {
    private static final String TAG = NetworkUtils.class.getSimpleName();

    private static final String BASE_URL = "https://api-cal.herokuapp.com/";
    private static final String API_VERSION = "v1";
    private static final String EVENT_ENDPOINT = "events";

    public static URL buildEventUrl(String eventId) {
        Uri.Builder builder = Uri.parse(BASE_URL).buildUpon()
                .appendPath(API_VERSION)
                .appendPath(EVENT_ENDPOINT);
        if (eventId != null) {
            builder.appendPath(eventId);
        }

        URL url = null;
        try {
            url = new URL(builder.build().toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.i(TAG, "Built URI " + url);

        return url;
    }

    // Returns all events as JSON
    public static String getEvents(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A"); // \A = beginning of string

            if (scanner.hasNext()) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    // Create and return new event as JSON
    public static String postEvent(URL url, Event event) {
        return "";
    }

    // Edit and return updated event as JSON
    public static String putEvent(URL url, Event event) {
        return "";
    }

    // Delete and return deleted event as JSON
    public static String deleteEvent(URL url, int eventId) {
        return "";
    }
}
