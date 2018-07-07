package me.marvinyan.ralendac.net;

public class ApiUtils {

    public static EventService getEventService() {
        return RetrofitClient.getClient().create(EventService.class);
    }
}
