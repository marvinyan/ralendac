package me.marvinyan.ralendac;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class EventActivity extends AppCompatActivity {
    private EditText mDescriptionEditText;
    private TextView mStartTimeTextView;
    private TextView mEndTimeTextView;

    private LocalDate mSelectedDate;
    private LocalDateTime mStartTime;
    private LocalDateTime mEndTime;
    private int mEventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        mDescriptionEditText = findViewById(R.id.tv_description);
        mStartTimeTextView = findViewById(R.id.tv_start_time);
        mEndTimeTextView = findViewById(R.id.tv_end_time);

        // Hide keyboard when clicking outside of description EditText
        mDescriptionEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focused) {
                if (!focused) {
                    hideKeyboard(view);
                }
            }
        });

        setupToolbar();
        setupInitialTimes();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mDescriptionEditText.clearFocus();
        if (item.getItemId() == R.id.action_save) {
            String description = mDescriptionEditText.getText().toString().trim();

            if (description.equals("")) {
                Toast.makeText(EventActivity.this, "Please fill in a description", Toast.LENGTH_LONG).show();
            } else {
                // POST/PUT to backend
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void displayTimePickerDialog(View view) {
        mDescriptionEditText.clearFocus();

        /*
            Possible states:
            1) Start time was set later than end time:
                    - Set end time to start time.
            2) End time was set earlier than start time.
                    - Set start time to end time.
            3) Start time is <= end time:
                    - Normal operation. Set times according to user input.
        */
        TimePickerDialog timePickerDialog;

        if (view.getId() == R.id.tv_start_time) {
            timePickerDialog = new TimePickerDialog(EventActivity.this, new TimePickerDialog.OnTimeSetListener() {

                @Override
                public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                    mStartTime = new LocalDateTime(mSelectedDate.getYear(), mSelectedDate.getMonthOfYear(), mSelectedDate.getDayOfMonth(), hour, minute);

                    if (!isValidTimeRange()) {
                        mEndTime = mStartTime;
                        Toast.makeText(EventActivity.this, "The end time has been reset to the start time.", Toast.LENGTH_LONG).show();
                    }

                    updateTimeTextViews();
                }
            }, mStartTime.getHourOfDay(), mStartTime.getMinuteOfHour(), false);
        } else {
            timePickerDialog = new TimePickerDialog(EventActivity.this, new TimePickerDialog.OnTimeSetListener() {

                @Override
                public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                    mEndTime = new LocalDateTime(mSelectedDate.getYear(), mSelectedDate.getMonthOfYear(), mSelectedDate.getDayOfMonth(), hour, minute);

                    if (!isValidTimeRange()) {
                        mStartTime = mEndTime;
                        Toast.makeText(EventActivity.this, "The start time has been reset to the end time.", Toast.LENGTH_LONG).show();
                    }

                    updateTimeTextViews();
                }
            }, mEndTime.getHourOfDay(), mEndTime.getMinuteOfHour(), false);
        }

        timePickerDialog.show();
    }

    private void setupToolbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_close);
        }
    }

    private void setupInitialTimes() {
        // Editing event:
        //      - Restore all fields
        Intent parentIntent = getIntent();
        if (parentIntent.hasExtra("eventId")) {
            mEventId = parentIntent.getIntExtra("eventId", -1);
            mDescriptionEditText.setText(parentIntent.getStringExtra("description"));
            mStartTime = (LocalDateTime) parentIntent.getSerializableExtra("startTime");
            mEndTime = (LocalDateTime) parentIntent.getSerializableExtra("endTime");
            mSelectedDate = new LocalDate(mStartTime.getYear(), mStartTime.getMonthOfYear(), mStartTime.getDayOfMonth());
        } else {
            // Creating new event:
            //      - Set start time to selected date with current time.
            //      - Set end time to the lesser of 1 hour after start time or 11:59pm.
            LocalTime now = new LocalTime();
            mSelectedDate = (LocalDate) parentIntent.getSerializableExtra("selectedDate");
            mStartTime = new LocalDateTime(mSelectedDate.getYear(), mSelectedDate.getMonthOfYear(), mSelectedDate.getDayOfMonth(), now.getHourOfDay(), now.getMinuteOfHour());
            mEndTime = mStartTime.plusHours(1);

            if (!isValidTimeRange()) {
                mEndTime = new LocalDateTime(mSelectedDate.getYear(), mSelectedDate.getMonthOfYear(), mSelectedDate.getDayOfMonth(), 23, 59);
            }
        }

        updateTimeTextViews();
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void updateTimeTextViews() {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("h:mm a");

        mStartTimeTextView.setText(formatter.print(mStartTime));
        mEndTimeTextView.setText(formatter.print(mEndTime));
    }

    private boolean isValidTimeRange() {
        // Ensure 1-day event and end time is >= start time
        return mStartTime.toLocalDate().isEqual(mEndTime.toLocalDate()) && !mEndTime.isBefore(mStartTime);
    }

    // Close activity instead of up navigating
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}
