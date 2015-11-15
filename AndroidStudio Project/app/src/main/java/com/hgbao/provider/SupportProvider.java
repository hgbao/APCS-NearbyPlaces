package com.hgbao.provider;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.hgbao.vngfresher.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public final class SupportProvider {
    //Current status
    public static boolean isNetworkConnected(Activity context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        return (info != null && info.isConnectedOrConnecting());
    }

    //Position
    public static LatLng getCurrentPosition(Activity context) {
        Toast toastErrorGPS = Toast.makeText(context, context.getResources().getString(R.string.gps_error), Toast.LENGTH_SHORT);
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            toastErrorGPS.show();
        } else {
            LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                toastErrorGPS.show();
            Location lastLocation = manager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            if (lastLocation != null)
                return new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        }
        return new LatLng(DataProvider.DEFAULT_LATITUDE, DataProvider.DEFAULT_LONGITUDE);
    }

    public static String getCurrentAddress(Activity context, LatLng position) {
        String result = context.getResources().getString(R.string.data_error);
        try {
            Geocoder geo = new Geocoder(context, Locale.getDefault());
            List<Address> list = geo.getFromLocation(position.latitude, position.longitude, 1);
            if (!list.isEmpty()) {
                Address address = list.get(0);
                result = address.getAddressLine(0) + ", " + address.getAdminArea() + ", " + address.getCountryName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    //Bitmap
    public static Bitmap getBitmapFromURL(String link) {
        try {
            URL url = new URL(link);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(DataProvider.TIME_OUT_REQUEST);
            connection.setReadTimeout(DataProvider.TIME_OUT_REQUEST);
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
