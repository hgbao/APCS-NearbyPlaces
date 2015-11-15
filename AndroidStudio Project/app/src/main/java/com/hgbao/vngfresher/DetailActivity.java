package com.hgbao.vngfresher;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.hgbao.model.Place;
import com.hgbao.provider.DataProvider;
import com.hgbao.thread.TaskDistance;
import com.hgbao.thread.TaskLoadImage;
import com.hgbao.thread.TaskPlaceDetail;

import java.text.DecimalFormat;

public class DetailActivity extends AppCompatActivity {
    Place place;
    double curLat, curLng;

    ImageView imgAvatar;
    TextView txtDistance, txtRating, txtAddress, txtPhone;
    RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        place = (Place) getIntent().getSerializableExtra(DataProvider.EXTRA_PLACE);
        curLat = getIntent().getDoubleExtra(DataProvider.EXTRA_LATITUDE, DataProvider.DEFAULT_LATITUDE);
        curLng = getIntent().getDoubleExtra(DataProvider.EXTRA_LONGITUDE, DataProvider.DEFAULT_LONGITUDE);

        if (place != null) {
            addControl();
            addEvent();
            if (place.getStatus() == -1)
                new TaskPlaceDetail(DetailActivity.this, imgAvatar, txtRating, txtAddress, txtPhone, ratingBar).execute(place);
            if (place.getStatus() == 0)
                new TaskLoadImage(DetailActivity.this, imgAvatar).execute(place);
            new TaskDistance(DetailActivity.this, txtDistance)
                    .execute(new Double[]{curLat, curLng, place.getLatitude(), place.getLongitude()});
        }
    }

    private void addControl() {
        //Action bar
        setSupportActionBar((Toolbar) findViewById(R.id.toolbarDetail));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(place.getName());

        //Controls
        imgAvatar = (ImageView) findViewById(R.id.imgPlaceAvatar);
        txtDistance = (TextView) findViewById(R.id.txtPlaceDistance);
        txtRating = (TextView) findViewById(R.id.txtPlaceRating);
        txtAddress = (TextView) findViewById(R.id.txtPlaceAddress);
        txtPhone = (TextView) findViewById(R.id.txtPlacePhone);
        ratingBar = (RatingBar) findViewById(R.id.ratingBarPlace);
    }

    private void addEvent() {
        txtAddress.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                StringBuilder uriDirection = new StringBuilder("http://maps.google.com/maps?");
                uriDirection.append("saddr=" + curLat + "," + curLng);
                uriDirection.append("&daddr=" + place.getLatitude() + "," + place.getLongitude());
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uriDirection.toString()));
                startActivity(intent);
                return true;
            }
        });

        txtPhone.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!place.getPhone().isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + place.getPhone().replace(" ", "")));
                    startActivity(intent);
                }
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_slide_right_1, R.anim.activity_slide_right_2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.activity_slide_right_1, R.anim.activity_slide_right_2);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
