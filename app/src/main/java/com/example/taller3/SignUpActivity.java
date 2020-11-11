package com.example.taller3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;

import static com.example.taller3.Util.requestPermission;

public class SignUpActivity extends AppCompatActivity {

    private static final int IMAGE_PICKER_REQUEST = 201;
    private static final int IMAGE_PICKER_PERMISSION = 211;

    private static final int LOCATION_CODE = 11;
    private static final int REQUEST_CHECK_SETTINGS = 12;

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private Button btnCancelar, btnRegistrar, btnCoordenadas;
    private EditText nombre, apellido, email, contra;
    private ImageView imagen;
    private TextView latitud, longitud;
    private AwesomeValidation validator;
    private Geocoder geocoder;
    private Marker locationMarker;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        user = null;

        imagen = findViewById(R.id.ivImagenRegistro);

        nombre = findViewById(R.id.etNombreRegistro);
        apellido = findViewById(R.id.etApellidoRegistro);
        email = findViewById(R.id.etEmailRegistro);
        contra = findViewById(R.id.etContraRegistro);
        latitud = findViewById(R.id.tvLatitudRegistro);
        longitud = findViewById(R.id.tvLongitudRegistro);

        btnCancelar = findViewById(R.id.btCancelar);
        btnRegistrar = findViewById(R.id.btRegistrarse);
        btnCoordenadas = findViewById(R.id.btObtenerCoordenadasRegistro);

        validator = new AwesomeValidation(ValidationStyle.BASIC);

        validator.addValidation(this, R.id.etNombreRegistro, RegexTemplate.NOT_EMPTY, R.string.errorValidacion);
        validator.addValidation(this, R.id.etApellidoRegistro, RegexTemplate.NOT_EMPTY, R.string.errorValidacion);
        validator.addValidation(this, R.id.etContraRegistro, RegexTemplate.NOT_EMPTY, R.string.errorValidacion);
        validator.addValidation(this, R.id.etContraRegistro, RegexTemplate.NOT_EMPTY, R.string.errorValidacion);
        validator.addValidation(this, R.id.tvLatitudRegistro, RegexTemplate.NOT_EMPTY,R.string.errorValidacion);
        validator.addValidation(this, R.id.tvLongitudRegistro, RegexTemplate.NOT_EMPTY,R.string.errorValidacion);

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!validator.validate()) {
                    return;
                }
                intentarRegistro();
            }
        });

        imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermission(SignUpActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE, "El permiso es para poner una foto de perfil", IMAGE_PICKER_PERMISSION);
            }
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = createLocationRequest();

        btnCoordenadas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationCallback = new LocationCallback(){
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        Location location = locationResult.getLastLocation();
                        //Log.i("LOCATION", "Location update in the callback:" + location);
                        if (location != null) {
                            updateLocation();
                        }
                    }
                };
            }
        });

        try{
            setProfilePic();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private LocationRequest createLocationRequest() {
        LocationRequest myRequest = new LocationRequest();
        myRequest.setInterval(1000);
        myRequest.setFastestInterval(5000);
        myRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return myRequest;
    }


    private void updateLocation() {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
                SettingsClient client = LocationServices.getSettingsClient(this);
                Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());


                /*Si el GPS esta apagado pregunta si lo puede prender*/
                task.addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case CommonStatusCodes
                                    .RESOLUTION_REQUIRED:
                                try {
                                    ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                    resolvableApiException.startResolutionForResult(SignUpActivity.this, REQUEST_CHECK_SETTINGS); //Empieza una actividad.
                                } catch (IntentSender.SendIntentException ex) {
                                    ex.printStackTrace();
                                }
                                break;
                            case LocationSettingsStatusCodes
                                    .SETTINGS_CHANGE_UNAVAILABLE:
                                break;
                        }
                    }
                });

                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Log.i("LOCATION", "onSuccess location");
                        if (location != null) {
                            latitud.setText((int)location.getLatitude());
                            longitud.setText((int)location.getLongitude());
                        }
                    }
                });

            }
    }

    private void setProfilePic() throws IOException {
        //FirebaseUser currentUser = mAuth.getCurrentUser();
        //final File localFile = File.createTempFile("images","jpg");
        //StorageReference imageRef = mStorageRef.child("images/profile/"+currentUser.getUid()+"/profilePic.jpg");
        //imageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            //@Override
            //public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
               // Bitmap selectedImage = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                //imagen.setImageBitmap(selectedImage);
            //}
        //});
    }

    public void intentarRegistro(){
        mAuth.createUserWithEmailAndPassword(email.getText().toString(), contra.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){


                            Toast.makeText(getApplicationContext(), "Cuenta creada correctamente",
                                    Toast.LENGTH_SHORT).show();
                            user = mAuth.getCurrentUser();
                            pasarNuevaActividad();
                        } else {
                            Toast.makeText(getApplicationContext(), "No se pudo crear la cuenta",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void pasarNuevaActividad(){
        Intent intent = new Intent(getApplicationContext(), MainMapActivity.class);
        startActivity(intent);
    }

    private void askForImage(){
        Intent pickImage = new Intent(Intent.ACTION_PICK);
        pickImage.setType("image/*");
        startActivityForResult(pickImage,IMAGE_PICKER_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case IMAGE_PICKER_PERMISSION:
                if(ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    askForImage();
                }
                return;
        }
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS: {
                if (resultCode == RESULT_OK) {
                    startLocationUpdates();
                } else {
                    Toast.makeText(this, "Sin acceso a la localizacion, hardware deshabilidato!", Toast.LENGTH_SHORT).show();
                }
                return;
            }

        }
    }
}