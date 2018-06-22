package me.marvinyan.ralendac;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
    private static final int NUM_WEEKS_DISPLAYED = 6;

    private DateTime displayedMonth;
    private DateTime today;
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

        displayedMonth = new DateTime().withTimeAtStartOfDay(); // Display current month on app start
        today = new DateTime().withTimeAtStartOfDay();

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_prev_month:
                displayedMonth = displayedMonth.minusMonths(1);
                break;
            case R.id.action_next_month:
                displayedMonth = displayedMonth.plusMonths(1);
                break;
        }

        LinearLayout weeksContainer = findViewById(R.id.layout_calendar_weeks);
        weeksContainer.removeAllViews();
        buildCalendar();

        return super.onOptionsItemSelected(item);
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
        setTitle(displayedMonth.monthOfYear().getAsText() + " " + displayedMonth.year().getAsText());
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

        // Populate last month's final dates if space available
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

        // Populate dates of current month
        int curWeekRow = 0;
        int curDayOfWeek = firstDayOfCurMonth;
        for (int i = 1; i <= curMonthTotalDays; i++) {
            if (i != 1 && curDayOfWeek > 6) {
                curWeekRow++;
            }

            TextView dtv = createDateTextView(i);

            // Highlight today's date
            if (displayedMonth.withDayOfMonth(i).equals(today)) {
                dtv.setTextColor(getResources().getColor(R.color.colorDateHighlight));
                dtv.setTypeface(null, Typeface.BOLD);
                dtv.setPaintFlags(dtv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            } else {
                dtv.setTextColor(Color.BLACK);
            }

            weeks[curWeekRow].addView(dtv);

            curDayOfWeek++;
            if (curDayOfWeek == 8) {
                curDayOfWeek = 1;
            }
        }

        // Populate next month's starting dates if space available
        int nextMonthCurDate = 1;
        for (int week = curWeekRow; week < NUM_WEEKS_DISPLAYED;) {
            LinearLayout finalWeek = weeks[week];
            if (finalWeek.getChildCount() == 7) {
                week++;
            } else {
                TextView dtv = createDateTextView(nextMonthCurDate);
                dtv.setTextColor(Color.GRAY);
                finalWeek.addView(dtv);
                nextMonthCurDate++;
            }
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
