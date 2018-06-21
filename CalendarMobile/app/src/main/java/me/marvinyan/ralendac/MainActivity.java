package me.marvinyan.ralendac;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import com.android.volley.Request.Method;
import java.util.List;
import me.marvinyan.ralendac.models.Event;
import me.marvinyan.ralendac.utilities.JsonUtils;
import me.marvinyan.ralendac.utilities.NetworkUtils;
import me.marvinyan.ralendac.utilities.VolleyResponseListener;
import me.marvinyan.ralendac.utilities.VolleyUtils;
import org.joda.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    public static final int EVENT_REQUEST_CODE = 1;

    private SwipeRefreshLayout swipeLayout;
    private TextView mLogTextView;
    private List<Event> allEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLogTextView = findViewById(R.id.tv_json_log);

        swipeLayout = findViewById(R.id.swipeLayout);
        swipeLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        getEvents();
                    }
                });

        getEvents();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Refetch events if an event was created, edited, or deleted.
        // TODO: Save an API call by just updating allEvents
        if (requestCode == EVENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            getEvents();
        }
    }

    public void showNewEventActivity(View view) {
        Intent eventActivityIntent = new Intent(MainActivity.this, EventActivity.class);

        LocalDate newEventTime = new LocalDate(2018, 6, 19);
        eventActivityIntent.putExtra("selectedDate", newEventTime);

        startActivityForResult(eventActivityIntent, EVENT_REQUEST_CODE);
    }

    public void showEditEventActivity(View view) {
        Intent eventActivityIntent = new Intent(MainActivity.this, EventActivity.class);

        Event selectedEvent = allEvents.get(allEvents.size() - 1);

        eventActivityIntent.putExtra("eventId", selectedEvent.getId());
        eventActivityIntent.putExtra("description", selectedEvent.getDescription());
        eventActivityIntent.putExtra("startTime", selectedEvent.getStartTime());
        eventActivityIntent.putExtra("endTime", selectedEvent.getEndTime());

        startActivityForResult(eventActivityIntent, EVENT_REQUEST_CODE);
    }

    private void getEvents() {
        String eventsUrlStr = NetworkUtils.buildEventUrl(null).toString();
        VolleyUtils.requestWithoutParams(
                MainActivity.this,
                eventsUrlStr,
                Method.GET,
                new VolleyResponseListener() {
                    @Override
                    public void onError(String message) {
                        mLogTextView.setText(message);
                        swipeLayout.setRefreshing(false);
                    }

                    @Override
                    public void onResponse(Object response) {
                        try {
                            allEvents = JsonUtils.getEventsFromJson((JSONObject) response);
                            StringBuilder builder = new StringBuilder();

                            for (Event event : allEvents) {
                                builder.append(event.toString() + "\n");
                            }

                            mLogTextView.setText(builder.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        swipeLayout.setRefreshing(false);
                    }
                });
    }
}
