package com.example.dexter.myapplication;


import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.ConnectionResult;

import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements LocationListener, ConnectionCallbacks, OnConnectionFailedListener {

    protected GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;


   final private int REQUEST_LOCATION_CODE_ASK_PERMISSION = 1;
    private static final String TAG = "LocationActivity";
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    private Location mCurrentLocation;
    LocationRequest mLocationRequest;
    TextView latitude;
    TextView longitude;
    TextView timeLocation;
    Button showLocation;
    String mLastUpdateTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate ....");
        //show error dialog if GooglePlayServices not available
        if (!isGooglePlayServicesAvailable()) {
            Toast.makeText(this, "Google Play Services Not Available", Toast.LENGTH_SHORT).show();
        }

        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        latitude = (TextView) findViewById(R.id.Latitude);
        longitude = (TextView) findViewById(R.id.Longitude);
        showLocation = (Button) findViewById(R.id.locationButton);
        timeLocation = (TextView) findViewById(R.id.timeTextView);


        createLocationRequest();
        buildGoogleApiClient();

        showLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if GPS is enabled
                if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                    buildAlertMessageNoGps();
                }
                updateUI();
            }
        });
    }

    protected void buildGoogleApiClient() {
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void updateUI() { //print Location details
        Log.d(TAG, "Location updated......");
        if (null != mCurrentLocation) {
            latitude.setText(String.valueOf(mCurrentLocation.getLatitude()));
            longitude.setText(String.valueOf(mCurrentLocation.getLongitude()));
            timeLocation.setText(mLastUpdateTime);
            Toast.makeText(this, "Location Updated", Toast.LENGTH_SHORT).show();

        } else {
            Log.d(TAG, "location is null .........");
            Toast.makeText(this, "Location is NULL", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) { //called when location has changed
        Log.d(TAG, "onLocationChanged...");
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
       // updateUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart....");
                   mGoogleApiClient.connect();
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {

            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, status, 0);
            dialog.show();
            return false;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

        Log.d(TAG, "onConnected - isConnected .: " + mGoogleApiClient.isConnected());
        // handlePermissionAndGetLocation();
       // if (mRequestingLocationUpdates) {
            startLocationUpdates();
       // }

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop.....");
        mGoogleApiClient.disconnect();

        Log.d(TAG, "onConnected - isConnected .: " + mGoogleApiClient.isConnected());
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED /*&& ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED*/) {

            ActivityCompat.requestPermissions(this,
                    new String[]{ Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_CODE_ASK_PERMISSION);
         }
         else
             fusedLocationProviderApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

        Log.d(TAG, "Location update started ..: ");
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped ....");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_CODE_ASK_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    Toast.makeText(this, "Permission Accepted", Toast.LENGTH_SHORT).show();

                } else {

                    // permission denied
                    Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
        Log.d(TAG, "Location update paused ....");
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
    public void onConnectionSuspended(int i) {
        //connection was lost
        //attempt to reestablish connection
        mGoogleApiClient.connect();
        Log.d(TAG, "Reestablish Connection - isConnected .: " + mGoogleApiClient.isConnected());
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }
}

