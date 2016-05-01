package com.example.ashutosh.mapping;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sony Vaio on 4/19/2016.
 */
public class features {
    private int _id;
    private int _track_id;
    private java.util.List<latlng_values> _features = new ArrayList<latlng_values>();
    private String TAG = "Feature class: ";

    public features(){
    }

    public void set_track_id(int _track_id) {
        this._track_id = _track_id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public void set_features(List<latlng_values> feature) {
        this._features = feature;
    }

    public int get_id() {
        return _id;
    }

    public int get_track_id() {
        return _track_id;
    }

    public List<latlng_values> get_features() {
        return _features;
    }
}
