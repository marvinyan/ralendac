package me.marvinyan.ralendac;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import me.marvinyan.ralendac.models.Event;
import me.marvinyan.ralendac.utilities.EventUtils;
import me.marvinyan.ralendac.utilities.JsonUtils;
import me.marvinyan.ralendac.utilities.NetworkUtils;
import me.marvinyan.ralendac.utilities.VolleyResponseListener;
import me.marvinyan.ralendac.utilities.VolleyUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    public static final int EVENT_REQUEST_CODE = 1;

    private DateTime mDisplayedMonth;
    private DateTime mToday;
    private Map<DateTime, List<Event>> mMappedEvents;
    private int mMaxWeeksInMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDisplayedMonth = new DateTime()
                .withDayOfMonth(1)
                .withTimeAtStartOfDay(); // Display current month on app start
        mToday = new DateTime().withTimeAtStartOfDay();

        findViewById(R.id.layout_progress_bar_calendar).setVisibility(View.VISIBLE);
        getEvents();
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
                mDisplayedMonth = mDisplayedMonth.minusMonths(1);
                break;
            case R.id.action_next_month:
                mDisplayedMonth = mDisplayedMonth.plusMonths(1);
                break;
        }

        buildCalendar();

        return super.onOptionsItemSelected(item);
    }


    public void startNewEventActivity(DateTime selectedDate) {
        Intent eventActivityIntent = new Intent(MainActivity.this, EventActivity.class);

        eventActivityIntent.putExtra("selectedDate", selectedDate);

        startActivityForResult(eventActivityIntent, EVENT_REQUEST_CODE);
    }

    public void startEditEventActivity(Event selectedEvent) {
        Intent eventActivityIntent = new Intent(MainActivity.this, EventActivity.class);

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
                        findViewById(R.id.layout_progress_bar_calendar).setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "Unable to connect to server",
                                Toast.LENGTH_LONG)
                                .show();
                    }

                    @Override
                    public void onResponse(Object response) {
                        findViewById(R.id.layout_progress_bar_calendar).setVisibility(View.GONE);
                        try {
                            List<Event> allEvents = JsonUtils
                                    .jsonToEvents((JSONObject) response);
                            mMappedEvents = EventUtils.getMappedEvents(allEvents);
                            buildCalendar();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    // Feb 1 starts on a Sunday = 4 weeks
    //
    // 30 day month starts on Sunday = 6 weeks
    // 31 day month starts on Saturday/Sunday = 6 weeks
    // All other months = 5 weeks
    private void getNumWeeksToDisplay() {
        int daysInDisplayedMonth = mDisplayedMonth.dayOfMonth().getMaximumValue();
        int dayOfWeek = mDisplayedMonth.getDayOfWeek();
        int monthOfYear = mDisplayedMonth.getMonthOfYear();

        mMaxWeeksInMonth = 5;

        if (monthOfYear == DateTimeConstants.FEBRUARY) {
            if (daysInDisplayedMonth == 28 && dayOfWeek == DateTimeConstants.SUNDAY) {
                mMaxWeeksInMonth = 4;
            }
        } else if ((daysInDisplayedMonth == 30 && dayOfWeek == DateTimeConstants.SATURDAY)
                || (daysInDisplayedMonth == 31 && (dayOfWeek == DateTimeConstants.FRIDAY
                || dayOfWeek == DateTimeConstants.SATURDAY))) {
            mMaxWeeksInMonth = 6;
        }

        Log.wtf("weeks", String.valueOf(mMaxWeeksInMonth));
    }

    private void buildCalendar() {
        LinearLayout weeksContainer = findViewById(R.id.layout_calendar_weeks);
        weeksContainer.removeAllViews();

        getNumWeeksToDisplay();
        weeksContainer.setWeightSum(mMaxWeeksInMonth);

        DateTimeFormatter formatter = DateTimeFormat.forPattern("MMMM Y");
        setTitle(formatter.print(mDisplayedMonth));

        LinearLayout[] weeks = new LinearLayout[mMaxWeeksInMonth];

        // Build rows representing weeks
        for (int i = 0; i < mMaxWeeksInMonth; i++) {
            LinearLayout week = new LinearLayout(MainActivity.this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    0,
                    1f
            );
            int paddingRight = getResources().getDimensionPixelSize(R.dimen.padding_datebox);
            params.setMargins(0, 8, 0, 0);

            week.setPadding(0, 0, paddingRight, 0);
            week.setWeightSum(7);
            week.setLayoutParams(params);
            week.setBackgroundDrawable(
                    ContextCompat.getDrawable(MainActivity.this, R.drawable.border_bottom_gray));

            weeks[i] = week;
            weeksContainer.addView(week);
        }

        // Populate last month's final dates if space available
        int prevMonthTotalDays = mDisplayedMonth.minusMonths(1).dayOfMonth().getMaximumValue();
        int curMonthTotalDays = mDisplayedMonth.dayOfMonth().getMaximumValue();
        int firstDayOfCurMonth = mDisplayedMonth.withDayOfMonth(1).dayOfWeek().get();

        DateTime prevMonth = mDisplayedMonth.minusMonths(1);
        if (firstDayOfCurMonth != 7) {
            for (int i = firstDayOfCurMonth - 1; i >= 0; i--) {
                LinearLayout dateBox = createDateBoxView(
                        prevMonth.withDayOfMonth(prevMonthTotalDays - i));
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

            LinearLayout dateBox = createDateBoxView(mDisplayedMonth.withDayOfMonth(date));
            weeks[curWeekRow].addView(dateBox);

            curDayOfWeek++;
            if (curDayOfWeek == 8) {
                curDayOfWeek = 1;
            }
        }

        // Populate next month's starting dates if space available
        DateTime nextMonth = mDisplayedMonth.plusMonths(1).withDayOfMonth(1);

        for (int week = curWeekRow; week < mMaxWeeksInMonth; ) {
            LinearLayout finalWeek = weeks[week];
            if (finalWeek.getChildCount() == 7) {
                week++;
            } else {
                finalWeek.addView(createDateBoxView(nextMonth));
                nextMonth = nextMonth.plusDays(1);
            }
        }
    }

    // LinearLayout with a TextView as the date and ScrollView>LinearLayout>TextViews for events
    private LinearLayout createDateBoxView(DateTime date) {
        LinearLayout dateBox = new LinearLayout(MainActivity.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                LayoutParams.MATCH_PARENT,
                1f
        );

        dateBox.setTag(date);

        int paddingLeft = getResources().getDimensionPixelSize(R.dimen.padding_datebox);
        dateBox.setPadding(paddingLeft, 0, 0, 0);
        dateBox.setClickable(true);
        dateBox.setLayoutParams(params);
        dateBox.setOrientation(LinearLayout.VERTICAL);
        dateBox.addView(createDateTextView(date));

        List<Event> eventsOfTheDay = new ArrayList<>();
        if (mMappedEvents != null && mMappedEvents.containsKey(date)) {
            eventsOfTheDay = mMappedEvents.get(date);
        }
        dateBox.addView(createEventsScrollView(eventsOfTheDay));

        // Create new event trigger
        dateBox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startNewEventActivity((DateTime) view.getTag());
            }
        });

        return dateBox;
    }

    private TextView createDateTextView(DateTime date) {
        TextView dtv = new TextView(MainActivity.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        dtv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        dtv.setGravity(Gravity.START);
        dtv.setText(date.dayOfMonth().getAsText());
        dtv.setLayoutParams(params);

        // Highlight today's date, darken selected month's dates, lighten preceding/following dates
        if (date.equals(mToday)) {
            dtv.setTextColor(getResources().getColor(R.color.colorDateHighlight));
            dtv.setTypeface(null, Typeface.BOLD);
            dtv.setPaintFlags(dtv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        } else if (date.monthOfYear().equals(mDisplayedMonth.monthOfYear())) {
            dtv.setTextColor(Color.BLACK);
        } else {
            dtv.setTextColor(Color.GRAY);
        }

        return dtv;
    }

    private ScrollView createEventsScrollView(List<Event> eventsOfTheDay) {
        ScrollView eventsScrollView = new ScrollView(MainActivity.this);
        LinearLayout eventList = new LinearLayout(MainActivity.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);

        eventList.setLayoutParams(params);
        eventList.setOrientation(LinearLayout.VERTICAL);

        for (Event event : eventsOfTheDay) {
            eventList.addView(createEventTextView(event));
        }

        eventsScrollView.addView(eventList);
        return eventsScrollView;
    }

    private TextView createEventTextView(final Event event) {
        TextView dtv = new TextView(MainActivity.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);

        int marginTop = getResources().getDimensionPixelSize(R.dimen.margin_top_event);
        params.setMargins(0, marginTop, 0, 0);

        dtv.setBackgroundDrawable(
                ContextCompat.getDrawable(MainActivity.this, R.drawable.background_rounded_event));
        dtv.setTextColor(Color.WHITE);
        int padding = getResources().getDimensionPixelSize(R.dimen.padding_event);
        dtv.setPadding(padding, padding, padding, padding);

        dtv.setClickable(true);
        dtv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        dtv.setGravity(Gravity.START);
        dtv.setText(event.getDescription());
        dtv.setSingleLine(true);
        dtv.setTypeface(null, Typeface.BOLD);
        dtv.setLayoutParams(params);

        dtv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                startEditEventActivity(event);
            }
        });
        return dtv;
    }
}
