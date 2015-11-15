package com.hgbao.thread;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
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

public class TaskPlaceGoogle extends AsyncTask<Double, Void, Boolean> {
    Activity context;
    GoogleMap map;
    ProgressDialog progressDialog;

    //long timeStart;

    public TaskPlaceGoogle(Activity context, GoogleMap map) {
        this.context = context;
        this.map = map;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getResources().getString(R.string.progress_loading));
        progressDialog.setCancelable(false);
        progressDialog.show();
        DataProvider.list_place.clear();
        //timeStart = System.currentTimeMillis();
    }

    @Override
    protected Boolean doInBackground(Double... params) {
        double latitude = params[0];
        double longitude = params[1];
        double radius = params[2];
        try {
            //Create the url request
            StringBuilder urlPlaceAPI = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
            urlPlaceAPI.append("location=" + latitude + "," + longitude);
            urlPlaceAPI.append("&radius=" + ((int) radius));
            urlPlaceAPI.append("&sensor=true");
            urlPlaceAPI.append("&key=" + context.getResources().getString(R.string.SERVER_KEY));

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
            JSONArray dataPlace = dataResponse.getJSONArray("results");
            for (int i = 0; i < dataPlace.length(); i++) {
                JSONObject jsonObject = dataPlace.getJSONObject(i);
                Place place = new Place();
                place.setId(jsonObject.getString("place_id"));
                place.setName(jsonObject.getString("name"));
                place.setLatitude(jsonObject.getJSONObject("geometry").getJSONObject("location").getDouble("lat"));
                place.setLongitude(jsonObject.getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
                DataProvider.list_place.add(place);
            }

        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        progressDialog.cancel();
        if (result && !DataProvider.list_place.isEmpty()) {
            //Log.i("TAG_JSON", (System.currentTimeMillis() - timeStart) + "");
            new TaskDrawMarkers(context, map).execute();
        }
        else {
            Toast.makeText(context, context.getResources().getString(R.string.loading_error), Toast.LENGTH_LONG).show();
        }
    }
}
