package me.marvinyan.ralendac;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.io.IOException;
import java.net.URL;

import me.marvinyan.ralendac.models.Event;
import me.marvinyan.ralendac.utilities.NetworkUtils;

public class MainActivity extends AppCompatActivity {
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefreshLayout = findViewById(R.id.swipeLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new FetchAllEventsTask().execute();
            }
        });
    }

    public void showEventActivity(View view) {
        Intent eventActivityIntent = new Intent(MainActivity.this, EventActivity.class);

        // New event
        LocalDate newEventTime = new LocalDate(2018, 6, 19);
//        eventActivityIntent.putExtra("selectedDate", newEventTime);

        // Edit event
        // Simulate extracting from selected event
        LocalDateTime startTime = new LocalDateTime(2018, 6, 19, 8, 15);
        LocalDateTime endTime = new LocalDateTime(2018, 6, 19,12, 30);
        Event selectedEvent = new Event(5, "Test description", startTime, endTime);

        eventActivityIntent.putExtra("eventId", selectedEvent.getId());
        eventActivityIntent.putExtra("description", selectedEvent.getDescription());
        eventActivityIntent.putExtra("startTime", selectedEvent.getStartTime());
        eventActivityIntent.putExtra("endTime", selectedEvent.getEndTime());

        startActivity(eventActivityIntent);
    }

    public class FetchAllEventsTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            URL getUrl = NetworkUtils.buildEventUrl(null);
            try {
                String jsonResponse = NetworkUtils.getEvents(getUrl);
//            Event[] jsonEventsData = JsonUtils.getEventsFromJson(MainActivity.this, jsonResponse);
                return jsonResponse;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String json) {
            swipeRefreshLayout.setRefreshing(false);
            TextView tv = findViewById(R.id.tv_json_log);
            tv.setText(json);
        }
    }
}
