package com.hgbao.vngfresher;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hgbao.model.Place;
import com.hgbao.provider.DataProvider;
import com.hgbao.provider.SupportProvider;
import com.hgbao.thread.TaskPlaceGoogle;
import com.hgbao.thread.TaskPlaceWebservice;

public class MainActivity extends AppCompatActivity {
    DrawerLayout drawerLayout;
    FloatingActionButton fabGoogle, fabWebservice;

    GoogleMap map;
    LatLng position;
    Marker positionMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addControl();
        addGoogleMap();
        addEvent();
    }

    private void addControl() {
        //Action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.title_activity_main));
        //Navigation drawer
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayoutMain);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();
        //Navigation menu
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigationViewMain);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.navigation_exit:
                        exit();
                }
                return true;
            }
        });

        //Floating action button
        fabGoogle = (FloatingActionButton) findViewById(R.id.fabGoogle);
        fabWebservice = (FloatingActionButton) findViewById(R.id.fabWebservice);
    }

    private void addGoogleMap() {
        //Current position
        position = SupportProvider.getCurrentPosition(MainActivity.this);
        //Google map
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.mapMain)).getMap();
        if (map != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, DataProvider.MAP_ZOOM_ALL));
            map.getUiSettings().setAllGesturesEnabled(true);
            map.setMyLocationEnabled(true);
            //Draw markers

            MarkerOptions options = new MarkerOptions();
            options.position(position);
            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_current));
            positionMarker = map.addMarker(options);
        }
    }

    private void addEvent() {
        if (map != null) {
            map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    position = SupportProvider.getCurrentPosition(MainActivity.this);
                    positionMarker.setPosition(position);
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, DataProvider.MAP_ZOOM_CURRENT));
                    return true;
                }
            });

            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                    for (int i = 0; i < DataProvider.list_place.size(); i++) {
                        Place place = DataProvider.list_place.get(i);
                        if (marker.getSnippet().equalsIgnoreCase(place.getId())) {
                            intent.putExtra(DataProvider.EXTRA_PLACE, place);
                            break;
                        }
                    }
                    startActivity(intent);
                    overridePendingTransition(R.anim.activity_slide_left_1, R.anim.activity_slide_left_2);
                    return true;
                }
            });
        }

        fabGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search(true);
            }
        });

        fabWebservice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search(false);
            }
        });
    }

    private void search(final boolean isGoogle) {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.custom_dialog_search);
        dialog.setTitle(getResources().getString(R.string.dialog_search_title));
        //Controls
        final TextView txtAddress = (TextView) dialog.findViewById(R.id.txtSearchAddress);
        final EditText txtRadius = (EditText) dialog.findViewById(R.id.txtSearchRadius);
        Button btnYes = (Button) dialog.findViewById(R.id.btnSearchYes);
        Button btnNo = (Button) dialog.findViewById(R.id.btnSearchNo);
        //Events
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double radius = 1000;
                if (!txtRadius.getText().toString().isEmpty())
                    radius = Double.parseDouble(txtRadius.getText().toString());
                //Method
                if (isGoogle) {
                    new TaskPlaceGoogle(MainActivity.this, map).execute(new Double[]{position.latitude, position.longitude, radius});
                }else{
                    TaskPlaceWebservice task = new TaskPlaceWebservice(MainActivity.this, map, getResources().getString(R.string.SERVER_KEY));
                    task.execute(new Double[]{position.latitude, position.longitude, radius});
                }
                dialog.dismiss();
            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        //Data
        txtAddress.setText(SupportProvider.getCurrentAddress(MainActivity.this, position));

        dialog.show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            exit();
    }

    private void exit() {
        //Show dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.dialog_exit_title));
        builder.setMessage(getResources().getString(R.string.dialog_exit_message));
        builder.setPositiveButton(getResources().getString(R.string.dialog_exit_positive), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.dialog_exit_negative), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }
}
