package encomenda.doces;

import android.app.Activity;
import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FcmNotificationSender {

    String userFcmToken;
    String titulo;
    String texto;
    Context context;
    Activity activity;

    private RequestQueue requestQueue;
    private final String postUrl = "https://fcm.googleapis.com/fcm/send";
    private final String fcmServerKey ="AAAAF7YHvS0:APA91bFuwUD-KwkzwFg6OGHtxeVovU3m4efiXsIPTYe-1H8HloCAVzk6bQJ74Kk5NHTh225wZPtHUWTASrUJF_yCcasoAaEc9LASy5E_J0Z5gx0XRPO_tW-lytES9TexCBH4BDHnoBdi";


    public FcmNotificationSender(String userFcmToken, String titulo, String texto, Context context, Activity activity) {
        this.userFcmToken = userFcmToken;
        this.titulo = titulo;
        this.texto = texto;
        this.context = context;
        this.activity = activity;
    }

    public void sendNotifications() {

        requestQueue = Volley.newRequestQueue(activity);
        JSONObject mainObj = new JSONObject();
        try {
            mainObj.put("to", userFcmToken);
            JSONObject notiObject = new JSONObject();
            notiObject.put("title", titulo);
            notiObject.put("body", texto);
            notiObject.put("icon", R.drawable.logo);



            mainObj.put("notification", notiObject);


            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl, mainObj, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                    // code run is got response

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // code run is got error

                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {


                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "key=" + fcmServerKey);
                    return header;


                }
            };
            requestQueue.add(request);


        } catch (JSONException e) {
            e.printStackTrace();
        }




    }
}

