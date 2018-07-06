package me.marvinyan.ralendac.util;

public interface VolleyResponseListener {

    void onError(String message);

    void onResponse(Object response);
}
