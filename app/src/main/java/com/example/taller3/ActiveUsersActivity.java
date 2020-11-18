package com.example.taller3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.taller3.Model.Usuario;

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
        listUsers.add(new Usuario("oiahsd","Santi","Caro","1237"));
        listUsers.add(new Usuario("asfasd","Nico","Ni","654"));
        listUsers.add(new Usuario("vasd","asd","vasva","24"));

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