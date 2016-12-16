package com.example.project;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import static com.example.project.R.id.map;

public class DisplayMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    SQLiteDatabase myDB= null;

    private GoogleMap mMap;
    private Circle circle;

    int radius_value   = 0;
    int radius_id      = 0;

    double latitude    = 0;
    double longitude   = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        myDB = this.openOrCreateDatabase("ProjectDB", MODE_PRIVATE, null);

        if(myDB.isOpen()) {
            Log.d("STATUS321","OPEN");
        }
        else {
            Log.d("STATUS321","LOCKED");
        }

        try
        {
            int count = -1;

            Cursor c = null;

            String query = "SELECT COUNT(*) FROM User";
            c = myDB.rawQuery(query, null);

            c.moveToFirst();
            count = c.getInt(0);
            c.close();

            //Check if User exists
            if(count > 0) {

                int id = 0;
                c = myDB.rawQuery("SELECT ID FROM User ORDER BY ID DESC LIMIT 1", null);
                c.moveToFirst();
                id = c.getInt(c.getColumnIndex("ID"));
                c.close();

                c = myDB.rawQuery("SELECT RADIUS_ID, LATITUDE, LONGITUDE FROM User WHERE ID = ?", new String[]{Integer.toString(id)});
                c.moveToFirst();
                radius_id = c.getInt(c.getColumnIndex("RADIUS_ID"));
                latitude = c.getDouble(c.getColumnIndex("LATITUDE"));
                longitude = c.getDouble(c.getColumnIndex("LONGITUDE"));

                c.close();

                c = myDB.rawQuery("SELECT RADIUS FROM Radius WHERE ID = ?", new String[]{Integer.toString(radius_id)});
                c.moveToFirst();
                radius_value = c.getInt(c.getColumnIndex("RADIUS"));
                c.close();

            }
            else {
                Log.d("Missing User","Add user first");
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void testMethod(View v) {

        showCrimes(Integer.parseInt(v.getTag().toString()));
    }

    public void showCrimes(int value) {

        try
        {
            mMap.clear();

            circle = mMap.addCircle(new CircleOptions()
                    .center(new LatLng(latitude, longitude))
                    .strokeWidth(2)
                    .strokeColor(Color.BLUE)
                    .fillColor(Color.parseColor("#500084d3"))
                    .radius(radius_value));

            int count = -1;

            Cursor c = null;
            String query = "SELECT * FROM Crime WHERE DATE >= date('now','-" + Integer.toString(value) +" day')";
            c = myDB.rawQuery(query, null);
            c.moveToFirst();

            Log.d("Count123", Integer.toString(c.getCount()));

            for (int i = 0; i < c.getCount(); i++) {

                Cursor c2 = null;

                query = "SELECT * FROM Crime_Location WHERE ID = ?";
                c2 = myDB.rawQuery(query, new String[]{Integer.toString(c.getInt(c.getColumnIndex("CRIME_LOCATION_ID")))});
                c2.moveToFirst();

                MarkerOptions Marker = new MarkerOptions();

                int icon = 0;

                //Setup Crime Icon
                switch(c.getString(c.getColumnIndex("TYPE"))){
                    case "Theft Motor Vehicle - Actual": icon = R.drawable.car;
                        break;
                    case "Robbery - Non-Commercial/Financial": icon = R.drawable.theft;
                        break;
                    case "Break & Enters - Commercial": icon = R.drawable.theft;
                        break;
                    case "Break & Enters - Other": icon = R.drawable.house;
                        break;
                    case "Theft Motor Vehicle - Attempt Only": icon = R.drawable.car;
                        break;
                    case "Sexual Assault": icon = R.drawable.robbery;
                        break;
                    case "Homicide": icon = R.drawable.robbery;
                        break;
                    case "Shooting": icon = R.drawable.shooting;
                        break;
                    case "Robbery - Commercial/Financial": icon = R.drawable.theft;
                        break;
                    case "Break & Enters - Residential": icon = R.drawable.house;
                        break;
                    default:
                        Log.e("", "no case");
                        icon = R.drawable.car;
                        return;
                }

                Log.e("LAT", c2.getString(c2.getColumnIndex("LATITUDE")));
                Log.e("LONG", c2.getString(c2.getColumnIndex("LONGITUDE")));
                Log.e("TYPE", c.getString(c.getColumnIndex("TYPE")));

                //Setup Marker
                Marker
                        .position(new LatLng(c2.getDouble(c2.getColumnIndex("LATITUDE")), c2.getDouble(c2.getColumnIndex("LONGITUDE"))))
                        .title(c.getString(c.getColumnIndex("TYPE")))
                        .icon(BitmapDescriptorFactory.fromResource(icon));

                //Add Marker
                mMap.addMarker(Marker);

                c.moveToNext();
                c2.close();
            }

            c.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng winnipeg = new LatLng(49.8731644, -97.1439138);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(winnipeg, 10.5f));
        mMap.getUiSettings().setTiltGesturesEnabled(false);

        this.showCrimes(7);
    }
}
