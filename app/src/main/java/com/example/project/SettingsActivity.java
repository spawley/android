package com.example.project;

        import android.app.Activity;
        import android.app.AlarmManager;
        import android.app.PendingIntent;
        import android.content.Context;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.hardware.SensorManager;
        import android.os.Bundle;
        import android.os.SystemClock;
        import android.preference.Preference;
        import android.preference.PreferenceFragment;
        import android.preference.PreferenceManager;
        import android.preference.SwitchPreference;
        import android.support.v7.app.AppCompatActivity;
        import android.util.Log;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new PrefsFragment()).commit();
    }

    public static class PrefsFragment extends PreferenceFragment{

        @Override
        public void onCreate(Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.pref_show);

            //Get Preference
            Preference pref = findPreference("notify_user");
            pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {

                    //Initialize Alarm
                    AlarmManager alarm = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                    Intent intent2 = new Intent(getActivity().getApplicationContext(), OnAlarmReceive.class);
                    PendingIntent pendingIntent =  PendingIntent.getBroadcast(getActivity().getApplicationContext(), 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);

                    if(newValue.equals(true)){

                        //Start Alarm
                        alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HALF_DAY,
                                AlarmManager.INTERVAL_HALF_DAY, pendingIntent);
                    }
                    else {

                        //Stop Alarm
                        alarm.cancel(pendingIntent);
                    }

                    // true to update the state of the Preference with the new value
                    // in case you want to disallow the change return false
                    return true;
                }
            });
        }


    }

}