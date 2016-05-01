package com.example.ashutosh.mapping;

/**
 * Created by Sony Vaio on 4/19/2016.
 */
public class latlng_values {

    public double latitude;
    public double longitude;
    public float accuracy;
    public double altitude;
    public  float speed;
    public long timestamp;
    public int mSensorStepC;
    public int mSensorStepD;
    public double mSensorAccX, mSensorAccY, mSensorAccZ;
    public String TAG = "latlng_class.";

    public int getmSensorStepC() {
        return mSensorStepC;
    }

    public void setmSensorStepC(int mSensorStepC) {
        this.mSensorStepC = mSensorStepC;
    }

    public int getmSensorStepD() {
        return mSensorStepD;
    }

    public void setmSensorStepD(int mSensorStepD) {
        this.mSensorStepD = mSensorStepD;
    }

    public double getmSensorAccX() {
        return mSensorAccX;
    }

    public void setmSensorAccX(double mSensorAccX) {
        this.mSensorAccX = mSensorAccX;
    }

    public double getmSensorAccY() {
        return mSensorAccY;
    }

    public void setmSensorAccY(double mSensorAccY) {
        this.mSensorAccY = mSensorAccY;
    }

    public double getmSensorAccZ() {
        return mSensorAccZ;
    }

    public void setmSensorAccZ(double mSensorAccZ) {
        this.mSensorAccZ = mSensorAccZ;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public double getLatitude() {
        return latitude;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
