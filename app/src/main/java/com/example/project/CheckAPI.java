package com.example.project;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CheckAPI extends Service {
    public CheckAPI() {
    }

    private final IBinder mBinder = new LocalBinder();
    Thread readthread;
    SQLiteDatabase myDB= null;

    Boolean isRunning = true;
    Boolean fromTask = true;
    double latitude  = 0;
    double longitude = 0;
    int radius_id = 0;
    int radius_value = 0;

    NotificationManager mNotificationManager;
    NotificationCompat.Builder mBuilder;

    SharedPreferences reader = null;

    LocalBroadcastManager broadcaster;

    private Activity activity = new MainActivity();


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        return mBinder;

    }

    public boolean serviceIsRunning() {

        return isRunning;
    }

    @Override
    public void onCreate() {

        broadcaster = LocalBroadcastManager.getInstance(this);
        reader = this.getSharedPreferences("check", Context.MODE_PRIVATE);

    }

    public void sendResult(String message) {

        //Setup Intent
        Intent intent = new Intent("Message");
        intent.putExtra("Success", message);

        intent.setClass(this, MainActivity.class);
        broadcaster.sendBroadcast(intent);
        Log.d("yyy","zzz");
    }

    public void runTask() {

        //Database
        myDB = this.openOrCreateDatabase("ProjectDB", MODE_PRIVATE, null);

        //Setup Boolean Value
        final SharedPreferences.Editor editor = reader.edit();
        editor.putBoolean("is_task", true);
        editor.commit();

        //Run APICheck
        APICall xx = new APICall();
        xx.execute();

    }

    public class LocalBinder extends Binder {
        public CheckAPI getService() {

            // Return this instance of LocalService so clients can call public methods
            return CheckAPI.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Database
        myDB = this.openOrCreateDatabase("ProjectDB", MODE_PRIVATE, null);

        //flag
        fromTask = false;

        mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.robbery)
                        .setContentTitle("New Crime");

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MapsActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);



        Notification noti = new Notification();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            noti.priority = Notification.PRIORITY_MIN;
        }
        startForeground(R.string.app_name, noti);



        final SharedPreferences.Editor editor = reader.edit();
        editor.putBoolean("is_task", false);
        editor.commit();

        //Execute api call
        APICall call = new APICall();
                call.execute();


        //Service wont restart itself
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

        final class APICall extends AsyncTask<URL, Integer, Long> {
            protected Long doInBackground(URL... urls) {

                //flag
                isRunning = true;

                ActivityManager am = (ActivityManager) CheckAPI.this.getSystemService(ACTIVITY_SERVICE);

                // The first in the list of RunningTasks is always the foreground task.
                ActivityManager.RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);

                Log.d("APPSTATUS",foregroundTaskInfo.topActivity.getPackageName());

                try{
                    String foregroundTaskPackageName = foregroundTaskInfo .topActivity.getPackageName();
                    PackageManager pm = CheckAPI.this.getPackageManager();
                    PackageInfo foregroundAppPackageInfo = pm.getPackageInfo(foregroundTaskPackageName, 0);
                    String foregroundTaskAppName = foregroundAppPackageInfo.applicationInfo.loadLabel(pm).toString();

                    Log.d("APPSTATUS",foregroundTaskAppName);

                }
                catch(Exception e){

                }

                try {

                    URL url = new URL("http://mapapi.winnipeg.ca/mapapi/wfs.ashx?maptypeid=2&featurelist=13706");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                    try {

                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;

                        while ((line = bufferedReader.readLine()) != null) {

                            stringBuilder.append(line).append("\n");
                        }

                        bufferedReader.close();

                        try {

                            int userCount = -1;

                            Cursor c = null;

                            String query = "SELECT COUNT(*) FROM User";
                            c = myDB.rawQuery(query, null);

                            c.moveToFirst();
                            userCount = c.getInt(0);
                            c.close();

                            if(userCount > 0) {

                                c = myDB.rawQuery("SELECT RADIUS_ID, LATITUDE, LONGITUDE FROM User ORDER BY ID DESC LIMIT 1", null);
                                c.moveToFirst();

                                radius_id = c.getInt(c.getColumnIndex("RADIUS_ID"));
                                latitude  = c.getDouble(c.getColumnIndex("LATITUDE"));
                                longitude = c.getDouble(c.getColumnIndex("LONGITUDE"));

                                c.close();

                                c = myDB.rawQuery("SELECT RADIUS FROM Radius WHERE ID = ?", new String[]{Integer.toString(radius_id)});
                                c.moveToFirst();
                                radius_value = c.getInt(c.getColumnIndex("RADIUS"));
                                c.close();
                            }

                        }
                        catch(Exception e){
                            Log.d("Error",e.toString());
                        }

                        String result = stringBuilder.toString();

                        //Setup JSON
                        JSONObject jObject = new JSONObject(result);
                        JSONArray array = jObject.getJSONArray("features");
                        JSONObject c = array.getJSONObject(0);
                        JSONArray array2 = c.getJSONArray("items");

                        //Loop through JSON Array
                        for(int i = 0; i < array2.length(); i++){

                            JSONObject cc = array2.getJSONObject(i);

                            // Storing each json item in variable
                            Integer id = cc.getInt("ID");
                            String date = cc.getString("STARTDATE");
                            String type = cc.getString("MASTERTYPE");

                            String geometry = cc.getString("geometry");

                            Log.d("Output2",date);
                            Log.d("Output3",type);
                            Log.d("Output4",geometry);

                            String[] separated = geometry.split(" ");

                            Log.d("lat",separated[0]);
                            Log.d("long",separated[1]);

                            //Grab month from json string
                            String month = date.substring(date.indexOf("-") + 1);
                            month = month.substring(0, date.indexOf("-") + 1);

                            //parse month into digit
                            Date date2 = new SimpleDateFormat("MMMM").parse(month);
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(date2);

                            //Construct datestring
                            String dateString = "20" + date.substring(date.length() - 2) + "-" + Integer.toString(cal.get(Calendar.MONTH) + 1) + "-" + date.substring(0,2);

                            Log.d("Output5",dateString);

                            int tableID = 0;
                            int count   = -1;

                            try {

                                Cursor cursor1 = null;

                                String query = "SELECT COUNT(*) FROM "
                                        + "Crime" + " WHERE " + "UNIQUE_ID" + " = ?";

                                cursor1 = myDB.rawQuery(query, new String[]{id.toString()});

                                cursor1.moveToFirst();
                                count = cursor1.getInt(0);
                                cursor1.close();

                                if (count < 1) {

                                    myDB.execSQL("INSERT INTO Crime_Location (LATITUDE, LONGITUDE)"
                                            + " VALUES (" + Double.parseDouble(separated[0]) + "," + Double.parseDouble(separated[1]) + ");");

                                    Cursor cursor2 = null;
                                    cursor2 = myDB.rawQuery("SELECT ID FROM Crime_Location ORDER BY ID DESC LIMIT 1", null);
                                    cursor2.moveToLast();
                                    tableID = cursor2.getInt(cursor2.getColumnIndex("ID"));
                                    cursor2.close();

                                    myDB.execSQL("INSERT INTO Crime (CRIME_LOCATION_ID, UNIQUE_ID, TYPE, DATE)"
                                            + " VALUES(" + tableID + ", " + id + ", '" + type + "', '" + dateString + "');");

                                    Location loc1 = new Location("");
                                    loc1.setLatitude(latitude);
                                    loc1.setLongitude(longitude);

                                    //The location sent in the api call
                                    Location loc2 = new Location("");
                                    loc2.setLatitude(Double.parseDouble(separated[0]));
                                    loc2.setLongitude(Double.parseDouble(separated[1]));

                                    if(loc1.distanceTo(loc2) <= radius_value) {

                                        final boolean task = reader.getBoolean("is_task", true);

                                        if(!task){
                                            mNotificationManager.notify(1, mBuilder.build());
                                            mBuilder.setContentText(type);
                                        }
                                    }
                                }

                            }
                            catch(Exception e)
                            {
                                Log.d("CHECKAPIERROR",e.toString());
                            }
                        }

                    } finally {
                        urlConnection.disconnect();
                        isRunning = false;
                    }
                } catch (Exception e) {
                    Log.e("ERROR", e.getMessage(), e);
                }

                return null;
            }

            protected void onProgressUpdate(Integer... progress) {
            }

            protected void onPreExecute()
            {

            }

            protected void onPostExecute(Long result) {
                isRunning = false;

                if(fromTask){
                    Log.d("APPSTATUS77","1122");
                    sendResult("test test");
                }

                stopSelf();
            }
        }

}

