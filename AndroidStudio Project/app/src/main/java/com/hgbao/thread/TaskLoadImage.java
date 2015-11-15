package com.hgbao.thread;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hgbao.model.Place;
import com.hgbao.provider.DataProvider;
import com.hgbao.provider.SupportProvider;
import com.hgbao.vngfresher.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class TaskLoadImage extends AsyncTask<Place, Void, Boolean> {
    Activity context;
    ImageView imgAvatar;

    Place place;
    ProgressDialog progressDialog;

    public TaskLoadImage(Activity context, ImageView imgAvatar) {
        this.context = context;
        this.imgAvatar = imgAvatar;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getResources().getString(R.string.progress_loading));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected Boolean doInBackground(Place... params) {
        place = params[0];
        try {
            String photoRef = place.getPhotoReference();
            if (!photoRef.isEmpty()) {
                StringBuilder urlPhotoApi = new StringBuilder("https://maps.googleapis.com/maps/api/place/photo?");
                urlPhotoApi.append("maxheight=" + ((int) context.getResources().getDimension(R.dimen.avatar_height)));
                urlPhotoApi.append("&photoreference=" + photoRef);
                urlPhotoApi.append("&key=" + context.getResources().getString(R.string.SERVER_KEY));
                //Get the data
                Bitmap avatar = SupportProvider.getBitmapFromURL(urlPhotoApi.toString());
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                avatar.compress(Bitmap.CompressFormat.PNG, 100, stream);
                place.setAvatar(stream.toByteArray());
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
        if (result) {
            if (place.getAvatar() != null) {
                imgAvatar.setImageBitmap(BitmapFactory.decodeStream(new ByteArrayInputStream(place.getAvatar())));
            }
            place.setStatus(1);
        } else {
            Toast.makeText(context, context.getResources().getString(R.string.loading_error), Toast.LENGTH_LONG).show();
        }
    }
}
