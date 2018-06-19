package me.marvinyan.ralendac;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void showEventActivity(View view) {
        Intent eventActivityIntent = new Intent(MainActivity.this, EventActivity.class);

        eventActivityIntent.putExtra("selectedDate", new LocalDate());
        eventActivityIntent.putExtra("description", "Test Description");

        LocalTime startTime = new LocalTime(8, 15);
        eventActivityIntent.putExtra("startTime", startTime);

        LocalTime endTime = new LocalTime(12, 30);
        eventActivityIntent.putExtra("endTime", endTime);
        startActivity(eventActivityIntent);
    }
}
