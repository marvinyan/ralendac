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
import android.widget.ScrollView;
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
            int margin = getResources().getDimensionPixelSize(R.dimen.datebox_margin);
            params.setMargins(0, 8, margin, 0);

            week.setWeightSum(7);
            week.setLayoutParams(params);
            week.setBackgroundDrawable(
                    ContextCompat.getDrawable(MainActivity.this, R.drawable.border_bottom_gray));

            weeks[i] = week;
            weeksContainer.addView(week);
        }

        // Populate last month's final dates if space available
        int prevMonthTotalDays = displayedMonth.minusMonths(1).dayOfMonth().getMaximumValue();
        int curMonthTotalDays = displayedMonth.dayOfMonth().getMaximumValue();
        int firstDayOfCurMonth = displayedMonth.withDayOfMonth(1).dayOfWeek().get();

        DateTime prevMonth = displayedMonth.minusMonths(1);
        if (firstDayOfCurMonth != 7) {
            for (int i = firstDayOfCurMonth - 1; i >= 0; i--) {
                LinearLayout dateBox = createDateBoxView(prevMonth.withDayOfMonth(prevMonthTotalDays - i));
                weeks[0].addView(dateBox);
            }
        }

        // Populate dates of current month
        int curWeekRow = 0;
        int curDayOfWeek = firstDayOfCurMonth;
        for (int date = 1; date <= curMonthTotalDays; date++) {
            // Edge case where the first day of the month is a Sunday
            if (date != 1 && curDayOfWeek > 6) {
                curWeekRow++;
            }

            LinearLayout dateBox = createDateBoxView(displayedMonth.withDayOfMonth(date));
            weeks[curWeekRow].addView(dateBox);

            curDayOfWeek++;
            if (curDayOfWeek == 8) {
                curDayOfWeek = 1;
            }
        }

        // Populate next month's starting dates if space available
        DateTime nextMonth = displayedMonth.plusMonths(1).withDayOfMonth(1);

        for (int week = curWeekRow; week < NUM_WEEKS_DISPLAYED;) {
            LinearLayout finalWeek = weeks[week];
            if (finalWeek.getChildCount() == 7) {
                week++;
            } else {
                finalWeek.addView(createDateBoxView(nextMonth));
                nextMonth = nextMonth.plusDays(1);
            }
        }
    }

    private TextView createDateTextView(DateTime date) {
        TextView dtv = new TextView(MainActivity.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        dtv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        dtv.setGravity(Gravity.START);
        dtv.setText(date.dayOfMonth().getAsText());
        dtv.setLayoutParams(params);

        // Highlight today's date, darken selected month's dates, lighten preceding/following dates
        if (date.equals(today)) {
            dtv.setTextColor(getResources().getColor(R.color.colorDateHighlight));
            dtv.setTypeface(null, Typeface.BOLD);
            dtv.setPaintFlags(dtv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        } else if (date.monthOfYear().equals(displayedMonth.monthOfYear())){
            dtv.setTextColor(Color.BLACK);
        } else {
            dtv.setTextColor(Color.GRAY);
        }

        return dtv;
    }

    // LinearLayout with a TextView as the date and ScrollView>LinearLayout>TextViews for events
    private LinearLayout createDateBoxView(DateTime date) {
        LinearLayout dateBox = new LinearLayout(MainActivity.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                LayoutParams.WRAP_CONTENT,
                1f
        );

        dateBox.setTag(date); // For seeding event activity

        int margin = getResources().getDimensionPixelSize(R.dimen.datebox_margin);
        params.setMargins(margin, 0, 0, 0);

        dateBox.setLayoutParams(params);
        dateBox.setOrientation(LinearLayout.VERTICAL);
        dateBox.addView(createDateTextView(date));
        dateBox.addView(createEventsScrollView());

        return dateBox;
    }

    private ScrollView createEventsScrollView() {
        ScrollView eventsScrollView = new ScrollView(MainActivity.this);
        LinearLayout eventList = new LinearLayout(MainActivity.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        eventList.setLayoutParams(params);
        eventList.setOrientation(LinearLayout.VERTICAL);

        eventList.addView(createEventTextView());
        eventList.addView(createEventTextView());

        eventsScrollView.addView(eventList);

        return eventsScrollView;
    }

    private TextView createEventTextView() {
        TextView dtv = new TextView(MainActivity.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        dtv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        dtv.setGravity(Gravity.START);
        dtv.setText("Wow an event");
        dtv.setSingleLine(true);
        dtv.setLayoutParams(params);

        return dtv;
    }
}
