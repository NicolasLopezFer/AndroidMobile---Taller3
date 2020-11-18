package com.example.taller3;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserDistanceMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker otherLocation;
    private LatLng otherLatlng;
    private String otherUid;
    private String otherNombre;
    private int contador = 0;

    private final double RADIUS_OF_EARTH_KM = 6371.01;

    private FirebaseDatabase database;
    private DatabaseReference reference;

    private FloatingActionButton principal;
    private FloatingActionButton logout;
    private FloatingActionButton lista;
    private TextView distancia;

    private FirebaseAuth mAuth;
    private DatabaseReference dbReference;


    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private static final int LOCATION_CODE = 11;
    private static final int REQUEST_CHECK_SETTINGS = 12;
    private Marker locationMarker;
    private LatLng posicionActual = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_distance_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth == null) {
            Intent intent = new Intent(UserDistanceMapActivity.this, MainActivity.class);
            mAuth.signOut();
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        otherUid = extras.getString("uid");
        otherNombre = extras.getString("nombre");

        database = FirebaseDatabase.getInstance();

        principal = findViewById(R.id.goBack);
        logout = findViewById(R.id.logoutSeguimiento);
        lista = findViewById(R.id.listaSeguimiento);
        distancia = findViewById(R.id.distancia);

        Drawable dr = getDrawable(R.drawable.logout);
        Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
        Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 80, 80, true));
        logout.setImageDrawable(d);

        Drawable drL = getDrawable(R.drawable.list);
        Bitmap bitmapL = ((BitmapDrawable) drL).getBitmap();
        Drawable dL = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmapL, 80, 80, true));
        lista.setImageDrawable(dL);

        Drawable drB = getDrawable(R.drawable.undo);
        Bitmap bitmapB = ((BitmapDrawable) drB).getBitmap();
        Drawable dB = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmapB, 80, 80, true));
        principal.setImageDrawable(dB);


        lista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDistanceMapActivity.this, ActiveUsersActivity.class);
                startActivity(intent);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDistanceMapActivity.this, MainActivity.class);
                mAuth.signOut();
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        principal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserDistanceMapActivity.this, MainMapActivity.class);
                startActivity(intent);
            }
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = createLocationRequest();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    updateLocation();
                }
            }
        };
        Util.requestPermission(this, Manifest.permission.ACCESS_FINE_LOCATION, "", LOCATION_CODE);


    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }
    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        reference = database.getReference("location");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Double latitude = 0.0;
                Double longitude = 0.0;

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    if (ds.getKey().equals(otherUid)) {
                        for (DataSnapshot i : ds.getChildren()) {
                            if (i.getKey().equals("latitude")) {
                                latitude = Double.valueOf(i.getValue().toString());
                            }
                            if (i.getKey().equals("longitude")) {
                                longitude = Double.valueOf(i.getValue().toString());
                            }
                        }
                    }

                }
                LatLng currentPosition = new LatLng(latitude, longitude);
                otherLatlng = currentPosition;
                placeMarker(currentPosition);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void placeMarker(LatLng currentPosition) {
        if (otherLocation != null) {
            otherLocation.remove();
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPosition));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(13));

        }
        showDistance();
        otherLocation = mMap.addMarker(new MarkerOptions().position(currentPosition).title(otherNombre));

    }


    private LocationRequest createLocationRequest() {
        LocationRequest myRequest = new LocationRequest();
        myRequest.setInterval(1000);
        myRequest.setFastestInterval(5000);
        myRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return myRequest;
    }


    private void updateLocation() {
        if (mMap != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                LocationRequest locationRequest = LocationRequest.create();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);


                Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(UserDistanceMapActivity.this).checkLocationSettings(builder.build());


                result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                        try {
                            LocationSettingsResponse response = task.getResult(ApiException.class);
                            // All location settings are satisfied. The client can initialize location
                            // requests here.


                            if (ActivityCompat.checkSelfPermission(UserDistanceMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(UserDistanceMapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {


                                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(UserDistanceMapActivity.this, new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        if (location != null) {
                                            Log.i(" LOCATION ", "Longitud: " + location.getLongitude());
                                            Log.i(" LOCATION ", "Latitud: " + location.getLatitude());
                                            placeMarkerCurrentLocation(location);
                                        }
                                    }
                                });
                            }

                        } catch (ApiException exception) {
                            switch (exception.getStatusCode()) {
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    // Location settings are not satisfied. But could be fixed by showing the
                                    // user a dialog.
                                    try {
                                        // Cast to a resolvable exception.
                                        ResolvableApiException resolvable = (ResolvableApiException) exception;
                                        // Show the dialog by calling startResolutionForResult(),
                                        // and check the result in onActivityResult().
                                        resolvable.startResolutionForResult(UserDistanceMapActivity.this, LocationRequest.PRIORITY_HIGH_ACCURACY);
                                    } catch (IntentSender.SendIntentException e) {
                                        // Ignore the error.
                                    } catch (ClassCastException e) {
                                        // Ignore, should be an impossible error.
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    // Location settings are not satisfied. However, we have no way to fix the
                                    // settings so we won't show the dialog.
                                    break;
                            }
                        }
                    }
                });


            }
        }
    }

    private void placeMarkerCurrentLocation(Location location) {
        LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        if (locationMarker != null) {
            locationMarker.remove();
        } else {
            posicionActual = myLocation;
        }

        if (myLocation != posicionActual) {
            dbReference = database.getReference("location/" + mAuth.getUid());
            dbReference.setValue(myLocation);
            posicionActual = myLocation;
        }
        locationMarker = mMap.addMarker(new MarkerOptions().position(myLocation).title("Tú").icon(BitmapDescriptorFactory.fromResource(R.drawable.googlemaps)));
        if (contador == 0) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
            contador += 1;
        }
        showDistance();
    }

    private void showDistance(){
        if(otherLocation != null && posicionActual!= null) {
            double dis = distance(otherLatlng.latitude, otherLatlng.longitude, posicionActual.latitude, posicionActual.longitude);
            distancia.setText("Distancia: " + String.valueOf(dis) + " Km.");
        }
    }

    public double distance(double lat1, double long1, double lat2, double long2) {
        double latDistance = Math.toRadians(lat1 - lat2);
        double lngDistance = Math.toRadians(long1 - long2);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double result = RADIUS_OF_EARTH_KM * c;
        return Math.round(result*100.0)/100.0;
    }
}