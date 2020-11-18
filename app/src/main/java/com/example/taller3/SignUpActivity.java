package com.example.taller3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
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
import com.example.taller3.Model.Usuario;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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
    private DatabaseReference dbReference;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

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
        validator.addValidation(this, R.id.tvLatitudRegistro, RegexTemplate.NOT_EMPTY, R.string.errorValidacion);
        validator.addValidation(this, R.id.tvLongitudRegistro, RegexTemplate.NOT_EMPTY, R.string.errorValidacion);

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validator.validate()) {
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



        btnCoordenadas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.requestPermission(SignUpActivity.this, Manifest.permission.ACCESS_FINE_LOCATION, "", LOCATION_CODE);
                updateLocation();
            }
        });
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        try {
            setProfilePic();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);



            Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(SignUpActivity.this).checkLocationSettings(builder.build());


            result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                @Override
                public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                    try {
                        LocationSettingsResponse response = task.getResult(ApiException.class);
                        // All location settings are satisfied. The client can initialize location
                        // requests here.


                        if (ActivityCompat.checkSelfPermission(SignUpActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(SignUpActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                            Log.i("LOCATION", "location.toString()");
                            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(SignUpActivity.this, new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    if (location != null) {
                                        Log.i("LOCATION", location.toString());
                                        latitud.setText(Double.toString(location.getLatitude()));
                                        longitud.setText(Double.toString(location.getLongitude()));
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
                                    resolvable.startResolutionForResult(SignUpActivity.this, LocationRequest.PRIORITY_HIGH_ACCURACY);
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

    public void intentarRegistro() {
        mAuth.createUserWithEmailAndPassword(email.getText().toString(), contra.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String uid = mAuth.getUid();
                            String nombre = SignUpActivity.this.nombre.getText().toString();
                            String apellido = SignUpActivity.this.apellido.getText().toString();
                            //TODO: Cedula
                            Usuario usuario = new Usuario(uid,nombre,apellido,"1234");

                            dbReference = database.getReference("users/"+uid);
                            dbReference.setValue(usuario);

                            Toast.makeText(getApplicationContext(), "Cuenta creada correctamente",
                                    Toast.LENGTH_SHORT).show();
                            user = mAuth.getCurrentUser();

                            mStorageRef = storage.getReference("profile/"+uid+"/profilePic.jpg");

                            mStorageRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Log.i("STORAGE","IMAGEN GUARDAD");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.i("STORAGE","IMAGEN NO GUARDAD");
                                }
                            });


                            pasarNuevaActividad();
                        } else {
                            Toast.makeText(getApplicationContext(), "No se pudo crear la cuenta",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void pasarNuevaActividad() {
        Intent intent = new Intent(getApplicationContext(), MainMapActivity.class);
        startActivity(intent);
    }

    private void askForImage() {
        Intent pickImage = new Intent(Intent.ACTION_PICK);
        pickImage.setType("image/*");
        startActivityForResult(pickImage, IMAGE_PICKER_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case IMAGE_PICKER_PERMISSION:
                if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    askForImage();
                }
                return;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
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
            case IMAGE_PICKER_REQUEST:
                if(resultCode == RESULT_OK){
                    imageUri = data.getData();
                }
                break;

        }
    }
}
