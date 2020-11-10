package com.example.taller3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.service.autofill.RegexValidator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private Button btnCancelar, btnRegistrar;
    private EditText nombre, apellido, email, contra, latitud, longitud;
    private ImageView imagen;
    private AwesomeValidation validator;
    private boolean v = false;

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
                //pedirPermiso(this, Manifest.permission.READ_EXTERNAL_STORAGE, "El permiso es para poner una foto de perfil", IMAGE_PICKER_PERMISSION)
            }
        });
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
}