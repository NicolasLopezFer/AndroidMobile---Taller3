package com.example.taller3;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserDistanceMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker otherLocation;
    private String otherUid;
    private String otherNombre;

    private FirebaseDatabase database;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_distance_map2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        otherUid = extras.getString("uid");
        otherNombre = extras.getString("nombre");

        database = FirebaseDatabase.getInstance();

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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        reference = database.getReference("location");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Double latitude=0.0;
                Double longitude=0.0;

                for(DataSnapshot ds : dataSnapshot.getChildren()){


                    if(ds.getKey().equals(otherUid)){
                        for(DataSnapshot i: ds.getChildren()){
                            if(i.getKey().equals("latitude")){
                                latitude = Double.valueOf(i.getValue().toString());
                            }
                            if(i.getKey().equals("longitude")){
                                longitude = Double.valueOf(i.getValue().toString());
                            }
                        }
                    }

                }
                LatLng currentPosition = new LatLng(latitude,longitude);
                placeMarker(currentPosition);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void  placeMarker(LatLng currentPosition){
        if (otherLocation != null){
            otherLocation.remove();
        }else{
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPosition));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(13));

        }
       otherLocation = mMap.addMarker(new MarkerOptions().position(currentPosition).title(otherNombre));

    }
}