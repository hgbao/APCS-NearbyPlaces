package com.hgbao.provider;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.model.Marker;
import com.hgbao.model.Place;

import java.util.ArrayList;

public final class DataProvider {
    //Extras
    public final static String EXTRA_PLACE = "EXTRA_PLACE";
    public final static String EXTRA_LATITUDE = "EXTRA_LATITUDE";
    public final static String EXTRA_LONGITUDE = "EXTRA_LONGITUDE";

    //Default values
    public final static double DEFAULT_LATITUDE = 10.7627166;
    public final static double DEFAULT_LONGITUDE = 106.6823101;
    public final static float MAP_ZOOM_ALL = 15;
    public final static float MAP_ZOOM_CURRENT = 17;

    //Webservice
    public final static String WEBSERVICE_NAMESPACE = "http://tempuri.org/";
    public final static String WEBSERVICE_URL = "http://www.hgbaotest.somee.com/vngfresherwebservice.asmx";
    public final static String WEBSERVICE_PARAMETER_LATITUDE= "latitude";
    public final static String WEBSERVICE_PARAMETER_LONGITUDE = "longitude";
    public final static String WEBSERVICE_PARAMETER_RADIUS = "radius";
    public final static String WEBSERVICE_PARAMETER_KEY = "key";
    public final static String WEBSERVICE_METHOD_GET = "getNearbyPlaces";

    //Request time
    public final static int TIME_OUT_REQUEST = 5000;

    //Attributes
    public final static ArrayList<Place> list_place = new ArrayList<>();
    public final static ArrayList<Marker> list_marker = new ArrayList<>();

    //Shared Preference
    public final static String SHARED_PREFERENCE = "mySharedPreference";
    public final static String PREF_NOTIFICATION = "NOTIFICATION";//0, 1\
    public final static String PREF_CURRENT_VERSION = "CURRENT_VERSION";
    public final static String PREF_UPDATE_AVAILABLE = "UPDATE_AVAILABLE";//0, 1
    public static long CURRENT_VERSION;
    public static long LATEST_VERSION;

    public final static void setPreference(Context context, String refName, String refValue) {
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCE, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(refName, refValue);
        editor.commit();
    }

    public final static String getReference(Context context, String refName) {
        SharedPreferences pref = context.getSharedPreferences(SHARED_PREFERENCE, context.MODE_PRIVATE);
        return pref.getString(refName, "");
    }
}