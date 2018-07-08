package me.marvinyan.ralendac.util.network;

public class ApiUtils {

    public static EventService getEventService() {
        return RetrofitClient.getClient().create(EventService.class);
    }
}
