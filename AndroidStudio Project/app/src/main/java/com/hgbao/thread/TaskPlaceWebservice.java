package com.hgbao.thread;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Base64;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.hgbao.model.Place;
import com.hgbao.provider.DataProvider;
import com.hgbao.vngfresher.R;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class TaskPlaceWebservice extends AsyncTask<Double, Void, Boolean> {
    Activity context;
    GoogleMap map;
    String key;
    ProgressDialog progressDialog;

    //long timeStart;

    public TaskPlaceWebservice(Activity context, GoogleMap map, String key) {
        this.context = context;
        this.map = map;
        this.key = key;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(context.getResources().getString(R.string.progress_loading));
        progressDialog.show();
        DataProvider.list_place.clear();
        //timeStart = System.currentTimeMillis();
    }

    @Override
    protected Boolean doInBackground(Double... params) {
        try {
            String namespace = DataProvider.WEBSERVICE_NAMESPACE;
            String method = DataProvider.WEBSERVICE_METHOD_GET;
            String action = namespace + method;
            SoapObject request = new SoapObject(namespace, method);
            request.addProperty(DataProvider.WEBSERVICE_PARAMETER_LATITUDE, params[0] + "");
            request.addProperty(DataProvider.WEBSERVICE_PARAMETER_LONGITUDE, params[1] + "");
            request.addProperty(DataProvider.WEBSERVICE_PARAMETER_RADIUS, ((int) params[2].doubleValue()) + "");
            request.addProperty(DataProvider.WEBSERVICE_PARAMETER_KEY, key);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

            HttpTransportSE transport = new HttpTransportSE(DataProvider.WEBSERVICE_URL);
            transport.call(action, envelope);
            SoapPrimitive objResult = (SoapPrimitive) envelope.getResponse();

            JSONObject dataResponse = new JSONObject(objResult.toString());
            JSONArray dataPlace = dataResponse.getJSONArray("results");
            for (int i = 0; i < dataPlace.length(); i++) {
                JSONObject jsonObject = dataPlace.getJSONObject(i);
                Place place = new Place();
                place.setId(jsonObject.getString("place_id"));
                place.setName(jsonObject.getString("name"));
                place.setLatitude(jsonObject.getDouble("lat"));
                place.setLongitude(jsonObject.getDouble("lng"));
                //Get the data that may not have
                String error = context.getResources().getString(R.string.data_error);
                //Rating
                if (jsonObject.has("rating"))
                    place.setRating(jsonObject.getDouble("rating"));
                else
                    place.setRating(0);
                //Address
                if (jsonObject.has("formatted_address"))
                    place.setAddress(jsonObject.getString("formatted_address"));
                else
                    place.setAddress(error);
                // Phone
                if (jsonObject.has("formatted_phone_number"))
                    place.setPhone(jsonObject.getString("formatted_phone_number"));
                else
                    place.setPhone(error);
                //Photo
                if (jsonObject.has("photo_reference"))
                    place.setPhotoReference(jsonObject.getString("photo_reference"));
                place.setStatus(0);
                DataProvider.list_place.add(place);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        progressDialog.cancel();
        if (result && !DataProvider.list_place.isEmpty()) {
            //Log.i("TAG_WEBSERVICE", (System.currentTimeMillis() - timeStart) + "");
            new TaskDrawMarkers(context, map).execute();
        } else {
            Toast.makeText(context, context.getResources().getString(R.string.loading_error), Toast.LENGTH_LONG).show();
        }
    }
}
