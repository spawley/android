package com.example.project;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

import static android.R.attr.button;

public class MainActivity extends AppCompatActivity {

    SQLiteDatabase myDB= null;
    CheckAPI mService;
    boolean mBound = false;
    ProgressDialog Dialog;


    private BroadcastReceiver  bReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            //put here whaterver you want your activity to do with the intent received

            Dialog.dismiss();
            unbindService(mConnection);

            //Initialize
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            //Intent
            Intent intent2 = new Intent(getApplicationContext(), OnAlarmReceive.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);

            // Getting current time with Calendar
            // add how many seconds in the future we want it to trigger
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, 10);

            //Will only trigger once (Non Repeating)
            //alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);


            if(prefs.getBoolean("notify_user", true)){

                // Hopefully your alarm will have a lower frequency than this!
                alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HALF_DAY,
                        AlarmManager.INTERVAL_HALF_DAY, pendingIntent);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

          /* Create a Database. */
        try {
            myDB = this.openOrCreateDatabase("ProjectDB", MODE_PRIVATE, null);

            /* Create a Table in the Database. */
            myDB.execSQL("CREATE TABLE IF NOT EXISTS Radius " +
                    "(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, RADIUS INTEGER )");

            myDB.execSQL("CREATE TABLE IF NOT EXISTS User " +
                    "(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, RADIUS_ID INTEGER, ADDRESS TEXT, LATITUDE FLOAT, LONGITUDE FLOAT, FOREIGN KEY(RADIUS_ID) REFERENCES Radius(ID) )");

            myDB.execSQL("CREATE TABLE IF NOT EXISTS Crime_Location " +
                    "(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, LATITUDE FLOAT, LONGITUDE FLOAT )");

            myDB.execSQL("CREATE TABLE IF NOT EXISTS Crime " +
                    "(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, CRIME_LOCATION_ID INTEGER, UNIQUE_ID INTEGER, TYPE TEXT, DATE DATE, FOREIGN KEY(CRIME_LOCATION_ID) REFERENCES Crime_Location(ID) )");

            /*retrieve data from database */
            Cursor c = null;

            String query = "SELECT COUNT(*) FROM User";

            c = myDB.rawQuery(query, null);
            c.moveToFirst();

            //Grab buttons
            Button displayLocation = (Button)findViewById(R.id.btnDisplayLocation);
            Button listView = (Button)findViewById(R.id.btnShowListView);
            Button setLocation = (Button)findViewById(R.id.btnPickLocation);

            //Apply style changes
            displayLocation.getBackground().setColorFilter(Color.parseColor("#3B73B3"), PorterDuff.Mode.MULTIPLY);
            listView.getBackground().setColorFilter(Color.parseColor("#3B73B3"), PorterDuff.Mode.MULTIPLY);
            setLocation.getBackground().setColorFilter(Color.parseColor("#3B73B3"), PorterDuff.Mode.MULTIPLY);

            //Change text
            if(c.getInt(0) > 0) {
                setLocation.setText("Change Location");
            }

            c.close();

        } catch (Exception e) {
            Log.d("MainERROR",e.toString());

        } finally {
            if (myDB != null)
                myDB.close();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        SharedPreferences editor = getSharedPreferences("MyPref", MODE_PRIVATE);

        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService

        final SharedPreferences reader = this.getSharedPreferences("check", Context.MODE_PRIVATE);
        final boolean first = reader.getBoolean("is_first", true);
        if(first){
            final SharedPreferences.Editor editor = reader.edit();
            editor.putBoolean("is_first", false);
            editor.commit();

            Intent intent = new Intent(this, CheckAPI.class);
            stopService(intent);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        }
    }

    public void launchSetupActivity(View v) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    public void launchDisplayActivity(View v) {
        Intent intent = new Intent(this, DisplayMapsActivity.class);
        startActivity(intent);
    }

    public void launchListActivity(View v) {

        String query = "SELECT COUNT(*) FROM User";

        Cursor cc = null;

        myDB = this.openOrCreateDatabase("ProjectDB", MODE_PRIVATE, null);

        cc = myDB.rawQuery(query, null);
        cc.moveToFirst();

        //Check if user created
        if(cc.getInt(0) > 0) {
            Intent intent = new Intent(this, ListActivity.class);
            startActivity(intent);
        }
        else {
            Toast.makeText(this, "You need to add select a location before you can view recent crimes in your area", Toast.LENGTH_LONG).show();
        }

        cc.close();

        if (myDB != null)
            myDB.close();

    }

    protected void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(bReceiver, new IntentFilter("Message"));
    }

    protected void onPause (){
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bReceiver);
    }

    public void showLoadScreen() {

        //Setup Dialog
        Dialog = new ProgressDialog(this);
        Dialog.setCanceledOnTouchOutside(false);
        Dialog.setMessage("Please wait...(This may take a minute)");
        Dialog.show();
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            // We've bound to LocalService, cast the IBinder and get LocalService instance
            CheckAPI.LocalBinder binder = (CheckAPI.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

            mService.runTask();

            showLoadScreen();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}
