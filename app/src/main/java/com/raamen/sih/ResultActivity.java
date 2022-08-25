package com.raamen.sih;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();
        if (intent != null) {
            ArrayList<ArrayList<Double>> str = (ArrayList<ArrayList<Double>>) intent.getSerializableExtra("avg");

            Log.i("helloavg", str.toString());
            //System.out.println(str);
            String url = "https://visara-api.herokuapp.com/";

            try {
                JSONArray arr = new JSONArray();

                double sumr = 0, sumg = 0, sumb = 0, sdr = 0, sdg = 0, sdb = 0;
                for (ArrayList<Double> d: str) {
                    sumr += d.get(0);
                    sdr += d.get(1);
                    sumg += d.get(2);
                    sdg += d.get(3);
                    sumb += d.get(4);
                    sdb += d.get(5);
                }

                arr.put((double) sumr/str.size());
                arr.put((double) sdr/str.size());
                arr.put((double) sumg/str.size());
                arr.put((double) sdg/str.size());
                arr.put((double) sumb/str.size());
                arr.put((double) sdb/str.size());


                Log.i("hellojson", arr.toString());
                JSONObject jsonParams = new JSONObject();
                jsonParams.put("images", arr);
                Log.i("hellojson", jsonParams.toString());

                // Building a request
                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        // Using a variable for the domain is great for testing
                        url,
                        jsonParams,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.i("helloapi", response.toString());
                                // Handle the response

                            }
                        },

                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.i("helloerror", error.getMessage());
                                // Handle the error

                            }
                        })  {@Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/json");
                    return params;
                } };

            /*

              For the sake of the example I've called newRequestQueue(getApplicationContext()) here
              but the recommended way is to create a singleton that will handle this.

              Read more at : https://developer.android.com/training/volley/requestqueue

              Category -> Use a singleton pattern

            */
                Volley.newRequestQueue(getApplicationContext()).
                        add(request);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }


    }
}