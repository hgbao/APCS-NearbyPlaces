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

public class TaskPlaceDetail extends AsyncTask<Place, Void, Boolean> {
    Activity context;
    ImageView imgAvatar;
    TextView txtRating, txtAddress, txtPhone;
    RatingBar ratingBar;

    Place place;
    ProgressDialog progressDialog;

    public TaskPlaceDetail(Activity context, ImageView imgAvatar, TextView txtRating, TextView txtAddress, TextView txtPhone, RatingBar ratingBar) {
        this.context = context;
        this.imgAvatar = imgAvatar;
        this.txtRating = txtRating;
        this.txtAddress = txtAddress;
        this.txtPhone = txtPhone;
        this.ratingBar = ratingBar;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(context.getResources().getString(R.string.progress_loading));
        progressDialog.show();
    }

    @Override
    protected Boolean doInBackground(Place... params) {
        //Get the place in list
        for (int i = 0; i < DataProvider.list_place.size(); i++){
            place = DataProvider.list_place.get(i);
            if (place.getId().equalsIgnoreCase(params[0].getId()))
                break;
        }
        //Load place
        if (place.getStatus() == -1) {
            try {
                //Create the url request
                StringBuilder urlPlaceAPI = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?");
                urlPlaceAPI.append("placeid=" + place.getId());
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
                JSONObject jsonObject = dataResponse.getJSONObject("result");
                String error = context.getResources().getString(R.string.data_error);
                //Rating
                if (jsonObject.has("rating"))
                    place.setRating(jsonObject.getDouble("rating"));
                else
                    place.setRating(0);
                // Phone
                if (jsonObject.has("formatted_phone_number"))
                    place.setPhone(jsonObject.getString("formatted_phone_number"));
                else
                    place.setPhone(error);
                //Address
                if (jsonObject.has("formatted_address"))
                    place.setAddress(jsonObject.getString("formatted_address"));
                else
                    place.setAddress(error);
                //Photo
                if (jsonObject.has("photos"))
                    place.setPhotoReference(jsonObject.getJSONArray("photos").getJSONObject(0).getString("photo_reference"));

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        progressDialog.cancel();
        if (result) {
            txtAddress.setText(place.getAddress());
            txtPhone.setText(place.getPhone());
            txtRating.setText(place.getRating() + "");
            ratingBar.setRating((float) place.getRating());
            //Load avatar
            new TaskLoadImage(context, imgAvatar).execute(place);
            place.setStatus(0);
        } else {
            Toast.makeText(context, context.getResources().getString(R.string.loading_error), Toast.LENGTH_LONG).show();
        }
    }
}
