package me.marvinyan.ralendac.utilities;

import android.content.Context;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

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
            final Map<String, String> params,
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
                                listener.onError(String.valueOf(error.networkResponse.statusCode));
                            }
                        }) {
                    @Override
                    public Map<String, String> getParams() {
                        return params;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String>  headers = new HashMap<String, String>();
                        headers.put("Content-Type", "application/x-www-form-urlencoded");
                        return headers;
                    }
                };

        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);
    }

    public static void put(
            Context context,
            String urlStr,
            final Map<String, String> params,
            final VolleyResponseListener listener) {
        StringRequest stringRequest =
                new StringRequest(
                        Request.Method.PUT,
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
                                listener.onError(String.valueOf(error.networkResponse.statusCode));
                            }
                        }) {
                    @Override
                    public Map<String, String> getParams() {
                        return params;
                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String>  headers = new HashMap<String, String>();
                        headers.put("Content-Type", "application/x-www-form-urlencoded");
                        return headers;
                    }
                };

        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);
    }
}
