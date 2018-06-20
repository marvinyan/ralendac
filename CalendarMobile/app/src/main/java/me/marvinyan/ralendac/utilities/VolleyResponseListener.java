package me.marvinyan.ralendac.utilities;

public interface VolleyResponseListener {
    void onError(String message);

    void onResponse(String response);
}
