package com.example.ashutosh.mapping;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;


import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Sony Vaio on 4/30/2016.
 */
public class LocationSensorService extends Service implements LocationListener, SensorEventListener, GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private final Context mContext;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    // Sensor data
    private SensorManager sensorManager;
    private Sensor mStepCounterSensor;
    private Sensor mStepDetectorSensor;
    private Sensor mAcceleratorSensor;
    boolean activityRunning;
    public float accuracy, speed;
    public double altitude;

    private int mSensorStepC, mSensorStepD;
    private double mSensorAccX, mSensorAccY, mSensorAccZ;

    // Declaring a Location Manager
    protected LocationManager locationManager;
    private Location mCurrentLocation;

    private ArrayList<Long> timeElap = new ArrayList<Long>();
    private ArrayList<Long> timeStamp = new ArrayList<Long>();
    //    File file;
//    OutputStream fos;
    private TextView acc_view, dist, sc, timeDisp, ac, sd;
    protected ArrayList<latlng_values> points = new ArrayList<>();


    public LocationSensorService(Context context) {
        this.mContext = context;
        getLocation();
        createSensors();
    }

    public void createSensors(){
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        activityRunning = true;
        // start step counter
        mStepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mStepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        mAcceleratorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                Toast.makeText(this.mContext,"GPS is not enabled!",Toast.LENGTH_LONG).show();
            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        Toast toast = Toast.makeText(this.getApplicationContext(), "Please provide permission for Location service.", Toast.LENGTH_LONG);
                        toast.show();
                    }
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            Toast toast = Toast.makeText(this.getApplicationContext(), "Please provide permission for Location service.", Toast.LENGTH_LONG);
                            toast.show();
                            showSettingsAlert();
                        }
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     * */
    public void stopSensorData(){
        if(locationManager != null){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Toast toast = Toast.makeText(this.getApplicationContext(), "Please provide permission for Location service.", Toast.LENGTH_LONG);
                toast.show();
                showSettingsAlert();
            }
            locationManager.removeUpdates(LocationSensorService.this);
        }

        sensorManager.unregisterListener(this, mStepCounterSensor);
        sensorManager.unregisterListener(this, mStepDetectorSensor);
        sensorManager.unregisterListener(this, mAcceleratorSensor);
    }

    /**
     * Function to get latitude
     * */
    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    /**
     * Function to get longitude
     * */
    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

    /**
     * Function to check GPS/wifi enabled
     * @return boolean
     * */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     * */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }


    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        if (location.hasAccuracy()) {
            accuracy = location.getAccuracy();
        } else {
            accuracy = 0;
        }
        //Log.e(TAG, "accuracy: " + accuracy);

        if (location.hasAltitude()) {
            altitude = location.getAltitude();
        } else {
            altitude = 0;
        }
        //Log.e(TAG, "altitude: " + altitude);

        if (location.hasSpeed()) {
            speed = location.getSpeed();
        } else {
            speed = 0;
        }
        //Log.e(TAG, "speed: " + speed);

        Long time_temp = location.getTime();

        //Add parameter to point var
        latlng_values temp = new latlng_values();
        temp.setAccuracy(accuracy);
        temp.setAltitude(altitude);
        temp.setLatitude(latitude);
        temp.setLongitude(longitude);
        temp.setSpeed(speed);
        temp.setTimestamp(time_temp);
        temp.setmSensorStepC(mSensorStepC);
        temp.setmSensorStepD(mSensorStepD);
        temp.setmSensorAccX(mSensorAccX);
        temp.setmSensorAccY(mSensorAccY);
        temp.setmSensorAccZ(mSensorAccZ);
        //Log.e(TAG, "temp: " + temp);
        points.add(temp);

        // sensor data

        sc.setText(String.format(Locale.US, "%d", mSensorStepC));
        sd.setText(String.format(Locale.US, "%d", mSensorStepD));
        ac.setText(String.format("%.2f,%.2f,%.2f", mSensorAccX, mSensorAccY, mSensorAccZ));
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
