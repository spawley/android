package com.example.project;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.identity.intents.Address;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.R.attr.radius;
import static com.example.project.R.id.add;
import static com.example.project.R.id.map;
import static java.security.AccessController.getContext;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Circle circle;
    SQLiteDatabase myDB= null;

    String userAddress = "";

    Dialog addressDialog = null;
    List<android.location.Address> addresses;

    List<Marker> marker = new ArrayList<Marker>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng winnipeg = new LatLng(49.8731644, -97.1439138);

        //Map settings
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(winnipeg, 10.5f));
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);

        //Database
        myDB = this.openOrCreateDatabase("ProjectDB", MODE_PRIVATE, null);
    }

    public void onMapSearch(View v) {

        EditText address = (EditText)findViewById(R.id.addressInput);
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {

            addresses = geocoder.getFromLocationName(address.getText().toString(), 1);

            if(addresses.size() > 0) {

              if(addresses.get(0).getLocality() != null) {

                  if (addresses.get(0).getLocality().equals("Winnipeg")) {

                      mMap.clear();

                      double latitude = addresses.get(0).getLatitude();
                      double longitude = addresses.get(0).getLongitude();

                      if (!marker.isEmpty()) {

                          marker.remove(0);

                      }

                      userAddress = addresses.get(0).getAddressLine(0);

                      marker.add(new Marker("Marker in Winnipeg", "Location", latitude, longitude));

                      mMap.addMarker(
                              new MarkerOptions()
                                      .position(marker.get(0).coordinates)
                                      .title(marker.get(0).title)
                                      .snippet(marker.get(0).snippet)
                      );

                      mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.get(0).coordinates, 12.5f), 1000, new GoogleMap.CancelableCallback() {
                          @Override
                          public void onFinish() {

                              ViewDialog alert = new ViewDialog();
                              alert.showConfirmationDialog(MapsActivity.this, addresses.get(0).getAddressLine(0) + ", Winnipeg");
                          }

                          @Override
                          public void onCancel() {

                          }
                      });

                      Log.d("lat", Double.toString(latitude));
                      Log.d("long", Double.toString(longitude));

                      Location loc1 = new Location("");
                      loc1.setLatitude(latitude);
                      loc1.setLongitude(longitude);


                      //The location sent in the api call
                      Location loc2 = new Location("");
                      loc2.setLatitude(49.8708166);
                      loc2.setLongitude(-97.0925137);

                      float distanceInMeters = loc1.distanceTo(loc2);

                      Log.d("test distance", Float.toString(distanceInMeters));

                  } else {

                      Toast.makeText(getApplicationContext(), "Hint: Include 'Winnipeg' in your address. For example: 123 Fake St, Winnipeg", Toast.LENGTH_LONG).show();

                  }
              }
                else {

                  Toast.makeText(getApplicationContext(), "Hint: Include 'Winnipeg' in your address. For example: 123 Fake St, Winnipeg", Toast.LENGTH_LONG).show();

              }
            }
            else {
                Toast.makeText(getApplicationContext(), "Sorry! Please try entering your address again", Toast.LENGTH_LONG).show();

            }
        }
            catch (IOException e) {

            }
    }


    /*
        Handles the custom "Confirmation Dialog"
     */
    public class ViewDialog {

        public void showConfirmationDialog(Activity activity, String msg){

            addressDialog = new Dialog(activity);
            final Dialog addressDialog = new Dialog(activity);
            addressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            addressDialog.setCancelable(false);
            addressDialog.setContentView(R.layout.custom_dialog);

            TextView text = (TextView) addressDialog.findViewById(R.id.top_text);
            text.setText(msg);
            text.setTextColor(Color.BLACK);

            Window window = addressDialog.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();

            wlp.gravity = Gravity.BOTTOM;
            wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(wlp);

            Button btnYes = (Button) addressDialog.findViewById(R.id.btn_dialog_yes);
            Button btnNo = (Button) addressDialog.findViewById(R.id.btn_dialog_no);


            btnYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addressDialog.dismiss();

                    ViewDialog alert = new ViewDialog();
                    alert.showRadiusDialog(MapsActivity.this);

                }
            });

            btnNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addressDialog.dismiss();
                }
            });

            addressDialog.show();

        }

        /*
            Handles the custom "Show Radius Dialog"
        */
        public void showRadiusDialog(Activity activity){

            final Dialog dialog = new Dialog(activity);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.custom_radius_dialog);

            EditText address = (EditText)findViewById(R.id.addressInput);
            Button btnSearch = (Button) findViewById(R.id.search_button);

            address.setVisibility(View.GONE);
            btnSearch.setVisibility(View.GONE);

            TextView text = (TextView) dialog.findViewById(R.id.top_text_radius);
            text.setTextColor(Color.BLACK);

            Window window = dialog.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();

            wlp.gravity = Gravity.BOTTOM;
            wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(wlp);

            final TextView seekbarProgress = (TextView) dialog.findViewById(R.id.seek_bar_radius_display);

            circle = mMap.addCircle(new CircleOptions()
                    .center(marker.get(0).coordinates)
                    .strokeWidth(2)
                    .strokeColor(Color.BLUE)
                    .fillColor(Color.parseColor("#500084d3"))
                    .radius(500));


            SeekBar mSeekbar = (SeekBar) dialog.findViewById(R.id.seek1);


            //Event listener
            mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
            {
                //Flags
                boolean zoomOut = false;
                boolean zoomOut2 = false;

                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
                {
                    seekbarProgress.setText(Integer.toString(progress + 500) + " Meters");

                    circle.setRadius(progress + 500);

                    if(progress > 500){

                        if(!zoomOut){
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.get(0).coordinates, 12.0f), 5, null);
                            zoomOut = true;
                        }
                    }

                    if(progress > 1000){

                        if(!zoomOut2){
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.get(0).coordinates, 10.5f), 5, null);
                            zoomOut2 = true;
                        }
                    }

                    if(progress < 1000){

                        if(zoomOut2){
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.get(0).coordinates, 12.0f), 5, null);

                            zoomOut2 = false;
                        }
                    }

                    if(progress < 500){

                        if(zoomOut){
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.get(0).coordinates, 12.5f), 5, null);

                            zoomOut = false;
                        }
                    }
                }

                public void onStartTrackingTouch(SeekBar seekBar) {}

                public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            Button dialogButton = (Button) dialog.findViewById(R.id.btn_dialog_confirm);
            Button dialogButton2 = (Button) dialog.findViewById(R.id.btn_dialog_back);

            //Button Event Listener
            dialogButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();

                    myDB.execSQL("INSERT INTO Radius (RADIUS)"
                            + " VALUES (" + (circle.getRadius()) + ");");

                    int id = 0;

                    try
                    {
                        Cursor c = null;
                        c = myDB.rawQuery("SELECT ID FROM Radius ORDER BY ID DESC LIMIT 1",null);
                        c.moveToFirst();
                        id = c.getInt(c.getColumnIndex("ID"));
                        c.close();

                        Log.d("row id", Double.toString(marker.get(0).coordinates.latitude));

                        myDB.execSQL("INSERT INTO User (RADIUS_ID, ADDRESS, LATITUDE, LONGITUDE)"
                                + " VALUES(" + id + ", '" + userAddress +"', " + marker.get(0).coordinates.latitude + ", " + marker.get(0).coordinates.longitude +  ");");

                    }
                    catch(Exception e)
                    {
                        Log.d("USERERROR",e.toString());
                    }

                    ViewDialog alert = new ViewDialog();

                    //Lanuch Dialog
                    alert.showEndDialog(MapsActivity.this);
                }
            });

            //Button Event Listener
            dialogButton2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    circle.remove();
                    ViewDialog alert = new ViewDialog();
                    alert.showConfirmationDialog(MapsActivity.this, addresses.get(0).getAddressLine(0) + ", Winnipeg");

                }
            });

            //Show Dialog
            dialog.show();

        }

        /*
            Handles the custom "Show End Dialog"
        */
        public void showEndDialog(Activity activity){

            addressDialog = new Dialog(activity);
            final Dialog addressDialog = new Dialog(activity);
            addressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            addressDialog.setCancelable(false);
            addressDialog.setContentView(R.layout.custom_end_dialog);

            TextView text = (TextView) addressDialog.findViewById(R.id.top_text);
            text.setTextColor(Color.BLACK);

            Window window = addressDialog.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();

            wlp.gravity = Gravity.BOTTOM;
            wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(wlp);

            Button btnOK = (Button) addressDialog.findViewById(R.id.btn_dialog_ok);

            //Button Event Listener
            btnOK.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addressDialog.dismiss();

                    Intent intent = new Intent(MapsActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            });

            //Show Dialog
            addressDialog.show();

        }
    }

}
