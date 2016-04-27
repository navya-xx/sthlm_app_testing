package com.example.ashutosh.mapping;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class MyDbHandler extends SQLiteOpenHelper {
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TRACK_ID = "_track_id";
    public static final String COLUMN_TRACK_TABLE = "_track_table";
    public static final String COLUMN_LATITUDE = "_LAT";
    public static final String COLUMN_LONGITUDE = "_LNG";
    public static final String COLUMN_TIMESTAMP = "_timestamp";
    public static final String COLUMN_ACCURACY = "_accuracy";
    public static final String COLUMN_SPEED = "_speed";
    public static final String COLUMN_ALTITUDE = "_altitude";
    public static final String COLUMN_UPDATE = "_update";
    private static final String COLUMN_DEVICE_ID = "_DEVICE_ID";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_USER_TRACKS = "user_track_list";
    private static final String TABLE_DEVICE_ID = "user_device_id";
    public static final String URL_INSERT_TRACK = "http://www.sthlmrunning.com/php/insert_track.php";
    // private static final String DATABASE_PATH = "/data/data/com.example.ashutosh.mapping/databases/"
    private String DATABASE_PATH = "";
    private static final String DATABASE_NAME = "sthlmrunning.db";
    public final Context myContext;
    public String user_id;
    public String dev_id;
    public Long timestamp;
    public String TABLE_TRACK;
    private Boolean UPDATE_FLAG = false;
    public String response_string = "";

    //String androidId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
    // String androidId = "akey1";
    public String DEVICE_ID;

    private SQLiteDatabase myDatabase;
    private static final String TAG = "Mapwa123";

    //We need to pass database information along to superclass
    public MyDbHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
        this.myContext = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db){
        String query = "CREATE TABLE " + TABLE_USER_TRACKS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TRACK_ID + " INTEGER NOT NULL, " +
                COLUMN_TRACK_TABLE + " TEXT NOT NULL, " +
                COLUMN_UPDATE + " INTEGER NOT NULL, " +
                COLUMN_TIMESTAMP + " TEXT " +
                ");";
        Log.e(TAG, "OnCreate database" + query);
        db.execSQL(query);
        Log.e(TAG, "User Table created!");

        DEVICE_ID =  UUID.randomUUID().toString();
        query = "CREATE TABLE " + TABLE_DEVICE_ID + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_DEVICE_ID + " TEXT NOT NULL" +
                ");";
        Log.e(TAG, "2OnCreate database" + query);
        db.execSQL(query);
        Log.e(TAG, "3User Table created!");
    }


    public void onCreate_newTrack(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_TRACK + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TRACK_ID + " INTEGER NOT NULL, " +
                COLUMN_LATITUDE + " REAL NOT NULL, " +
                COLUMN_LONGITUDE + " REAL NOT NULL, " +
                COLUMN_ACCURACY + " REAL NOT NULL, " +
                COLUMN_ALTITUDE + " REAL NOT NULL, " +
                COLUMN_SPEED + " REAL NOT NULL, " +
                COLUMN_TIMESTAMP + " TIMESTAMP NULL" +
                ");";
        Log.e(TAG, "New track creation" + query);
        db.execSQL(query);
        Log.e(TAG, "New track added to DB");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e(TAG, "Upgrading DB");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_TRACKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICE_ID);
        onCreate(db);
    }

    // Add track to the track_list
    public void addTrack(int track_id, SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TRACK_ID, track_id);
        values.put(COLUMN_TRACK_TABLE, this.TABLE_TRACK);
        values.put(COLUMN_UPDATE, 0);
        values.put(COLUMN_TIMESTAMP, this.timestamp);
        db.insert(TABLE_USER_TRACKS, null, values);
    }

    //Add a new row to the database
    public void addProduct(features feature) {
        ContentValues values = new ContentValues();
        int track_id = feature.get_id();
        java.util.List<latlng_values> latlong;
        latlong = feature.get_features();
        SQLiteDatabase db = getWritableDatabase();
        addTrack(track_id, db);
        Log.e(TAG, "Track added to track_list");
        for (int i = 0; i < latlong.size(); i++) {
            latlng_values temp = latlong.get(i);
            values.put(COLUMN_TRACK_ID, track_id);
            values.put(COLUMN_LATITUDE, temp.getLatitude());
            values.put(COLUMN_LONGITUDE, temp.getLongitude());
            values.put(COLUMN_ACCURACY, temp.accuracy);
            values.put(COLUMN_ALTITUDE, temp.altitude);
            values.put(COLUMN_SPEED, temp.speed);
            values.put(COLUMN_TIMESTAMP, temp.getTimestamp());
            Log.e(TAG,"latlng value to add:" + values);
            db.insert(TABLE_TRACK, null, values);
        }

        Log.e(TAG, "LatLong data added to track table");

        db.close();
    }

    //Delete a product from the database
    public void deleteProduct(String productName) {
        //SQLiteDatabase db = getWritableDatabase();
        //db.execSQL("DELETE FROM " + TABLE_PRODUCTS + " WHERE " + COLUMN_PRODUCTNAME + "=\"" + productName + "\";");
    }

    public SQLiteDatabase getMyDatabase() {
        return myDatabase;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setTABLE_TRACK(Long timestamp) {
        this.TABLE_TRACK = "_track_" + timestamp.toString();
    }

    public String getDEVICE_ID() {
        return DEVICE_ID;
    }

    public void setDEVICE_ID() {
        String query = "SELECT " + COLUMN_DEVICE_ID + " FROM " + TABLE_DEVICE_ID +" WHERE 1;";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(query,null);
        if(cursor.getCount() == 0){
            Log.e(TAG, "Something went wrong! Device_id not created when database was created.");
        } else {
            DEVICE_ID = cursor.getString(0);
            this.DEVICE_ID = DEVICE_ID;
        }
    }

    public String getTABLE_TRACK() {
        return TABLE_TRACK;
    }

    /**
     * Get list of tables that are not yet updated in the database
     * @return
     */
    public ArrayList<String> SQLite_getListofUpdateTracks(){
        ArrayList<String> trackList = new ArrayList<String>();
        String selectQuery = "SELECT " + COLUMN_TRACK_TABLE + " FROM " + TABLE_USER_TRACKS + " where " + COLUMN_UPDATE + " = '0'";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        int index = 0;
        if (cursor.moveToFirst()) {
            do {
                trackList.add(index, cursor.getString(0));
                index = index + 1;
            } while (cursor.moveToNext());
        }
        database.close();

        //Gson gson = new GsonBuilder().create();
        //Use GSON to serialize Array List to JSON
        //return gson.toJson(wordList);

        return trackList;
    }

    /**
     * Update list of tables given to the MySQL database
     */
    public void SQLite_updateTracksMySQL(ArrayList<String> trackList){
        SQLiteDatabase database = this.getWritableDatabase();
        for(int i=0; i<trackList.size(); i++){
            String query = "SELECT * FROM " + trackList.get(i);
            Cursor cursor = database.rawQuery(query, null);
            int index = 0;
            if(cursor.moveToFirst()){
                do {

                } while (cursor.moveToNext());
            }
        }

        database.close();
    }

    /**
     * Update Sync status against tracks
     * @param id
     * @param status
     */
    public void updateSyncStatus(String id, String status){
        SQLiteDatabase database = this.getWritableDatabase();
        String updateQuery = "UPDATE " + TABLE_USER_TRACKS + " SET " + COLUMN_UPDATE + " = '"+ status +"' WHERE " + COLUMN_TRACK_TABLE + "="+"'"+ id +"'";
        Log.e(TAG, updateQuery);
        database.execSQL(updateQuery);
        database.close();
    }


    /**
     *  Get list of all the tracks recorded by user
     */
    public ArrayList<HashMap<String, String>> getAllTracks() {
        ArrayList<HashMap<String, String>> trackList;
        trackList = new ArrayList<HashMap<String, String>>();
        String query = "SELECT * FROM " + this.TABLE_TRACK;
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(COLUMN_ID, cursor.getString(0));
                map.put(COLUMN_TRACK_ID, cursor.getString(1));
                map.put(COLUMN_TRACK_TABLE, cursor.getString(2));
                map.put(COLUMN_UPDATE, cursor.getString(3));
                map.put(COLUMN_TIMESTAMP, cursor.getString(4));
                trackList.add(map);
            } while (cursor.moveToNext());
        }
        database.close();
        return trackList;
    }



    /*
     * Making service call
     * @url - url to make request
     * @method - http request method
     * @params - http request params
     * */
    public void mysql_update_tracks(String url, int method, RequestParams params) {
        AsyncHttpClient client = new AsyncHttpClient();
        ArrayList<String> trackList = this.SQLite_getListofUpdateTracks();
        String jsonVal = "";
        // iterate in every table and update content in MySQL database
        for(int i=0; i<trackList.size();i++){
            jsonVal = get_json_track(trackList.get(i));
            post_http_json(jsonVal, URL_INSERT_TRACK);
            int status = (UPDATE_FLAG)?1:0;
            updateSyncStatus(trackList.get(i),Integer.toString(status));
        }
    }

    public String get_json_track(String table_name) {
        ArrayList<HashMap<String, String>> track = new ArrayList<HashMap<String, String>>();
        String query = "SELECT * FROM " + table_name;
        final String temp = table_name;
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<>();
                map.put(COLUMN_ID, cursor.getString(0));
                map.put(COLUMN_TRACK_ID, cursor.getString(1));
                map.put(COLUMN_LATITUDE, cursor.getString(2));
                map.put(COLUMN_LONGITUDE, cursor.getString(3));
                map.put(COLUMN_ACCURACY, cursor.getString(4));
                map.put(COLUMN_ALTITUDE, cursor.getString(5));
                map.put(COLUMN_SPEED, cursor.getString(6));
                map.put(COLUMN_TIMESTAMP, cursor.getString(7));
                track.add(map);
            } while (cursor.moveToNext());
        }
        database.close();

        Gson gson = new GsonBuilder().create();
        String JSON_string = gson.toJson(track);
        return JSON_string;
    }

    public void post_http_json(String JSON_string, String url){
        RequestParams params = new RequestParams();

        // ========================================================
        AsyncHttpClient client = new AsyncHttpClient();
        params.put("JSON",JSON_string);
        params.put("DEVICE_ID",DEVICE_ID);
        Log.e(TAG, JSON_string);
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String res = new String(responseBody);
                    response_string = res;
                    Log.e(TAG, res);
                    if(res != "0") {
                        Log.e(TAG, "Data updated in MySQL database successfully!");
                        UPDATE_FLAG = true;
                    }
                    else {
                        Log.e(TAG, "ERROR in Data update in MySQL database successfully!");
                    }
                } catch (Exception e) {

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                if(statusCode == 404){
                    Log.e(TAG, "ERROR 404 : Requested resource not found");
                }else if(statusCode == 500){
                    Log.e(TAG, "Something went wrong at server end");
                }else{
                    Log.e(TAG, "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet]");
                }
            }
        });
    }

    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public boolean isInternetAvailable() {
        Log.e(TAG,"Trying to get internet!");
        try {
            InetAddress ipAddr = InetAddress.getByName("http://www.sthlmrunning.com"); //You can replace it with your name

            if (ipAddr.equals("")) {
                Log.e(TAG,"IP false internet!");
                return false;
            } else {
                Log.e(TAG,"GOt internet!");
                return true;
            }

        } catch (Exception e) {
            Log.e(TAG,"Failed to get internet access!");
            return false;
        }

    }


    }