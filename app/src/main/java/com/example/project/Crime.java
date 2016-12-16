package com.example.project;

import java.util.Date;

/**
 * Created by Scott on 11/30/2016.
 */

public class Crime {

    public String type, date;
    public Float distance;

    public Crime(String type, String date, Float distance){

        this.type = type;
        this.distance = distance;
        this.date = date;
    }
}