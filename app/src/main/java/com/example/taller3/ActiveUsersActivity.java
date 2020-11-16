package com.example.taller3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ActiveUsersActivity extends AppCompatActivity {

    private List<Usuario> listUsers;
    private ListView listViewUsers;
    UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_users);

        getSupportActionBar().setTitle("Usuarios activos");

        listViewUsers = findViewById(R.id.listViewUsuariosActivos);
        listUsers = new ArrayList<Usuario>();
        listUsers.add(new Usuario("nico", "correo@mail.com", "lopez", 1020826110, (long)4.5, (long)5.5));
        listUsers.add(new Usuario("nicoooo", "correo@mail.com", "lopez", 1020826110, (long)4.5, (long)5.5));
        listUsers.add(new Usuario("nicoooooooo", "correo@mail.com", "lopez", 1020826110, (long)4.5, (long)5.5));

        userAdapter = new UserAdapter(this, listUsers);
        listViewUsers.setAdapter(userAdapter);

        listViewUsers.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Usuario e = (Usuario) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(view.getContext(), UserDistanceMapActivity.class);
                startActivity(intent);
            }
        });
    }
}