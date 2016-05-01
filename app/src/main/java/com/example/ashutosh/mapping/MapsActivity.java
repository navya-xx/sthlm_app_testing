package com.example.ashutosh.mapping;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements LocationListener, OnMapReadyCallback, View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    private static boolean atStartPoint = false;
    private static final String TAG = "Mapwa";
    private GoogleMap mMap;
    public static int numLoc = 0;
    private GoogleApiClient mGoogleApiClient;
    public static List<LatLng> gpsLoc = new ArrayList<>();

    public static LatLng start_loc;
    private LocationRequest mLocationRequest;
    //TextView posX, posY;
    private Location mCurrentLocation;
   /* private final SensorManager mSensorManager;
    private final Sensor mAccelerometer;

    public MapsActivity() {
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map1);
        mapFragment.getMapAsync(this);

        final Button button2 = (Button) findViewById(R.id.start3);
        button2.setOnClickListener(this);
        button2.setEnabled(false);
        //posX = (TextView) findViewById(R.id.positionX);
        //posY = (TextView) findViewById(R.id.positionY);
        Log.e(TAG, "Before mapping");
        // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(AppIndex.API).build();
        Log.e(TAG, "After mapping");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        load_track();

        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        Log.d(TAG, "Poly op");

        createLocationRequest();
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

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Get the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        // Get Current Location
        Location myLocation = locationManager.getLastKnownLocation(provider);

        // set map type
        //mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        for (int i = 0; i < numLoc; i++) {
            LatLng point = gpsLoc.get(i);
            options.add(point);
        }
        Log.d(TAG, "track drawn");
        //map.addMarker(); //add Marker in current position
        mMap.addPolyline(options); //add Polyline*/

        mMap.setMyLocationEnabled(true);
        LatLng defaultLoc = new LatLng(59.3293230, 18.0685810);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultLoc, 4.0f));

        try {
            // Get latitude of the current location
            double latitude = myLocation.getLatitude();

            // Get longitude of the current location
            double longitude = myLocation.getLongitude();

            // Create a LatLng object for the current location
            LatLng latLng = new LatLng(latitude, longitude);
            //LatLng latLng = defaultLoc;
            start_loc = latLng;
        } catch (Exception e) {
            startLocationUpdates();
        }

        LatLngBounds.Builder bc = new LatLngBounds.Builder();
        bc.include(start_loc);
        bc.include(gpsLoc.get(0));

        Context context = this.getApplicationContext();
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

       mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bc.build(), width, height, 100));

        final Button button2 = (Button) findViewById(R.id.start3);
        button2.setEnabled(true);
    }


    protected void createLocationRequest() {
        final long INTERV = 1000; //1 sec
        final long FASTEST_INTERV = 1000; // 1sec
        final float SMALLEST_DISPLACE = 0.001F; //unit is meter
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERV);
        mLocationRequest.setFastestInterval(FASTEST_INTERV);
        mLocationRequest.setSmallestDisplacement(SMALLEST_DISPLACE);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    public void onClick(View v){
        switch(v.getId())
        {
            case R.id.start3:
            {
                Log.e(TAG,"Ready for MapsActivity2!");
                startActivity(new Intent(MapsActivity.this, MapsActivity2.class));
                Log.e(TAG,"Done with MapsActivity2!");

                break;
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
    }


    public void distFromStart(){

        float distFrSrt = distanceGps(start_loc, gpsLoc.get(0));
        String currentDist = "Distance from start point: " + String.format("%.6f", distFrSrt);
        Toast.makeText(getApplicationContext(), currentDist, Toast.LENGTH_LONG).show();

        if(distFrSrt < 10)
            atStartPoint = true;
    }

    public void nearest_track(LatLng start_loc){
        float distMin=10000000;
        int idTrack=0;
        TrackList tempTrack;
        for(int i=0; i< com.example.ashutosh.mapping.List.numTracks; i++){
            tempTrack=com.example.ashutosh.mapping.List.tracks.get(i);
            if(i==0){
                distMin=distanceGps(start_loc,tempTrack.startPoint);
                idTrack=i+1;
            }
            else{
                float t=distanceGps(start_loc,tempTrack.startPoint);
                if(t<distMin){
                    distMin=t;
                    idTrack=i+1;
                }
            }
        }
        tempTrack=com.example.ashutosh.mapping.List.tracks.get(idTrack-1);
        numLoc = tempTrack.listLen;
        gpsLoc = tempTrack.gpsList;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

    }

    @Override
    public void onStop() {
        super.onStop();

        mGoogleApiClient.disconnect();
    }



/*
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
        //Right in here is where you put code to read the current sensor values and
        //update any views you might have that are displaying the sensor information
        //You'd get accelerometer values like this:
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
            return;
        String strX=new String().valueOf(event.values[0]);
        String strY=new String().valueOf(event.values[1]);
        String strZ=new String().valueOf(event.values[2]);
        TextView accX = (TextView)findViewById(R.id.accX);
        accX.stxt(strX);
        TextView accY = (TextView)findViewById(R.id.accY);
        accY.setText(strY);
        TextView accZ = (TextView)findViewById(R.id.accZ);
        accZ.setText(strZ);
    }*/

    public float distanceGps(LatLng a, LatLng b){
        Location loc1 = new Location("");
        loc1.setLatitude(a.latitude);
        loc1.setLongitude(a.longitude);

        Location loc2 = new Location("");
        loc2.setLatitude(b.latitude);
        loc2.setLongitude(b.longitude);

        return loc1.distanceTo(loc2);
    }

    @Override
    public void onLocationChanged(Location location) {

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

    private void load_track(){
        Log.d(TAG, "Loading Track");

        if(com.example.ashutosh.mapping.List.trackId == 0){
            nearest_track(start_loc);
            Log.d(TAG, "Loading Track nearest");
        }
        else {
            TrackList tTrack;
            tTrack=com.example.ashutosh.mapping.List.tracks.get(com.example.ashutosh.mapping.List.trackId -1);
            numLoc=tTrack.listLen;
            gpsLoc=tTrack.gpsList;
            Log.d(TAG, "Loading Track num");
        }


    }

}

