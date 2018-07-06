package me.marvinyan.ralendac;

import android.app.Activity;
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
import com.android.volley.Request.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import me.marvinyan.ralendac.model.Event;
import me.marvinyan.ralendac.util.NetworkUtils;
import me.marvinyan.ralendac.util.VolleyResponseListener;
import me.marvinyan.ralendac.util.VolleyUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;

public class EventActivity extends AppCompatActivity {

    private EditText mDescriptionEditText;
    private TextView mStartTimeTextView;
    private TextView mEndTimeTextView;

    private DateTime mSelectedDate;
    private DateTime mStartTime;
    private DateTime mEndTime;
    private int mEventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        mDescriptionEditText = findViewById(R.id.tv_description);
        mStartTimeTextView = findViewById(R.id.tv_start_time);
        mEndTimeTextView = findViewById(R.id.tv_end_time);

        // Hide keyboard when clicking outside of description EditText
        mDescriptionEditText.setOnFocusChangeListener(
                new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean focused) {
                        if (!focused) {
                            hideKeyboard(view);
                        }
                    }
                });

        setupToolbar();
        setupInitialTimes();
        setupHeadingTextView();
    }

    private void setupHeadingTextView() {
        DateTimeFormatter formatter = DateTimeFormat.fullDate().withLocale(Locale.getDefault());
        TextView selectedDateTV = findViewById(R.id.tv_selected_date);
        selectedDateTV.setText(formatter.print(mSelectedDate));
    }

    // Close activity instead of up navigating
    @Override
    public boolean onSupportNavigateUp() {
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, resultIntent);
        finish();
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.event, menu);

        MenuItem deleteBtn = menu.findItem(R.id.action_delete);
        if (mEventId != -1) {
            deleteBtn.setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mDescriptionEditText.clearFocus();

        switch (item.getItemId()) {
            case R.id.action_save:
                String description = mDescriptionEditText.getText().toString().trim();

                if (description.equals("")) {
                    Toast.makeText(EventActivity.this, "Please fill in a description",
                            Toast.LENGTH_LONG)
                            .show();
                } else {
                    if (mEventId == -1) {
                        createEvent();
                    } else {
                        editEvent();
                    }
                }
                break;
            case R.id.action_delete:
                deleteEvent();
                break;
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
            timePickerDialog =
                    new TimePickerDialog(
                            EventActivity.this,
                            new TimePickerDialog.OnTimeSetListener() {

                                @Override
                                public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                                    mStartTime =
                                            new DateTime(
                                                    mSelectedDate.getYear(),
                                                    mSelectedDate.getMonthOfYear(),
                                                    mSelectedDate.getDayOfMonth(),
                                                    hour,
                                                    minute);

                                    if (!isValidTimeRange()) {
                                        mEndTime = mStartTime;
                                        Toast.makeText(
                                                EventActivity.this,
                                                "The end time has been reset to the "
                                                        + "start time.",
                                                Toast.LENGTH_LONG)
                                                .show();
                                    }

                                    updateTimeTextViews();
                                }
                            },
                            mStartTime.getHourOfDay(),
                            mStartTime.getMinuteOfHour(),
                            false);
        } else {
            timePickerDialog =
                    new TimePickerDialog(
                            EventActivity.this,
                            new TimePickerDialog.OnTimeSetListener() {

                                @Override
                                public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                                    mEndTime =
                                            new DateTime(
                                                    mSelectedDate.getYear(),
                                                    mSelectedDate.getMonthOfYear(),
                                                    mSelectedDate.getDayOfMonth(),
                                                    hour,
                                                    minute);

                                    if (!isValidTimeRange()) {
                                        mStartTime = mEndTime;
                                        Toast.makeText(
                                                EventActivity.this,
                                                "The start time has been reset to the "
                                                        + "end time.",
                                                Toast.LENGTH_LONG)
                                                .show();
                                    }

                                    updateTimeTextViews();
                                }
                            },
                            mEndTime.getHourOfDay(),
                            mEndTime.getMinuteOfHour(),
                            false);
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

        if (parentIntent.hasExtra("eventToEdit")) {
            Event eventToEdit = parentIntent.getParcelableExtra("eventToEdit");

            mEventId = eventToEdit.getId();
            mDescriptionEditText.setText(eventToEdit.getDescription());
            mStartTime = eventToEdit.getStartTime();
            mEndTime = eventToEdit.getEndTime();
            mSelectedDate =
                    new DateTime(
                            mStartTime.getYear(), mStartTime.getMonthOfYear(),
                            mStartTime.getDayOfMonth(), 0, 0);
        } else {
            // Creating new event:
            //      - Set start time to selected date with current time.
            //      - Set end time to the lesser of 1 hour after start time or 11:59pm.
            mEventId = -1;
            LocalTime now = new LocalTime();
            mSelectedDate = (DateTime) parentIntent.getSerializableExtra("selectedDate");
            mStartTime =
                    new DateTime(
                            mSelectedDate.getYear(),
                            mSelectedDate.getMonthOfYear(),
                            mSelectedDate.getDayOfMonth(),
                            now.getHourOfDay(),
                            now.getMinuteOfHour());
            mEndTime = mStartTime.plusHours(1);

            if (!isValidTimeRange()) {
                mEndTime =
                        new DateTime(
                                mSelectedDate.getYear(),
                                mSelectedDate.getMonthOfYear(),
                                mSelectedDate.getDayOfMonth(),
                                23,
                                59);
            }
        }

        updateTimeTextViews();
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
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
        return mStartTime.toLocalDate().isEqual(mEndTime.toLocalDate())
                && !mEndTime.isBefore(mStartTime);
    }

    private void createEvent() {
        String eventsUrlStr = NetworkUtils.buildEventUrl(null).toString();
        sendVolleyRequest(Method.POST, eventsUrlStr);
    }

    private void editEvent() {
        String eventsUrlStr = NetworkUtils.buildEventUrl(String.valueOf(mEventId)).toString();
        sendVolleyRequest(Method.PUT, eventsUrlStr);
    }

    private void deleteEvent() {
        String eventsUrlStr = NetworkUtils.buildEventUrl(String.valueOf(mEventId)).toString();
        sendVolleyRequest(Method.DELETE, eventsUrlStr);
    }

    private void sendVolleyRequest(final int method, String urlStr) {
        Map<String, String> params = makeParamsMap();

        try {
            VolleyUtils.requestWithParams(
                    EventActivity.this,
                    urlStr,
                    method,
                    params,
                    new VolleyResponseListener() {
                        @Override
                        public void onError(String message) {
                            Toast.makeText(EventActivity.this, "Status code: " + message,
                                    Toast.LENGTH_LONG)
                                    .show();
                        }

                        @Override
                        public void onResponse(Object response) {
                            String toastMsg = "";
                            switch (method) {
                                case Method.POST:
                                    toastMsg = "Event was created successfully";
                                    break;
                                case Method.PUT:
                                    toastMsg = "Event was updated successfully";
                                    break;
                                case Method.DELETE:
                                    toastMsg = "Event was deleted successfully";
                                    break;
                            }

                            Toast.makeText(EventActivity.this, toastMsg, Toast.LENGTH_LONG).show();
                            Intent resultIntent = new Intent();
                            setResult(Activity.RESULT_OK, resultIntent);
                            finish();
                        }
                    });
        } catch (JSONException e) {
            Toast.makeText(EventActivity.this, "Unable to send request", Toast.LENGTH_LONG).show();
        }
    }

    private Map<String, String> makeParamsMap() {
        Map<String, String> params = new HashMap<>();

        params.put("description", mDescriptionEditText.getText().toString());
        params.put("start_time", mStartTime.toDateTime(DateTimeZone.UTC).toString());
        params.put("end_time", mEndTime.toDateTime(DateTimeZone.UTC).toString());

        return params;
    }
}
