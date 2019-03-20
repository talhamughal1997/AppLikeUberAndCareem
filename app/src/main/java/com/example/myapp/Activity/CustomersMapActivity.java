package com.example.myapp.Activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.myapp.ChangeActivities;
import com.example.myapp.R;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class CustomersMapActivity extends FragmentActivity implements View.OnClickListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    LatLng pickupLocation;
    Marker mDriverMarker;
    Button btnLogOut, btnCallDriver;

    int radius = 1;
    boolean driverFound = false;
    String driverFoundID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customers_map);
        ViewsInitialization();
        ViewsListeners();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

        String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.setLocation(userid, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                Log.d("geofire", "onComplete: " + key + "   " + error);
            }
        });
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriversAvailable");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userid, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLogOut: {
                FirebaseAuth.getInstance().signOut();
                new ChangeActivities().ChangeActivity(CustomersMapActivity.this, CustomersLogInActivity.class);
                CustomersMapActivity.this.finish();
                break;
            }
            case R.id.btnCallDriver: {
                String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("CustomerRequest");
                GeoFire geoFire = new GeoFire(ref);
                geoFire.setLocation(user_id, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {

                    }
                });
                pickupLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                mMap.addMarker(new MarkerOptions().position(pickupLocation).title("PickUp Here"));
                btnCallDriver.setText("Getting Your Driver ....");

                getCloserDriver();
                break;
            }
        }
    }

    private void ViewsListeners() {
        btnLogOut.setOnClickListener(this);
        btnCallDriver.setOnClickListener(this);
    }

    private void ViewsInitialization() {
        btnLogOut = findViewById(R.id.btnLogOut);
        btnCallDriver = findViewById(R.id.btnCallDriver);
    }

    private void getCloserDriver() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("CustomerRequest");
        GeoFire geoFire = new GeoFire(ref);

        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.latitude), radius);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.d("tag", "onKeyEntered: " + key);
                if (!driverFound) {
                    driverFound = true;
                    driverFoundID = key;

                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID);
                    String currentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map = new HashMap();
                    map.put("customerRideId", currentId);
                    driverRef.updateChildren(map);

                    getDriverLocation();
                    btnCallDriver.setText("Looking For Driver Location ....");

                }
            }

            @Override
            public void onKeyExited(String key) {
                Log.d("tag", "onKeyExited: " + key);
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Log.d("tag", "onKeyMoved: " + key);
            }

            @Override
            public void onGeoQueryReady() {
                Log.d("tag", "onGeoQueryReady: ");
                if (!driverFound) {
                    radius++;
                    getCloserDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.d("tag", "onGeoQueryError: " + error);
            }
        });
    }

    private void getDriverLocation() {
        DatabaseReference driverLocationRef = FirebaseDatabase.getInstance().getReference().child("DriversWorking").child(driverFoundID).child("1");
        driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    btnCallDriver.setText("Driver Found");

                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }

                    LatLng driverLatLng = new LatLng(locationLat, locationLng);
                    if (mDriverMarker != null) {
                        mDriverMarker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(pickupLocation.latitude);
                    loc1.setLongitude(pickupLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(driverLatLng.latitude);
                    loc2.setLongitude(driverLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);

                    if (distance < 100) {
                        btnCallDriver.setText("Driver Here");
                    } else {
                        btnCallDriver.setText("Driver Found : " + String.valueOf(distance));
                    }


                    mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Your Driver"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
