package com.example.taller3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.taller3.Model.Usuario;
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


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String CHANNEL_ID = "MiApp";
    private int notificationId = 66;

    private GoogleMap mMap;
    private ArrayList<Ubicacion> ubicacions;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private static final int LOCATION_CODE = 11;
    private static final int REQUEST_CHECK_SETTINGS = 12;
    private Marker locationMarker;
    private int contador = 0;
    private int contadorAux = 0;
    private FrameLayout frameLayout;

    private FloatingActionButton disponible;
    private FloatingActionButton logout;
    private FloatingActionButton lista;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

    private DatabaseReference dbReference;
    private LatLng posicionActual = null;

    private Boolean estado = false;
    Map<String, Boolean> old = new HashMap<>();
    private List<String> values = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance();

        disponible = findViewById(R.id.disponible);
        logout = findViewById(R.id.logout);
        lista = findViewById(R.id.lista);

        Drawable dr = getDrawable(R.drawable.logout);
        Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
        Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 80, 80, true));
        logout.setImageDrawable(d);

        Drawable drL = getDrawable(R.drawable.list);
        Bitmap bitmapL = ((BitmapDrawable) drL).getBitmap();
        Drawable dL = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmapL, 80, 80, true));
        lista.setImageDrawable(dL);

        dbReference = database.getReference("status/" + mAuth.getUid());

        setImageEstado();
        createNotificationChannel();


        lista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMapActivity.this, ActiveUsersActivity.class);
                startActivity(intent);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainMapActivity.this, MainActivity.class);
                mAuth.signOut();
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        disponible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setImageEstado();
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

        dbReference = database.getReference("status");
        dbReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Boolean> nuevo = new HashMap<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String uid = ds.getKey();
                    Boolean state = Boolean.parseBoolean(ds.getValue().toString());
                    nuevo.put(uid, state);

                    if (!values.contains(uid)) {
                        values.add(uid);
                    }
                }
                if (contadorAux == 0) {
                    old = nuevo;
                    contadorAux++;
                }
                String changed = "";
                for (String val : values) {

                    if (nuevo.get(val) != old.get(val)) {
                        changed = val;
                    }
                }

                old = nuevo;

                if (!changed.equals("")) {
                    if (!changed.equals(mAuth.getUid()) && contadorAux > 0 && nuevo.get(changed)) {
                        //TODO: REVISAR SI ES OTRA PERSONA Y ENVIAR NOTIFICACION

                        dbReference = database.getReference("users/"+changed);
                        String finalChanged = changed;

                        dbReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Usuario usuarioNotificacion = dataSnapshot.getValue(Usuario.class);
                                crearNotificacion(usuarioNotificacion.getNombre(), finalChanged);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("RTDB", "error en la consulta", databaseError.toException());
            }

        });


    }

    private void crearNotificacion(String nombre,String uid){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainMapActivity.this, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.bell);
        builder.setContentTitle("Cambio de estado");
        builder.setContentText(nombre+" Ahora esta Activo!");
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent intent = new Intent(MainMapActivity.this, UserDistanceMapActivity.class);
        intent.putExtra("uid", uid);
        intent.putExtra("nombre", nombre);
        PendingIntent pendingIntent = PendingIntent.getActivity(MainMapActivity.this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(MainMapActivity.this);
        notificationManagerCompat.notify(notificationId, builder.build());
    }

    private void setImageEstado() {
        estado = !estado;
        Drawable d;
        if (estado) {
            Drawable dr = getDrawable(R.drawable.check);
            Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
            d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 80, 80, true));
        } else {
            Drawable dr = getDrawable(R.drawable.cancel);
            Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();
            d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 80, 80, true));
        }

        disponible.setImageDrawable(d);
        dbReference = database.getReference("status/" + mAuth.getUid());
        dbReference.setValue(estado);
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
            //mMap.setMyLocationEnabled(true);

        }

        try {
            readMarkersJson();
            placeMarkers();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void placeMarkers() {
        for (Ubicacion place : ubicacions) {
            LatLng position = new LatLng(place.getLatitude(), place.getLongitude());
            mMap.addMarker(new MarkerOptions().position(position).title(place.getName()));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
        }
        mMap.moveCamera(CameraUpdateFactory.zoomTo(10));

    }

    private void readMarkersJson() throws JSONException {
        ubicacions = new ArrayList<>();
        JSONObject json = new JSONObject(loadJSONFromAsset());
        JSONArray locationsJsonArray = json.getJSONArray("locationsArray");

        for (int i = 0; i < locationsJsonArray.length(); i++) {
            JSONObject jsonObject = locationsJsonArray.getJSONObject(i);
            String name = jsonObject.getString("name");
            Double latitude = jsonObject.getDouble("latitude");
            Double longitude = jsonObject.getDouble("longitude");
            Ubicacion ubicacion = new Ubicacion(name, latitude, longitude);
            ubicacions.add(ubicacion);
        }

    }

    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = this.getAssets().open("locations.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            Log.i("JSON", String.valueOf(ex));
        }
        return json;
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


                Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(MainMapActivity.this).checkLocationSettings(builder.build());


                result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                        try {
                            LocationSettingsResponse response = task.getResult(ApiException.class);
                            // All location settings are satisfied. The client can initialize location
                            // requests here.


                            if (ActivityCompat.checkSelfPermission(MainMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainMapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {


                                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(MainMapActivity.this, new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        if (location != null) {
                                            Log.i(" LOCATION ", "Longitud: " + location.getLongitude());
                                            Log.i(" LOCATION ", "Latitud: " + location.getLatitude());
                                            placeMarker(location);
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
                                        resolvable.startResolutionForResult(MainMapActivity.this, LocationRequest.PRIORITY_HIGH_ACCURACY);
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

    private void placeMarker(Location location) {
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
        locationMarker = mMap.addMarker(new MarkerOptions().position(myLocation).title("Tú"));
        if (contador == 0) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
            contador += 1;
        }
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Activity.RESULT_OK: {
                if (resultCode == RESULT_OK) {
                    Log.i("AA", "AAAAAAAA");
                } else {
                    Toast.makeText(this, "Sin acceso a la localizacion, hardware deshabilidato!", Toast.LENGTH_SHORT).show();
                }
                return;
            }

        }
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "CanalNotificaciones";
            String descripcion = "Cambio en Firebase";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);

            channel.setDescription(descripcion);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}