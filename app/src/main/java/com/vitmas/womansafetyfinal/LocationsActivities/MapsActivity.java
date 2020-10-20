package com.vitmas.womansafetyfinal.LocationsActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.vitmas.womansafetyfinal.R;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private Button getgeo;
    private GoogleMap mMap;
    private GeofencingClient geofencingClient;
    private GeoPoint geoPoint;
    private LatLng latLngcurrent;
    private GpsTracker gpsTracker;
    private float GEOFENCE_RADIUS = 100;
    private int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private int BACKGRO0ND_FINE_LOCATION_ACCESS_REQUEST_CODE = 10002;
    private GeofenceHelper geofenceHelper;
    private static final String TAG = "MapsActivity";
    private String GEOFENCE_ID = "SOME_GEOFENCE_ID";
    private FirebaseFirestore firestore;
    static int count=0;
    private final long EVERY_FIVE_SECOND = 5000;
    private Handler handler;
    private Runnable runnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceHelper = new GeofenceHelper(this);
        firestore = FirebaseFirestore.getInstance();
        getgeo = (Button) findViewById(R.id.getgeo);

        executeHandler();

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
        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        getgeo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }
    private void executeHandler(){
        //If the handler and runnable are null we create it the first time.
        if(handler == null && runnable == null){
            handler = new Handler();

            runnable = new Runnable() {
                @Override
                public void run() {
                    //Updating firebase store
                    fireStoreUpdate();
                    //And we execute it again
                    handler.postDelayed(this, EVERY_FIVE_SECOND);
                }
            };
        }
        //If the handler and runnable are not null, we execute it again when the app is resumed.
        else{
            handler.postDelayed(runnable, EVERY_FIVE_SECOND);
        }
    }
    private void fireStoreUpdate(){
        if (Build.VERSION.SDK_INT >= 29) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                LatLng latLng= getLocation();
                sendLocation();
                if(count==0){
                    addMArker(latLng);
                    addCircle(latLng, GEOFENCE_RADIUS);
                    addGeofence(latLng, GEOFENCE_RADIUS);
                }

                Map<String, Object> putdata = new HashMap<>();
                GeoPoint geoPoint1 = geoPoint;
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
                String name = account.getGivenName();
                String email=account.getEmail();
                putdata.put("Geopoint", geoPoint1);
                putdata.put("Name :",name);
                firestore.collection("User Location").document(email).set(putdata)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                count=1;


            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    ActivityCompat.requestPermissions(MapsActivity.this, new String[]{
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGRO0ND_FINE_LOCATION_ACCESS_REQUEST_CODE);
                } else {
                    ActivityCompat.requestPermissions(MapsActivity.this, new String[]{
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGRO0ND_FINE_LOCATION_ACCESS_REQUEST_CODE);
                }
            }
        } else {
            LatLng latLng= getLocation();
            sendLocation();
            if(count==0){
                addMArker(latLng);
                addCircle(latLng, GEOFENCE_RADIUS);
                addGeofence(latLng, GEOFENCE_RADIUS);
            }

            Map<String, Object> putdata = new HashMap<>();
            GeoPoint geoPoint1 = geoPoint;
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
            String name = account.getGivenName();
            String email=account.getEmail();
            putdata.put("Geopoint", geoPoint1);
            putdata.put("Name :",name);
            firestore.collection("User Location").document(email).set(putdata)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
            count=1;
        }
    }
    public void sendLocation(){
        UserLocation userLocation=new UserLocation();
        userLocation.setGeoPoint(geoPoint);
    }
    public LatLng getLocation() {
        gpsTracker = new GpsTracker(MapsActivity.this);
        if (gpsTracker.canGetLocation()) {
            double latitude = gpsTracker.getLatitude();
            double longitude = gpsTracker.getLongitude();
            geoPoint = new GeoPoint(latitude, longitude);
            latLngcurrent = new LatLng(latitude, longitude);
            return latLngcurrent;
        } else {
            gpsTracker.showSettingsAlert();
        }
        return latLngcurrent;
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng satana = new LatLng(20.5982, 74.2033);
        mMap.addMarker(new MarkerOptions().position(satana).title("Marker in Satana"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(satana));
        enableUserLocation();
    }
    public void addMArker(LatLng latLng){
        MarkerOptions markerOptions=new MarkerOptions().position(latLng);
        mMap.addMarker(markerOptions);
    }
    public  void addCircle(LatLng latLng,float radius){
        CircleOptions circleOptions=new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255,255,0,0));
        circleOptions.fillColor(Color.argb(64,255,0,0));
        circleOptions.strokeWidth(4);
        mMap.addCircle(circleOptions);
    }
    private void addGeofence(LatLng latLng, float radius) {
        Geofence geofence = geofenceHelper.getGeofence(GEOFENCE_ID, latLng, radius, Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG,"On Success : GeoFence Added ");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errormessage=geofenceHelper.getErrorString(e);
                        Log.d(TAG,"On Faliure : "+errormessage);
                    }
                });
    }
    private void enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //We need to ahow a dialog for permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
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
                mMap.setMyLocationEnabled(true);
            }
            else{
                //We do not have the permission
            }
        }
        if (requestCode == BACKGRO0ND_FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
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
                Toast.makeText(getApplicationContext(),"You can add Geofences",Toast.LENGTH_LONG).show();
                mMap.setMyLocationEnabled(true);
            }
            else{
                //We do not have the permission
                Toast.makeText(getApplicationContext(),"You can not add Geofences!!!!!!",Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        //execute the handler again.
        executeHandler();
    }
}
