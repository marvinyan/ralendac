package me.marvinyan.ralendac.calendar;

import java.util.List;
import java.util.Map;
import me.marvinyan.ralendac.BasePresenter;
import me.marvinyan.ralendac.BaseView;
import me.marvinyan.ralendac.data.Event;
import org.joda.time.DateTime;

public interface CalendarContract {
    interface View extends BaseView<Presenter> {
        void showNewEventUi(DateTime selectedDate);

        void showEditEventUi(Event selectedEvent);

        void showLoadingIndicator(boolean active);

        void buildCalendar(DateTime displayedMonth);

        void showBadConnectionMessage();

        void showEvents(Map<DateTime, List<Event>> mappedEvents);
    }

    interface Presenter extends BasePresenter {
        void loadEvents();

        void refreshCalendar(DateTime displayedMonth);
    }
}
