package me.marvinyan.ralendac.utilities;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * The purpose of this class is to separate network operations from activities for better
 * maintainability.
 *
 * <p>https://stackoverflow.com/a/44470827
 */
public class VolleyUtils {

    public static void get(Context context, String urlStr, final VolleyResponseListener listener) {
        JsonObjectRequest getRequest =
                new JsonObjectRequest(
                        Request.Method.GET,
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
                                listener.onError(error.toString());
                            }
                        });

        VolleySingleton.getInstance(context).addToRequestQueue(getRequest);
    }

    public static void post(
            Context context,
            String urlStr,
            final Map<String, String> getParams,
            final VolleyResponseListener listener) {
        StringRequest stringRequest =
                new StringRequest(
                        Request.Method.POST,
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
                                listener.onError(error.toString());
                            }
                        }) {
                    @Override
                    public Map<String, String> getHeaders() {
                        HashMap<String, String> headers = new HashMap<>();
                        getParams.put("Content-Type", "application/json; charset=utf-8");
                        return headers;
                    }
                };

        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);
    }
}
