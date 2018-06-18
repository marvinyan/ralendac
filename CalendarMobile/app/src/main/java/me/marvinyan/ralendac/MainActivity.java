package me.marvinyan.ralendac;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private EditText mDescriptionEditText;
    private TextView mStartTimeTextView;
    private TextView mEndTimeTextView;

    private int startHour;
    private int startMinute;
    private int endHour;
    private int endMinute;

    private String startIsoDate;
    private String endIsoDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        // Creating new event:
        //      - Set start time to current time.
        //      - Set end time to the lesser of 1 hour after start time or 11:59pm.
        startHour = hour;
        startMinute = minute;

        hour++;
        if (hour == 24) {
            hour = 23;
            minute = 59;
        }

        endHour = hour;
        endMinute = minute;

        updateTimeTextViews();
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
            timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {

                @Override
                public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                    startHour = hour;
                    startMinute = minute;

                    if (!validTimeRange()) {
                        endHour = startHour;
                        endMinute = startMinute;
                    }
                    updateTimeTextViews();
                }
            }, startHour, startMinute, false);
        } else {
            timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {

                @Override
                public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                    endHour = hour;
                    endMinute = minute;

                    if (!validTimeRange()) {
                        startHour = endHour;
                        startMinute = endMinute;
                    }
                    updateTimeTextViews();
                }
            }, endHour, endMinute, false);
        }

        timePickerDialog.show();
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void updateTimeTextViews() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.US);
        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.HOUR_OF_DAY, startHour);
        cal.set(Calendar.MINUTE, startMinute);
        mStartTimeTextView.setText(timeFormat.format(cal.getTime()));

        cal.set(Calendar.HOUR_OF_DAY, endHour);
        cal.set(Calendar.MINUTE, endMinute);
        mEndTimeTextView.setText(timeFormat.format(cal.getTime()));
    }

    private boolean validTimeRange() {
        return (startHour < endHour) || (startHour == endHour && startMinute <= endMinute);
    }
}
