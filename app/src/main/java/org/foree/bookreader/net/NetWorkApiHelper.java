package org.foree.bookreader.net;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.foree.bookreader.base.BaseApplication;

import java.io.UnsupportedEncodingException;

/**
 * Created by foree on 16-7-18.
 */
public class NetWorkApiHelper {
    private static NetWorkApiHelper INSTANCE = null;
    RequestQueue queue;

    public static NetWorkApiHelper newInstance() {

        if (INSTANCE == null)
            INSTANCE = new NetWorkApiHelper();

        return INSTANCE;
    }

    public NetWorkApiHelper() {
        queue = Volley.newRequestQueue(BaseApplication.getInstance().getApplicationContext());
    }

    public void getRequest(String requestUrl, final String charSet, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl, listener, errorListener) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String str = null;
                try {
                    str = new String(response.data, charSet);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return Response.success(str,
                        HttpHeaderParser.parseCacheHeaders(response));
            }
        };

        queue.add(stringRequest);
    }


}
