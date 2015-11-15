package com.hgbao.thread;

import android.app.Activity;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hgbao.model.Place;
import com.hgbao.provider.DataProvider;

import java.util.ArrayList;

public class TaskDrawMarkers extends AsyncTask<Void, MarkerOptions, Void> {
    Activity context;
    GoogleMap map;

    public TaskDrawMarkers(Activity context, GoogleMap map) {
        this.context = context;
        this.map = map;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        for (int i = 0; i < DataProvider.list_marker.size(); i++){
            DataProvider.list_marker.get(i).remove();
        }
        DataProvider.list_marker.clear();
    }

    @Override
    protected Void doInBackground(Void... params) {
        ArrayList<Place> list = DataProvider.list_place;
        for (int i = 0; i < list.size(); i++) {
            Place place = list.get(i);
            //Create marker
            MarkerOptions option = new MarkerOptions();
            option.position(new LatLng(place.getLatitude(), place.getLongitude()));
            option.snippet(place.getId());
            publishProgress(option);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(MarkerOptions... values) {
        super.onProgressUpdate(values);
        if (map != null) {
            Marker marker = map.addMarker(values[0]);
            marker.hideInfoWindow();
            DataProvider.list_marker.add(marker);
        }
    }
}
