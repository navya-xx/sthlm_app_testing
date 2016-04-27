package com.example.ashutosh.mapping;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//import com.google.maps.android.ui.IconGenerator;


public class MapsActivity2 extends Activity implements
        LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    private static final String TAG = "MAPSACTIVITY2";
    // Map initialization parameters
    private static final long INTERVAL = 0; //1 sec
    private static final float SMALLEST_DISPLACEMENT = 0F; //unit is meter
    private final long FASTEST_INTERVAL = 0; // 1sec
    public long totTime = 0;
    protected ArrayList<Date> dateStamp = new ArrayList<>();
    protected ArrayList<latlng_values> points = new ArrayList<>();
    ArrayList<LatLng> drawable_points = new ArrayList<>();
    Polyline line; //added
    double distance = 0;
    int flag_stop = 0;
    // Database handle
    MyDbHandler dbHandler;
    // Map variables
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;

    private ArrayList<Long> timeElap = new ArrayList<Long>();
    private ArrayList<Long> timeStamp = new ArrayList<Long>();
    //    File file;
//    OutputStream fos;
    private TextView acc_view, dist, sz, timeDisp;


    public MapsActivity2() {

    }


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void dist_calc(double l1, double l2, double lo1, double lo2) {
        double lat1 = Math.toRadians(l1);
        double lat2 = Math.toRadians(l2);
        double lon1 = Math.toRadians(lo1);
        double lon2 = Math.toRadians(lo2);
        double dlat = Math.toRadians(lat2 - lat1);
        double dlon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dlon / 2) * Math.sin(dlon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double raz = 6371000;
        distance += raz * c;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        setContentView(R.layout.activity_maps2);
        Log.d(TAG, "onCreate2 ...............................");

        MapFragment mapFragment =
                (MapFragment) getFragmentManager().findFragmentById(R.id.map2);

        Log.d(TAG, "onCreate3 ...............................");
        mapFragment.getMapAsync(this);

        Log.d(TAG, "onCreate4 ...............................");

        acc_view = (TextView) findViewById(R.id.accu);
        sz = (TextView) findViewById(R.id.sz);
        dist = (TextView) findViewById(R.id.dist);
        timeDisp = (TextView)findViewById(R.id.timeDur);

        final Button button_save = (Button) findViewById(R.id.save);
        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopLocationUpdates();
                flag_stop = 1;
                BackgroundTask task = new BackgroundTask(MapsActivity2.this);
                task.execute();
                //progressDialog.show(MapsActivity2.this, "Saving data", "Please wait...", true);
            }
        });
    }

    private void setDB_params() {
        dbHandler = new MyDbHandler(this, null, null, 2);
        dbHandler.setDEVICE_ID();
        // get timestamp just for creating new table for this track
        Long ts = System.currentTimeMillis();
        dbHandler.setTimestamp(ts);
        dbHandler.setTABLE_TRACK(ts);
    }

    public Boolean write_db() {
        Log.e(TAG, "testing3");
        SQLiteDatabase db = dbHandler.getWritableDatabase();

        // create new table for track
        try {
            dbHandler.onCreate_newTrack(db);
            Log.e(TAG, "OnCreate_newTrack database");
        } catch (SQLException te) {
            throw new Error("Unable to create table.");
        }
        db.close();

        // add features
        features new_feature = new features();
        // get track
        TrackList tTrack;
        tTrack = com.example.ashutosh.mapping.List.tracks.get(com.example.ashutosh.mapping.List.trackId);
        new_feature.set_track_id(tTrack.id);
        Log.e(TAG, "testing5");

        // import lat long values
        List<latlng_values> feat_list = new ArrayList<latlng_values>();
        Log.e(TAG, "compare - points : " + points.size() + " , timeStamp:" + timeStamp.size());
        // should not go in this now!
        if (timeStamp.size() + 1 == points.size()) timeStamp.add(mCurrentLocation.getTime());

        for (int i = 0; i < points.size(); i++) {
            latlng_values temp = new latlng_values();
            temp.setLatitude(points.get(i).latitude);
            temp.setLongitude(points.get(i).longitude);
            temp.setTimestamp(timeStamp.get(i));
            Log.e(TAG, "temp vals: " + temp);
            feat_list.add(i, temp);
        }
        new_feature.set_features(feat_list);

        // put data
        try {
            dbHandler.addProduct(new_feature);
        } catch (SQLException tabE) {
            throw tabE;
        }

        return true;
    }

    public void export_mysql() {
        Log.e(TAG, "testing6");
        if (dbHandler.isNetworkAvailable(this)) {
            Log.e(TAG, "testing7");
            dbHandler.post_http_json(dbHandler.get_json_track(dbHandler.getTABLE_TRACK()), dbHandler.URL_INSERT_TRACK);
        }
    }

    //
    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart fired ..............");
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop fired ..............");
        mGoogleApiClient.disconnect();
        Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int status = googleAPI.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            googleAPI.getErrorDialog(this, status, 0).show();
            Toast.makeText(getApplicationContext(), "Google Play Services is not Available", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ..............: ");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    public void timeElapsed() {
        long d0 = dateStamp.get(0).getTime();
        long d1 = dateStamp.get(points.size() - 1).getTime();
        long d2 = dateStamp.get(points.size() - 2).getTime();

        //in seconds
        long diff = (d1 - d2) / 1000;
        totTime = (d1 - d0) / 1000;
        timeElap.add(diff);
        timeDisp.setText(String.valueOf(totTime));
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Firing onLocationChanged..............................................");
        mCurrentLocation = location;

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        Bundle extras = location.getExtras();

        float accuracy;
        if(location.hasAccuracy()){
            accuracy = location.getAccuracy();
        } else {
            accuracy = 0;
        }

        double altitude;
        if(location.hasAltitude()) {
            altitude = location.getAltitude();
        } else {
            altitude = 0;
        }

        float speed;
        if(location.hasSpeed()){
            speed = location.getSpeed();
        } else {
            speed = 0;
        }

        String provider = location.getProvider();
        Long time_temp = location.getTime();

        //Add parameter to point var
        latlng_values temp = new latlng_values();
        temp.setAccuracy(accuracy);
        temp.setAltitude(altitude);
        temp.setLatitude(latitude);
        temp.setLongitude(longitude);
        temp.setSpeed(speed);
        temp.setTimestamp(time_temp);
        points.add(temp);

        TextView lat = (TextView) findViewById(R.id.lat);
        lat.setText(String.valueOf(latitude));
        TextView longi = (TextView) findViewById(R.id.longi);
        longi.setText(String.valueOf(longitude));
        acc_view.setText(String.format("%.2f", accuracy));
        int sz = points.size();
        if(sz>2) {
            dist_calc(points.get(sz - 1).latitude, points.get(sz - 2).latitude, points.get(sz - 1).longitude, points.get(sz - 2).longitude);
            dist.setText(String.format("%.2f", distance));
            timeElapsed();
        }
        dateStamp.add(new Date());


        TextView speed_view = (TextView) findViewById(R.id.speed);
        speed_view.setText(String.valueOf(speed));

        Log.d(TAG, "after points");
        if (points.size() > 2)
            redrawLine();
    }

    private void redrawLine() {
        get_mapable(points);
        line.setPoints(this.drawable_points);
    }

    private void get_mapable(ArrayList<latlng_values> points) {
        int size_ = points.size();
        ArrayList<LatLng> drawable_points = new ArrayList<>();
        for(int i=0; i<size_; i++) {
            LatLng temp = new LatLng(points.get(i).latitude,points.get(i).longitude);
            drawable_points.add(temp);
        }
        this.drawable_points = drawable_points;
    }

    private void addMarker() {
        MarkerOptions options = new MarkerOptions();

        LatLng currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        Log.d(TAG, "addmarker3.......................");

        options.position(currentLatLng);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mGoogleApiClient.isConnected())
            stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .......................");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.d(TAG, "onMapReadyhula .......................");
        createLocationRequest();
        Log.d(TAG, "onMapReady2 .......................");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Log.d(TAG, "onMapReady3 .......................");
        googleMap.setMyLocationEnabled(true);
        Log.d(TAG, "onMapReady4.......................");


        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {

                Toast.makeText(getApplicationContext(), "Location button has been clicked", Toast.LENGTH_LONG).show();
                if (points.size() > 0)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(MapsActivity2.this.drawable_points.get(MapsActivity2.this.drawable_points.size() - 1), 16));
                return true;
            }
        });
        Log.d(TAG, "onMapReady5 .......................");


        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setAllGesturesEnabled(true);
        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        //LatLngBounds track = new LatLngBounds(gpsLoc.get(0), gpsLoc.get(numLoc - 1));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(track,0));

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(MapsActivity.start_loc, 16.0f));
        for (int i = 0; i < MapsActivity.numLoc; i++) {
            LatLng point = MapsActivity.gpsLoc.get(i);
            options.add(point);
        }
        //map.addMarker(); //add Marker in current position
        googleMap.addPolyline(options); //add Polyline*/

        line = googleMap.addPolyline(new PolylineOptions().width(3).color(Color.RED));
        Log.d(TAG, "onMapReady6 .......................");
        dateStamp.add(0, new Date());
        long t = 0;
        timeElap.add(t);
        Log.d(TAG, "onMapReady6hula .......................");

    }

    private class BackgroundTask extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;

        public BackgroundTask(MapsActivity2 activity) {
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Please wait...");
            dialog.setTitle("Saving data");
            //dialog.setIndeterminate(true);
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.e(TAG, "testing2");
            //write_points();
            setDB_params();
            if (write_db()) {
                export_mysql();
            }
            return null;
        }
    }

}