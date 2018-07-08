package me.marvinyan.ralendac.calendar;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import me.marvinyan.ralendac.R;
import org.joda.time.DateTime;

public class CalendarActivity extends AppCompatActivity {

    private CalendarPresenter mCalendarPresenter;

    private DateTime mDisplayedMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        mDisplayedMonth = new DateTime()
                .withDayOfMonth(1)
                .withTimeAtStartOfDay(); // Display current month on app start

        FragmentManager fragmentManager = getSupportFragmentManager();
        CalendarFragment calendarFragment = (CalendarFragment) fragmentManager
                .findFragmentById(R.id.calendar_content);

        if (calendarFragment == null) {
            calendarFragment = new CalendarFragment();

            fragmentManager.beginTransaction()
                    .add(R.id.calendar_content, calendarFragment)
                    .commit();
        }

        mCalendarPresenter = new CalendarPresenter(calendarFragment);
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

        mCalendarPresenter.refreshCalendar(mDisplayedMonth);

        return super.onOptionsItemSelected(item);
    }
}
