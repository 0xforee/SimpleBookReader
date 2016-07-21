package org.foree.zetianjia;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by foree on 16-7-21.
 */
public class NetRequest {
    public static void getHtml(String url, final NetCallback netCallback) {
        final Map<String,String> headers = new HashMap<>();
        NetWorkApiHelper.newInstance().getRequest(url, headers, new Response.Listener<String>() {
                    @Override
                    public void onResponse (String response){
                        //Log.i("HH", "onResponse " + response);
                        if (netCallback != null){
                            netCallback.onSuccess(response);
                        }
                    }
                }
                ,new Response.ErrorListener()

                {
                    @Override
                    public void onErrorResponse (VolleyError error){
                        Log.e("HH", "onErrorResponse " + error.getMessage());
                        if (netCallback != null){
                            netCallback.onFail(error.getMessage());
                        }

                    }
                }

        );
    }
}

