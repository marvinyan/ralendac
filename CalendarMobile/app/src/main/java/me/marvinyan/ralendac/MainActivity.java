package me.marvinyan.ralendac;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import me.marvinyan.ralendac.models.Event;
import me.marvinyan.ralendac.utilities.NetworkUtils;
import me.marvinyan.ralendac.utilities.VolleyResponseListener;
import me.marvinyan.ralendac.utilities.VolleyUtils;

public class MainActivity extends AppCompatActivity {
    public SwipeRefreshLayout swipeLayout;
    public TextView mLogTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLogTextView = findViewById(R.id.tv_json_log);

        swipeLayout = findViewById(R.id.swipeLayout);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getEvents();
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

    private void getEvents() {
        String eventsUrlStr = NetworkUtils.buildEventUrl(null).toString();
        VolleyUtils.get(MainActivity.this, eventsUrlStr, new VolleyResponseListener() {
            @Override
            public void onError(String message) {
                mLogTextView.setText(message);
                swipeLayout.setRefreshing(false);
            }

            @Override
            public void onResponse(String response) {
                mLogTextView.setText(response);
                swipeLayout.setRefreshing(false);
            }
        });
    }
}
