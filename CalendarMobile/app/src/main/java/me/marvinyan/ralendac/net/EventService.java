package me.marvinyan.ralendac.net;

import me.marvinyan.ralendac.model.Event;
import me.marvinyan.ralendac.model.Events;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface EventService {

    @GET("events")
    Call<Events> getEvents();

    @POST("events")
    Call<Event> createEvent(@Body Event event);

    @PUT("events/{id}")
    Call<Event> updateEvent(@Path("id") int eventId, @Body Event event);

    @DELETE("events/{id}")
    Call<Event> deleteEvent(@Path("id") int eventId);
}
