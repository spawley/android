package com.example.project;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by Scott on 11/28/2016.
 */

public class Startup extends Application {

        public Startup() {
            // this method fires only once per application start.
            // getApplicationContext returns null here
            Log.i("main", "Constructor fired");
        }

        @Override
        public void onCreate() {
            super.onCreate();

            // this method fires once as well as constructor
            // but also application has context here
            Log.i("main", "onCreate fired");

            final SharedPreferences reader = this.getSharedPreferences("check", Context.MODE_PRIVATE);
            final SharedPreferences.Editor editor = reader.edit();
            editor.putBoolean("is_first", true);
            editor.commit();

            /*
            SQLiteDatabase myDB= null;
            myDB = this.openOrCreateDatabase("ProjectDB", MODE_PRIVATE, null);

            myDB.execSQL("DROP TABLE Radius ");
            myDB.execSQL("DROP TABLE User ");
            myDB.execSQL("DROP TABLE Crime ");
            myDB.execSQL("DROP TABLE Crime_Location ");
            */
        }
}
