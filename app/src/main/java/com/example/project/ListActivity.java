package com.example.project;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.BooleanResult;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

import static android.R.attr.type;

public class ListActivity extends android.app.ListActivity {

    private CustomAdapter mAdapter;
    SQLiteDatabase myDB= null;

    double latitude;
    double longitude;
    int radius_id;
    int radius_value;

    String type;
    String crimeDate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myDB = this.openOrCreateDatabase("ProjectDB", MODE_PRIVATE, null);

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
                latitude = c.getDouble(c.getColumnIndex("LATITUDE"));
                longitude = c.getDouble(c.getColumnIndex("LONGITUDE"));

                c.close();

                c = myDB.rawQuery("SELECT RADIUS FROM Radius WHERE ID = ?", new String[]{Integer.toString(radius_id)});
                c.moveToFirst();

                radius_value = c.getInt(c.getColumnIndex("RADIUS"));
                c.close();

                query = "SELECT * FROM Crime WHERE DATE >= date('now','-14 day')";
                c = myDB.rawQuery(query, null);
                c.moveToFirst();

                ArrayList<Crime> collection = new ArrayList<Crime>();

                for (int i = 0; i < c.getCount(); i++) {

                    type      = c.getString(c.getColumnIndex("TYPE"));
                    crimeDate = c.getString(c.getColumnIndex("DATE"));

                    Cursor c2 = null;

                    query = "SELECT * FROM Crime_Location WHERE ID = ?";
                    c2 = myDB.rawQuery(query, new String[]{Integer.toString(c.getInt(c.getColumnIndex("CRIME_LOCATION_ID")))});
                    c2.moveToFirst();

                    Location loc1 = new Location("");
                    loc1.setLatitude(latitude);
                    loc1.setLongitude(longitude);

                    //The location sent in the api call
                    Location loc2 = new Location("");
                    loc2.setLatitude(c2.getDouble(c2.getColumnIndex("LATITUDE")));
                    loc2.setLongitude(c2.getDouble(c2.getColumnIndex("LONGITUDE")));

                    Log.d("RANGE", c.getString(c.getColumnIndex("CRIME_LOCATION_ID")));

                    mAdapter = new CustomAdapter(this);

                    float distance = loc1.distanceTo(loc2);

                    if(distance <= radius_value) {

                        collection.add(new Crime(type,crimeDate,distance));
                    }

                    c.moveToNext();
                }

                if(collection.size() >= 1){

                    Collections.sort(collection, new Comparator<Crime>() {
                        @Override
                        public int compare(Crime p1, Crime p2) {

                            Date date1 = new Date();
                            Date date2 = new Date();

                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

                                date1 = sdf.parse(p1.date);
                                date2 = sdf.parse(p2.date);

                            }
                            catch(Exception e) {

                                Log.d("error", e.toString());
                            }

                            return (date1.after(date2)) ? -1 : 1;
                        }
                    });


                    for(Crime crime : collection) {
                        mAdapter.addItem(crime);
                    }
                }
                else {

                    mAdapter.addItem(new Crime("None",null,null));
                }


                setListAdapter(mAdapter);

            }

        }
        catch(Exception e){
            Log.d("Error",e.toString());
        }
    }
}

class CustomAdapter extends BaseAdapter {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;

    private ArrayList<Crime> mData = new ArrayList<Crime>();
    private TreeSet<Integer> sectionHeader = new TreeSet<Integer>();

    private LayoutInflater mInflater;

    public int count = 0;

    public CustomAdapter(Context context) {
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addItem(Crime Crime) {
        mData.add(Crime);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return getCount();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Crime getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        int rowType = getItemViewType(position);

        convertView = null;

        if (convertView == null) {
            holder = new ViewHolder();
            if(position != 0) {
                switch (rowType) {
                    case TYPE_ITEM:
                        convertView = mInflater.inflate(R.layout.snippet_item1, null);
                        holder.textView = (TextView) convertView.findViewById(R.id.text);
                        holder.textView.setText(mData.get(position).type);
                        holder.textView = (TextView) convertView.findViewById(R.id.text1);
                        holder.textView.setText(mData.get(position).date);
                        break;
                    case TYPE_SEPARATOR:
                        convertView = mInflater.inflate(R.layout.snippet_item2, null);
                        holder.textView = (TextView) convertView.findViewById(R.id.textSeparator);
                        holder.textView.setText("");
                        break;
                }
                convertView.setTag(holder);
            }
            else {

                if(!mData.get(0).type.equals("None")) {

                    //Kind of a hacky solution to ensure the header doesn't end up overriding the first item
                    if(!mData.get(0).type.equals("Placeholder")) {
                        mData.add(0, new Crime("Placeholder",null,null));
                    }

                    count = mData.size() - 1;

                    convertView = mInflater.inflate(R.layout.snippet_item2, null);
                    holder.textView = (TextView) convertView.findViewById(R.id.textSeparator);
                    holder.textView.setText(count + " crimes have occurred in your area over the last 2 weeks");

                }
                else {
                    convertView = mInflater.inflate(R.layout.snippet_item2, null);
                    holder.textView = (TextView) convertView.findViewById(R.id.textSeparator);
                    holder.textView.setText("0 crimes have occurred in your area over the last 2 weeks");
                }

                convertView.setTag(holder);
            }

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        return convertView;
    }

    public static class ViewHolder {
        public TextView textView;
    }
}
