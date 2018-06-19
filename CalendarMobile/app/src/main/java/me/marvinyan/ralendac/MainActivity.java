package me.marvinyan.ralendac;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import me.marvinyan.ralendac.models.Event;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void showEventActivity(View view) {
        Intent eventActivityIntent = new Intent(MainActivity.this, EventActivity.class);

        // New event
        LocalDate newEventTime = new LocalDate(2018, 6, 19);
//        eventActivityIntent.putExtra("selectedDate", newEventTime);

        // Edit event
        // Simulate extracting from selected event
        LocalDateTime startTime = new LocalDateTime(2018, 6, 19, 8, 15);
        LocalDateTime endTime = new LocalDateTime(2018, 6, 19,12, 30);
        Event selectedEvent = new Event(5, "Test description", startTime, endTime);

        eventActivityIntent.putExtra("eventId", selectedEvent.getId());
        eventActivityIntent.putExtra("description", selectedEvent.getDescription());
        eventActivityIntent.putExtra("startTime", selectedEvent.getStartTime());
        eventActivityIntent.putExtra("endTime", selectedEvent.getEndTime());

        startActivity(eventActivityIntent);
    }
}
