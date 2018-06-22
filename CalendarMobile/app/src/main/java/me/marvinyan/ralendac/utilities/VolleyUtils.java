package me.marvinyan.ralendac.utilities;

import android.content.Context;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import java.util.Map;
import org.json.JSONObject;

/**
 * The purpose of this class is to separate network operations from activities for better
 * maintainability.
 *
 * <p>Helpful link: https://stackoverflow.com/a/44470827
 */
public class VolleyUtils {

    // TODO: This is only relevant for GET requests
    public static void requestWithoutParams(
            Context context,
            String urlStr,
            int method,
            final VolleyResponseListener listener) {
        JsonObjectRequest getRequest =
                new JsonObjectRequest(
                        method,
                        urlStr,
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                listener.onResponse(response);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                if (error.networkResponse != null) {
                                    listener.onError(String.valueOf(error.networkResponse.statusCode));
                                }
                            }
                        });

        VolleySingleton.getInstance(context).addToRequestQueue(getRequest);
    }

    public static void requestWithParams(
            Context context,
            String urlStr,
            int method,
            final Map<String, String> params,
            final VolleyResponseListener listener) {
        StringRequest stringRequest =
                new StringRequest(
                        method,
                        urlStr,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                listener.onResponse(response);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                if (error.networkResponse != null) {
                                    listener.onError(String.valueOf(error.networkResponse.statusCode));
                                }
                            }
                        }) {
                    @Override
                    public Map<String, String> getParams() {
                        return params;
                    }
                };

        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);
    }
}
