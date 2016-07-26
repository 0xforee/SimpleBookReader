package org.foree.zetianji;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by foree on 16-7-18.
 */
public class NetWorkApiHelper {
    private static NetWorkApiHelper INSTANCE = null;
    RequestQueue queue;

    public static NetWorkApiHelper newInstance() {

        if ( INSTANCE == null)
            INSTANCE = new NetWorkApiHelper();

        return INSTANCE;
    }

    public NetWorkApiHelper(){
        queue = Volley.newRequestQueue(BaseApplication.getInstance().getApplicationContext());
    }

    public void getRequest(String requestUrl, final Map<String, String> headers, Response.Listener<String> listener, Response.ErrorListener errorListener){
        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl, listener, errorListener){
        @Override
        protected Response<String> parseNetworkResponse(NetworkResponse response) {
            String str = null;
            try {
                // TODO:增加手动切换编码或者自动获取编码的功能
                str = new String(response.data, "utf-8");
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
