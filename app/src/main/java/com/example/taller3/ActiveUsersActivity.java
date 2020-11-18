package com.example.taller3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.taller3.Model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActiveUsersActivity extends AppCompatActivity {

    private List<Usuario> listUsers = new ArrayList<Usuario>();
    private ListView listViewUsers;
    UserAdapter userAdapter;
    private Usuario usParaMeter;
    boolean b1 = false;
    boolean b2 = false;
    boolean b3 = false;
    String nom = "";
    String apell = "";
    String identi = "";

    //AutenticationDatabase my User
    private FirebaseAuth mAuth;

    //Dinamic database
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private DatabaseReference dR;

    //Image database
    private StorageReference mStorageRef;
    private FirebaseStorage storage;

    private Boolean estado = false;
    Map<String, Boolean> old = new HashMap<>();
    private List<String> values = new ArrayList<>();
    private int contadorAux = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_users);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        getSupportActionBar().setTitle("Usuarios activos");
        listViewUsers = findViewById(R.id.listViewUsuariosActivos);

        try {
            setLista();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void setLista() throws IOException {
        databaseReference = database.getReference("status");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listUsers.clear();
                Map<String, Boolean> nuevo = new HashMap<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String uid = ds.getKey();
                    Boolean state = Boolean.parseBoolean(ds.getValue().toString());
                    Log.i("RTDB", ds.getValue().toString());
                    nuevo.put(uid, state);

                    if (!values.contains(uid)) {
                        values.add(uid);
                    }
                }

                for(String uidUnico : values){
                    dR = database.getReference("users/"+uidUnico);
                    dR.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Usuario us = new Usuario();
                            for(DataSnapshot ds : dataSnapshot.getChildren()){
                                if(nuevo.get(uidUnico) == true && !uidUnico.equals(mAuth.getCurrentUser().getUid())){
                                    if(ds.getKey().equals("nombre")) {
                                        nom = ds.getValue().toString();
                                        b1 = true;
                                    }

                                    if(b1){
                                        us.setNombre(nom);
                                        us.setUid(uidUnico);
                                        File localFile = null;
                                        try {
                                            localFile = File.createTempFile("images","jpg");
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        StorageReference imageRef = mStorageRef.child("profile/"+uidUnico+"/profilePic.jpg");
                                        File finalLocalFile = localFile;
                                        imageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                Bitmap selectedImage = BitmapFactory.decodeFile(finalLocalFile.getAbsolutePath());
                                                us.setImagenUsuario(selectedImage);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.i("ERROR", "NO SE CARGO IMAGEN");
                                            }
                                        });
                                        Log.i("USUARIO ANTES DE METER1", us.getNombre());
                                        Log.i("USUARIO ANTES DE METER2", us.getUid());
                                        listUsers.add(us);
                                        b1 = false;
                                    }
                                }
                                setAdapter();
                                }

                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.i("STATUS", "PROBLEMASS", databaseError.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i("STATUS", "PROBLEMASS", databaseError.toException());
            }
        });
    }

    public void setAdapter(){
        Log.i("Tam2:", String.valueOf(listUsers.size()));
        userAdapter = new UserAdapter(this, listUsers);
        listViewUsers.setAdapter(userAdapter);
    }

}