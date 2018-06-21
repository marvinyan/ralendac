package me.marvinyan.ralendac;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.android.volley.Request.Method;
import java.util.List;
import me.marvinyan.ralendac.models.Event;
import me.marvinyan.ralendac.utilities.JsonUtils;
import me.marvinyan.ralendac.utilities.NetworkUtils;
import me.marvinyan.ralendac.utilities.VolleyResponseListener;
import me.marvinyan.ralendac.utilities.VolleyUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final int EVENT_REQUEST_CODE = 1;
    private static final int NUM_WEEKS_DISPLAYED = 5;

    private DateTime displayedMonth;
    private SwipeRefreshLayout swipeLayout;
//    private TextView mLogTextView;
    private List<Event> allEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        mLogTextView = findViewById(R.id.tv_json_log);

        swipeLayout = findViewById(R.id.swipeLayout);
        swipeLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        getEvents();
                    }
                });

        displayedMonth = new DateTime().plusMonths(1); // Display current month on app start

        getEvents();
        buildCalendar();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Refetch events if an event was created, edited, or deleted.
        // TODO: Save an API call by just updating allEvents with return result of EventActivity
        if (requestCode == EVENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            getEvents();
        }
    }

    public void startNewEventActivity(View view) {
        Intent eventActivityIntent = new Intent(MainActivity.this, EventActivity.class);

        LocalDate newEventTime = new LocalDate(2018, 6, 19);
        eventActivityIntent.putExtra("selectedDate", newEventTime);

        startActivityForResult(eventActivityIntent, EVENT_REQUEST_CODE);
    }

    public void startEditEventActivity(View view) {
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
//                        mLogTextView.setText(message);
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

//                            mLogTextView.setText(builder.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        swipeLayout.setRefreshing(false);
                    }
                });
    }

    private void buildCalendar() {
        LinearLayout[] weeks = new LinearLayout[NUM_WEEKS_DISPLAYED];
        LinearLayout weeksContainer = findViewById(R.id.layout_calendar_weeks);

        // Build rows representing weeks
        for (int i = 0; i < NUM_WEEKS_DISPLAYED; i++) {
            LinearLayout week = new LinearLayout(MainActivity.this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    0,
                    1f
            );

            week.setWeightSum(7);
            week.setLayoutParams(params);
            week.setBackgroundDrawable(
                    ContextCompat.getDrawable(MainActivity.this, R.drawable.border_bottom_gray));
            week.setPadding(40, 8, 27, 0); // TODO: Make this responsive

            weeks[i] = week;
            weeksContainer.addView(week);
        }

        // Populate first week (with last month's days included)
        int prevMonthTotalDays = displayedMonth.minusMonths(1).dayOfMonth().getMaximumValue();
        int curMonthTotalDays = displayedMonth.dayOfMonth().getMaximumValue();
        int firstDayOfCurMonth = displayedMonth.withDayOfMonth(1).dayOfWeek().get();

        if (firstDayOfCurMonth != 7) {
            for (int i = firstDayOfCurMonth - 1; i >= 0; i--) {
                TextView dtv = createDateTextView(prevMonthTotalDays - i);
                dtv.setTextColor(Color.GRAY);
                weeks[0].addView(dtv);
            }
        }

        // Populate days 1 to 30 of current month
        // Since Sunday is not the first day of week according to ISO standard, I decided not to
        // call this curWeek
        int curWeekRow = 0;
        int curDayOfWeek = firstDayOfCurMonth;
        for (int i = 1; i <= curMonthTotalDays; i++) {
            if (i != 1 && curDayOfWeek > 6) {
                curWeekRow++;
            }

            TextView dtv = createDateTextView(i);
            dtv.setTextColor(Color.BLACK);
            weeks[curWeekRow].addView(dtv);

            curDayOfWeek++;
            if (curDayOfWeek == 8) {
                curDayOfWeek = 1;
            }
        }

        // Populate next month's dates if space available
        LinearLayout finalWeek = weeks[NUM_WEEKS_DISPLAYED - 1];
        for (int i = 1; finalWeek.getChildCount() < 7; i++) {
            TextView dtv = createDateTextView(i);
            dtv.setTextColor(Color.GRAY);
            finalWeek.addView(dtv);
        }
    }

    private TextView createDateTextView(int date) {
        TextView dateTextView = new TextView(MainActivity.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f);

        dateTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        dateTextView.setGravity(Gravity.START);
        dateTextView.setText(String.valueOf(date));
        dateTextView.setLayoutParams(params);

        return dateTextView;
    }
}
