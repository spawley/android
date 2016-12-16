package com.example.project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import android.content.ServiceConnection;


/**
 * Created by Scott on 12/1/2016.
 */

public class OnAlarmReceive  extends BroadcastReceiver{

    /*
        When AlarmManager triggers it will execute code in this method
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("Hello", "BroadcastReceiver, in onReceive:");

        Intent i = new Intent(context, MainActivity.class);

        Intent intent2 = new Intent(context, CheckAPI.class);
        context.startService(intent2);
    }

}
