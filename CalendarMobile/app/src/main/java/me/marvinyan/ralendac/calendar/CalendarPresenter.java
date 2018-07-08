package me.marvinyan.ralendac.calendar;

import android.support.annotation.NonNull;
import java.util.List;
import java.util.Map;
import me.marvinyan.ralendac.data.Event;
import me.marvinyan.ralendac.data.Events;
import me.marvinyan.ralendac.util.EventUtils;
import me.marvinyan.ralendac.util.network.ApiUtils;
import org.joda.time.DateTime;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CalendarPresenter implements CalendarContract.Presenter {

    private final CalendarContract.View mCalendarView;
    private Map<DateTime, List<Event>> mMappedEvents;

    public CalendarPresenter(@NonNull CalendarContract.View calendarView) {
        mCalendarView = calendarView;
        mCalendarView.setPresenter(this);
        loadEvents();
    }

    @Override
    public void loadEvents() {
        mCalendarView.showLoadingIndicator(true);

        Call<Events> call = ApiUtils.getEventService().getEvents();
        call.enqueue(new Callback<Events>() {
            @Override
            public void onResponse(Call<Events> call, Response<Events> response) {
                mCalendarView.showLoadingIndicator(false);

                List<Event> allEvents = null;
                if (response.body() != null) {
                    allEvents = response.body().getEvents();
                }

                processEvents(allEvents);
            }

            @Override
            public void onFailure(Call<Events> call, Throwable t) {
                mCalendarView.showLoadingIndicator(false);
                mCalendarView.showBadConnectionMessage();
            }
        });
    }

    // For changing months
    @Override
    public void refreshCalendar(DateTime displayedMonth) {
        mCalendarView.buildCalendar(displayedMonth);
        mCalendarView.showEvents(mMappedEvents);
    }

    // For initial load and showing events after a change is made
    private void processEvents(List<Event> events) {
        mMappedEvents = EventUtils.getMappedEvents(events);
        mCalendarView.showEvents(mMappedEvents);
    }
}
