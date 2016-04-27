package com.example.ashutosh.mapping;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Ashutosh on 04-03-2016.
 */
public class TrackList {
    String trName;
    public int id;
    public int listLen;
    public LatLng startPoint;
    public java.util.List<LatLng> gpsList;

    public void setPara(String n,int tId, int l, LatLng s, java.util.List<LatLng> g){
        trName=n;
        id = tId;
        listLen=l;
        startPoint=s;
        gpsList=g;
    }
}
