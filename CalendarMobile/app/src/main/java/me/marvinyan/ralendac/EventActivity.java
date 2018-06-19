package me.marvinyan.ralendac;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class EventActivity extends AppCompatActivity {
    private EditText mDescriptionEditText;
    private TextView mStartTimeTextView;
    private TextView mEndTimeTextView;

    private LocalTime mStartTime;
    private LocalTime mEndTime;

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

    public void displayTimePickerDialog(View view) {
        mDescriptionEditText.clearFocus();

        /*
            Possible states:
            1) Start time was set later than end time:
                    - Set end time to start time.
            2) End time was set earlier than start time.
                    - Set start time to end time.
        */
        TimePickerDialog timePickerDialog;

        if (view.getId() == R.id.tv_start_time) {
            timePickerDialog = new TimePickerDialog(EventActivity.this, new TimePickerDialog.OnTimeSetListener() {

                @Override
                public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                    mStartTime = new LocalTime(hour, minute);

                    if (!validTimeRange()) {
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
                    mEndTime = new LocalTime(hour, minute);

                    if (!validTimeRange()) {
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
        if (parentIntent.hasExtra("description")) {
            mDescriptionEditText.setText(parentIntent.getStringExtra("description"));
            mStartTime = (LocalTime) parentIntent.getSerializableExtra("startTime");
            mEndTime = (LocalTime) parentIntent.getSerializableExtra("endTime");
        } else {
            // Creating new event:
            //      - Set start time to current time.
            //      - Set end time to the lesser of 1 hour after start time or 11:59pm.
            mStartTime = new LocalTime();
            mEndTime = mStartTime.plusHours(1);

            if (!validTimeRange()) {
                mEndTime = new LocalTime(23, 59);
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

    private boolean validTimeRange() {
        return !mEndTime.isBefore(mStartTime);
    }

    // Close activity instead of up navigating
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return false;
    }
}
