package com.example.taller3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.basgeekball.awesomevalidation.utility.RegexTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
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

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private Button btnCancelar, btnRegistrar;
    private EditText nombre, apellido, email, contra, latitud, longitud;
    private ImageView imagen;
    private AwesomeValidation validator;
    private boolean v = false;

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
        latitud = findViewById(R.id.etLatitudRegistro);
        longitud = findViewById(R.id.etLongitudRegistro);

        btnCancelar = findViewById(R.id.btCancelar);
        btnRegistrar = findViewById(R.id.btRegistrarse);

        validator = new AwesomeValidation(ValidationStyle.BASIC);

        validator.addValidation(this, R.id.etNombreRegistro, RegexTemplate.NOT_EMPTY, R.string.errorValidacion);
        validator.addValidation(this, R.id.etApellidoRegistro, RegexTemplate.NOT_EMPTY, R.string.errorValidacion);
        validator.addValidation(this, R.id.etContraRegistro, RegexTemplate.NOT_EMPTY, R.string.errorValidacion);
        validator.addValidation(this, R.id.etContraRegistro, RegexTemplate.NOT_EMPTY, R.string.errorValidacion);
        validator.addValidation(this, R.id.etLatitudRegistro, RegexTemplate.NOT_EMPTY, R.string.errorValidacion);
        validator.addValidation(this, R.id.etLongitudRegistro, RegexTemplate.NOT_EMPTY, R.string.errorValidacion);

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intentarRegistro();
            }
        });

        imagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermission(SignUpActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE, "El permiso es para poner una foto de perfil", IMAGE_PICKER_PERMISSION);
            }
        });

        try{
            setProfilePic();
        } catch (IOException e){
            e.printStackTrace();
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
}