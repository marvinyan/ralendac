package me.marvinyan.ralendac.calendar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;
import java.util.Map;
import me.marvinyan.ralendac.R;
import me.marvinyan.ralendac.calendar.CalendarContract.Presenter;
import me.marvinyan.ralendac.data.Event;
import me.marvinyan.ralendac.events.EventActivity;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class CalendarFragment extends Fragment implements CalendarContract.View {

    private CalendarContract.Presenter mPresenter;

    public static final int EVENT_REQUEST_CODE = 1;

    private DateTime mDisplayedMonth;
    private DateTime mToday;
    private int mMaxWeeksInMonth;
    private LinearLayout[] mWeekLayouts;

    public CalendarFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToday = new DateTime().withTimeAtStartOfDay();
        mDisplayedMonth = new DateTime()
                .withDayOfMonth(1)
                .withTimeAtStartOfDay();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_calendar, container, false);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.refreshCalendar(mDisplayedMonth);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Refetch events if an event was created, edited, or deleted.
        // TODO: Save an API call by just updating allEvents with return result of EventActivity
        if (requestCode == EVENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            mPresenter.loadEvents();
        }
    }

    @Override
    public void setPresenter(Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showNewEventUi(DateTime selectedDate) {
        Intent eventActivityIntent = new Intent(getContext(), EventActivity.class);

        eventActivityIntent.putExtra("selectedDate", selectedDate);
        startActivityForResult(eventActivityIntent, EVENT_REQUEST_CODE);
    }

    @Override
    public void showEditEventUi(Event selectedEvent) {
        Intent eventActivityIntent = new Intent(getContext(), EventActivity.class);

        eventActivityIntent.putExtra("eventToEdit", selectedEvent);
        startActivityForResult(eventActivityIntent, EVENT_REQUEST_CODE);
    }

    @Override
    public void showLoadingIndicator(boolean active) {
        if (getView() == null) {
            return;
        }

        if (active) {
            getView().findViewById(R.id.layout_progress_bar_calendar).setVisibility(View.VISIBLE);
        } else {
            getView().findViewById(R.id.layout_progress_bar_calendar).setVisibility(View.GONE);
        }
    }

    @Override
    public void buildCalendar(DateTime displayedMonth) {
        mDisplayedMonth = displayedMonth;
        getNumWeeksToDisplay();

        LinearLayout weeksContainer = getView().findViewById(R.id.layout_calendar_weeks);
        weeksContainer.removeAllViews();
        weeksContainer.setWeightSum(mMaxWeeksInMonth);

        DateTimeFormatter formatter = DateTimeFormat.forPattern("MMMM Y");
        getActivity().setTitle(formatter.print(mDisplayedMonth));

        mWeekLayouts = new LinearLayout[mMaxWeeksInMonth];

        // Build rows representing weeks
        for (int i = 0; i < mMaxWeeksInMonth; i++) {
            LinearLayout week = new LinearLayout(getContext());
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
                    ContextCompat.getDrawable(getContext(), R.drawable.border_bottom_gray));

            mWeekLayouts[i] = week;
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
                mWeekLayouts[0].addView(dateBox);
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
            mWeekLayouts[curWeekRow].addView(dateBox);

            curDayOfWeek++;
            if (curDayOfWeek == 8) {
                curDayOfWeek = 1;
            }
        }

        // Populate next month's starting dates if space available
        DateTime nextMonth = mDisplayedMonth.plusMonths(1).withDayOfMonth(1);

        for (int week = curWeekRow; week < mMaxWeeksInMonth; ) {
            LinearLayout finalWeek = mWeekLayouts[week];
            if (finalWeek.getChildCount() == 7) {
                week++;
            } else {
                finalWeek.addView(createDateBoxView(nextMonth));
                nextMonth = nextMonth.plusDays(1);
            }
        }
    }

    @Override
    public void showBadConnectionMessage() {
        Toast.makeText(getContext(), "Unable to connect to server",
                Toast.LENGTH_LONG)
                .show();
    }

    @Override
    public void showEvents(Map<DateTime, List<Event>> mappedEvents) {
        for (LinearLayout weekLayout : mWeekLayouts) {
            for (int i = 0; i < weekLayout.getChildCount(); i++) {
                LinearLayout dateBox = (LinearLayout) weekLayout.getChildAt(i);

                // Clear previous events
                if (dateBox.getChildCount() > 1) {
                    dateBox.removeViewAt(1);
                }

                dateBox.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showNewEventUi((DateTime) view.getTag());
                    }
                });

                DateTime curDate = (DateTime) dateBox.getTag();
                List<Event> eventsOfTheDay;
                if (mappedEvents != null && mappedEvents.containsKey(curDate)) {
                    eventsOfTheDay = mappedEvents.get(curDate);

                    dateBox.addView(createEventsScrollView(eventsOfTheDay));
                }
            }
        }
    }

    // LinearLayout with a TextView as the date and ScrollView>LinearLayout>TextViews for events
    private LinearLayout createDateBoxView(DateTime date) {
        LinearLayout dateBox = new LinearLayout(getContext());
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

        return dateBox;
    }

    private TextView createDateTextView(DateTime date) {
        TextView dtv = new TextView(getContext());
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
        ScrollView eventsScrollView = new ScrollView(getContext());
        LinearLayout eventList = new LinearLayout(getContext());
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
        TextView dtv = new TextView(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);

        int marginTop = getResources().getDimensionPixelSize(R.dimen.margin_top_event);
        params.setMargins(0, marginTop, 0, 0);

        dtv.setBackgroundDrawable(
                ContextCompat.getDrawable(getContext(), R.drawable.background_rounded_event));
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
                showEditEventUi(event);
            }
        });
        return dtv;
    }


    // Feb 1 starts on a Sunday = 4 weeks
    // 30 day month starts on Sat = 6 weeks
    // 31 day month starts on Fri/Sat = 6 weeks
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
    }
}
