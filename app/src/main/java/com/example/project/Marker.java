package com.example.project;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Scott on 11/27/2016.
 */

public class Marker {

    public String title, snippet;
    public Double latitude, longitude;
    public LatLng coordinates;

    public Marker(String title, String snippet, Double latitude, Double longitude) {
        this.title = title;
        this.snippet = snippet;
        this.coordinates = new LatLng(latitude, longitude);

    }
}
