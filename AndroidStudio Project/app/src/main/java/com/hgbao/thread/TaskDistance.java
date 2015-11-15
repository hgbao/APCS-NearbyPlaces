package com.hgbao.thread;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.hgbao.model.Place;
import com.hgbao.provider.DataProvider;
import com.hgbao.vngfresher.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class TaskDistance extends AsyncTask<Double, Void, String> {
    Activity context;
    TextView txtDistance;

    public TaskDistance(Activity context, TextView txtDistance) {
        this.context = context;
        this.txtDistance = txtDistance;
    }

    @Override
    protected String doInBackground(Double... params) {
        String result = "";
        try {
            //Create the url request
            StringBuilder urlPlaceAPI = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");
            urlPlaceAPI.append("origin=" + params[0] + ",%20" + params[1]);
            urlPlaceAPI.append("&destination=" + params[2] + ",%20" + params[3]);
            urlPlaceAPI.append("&key=" + context.getResources().getString(R.string.BROWSER_KEY));

            //Get the data
            InputStreamReader reader = new InputStreamReader(new URL(urlPlaceAPI.toString()).openStream(), "UTF-8");
            BufferedReader br = new BufferedReader(reader);
            String line = br.readLine();
            StringBuilder strBuilder = new StringBuilder();
            while (line != null) {
                strBuilder.append(line);
                line = br.readLine();
            }
            br.close();
            reader.close();

            //Convert data to Json
            JSONObject dataResponse = new JSONObject(strBuilder.toString());
            result = dataResponse.getJSONArray("routes").getJSONObject(0)
                    .getJSONArray("legs").getJSONObject(0)
                    .getJSONObject("distance").getString("text");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (!result.isEmpty()) {
            txtDistance.setText(result);
        }
    }
}
