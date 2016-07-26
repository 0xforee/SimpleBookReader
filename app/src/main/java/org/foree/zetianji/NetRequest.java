package org.foree.zetianji;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;


/**
 * Created by foree on 16-7-21.
 */
public class NetRequest {
    public static void getHtml(String url, String charSet, final NetCallback netCallback) {
        NetWorkApiHelper.newInstance().getRequest(url, charSet, new Response.Listener<String>() {
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

